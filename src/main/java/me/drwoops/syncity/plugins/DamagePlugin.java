// copyright (c) 2021 by drwoops <thedrwoops@gmail.com>
package me.drwoops.syncity.plugins;

import me.drwoops.syncity.Syncity;
import me.drwoops.syncity.SyncityPlugin;
import org.bukkit.entity.Player;
import org.json.JSONObject;

public class DamagePlugin extends SyncityPlugin {

    public DamagePlugin(Syncity plugin) {
        super(plugin);
    }

    @Override
    public JSONObject get(Player player) {
        JSONObject data = new JSONObject();
        data.put("health", player.getHealth());
        data.put("absorption", player.getAbsorptionAmount());
        return data;
    }

    @Override
    public void put(Player player, JSONObject data) {
        if (data == null) return;
        if (data.has("health")) player.setHealth(data.getDouble("health"));
        if (data.has("absorption")) player.setAbsorptionAmount(data.getDouble("absorption"));
    }
}
