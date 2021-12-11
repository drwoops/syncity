// copyright (c) 2012 by drwoops <thedrwoops@gmail.com>
package me.drwoops.syncity;

import me.drwoops.syncity.plugins.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import java.util.HashMap;

public final class Syncity extends JavaPlugin implements Listener {

    final public int version = 1;

    public HashMap<String,SyncityPlugin> plugins;
    public Database db;

    @Override
    public void onEnable() {
        // get config defaults
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        // create the database connection
        db = new Database(this);
        // register all syncity plugins
        plugins = new HashMap<String,SyncityPlugin>();
        plugins.put("advancements", new AdvancementsPlugin(this));
        plugins.put("experience", new ExperiencePlugin(this));
        plugins.put("damage", new DamagePlugin(this));
        plugins.put("hunger", new HungerPlugin(this));
        plugins.put("inventory", new InventoryPlugin(this));
        plugins.put("effects", new PotionEffectsPlugin(this));
        // register event handlers
        getServer().getPluginManager().registerEvents(this, this);
    }

    public JSONObject get_from_player(Player player) {
        JSONObject data = new JSONObject();
        // version the schema in case we need migrations down the line
        // only increase the version for incompatible schema changes
        // extensions don't count
        data.put("version", version);
        plugins.forEach(
                (key, p) -> {
                    data.put(key, p.get(player));
                }
        );
        return data;
    }

    public JSONObject get_from_database(Player player) {
        return db.loadPlayerData(player);
    }

    public void update_player(Player player, JSONObject data) {
        if (data != null) {
            plugins.forEach(
                    (k, p) -> {
                        if (data.has(k))
                            p.put(player, data.getJSONObject(k));
                    }
            );
            // remove the player from the database
            // the server is now responsible for saving user data
            db.removePlayer(player);
        }
    }

    public void save_player(Player player) {
        db.savePlayerData(player, get_from_player(player));
    }

    public void load_player(Player player) {
        update_player(player, db.loadPlayerData(player));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        load_player(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        save_player(event.getPlayer());
    }

}
