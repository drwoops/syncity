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

public class ExperiencePlugin extends SyncityPlugin {

    public ExperiencePlugin(Syncity plugin) {
        super(plugin);
    }

    @Override
    public JSONObject get(Player player) {
        debug("  saving level and experience: "+player.getName());
        JSONObject data = new JSONObject();
        debug("    saving level: "+player.getName());
        data.put("level", player.getLevel());
        debug( "    saving experience: "+player.getName());
        data.put("exp", player.getExp());
        return data;
    }

    @Override
    public void put(Player player, JSONObject data) {
        debug("  restoring level and experience: "+player.getName());
        if (data != null) {
            if (data.has("level")) {
                debug("    restoring level: "+player.getName());
                player.setLevel(data.getInt("level"));
            }
            if (data.has("exp")) {
                debug( "    restoring experience: "+player.getName());
                player.setExp(data.getFloat("exp"));
            }
        }
    }
}
