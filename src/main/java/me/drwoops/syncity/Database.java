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

package me.drwoops.syncity;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.sql.*;

public class Database {

    Syncity plugin;
    Connection db;

    public Database(Syncity syncity) {
        plugin = syncity;
        db = null;
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
                        "CREATE TABLE IF NOT EXISTS syncity_status (uuid CHAR(100) PRIMARY KEY, status INT)"
                );
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("ensureConnection: "+e.toString());
        }
    }

    static final int STATUS_LEFT = 0;
    static final int STATUS_JOINED = 1;

    void wait_for_status_left(Player player) {
        try {
            ensureConnection();
            PreparedStatement stmt = db.prepareStatement(
                    "SELECT status FROM syncity_status where uuid = ?"
            );
            stmt.setString(1, player.identity().uuid().toString());
            for (int i = 0; i < 10; ++i) {
                ResultSet r = stmt.executeQuery();
                if (!r.next()) return;
                if (r.getInt(1) == STATUS_LEFT) return;
                try { Thread.sleep(10); } catch (InterruptedException e) {}
            }
            plugin.getLogger().warning("wait_for_status_left timed out");
        } catch (SQLException e) {
            plugin.getLogger().warning("wait_for_status_left: "+e.toString());
        }
    }

    void set_status(Player player, int status) {
        try {
            ensureConnection();
            PreparedStatement stmt = db.prepareStatement(
                    "INSERT INTO syncity_status (uuid, status) VALUES (? , ?) ON DUPLICATE KEY UPDATE status=VALUES(status)"
            );
            stmt.setString(1, player.identity().uuid().toString());
            stmt.setInt(2, status);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("set_status: "+e.toString());
        }
    }

    public void savePlayerData(Player player, JSONObject data) {
        try {
            ensureConnection();
            PreparedStatement stmt = db.prepareStatement(
                    "INSERT INTO syncity (uuid, data) VALUES (? , ?) ON DUPLICATE KEY UPDATE data=VALUES(data)"
            );
            stmt.setString(1, player.identity().uuid().toString());
            stmt.setString(2, data.toString());
            stmt.executeUpdate();
            plugin.getLogger().info("Player data saved: " + player.getName());
        } catch (SQLException e) {
            plugin.getLogger().warning("savePlayerData: "+e.toString());
        }
    }

    public JSONObject loadPlayerData(Player player) {
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
        } catch (SQLException e) {
            plugin.getLogger().warning("loadPlayerData: "+e.toString());
            return null;
        }
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
