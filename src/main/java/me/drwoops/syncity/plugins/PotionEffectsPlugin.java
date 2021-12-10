package me.drwoops.syncity.plugins;

import me.drwoops.syncity.Syncity;
import me.drwoops.syncity.SyncityPlugin;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;

public class PotionEffectsPlugin extends SyncityPlugin {

    public PotionEffectsPlugin(Syncity plugin) {
        super(plugin);
    }

    private JSONObject serialize_effect(PotionEffect effect) {
        JSONObject data = new JSONObject();
        data.put("type", effect.getType().getName());
        data.put("amplifier", effect.getAmplifier());
        data.put("duration", effect.getDuration());
        return data;
    }

    @Override
    public JSONObject get(Player player) {
        Collection<PotionEffect> effects = player.getActivePotionEffects();
        JSONArray effects_json = new JSONArray(effects.size());
        for (PotionEffect effect: effects) {
            effects_json.put(serialize_effect(effect));
            // we are leaving the server, so remove the effect here
            // it will be reacquired wherever we join from
            player.removePotionEffect(effect.getType());
        }
        JSONObject data = new JSONObject();
        data.put("effects", effects_json);
        return data;
    }

    private PotionEffect deserialize_effect(JSONObject effect_json) {
        PotionEffectType type = PotionEffectType.getByName(effect_json.getString("type"));
        int amplifier = effect_json.getInt("amplifier");
        int duration = effect_json.getInt("duration");
        return new PotionEffect(type, duration, amplifier);
    }

    @Override
    public void put(Player player, JSONObject data) {
        if (data==null) return;
        if (data.has("effects")) {
            JSONArray effects_json = data.getJSONArray("effects");
            effects_json.forEach(
                    e -> player.addPotionEffect(deserialize_effect((JSONObject) e))
            );
        }
    }
}