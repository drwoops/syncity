package me.drwoops.syncity.database;

import org.bukkit.entity.Player;
import org.json.JSONObject;

public class DatabaseSaveTask implements DatabaseTask {
    Player player;
    JSONObject data;

    public DatabaseSaveTask(Player player, JSONObject data) {
        this.player = player;
        this.data = data;
    }

    @Override
    public void run(Database database) {
        database.savePlayerData(player, data);
    }
}
