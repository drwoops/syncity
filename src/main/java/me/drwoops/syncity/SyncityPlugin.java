package me.drwoops.syncity;

import org.bukkit.entity.Player;
import org.json.JSONObject;

public interface SyncityPlugin {

    JSONObject get(Player player);
    void put(Player player, JSONObject data);
}
