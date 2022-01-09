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

package me.drwoops.syncity;

import me.drwoops.syncity.database.*;
import me.drwoops.syncity.plugins.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class Syncity extends JavaPlugin implements Listener {

    final public int version = 1;

    public HashMap<String,SyncityPlugin> plugins;
    public Database db;
    private boolean _debug = true;
    HashMap<String, BukkitTask> save_tasks;
    int save_period;

    public boolean getDebug() { return _debug; }
    public void debug(String msg) { if (getDebug()) info(msg); }

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
        plugins.put("statistics", new StatisticsPlugin(this));
        // register event handlers
        getServer().getPluginManager().registerEvents(this, this);
        save_tasks = new HashMap<String, BukkitTask>();
        save_period = getConfig().getInt("save-period");
        for (Player p: getServer().getOnlinePlayers()) {
            on_player_join(p);
        }
    }

    @Override
    public void onDisable() {
        for(Player p: getServer().getOnlinePlayers()) {
            if (p.isOnline()) save_player(p);
        }
        CompletableFuture<Boolean> done = new CompletableFuture<Boolean>();
        db.add_task(new DatabaseStopTask(done));
        // wait until all tasks have been processed
        debug("waiting for all database tasks to complete");
        try { done.get(); } catch (Exception ignored) {}
        debug("database tasks completed");
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

    public CompletableFuture<JSONObject> get_from_player_synchronously(Player player) {
        CompletableFuture<JSONObject> data = new CompletableFuture<JSONObject>();
        new BukkitRunnable() {
            @Override
            public void run() {
                data.complete(get_from_player(player));
            }
        }.runTask(this);
        return data;
    }

    public void update_player(Player player, JSONObject data) {
        if (data != null) {
            plugins.forEach(
                    (k, p) -> {
                        if (data.has(k)) {
                            info(" trying to restore "+k+" for "+player.getName());
                            p.put(player, data.getJSONObject(k));
                            info("player "+k+" restored: "+player.getName());
                        }
                    }
            );
        }
    }

    public void save_player(Player player) {
        JSONObject data = get_from_player(player);
        db.add_task(new DatabaseSaveTask(player, data));
    }

    public void login_player(Player player) {
        if (!save_tasks.containsKey(player.identity().uuid().toString()))
            on_player_join(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        on_player_join(player);
    }

    void on_player_join(Player player) {
        db.add_task(new DatabaseLoginTask(player));
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                JSONObject data = null;
                try {
                    data = get_from_player_synchronously(player).get();
                } catch (InterruptedException ignored) {
                } catch (ExecutionException ignored) {
                }
                if (data != null )
                    db.add_task(new DatabaseSaveTask(player, data));
            }
        }.runTaskTimerAsynchronously(this, save_period*1000, save_period*1000);
        save_tasks.put(player.identity().uuid().toString(), task);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        save_tasks.remove(player.identity().uuid().toString()).cancel();
        JSONObject data = get_from_player(player);
        db.add_task(new DatabaseLogoutTask(player, data));
    }

    public void info(String msg) {
        getLogger().info(msg);
    }

    public void warning(String msg) {
        getLogger().warning(msg);
    }

}
