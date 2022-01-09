package me.drwoops.syncity.database;

import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.sql.SQLException;

public class DatabaseLogoutTask implements DatabaseTask {
    Player player;
    JSONObject data;

    public DatabaseLogoutTask(Player player, JSONObject data) {
        this.player = player;
        this.data = data;
    }

    @Override
    public void run(Database database) {
        database.savePlayerData(player, data);
        database.update_status_on_logout(player);
    }
}
