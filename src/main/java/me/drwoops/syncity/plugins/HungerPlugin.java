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
        debug("saving Hunger");
        JSONObject data = new JSONObject();
        debug("  saving food: "+player.getName());
        data.put("food", player.getFoodLevel());
        debug("  saving exhaustion: "+player.getName());
        data.put("exhaustion", player.getExhaustion());
        debug("  saving saturation: "+player.getName());
        data.put("saturation", player.getSaturation());
        return data;
    }

    @Override
    public void put(Player player, JSONObject data) {
        debug("restoring hunger: "+player.getName());
        if (data == null) return;
        if (data.has("food")) {
            debug("  restoring food: "+player.getName());
            player.setFoodLevel(data.getInt("food"));
        }
        if (data.has("exhaustion")) {
            debug("  restoring exhaustion: "+player.getName());
            player.setExhaustion(data.getFloat("exhaustion"));
        }
        if (data.has("saturation")) {
            debug("  restoring saturation: "+player.getName());
            player.setSaturation(data.getFloat("saturation"));
        }
    }
}
