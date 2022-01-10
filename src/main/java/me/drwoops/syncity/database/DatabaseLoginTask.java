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
package me.drwoops.syncity.database;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;

public class DatabaseLoginTask implements DatabaseTask {
    Player player;

    public DatabaseLoginTask(Player player) {
        this.player = player;
    }

    @Override
    public void run(Database database) {
        // immediately load the last snapshot
        final JSONObject data = database.loadPlayerData(player);
        // synchronously update the player
        new BukkitRunnable() {
            @Override
            public void run() {
                database.plugin.update_player(player, data);
            }
        }.runTask(database.plugin);
        // now wait for the quit event to finish updating the database
        // and update the player again
        database.wait_for_player_quit_data(player);
        final JSONObject data2 = database.loadPlayerData(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                database.plugin.update_player(player, data2);
            }
        }.runTask(database.plugin);
        database.update_status_on_login(player);
    }
}
