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

    boolean is_included(String name) {
        boolean inc = false;
        for (String v: include) {
            if (name.startsWith(v)) {
                inc = true;
                break;
            }
        }
        if (inc) {
            for (String v : exclude) {
                if (name.startsWith(v))
                    return false;
            }
        }
        return true;
    }

    public JSONObject get(Player player) {
        debug("  saving player advancements: "+player.getName());
        JSONObject advancements = new JSONObject();
        Iterator<Advancement> ai = advancementIterator();
        while (ai.hasNext()) {
            Advancement a = ai.next();
            String name = a.getKey().toString();
            if (! is_included(name)) {
                debug("    ignoring "+name);
                continue;
            }
            debug("    saving "+name);
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
        debug("  restoring player advancements: "+player.getName());
        for (String aks : data.keySet()) {
            if (! is_included(aks)) {
                debug("    ignoring "+aks);
                continue;
            }
            debug("    restoring "+aks);
            int i = aks.indexOf(":");
            String namespace = (i == -1)?"minecraft":aks.substring(0, i);
            String key = (i == -1)?aks:aks.substring(i+1);
            NamespacedKey ak = new NamespacedKey(namespace, key);
            debug("    - "+ak);
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
