/*
 Copyright (c) 2021 by drwoops <thedrwoops@gmail.com>

 This file is part of syncity.

 Syncity is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Syncity is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with Foobar.  If not, see <https://www.gnu.org/licenses/>.
*/

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
