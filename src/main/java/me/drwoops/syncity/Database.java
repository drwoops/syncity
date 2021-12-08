package me.drwoops.syncity;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.io.StringReader;
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
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("ensureConnection: "+e.toString());
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