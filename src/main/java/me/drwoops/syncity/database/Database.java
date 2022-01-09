/*
 Copyright (c) 2021 by drwoops <thedrwoops@gmail.com>

 This file is part of syncity.

 Syncity is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Syncity is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with Foobar.  If not, see <https://www.gnu.org/licenses/>.
*/

package me.drwoops.syncity.database;

import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import me.drwoops.syncity.Syncity;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;

import java.sql.*;
import java.util.concurrent.LinkedBlockingQueue;

public class Database {

    Syncity plugin;
    Connection db;
    LinkedBlockingQueue<DatabaseTask> queue;
    boolean again;

    public Database(Syncity syncity) {
        plugin = syncity;
        db = null;
        queue = new LinkedBlockingQueue<DatabaseTask>();
        again = true;
        Database that = this;
        new BukkitRunnable() {

            @Override
            public void run() {
                while (again) {
                    try {
                        queue.take().run(that);
                    } catch (InterruptedException ignored) {}
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public void add_task(DatabaseTask task) {
        queue.add(task);
    }

    Connection getConnection() throws SQLException {
        FileConfiguration conf = plugin.getConfig();
        String user     = conf.getString("mysql.user");
        String password = conf.getString("mysql.password");
        String host     = conf.getString("mysql.host");
        int port        = conf.getInt(   "mysql.port");
        String database = conf.getString("mysql.database");
        String url = "jdbc:mysql://"+host+":"+port+"/"+database;
        return DriverManager.getConnection(url, user, password);
    }

    void ensureConnection() {
        try {
            if (db == null || db.isClosed()) {
                db = getConnection();
                PreparedStatement stmt = db.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS syncity (uuid CHAR(100) PRIMARY KEY, data JSON)"
                );
                stmt.executeUpdate();
                stmt = db.prepareStatement(
                        "DROP TABLE IF EXISTS syncity_status"
                );
                stmt.executeUpdate();
                stmt = db.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS syncity_status_1 (uuid CHAR(100) PRIMARY KEY, login INT, logout INT)"
                );
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("ensureConnection: "+e.toString());
        }
    }

    void closeConnection() {
        if (db != null) {
            try { db.close(); } catch (SQLException ignored) {}
            db = null;
        }
    }

    public void wait_for_player_quit_data (Player player) {
        for (int i=0; i<50; i++) {
            try {
                ensureConnection();
                PreparedStatement stmt = db.prepareStatement(
                        "SELECT login,logout FROM syncity_status_1 where uuid = ?"
                );
                stmt.setString(1, player.identity().uuid().toString());
                ResultSet r = stmt.executeQuery();
                if (!r.next()) return;
                if (r.getInt(1) == r.getInt(2)) return;
                try { Thread.sleep(10); } catch (InterruptedException ignored) {}
            } catch (CommunicationsException ignored) {
                plugin.warning("wait_for_player_quit_data: communications link failure... retrying");
                closeConnection();
            } catch (SQLException e) {
                plugin.warning("wait_for_player_quit_data: "+e.toString());
                return;
            }
        }
        plugin.warning("wait_for_player_quit_data: failed! too many failures or iterations");
    }

    public void update_status_on_login(Player player) {
        for (int i=0; i<10; i++) {
            try {
                ensureConnection();
                PreparedStatement stmt = db.prepareStatement(
                        "SELECT logout FROM syncity_status_1 where uuid = ?"
                );
                stmt.setString(1, player.identity().uuid().toString());
                ResultSet r = stmt.executeQuery();
                if (!r.next()) {
                    stmt = db.prepareStatement(
                            "INSERT INTO syncity_status_1 (uuid, login, logout) VALUES (?, ?, ?)"
                    );
                    stmt.setString(1, player.identity().uuid().toString());
                    stmt.setInt(2, 1);
                    stmt.setInt(3, 0);
                    stmt.executeUpdate();
                    return;
                }
                int logout = r.getInt(1);
                stmt = db.prepareStatement(
                        "UPDATE syncity_status_1 SET login = ? WHERE uuid = ?"
                );
                stmt.setInt(1, logout+1);
                stmt.setString(2, player.identity().uuid().toString());
                stmt.executeUpdate();
                return;
            } catch (CommunicationsException ignored) {
                plugin.warning("update_status_on_login: communications link failure... retrying");
                closeConnection();
            } catch (SQLException e) {
                plugin.warning("update_status_on_login: "+e.toString());
                return;
            }
        }
        plugin.warning("update_status_on_login: failed! too many failures");
    }

    public void update_status_on_logout(Player player) {
        for (int i=0; i<10; i++) {
            try {
                ensureConnection();
                PreparedStatement stmt = db.prepareStatement(
                        "SELECT login from syncity_status_1 where uuid = ?"
                );
                stmt.setString(1, player.identity().uuid().toString());
                ResultSet r = stmt.executeQuery();
                if (!r.next()) {
                    stmt = db.prepareStatement(
                            "INSERT INTO syncity_status_1 (uuid, login, logout) VALUES (?, ?, ?)"
                    );
                    stmt.setString(1, player.identity().uuid().toString());
                    stmt.setInt(2, 1);
                    stmt.setInt(3, 1);
                    stmt.executeUpdate();
                    return;
                }
                int login = r.getInt(1);
                stmt = db.prepareStatement(
                        "UPDATE syncity_status_1 SET logout = ? WHERE uuid = ?"
                );
                stmt.setInt(1, login);
                stmt.setString(2, player.identity().uuid().toString());
                stmt.executeUpdate();
                return;
            } catch (CommunicationsException ignored) {
                plugin.warning("update_status_on_logout: communications link failure... retrying");
                closeConnection();
            } catch (SQLException e) {
                plugin.warning("update_status_on_logout: "+e.toString());
                return;
            }
        }
        plugin.warning("update_status_on_logout: failed! too many failures");
    }

    public void savePlayerData(Player player, JSONObject data) {
        if (data == null) return;
        for (int i=0; i<10; i++) {
            try {
                ensureConnection();
                PreparedStatement stmt = db.prepareStatement(
                        "INSERT INTO syncity (uuid, data) VALUES (? , ?) ON DUPLICATE KEY UPDATE data=VALUES(data)"
                );
                stmt.setString(1, player.identity().uuid().toString());
                stmt.setString(2, data.toString());
                stmt.executeUpdate();
                plugin.getLogger().info("Player data saved: " + player.getName());
                return;
            } catch (CommunicationsException e) {
                plugin.warning("savePlayerData: communications link failure... retrying");
                closeConnection();
            } catch (SQLException e) {
                plugin.warning("savePlayerData: " + e.toString());
                return;
            }
        }
        plugin.warning("savePlayerData: failed! too many failures");
    }

    public JSONObject loadPlayerData(Player player) {
        for (int i=0; i<10; i++) {
            try {
                ensureConnection();
                PreparedStatement stmt = db.prepareStatement(
                        "SELECT data from syncity WHERE uuid = ?"
                );
                stmt.setString(1, player.identity().uuid().toString());
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) return null;
                String json = rs.getString(1);
                JSONObject data = new JSONObject(json);
                rs.close();
                return data;
            } catch (CommunicationsException e) {
                plugin.warning("loadPlayerData: communications link failure... retrying");
                closeConnection();
            } catch (SQLException e) {
                plugin.warning("loadPlayerData: " + e.toString());
                return null;
            }
        }
        plugin.warning("loadPlayerData: failed! too many failures");
        return null;
    }

    public void removePlayer(Player player) {
        try {
            ensureConnection();
            PreparedStatement stmt = db.prepareStatement(
                    "DELETE FROM syncity WHERE uuid = ?"
            );
            stmt.setString(1, player.identity().uuid().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("removePlayer: "+e.toString());
        }
    }


}
