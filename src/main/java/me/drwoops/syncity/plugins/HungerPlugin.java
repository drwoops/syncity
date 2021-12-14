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
import org.bukkit.entity.Player;
import org.json.JSONObject;

public class HungerPlugin extends SyncityPlugin {

    public HungerPlugin(Syncity plugin) {
        super(plugin);
    }

    @Override
    public JSONObject get(Player player) {
        JSONObject data = new JSONObject();
        data.put("food", player.getFoodLevel());
        data.put("exhaustion", player.getExhaustion());
        data.put("saturation", player.getSaturation());
        return data;
    }

    @Override
    public void put(Player player, JSONObject data) {
        if (data == null) return;
        if (data.has("food")) player.setFoodLevel(data.getInt("food"));
        if (data.has("exhaustion")) player.setExhaustion(data.getFloat("exhaustion"));
        if (data.has("saturation")) player.setSaturation(data.getFloat("saturation"));
    }
}
