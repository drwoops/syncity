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
        debug("  saving statistics: ", player.getName());
        JSONObject data = new JSONObject();
        for (Statistic stat: Statistic.values()) {
            Statistic.Type styp = stat.getType();
            if (styp == Statistic.Type.UNTYPED) {
                // simple statistic
                int n = player.getStatistic(stat);
                debug("    saving ", stat.name());
                data.put(stat.getKey().toString(), n);
            } else {
                JSONObject subdata = new JSONObject();
                if (styp == Statistic.Type.ENTITY) {
                    // entity
                    for (EntityType typ : EntityType.values()) {
                        if (typ == EntityType.UNKNOWN) continue;
                        int n = player.getStatistic(stat, typ);
                        if (n > 0) {
                            debug("    saving ", typ.name());
                            subdata.put(typ.getKey().toString(), n);
                        }
                    }
                } else {
                    // block or item
                    for (Material mat : Material.values()) {
                        int n = player.getStatistic(stat, mat);
                        if (n > 0) {
                            debug("    saving ", mat.name());
                            subdata.put(mat.getKey().toString(), n);
                        }
                    }
                }
                if (subdata.length() != 0) {
                    debug("    saving ", stat.name());
                    data.put(stat.getKey().toString(), subdata);
                }
            }
        }
        return data;
    }

    @Override
    public void put(Player player, JSONObject data) {
        if (data==null) return;
        debug("  restoring statistics: ", player.getName());
        for (Statistic stat: Statistic.values()) {
            String name = stat.getKey().toString();
            if (!data.has(name)) continue;
            debug("    restoring ", name);
            Statistic.Type styp = stat.getType();
            if (styp == Statistic.Type.UNTYPED) {
                // simple statistic
                player.setStatistic(stat, data.getInt(name));
            } else {
                JSONObject subdata = data.getJSONObject(name);
                if (styp == Statistic.Type.ENTITY) {
                    // entity
                    for (EntityType typ: EntityType.values()) {
                        if (typ == EntityType.UNKNOWN) continue;
                        String subname = typ.getKey().toString();
                        if (subdata.has(subname)) {
                            debug("    - ", typ.name());
                            player.setStatistic(stat, typ, subdata.getInt(subname));
                        }
                    }
                } else {
                    // block or item
                    for (Material mat: Material.values()) {
                        String subname = mat.getKey().toString();
                        if (subdata.has(subname)) {
                            debug("    - ", subname);
                            player.setStatistic(stat, mat, subdata.getInt(subname));
                        }
                    }
                }
            }
        }
    }
}
