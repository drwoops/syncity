package me.drwoops.syncity.plugins;

import me.drwoops.syncity.Syncity;
import me.drwoops.syncity.SyncityPlugin;
import org.bukkit.entity.Player;
import org.json.JSONObject;

public class ExperiencePlugin extends SyncityPlugin {

    public ExperiencePlugin(Syncity plugin) {
        super(plugin);
    }

    @Override
    public JSONObject get(Player player) {
        JSONObject data = new JSONObject();
        data.put("level", player.getLevel());
        data.put("exp", player.getExp());
        return data;
    }

    @Override
    public void put(Player player, JSONObject data) {
        if (data != null) {
            if (data.has("level")) player.setLevel(data.getInt("level"));
            if (data.has("exp")) player.setExp(data.getFloat("exp"));
        }
    }
}
