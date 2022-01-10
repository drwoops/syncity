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

import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.sql.SQLException;

public class DatabaseLogoutTask implements DatabaseTask {
    Player player;
    JSONObject data;

    public DatabaseLogoutTask(Player player, JSONObject data) {
        this.player = player;
        this.data = data;
    }

    @Override
    public void run(Database database) {
        database.savePlayerData(player, data);
        database.update_status_on_logout(player);
    }
}
