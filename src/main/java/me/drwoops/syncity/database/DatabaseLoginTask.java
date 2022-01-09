package me.drwoops.syncity.database;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;

public class DatabaseLoginTask implements DatabaseTask {
    Player player;

    public DatabaseLoginTask(Player player) {
        this.player = player;
    }

    @Override
    public void run(Database database) {
        // immediately load the last snapshot
        final JSONObject data = database.loadPlayerData(player);
        // synchronously update the player
        new BukkitRunnable() {
            @Override
            public void run() {
                database.plugin.update_player(player, data);
            }
        }.runTask(database.plugin);
        // now wait for the quit event to finish updating the database
        // and update the player again
        database.wait_for_player_quit_data(player);
        final JSONObject data2 = database.loadPlayerData(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                database.plugin.update_player(player, data2);
            }
        }.runTask(database.plugin);
        database.update_status_on_login(player);
    }
}
