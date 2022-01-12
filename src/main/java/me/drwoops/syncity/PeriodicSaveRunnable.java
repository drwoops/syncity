package me.drwoops.syncity;

import me.drwoops.syncity.database.DatabaseSaveTask;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class PeriodicSaveRunnable extends BukkitRunnable {

    Syncity plugin;
    Player player;
    public boolean in_progress;

    public PeriodicSaveRunnable(Syncity plugin, Player player) {
        super();
        this.plugin = plugin;
        this.player = player;
        this.in_progress = false;
    }

    @Override
    public void run() {
        if (in_progress) {
            plugin.warning("skipping periodic save. one is already in progress for "+player.getName());
            return;
        }
        plugin.debug("periodic save for: ", player.getName());
        JSONObject data = null;
        try {
            data = plugin.get_from_player_synchronously(player).get();
        } catch (InterruptedException ignored) {
        } catch (ExecutionException ignored) {
        }
        if (data != null )
            plugin.db.add_task(new DatabaseSaveTask(player, data, this));
    }
}
