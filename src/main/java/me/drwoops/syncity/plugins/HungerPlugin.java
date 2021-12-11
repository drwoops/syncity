// copyright (c) 2021 by drwoops <thedrwoops@gmail.com>
package me.drwoops.syncity.plugins;

import me.drwoops.syncity.Syncity;
import me.drwoops.syncity.SyncityPlugin;
import org.bukkit.entity.Player;
import org.json.JSONObject;

public class HungerPlugin extends SyncityPlugin {

    public HungerPlugin(Syncity plugin) {
        super(plugin);
    }

    @Override
    public JSONObject get(Player player) {
        JSONObject data = new JSONObject();
        data.put("food", player.getFoodLevel());
        data.put("exhaustion", player.getExhaustion());
        data.put("saturation", player.getSaturation());
        return data;
    }

    @Override
    public void put(Player player, JSONObject data) {
        if (data == null) return;
        if (data.has("food")) player.setFoodLevel(data.getInt("food"));
        if (data.has("exhaustion")) player.setExhaustion(data.getFloat("exhaustion"));
        if (data.has("saturation")) player.setSaturation(data.getFloat("saturation"));
    }
}
