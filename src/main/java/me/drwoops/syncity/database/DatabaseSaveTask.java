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

import me.drwoops.syncity.PeriodicSaveRunnable;
import org.bukkit.entity.Player;
import org.json.JSONObject;

public class DatabaseSaveTask implements DatabaseTask {
    Player player;
    PeriodicSaveRunnable runnable;
    JSONObject data;

    public DatabaseSaveTask(Player player, JSONObject data, PeriodicSaveRunnable runnable) {
        this.player = player;
        this.runnable = runnable;
        this.data = data;
    }

    @Override
    public void run(Database database) {
        database.savePlayerData(player, data);
        if (runnable != null) runnable.in_progress = false;
    }
}
