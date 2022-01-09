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
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.bukkit.Bukkit.advancementIterator;
import static org.bukkit.Bukkit.getAdvancement;

public class AdvancementsPlugin extends SyncityPlugin {

    List<String> exclude;
    List<String> include;

    public AdvancementsPlugin(Syncity plugin) {
        super(plugin);
        this.exclude = plugin.getConfig().getStringList("plugins.advancements.exclude");
        this.include = plugin.getConfig().getStringList("plugins.advancements.include");
    }

    public JSONObject get(Player player) {
        JSONObject advancements = new JSONObject();
        Iterator<Advancement> ai = advancementIterator();
        loop:
        while (ai.hasNext()) {
            Advancement a = ai.next();
            String name = a.getKey().toString();
            boolean inc = false;
            for (String v: include) {
                if (name.startsWith(v)) {
                    inc = true;
                    break;
                }
            }
            if (inc) {
                for (String v : exclude) {
                    if (name.startsWith(v)) {
                        debug("ignoring "+name);
                        continue loop;
                    }
                }
            } else
                continue;
            debug("saving "+name);
            AdvancementProgress ap = player.getAdvancementProgress(a);
            Collection<String> ac = ap.getAwardedCriteria();
            if (!ac.isEmpty()) {
                JSONArray l = new JSONArray(ac);
                advancements.put(name, l);
            }
        }
        return advancements;
    }

    public void put(Player player, JSONObject data) {
        loop:
        for (String aks : data.keySet()) {
            debug("restoring "+aks);
            boolean inc = false;
            for (String v: include) {
                if (aks.startsWith(v)) {
                    inc = true;
                    break;
                }
            }
            if (inc) {
                for (String v : exclude) {
                    if (aks.startsWith(v)) {
                        debug("ignoring "+aks);
                        continue loop;
                    }
                }
            } else
                continue;
            int i = aks.indexOf(":");
            String namespace = (i == -1)?"minecraft":aks.substring(0, i);
            String key = (i == -1)?aks:aks.substring(i+1);
            NamespacedKey ak = new NamespacedKey(namespace, key);
            debug("AdvancementPlugin.put "+aks+" "+ak);
            Advancement a = getAdvancement(ak);
            AdvancementProgress ap = player.getAdvancementProgress(a);
            JSONArray l = data.getJSONArray(a.getKey().toString());
            Iterator<Object> ci = l.iterator();
            while (ci.hasNext()) {
                String c = (String) ci.next();
                ap.awardCriteria(c);
            }
        }
    }
}
