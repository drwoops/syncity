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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Base64;

public class InventoryPlugin extends SyncityPlugin {

    public InventoryPlugin(Syncity plugin) {
        super(plugin);
    }

    private JSONArray serialize_itemstack(ItemStack[] list) {
        Base64.Encoder encoder = Base64.getEncoder();
        int n = list.length;
        JSONArray json = new JSONArray(n);
        for (int i=0; i<n; i++) {
            json.put(i, (list[i]==null)?"":encoder.encodeToString(list[i].serializeAsBytes()));
        }
        return json;
    }

    @Override
    public JSONObject get(Player player) {
        JSONObject data = new JSONObject();
        PlayerInventory inv = player.getInventory();
        data.put("contents", serialize_itemstack(inv.getStorageContents()));
        data.put("armor", serialize_itemstack(inv.getArmorContents()));
        data.put("extra", serialize_itemstack(inv.getExtraContents()));
        data.put("enderchest", serialize_itemstack(player.getEnderChest().getContents()));
        return data;
    }

    private ItemStack[] deserialize_itemstack(JSONArray json) {
        Base64.Decoder decoder = Base64.getDecoder();
        int n = json.length();
        ItemStack[] list = new ItemStack[n];
        for (int i=0; i<n; i++) {
            String js = json.getString(i);
            list[i] = (js=="")?null:ItemStack.deserializeBytes(decoder.decode(json.getString(i)));
        }
        return list;
    }

    @Override
    public void put(Player player, JSONObject data) {
        info("restoring player's stuff");
        if (data == null) return;
        debug("  there is actual stuff");
        Base64.Decoder decoder = Base64.getDecoder();
        PlayerInventory inv = player.getInventory();
        if (data.has("contents")) {
            debug("  restoring contents");
            JSONArray contents_json = data.getJSONArray("contents");
            inv.setStorageContents(deserialize_itemstack(contents_json));
        }
        if (data.has("armor")) {
            debug("  restoring armor");
            JSONArray armor_json = data.getJSONArray("armor");
            inv.setArmorContents(deserialize_itemstack(armor_json));
        }
        if (data.has("extra")) {
            debug("  restoring extra");
            JSONArray extra_json = data.getJSONArray("extra");
            inv.setExtraContents(deserialize_itemstack(extra_json));
        }
        if (data.has("enderchest")) {
            debug("  restoring enderchest");
            JSONArray enderchest_json = data.getJSONArray("enderchest");
            player.getEnderChest().setContents(deserialize_itemstack(enderchest_json));
        }
    }
}
