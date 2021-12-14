package me.drwoops.syncity.plugins;

import me.drwoops.syncity.Syncity;
import me.drwoops.syncity.SyncityPlugin;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.json.JSONObject;

public class StatisticsPlugin extends SyncityPlugin {

    public StatisticsPlugin(Syncity plugin) { super(plugin); }

    @Override
    public JSONObject get(Player player) {
        JSONObject data = new JSONObject();
        for (Statistic stat: Statistic.values()) {
            if (stat.isSubstatistic()) {
                JSONObject subdata = new JSONObject();
                if (stat.isBlock()) {
                    // block
                    for (Material mat : Material.values()) {
                        int n = player.getStatistic(stat, mat);
                        if (n > 0) subdata.put(mat.getKey().toString(), n);
                    }
                    if (subdata.length() != 0)
                        data.put(stat.getKey().toString(), subdata);
                } else {
                    // entity
                    for (EntityType typ : EntityType.values()) {
                        int n = player.getStatistic(stat, typ);
                        if (n > 0) subdata.put(typ.getKey().toString(), n);
                    }
                }
                data.put(stat.getKey().toString(), subdata);
            } else {
                // simple statistic
                int n = player.getStatistic(stat);
                data.put(stat.getKey().toString(), n);
            }
        }
        return data;
    }

    @Override
    public void put(Player player, JSONObject data) {
        if (data==null) return;
        for (Statistic stat: Statistic.values()) {
            String name = stat.getKey().toString();
            if (!data.has(name)) continue;
            if (stat.isSubstatistic()) {
                JSONObject subdata = data.getJSONObject(name);
                if (stat.isBlock()) {
                    // block
                    for (Material mat: Material.values()) {
                        String subname = mat.getKey().toString();
                        if (subdata.has(subname))
                            player.setStatistic(stat, mat, subdata.getInt(subname));
                    }
                } else {
                    // entity
                    for (EntityType typ: EntityType.values()) {
                        String subname = typ.getKey().toString();
                        if (subdata.has(subname))
                            player.setStatistic(stat, typ, subdata.getInt(subname));
                    }
                }
            } else {
                // simple statistic
                player.setStatistic(stat, data.getInt(name));
            }
        }
    }
}
