package me.drwoops.syncity;

import org.bukkit.entity.Player;
import org.json.JSONObject;

public abstract class SyncityPlugin {

    protected Syncity plugin;

    public SyncityPlugin(Syncity plugin) {
        this.plugin = plugin;
    }
    public abstract JSONObject get(Player player);
    public abstract void put(Player player, JSONObject data);
}
