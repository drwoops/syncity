package me.drwoops.syncity;

import me.drwoops.syncity.plugins.AdvancementsPlugin;
import me.drwoops.syncity.plugins.ExperiencePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public final class Syncity extends JavaPlugin implements Listener {

    public HashMap<String,SyncityPlugin> plugins;
    public ArrayList<String> exclude;
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
        plugins.put("level", new ExperiencePlugin(this));
        // register event handlers
        getServer().getPluginManager().registerEvents(this, this);
    }

    public JSONObject get_from_player(Player player) {
        JSONObject data = new JSONObject();
        plugins.forEach(
                (key, p) -> {
                    getLogger().info("get_from_player: " + key);
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
