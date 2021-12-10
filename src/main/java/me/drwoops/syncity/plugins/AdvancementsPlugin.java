package me.drwoops.syncity.plugins;

import me.drwoops.syncity.Syncity;
import me.drwoops.syncity.SyncityPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.bukkit.Bukkit.advancementIterator;
import static org.bukkit.Bukkit.getAdvancement;

public class AdvancementsPlugin extends SyncityPlugin {

    List<String> exclude;

    public AdvancementsPlugin(Syncity plugin) {
        super(plugin);
        this.exclude = plugin.getConfig().getStringList("advancements.exclude");
    }

    public JSONObject get(Player player) {
        JSONObject advancements = new JSONObject();
        Iterator<Advancement> ai = advancementIterator();
        loop:
        while (ai.hasNext()) {
            Advancement a = ai.next();
            String name = a.getKey().toString();
            for (String v : exclude) {
                if (name.startsWith(v)) {
                    continue loop;
                }
            }
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
        for (String aks : data.keySet()) {
            NamespacedKey ak = NamespacedKey.fromString(aks);
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
