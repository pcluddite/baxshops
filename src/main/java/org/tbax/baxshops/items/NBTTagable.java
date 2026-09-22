/*
 * Copyright (C) Timothy Baxendale
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.tbax.baxshops.items;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.Map;

public final class NBTTagable
{
    private ItemStack stack;
    private ItemMeta itemMeta;

    public NBTTagable(ItemStack stack)
    {
        setStack(stack);
    }

    public ItemStack getStack()
    {
        return stack.clone();
    }

    public void setStack(ItemStack stack)
    {
        this.stack = stack.clone();
        this.itemMeta = this.stack.getItemMeta();
    }

    public JsonElement getEnchantElement()
    {
        if (stack.getEnchantments() == null || stack.getEnchantments().isEmpty())
            return null;
        JsonObject enchantObject = new JsonObject();
        for (Map.Entry<Enchantment, Integer> enchants : stack.getEnchantments().entrySet()) {
            enchantObject.addProperty(enchants.getKey().getKey().toString(), enchants.getValue());
        }
        return enchantObject;
    }

    public JsonElement getLoreElement()
    {
        if (itemMeta.hasLore()) {
            JsonArray lore = new JsonArray();
            for (String line : itemMeta.getLore()) {
                JsonObject text = new JsonObject();
                text.addProperty("text", line);
                lore.add(text.toString());
            }
            return lore;
        }
        return null;
    }

    public JsonElement getNameElement()
    {
        if (itemMeta.hasDisplayName()) {
            JsonObject text = new JsonObject();
            text.addProperty("text", stack.getItemMeta().getDisplayName());
            return text;
        }
        return null;
    }

    public JsonElement getDisplayElement()
    {
        JsonObject display = new JsonObject();
        JsonElement nameElement = getNameElement();
        if (nameElement != null) {
            display.add("Name", nameElement);
        }
        JsonElement loreElement = getLoreElement();
        if (loreElement != null) {
            display.add("Lore", loreElement);
        }
        if (display.size() > 0) {
            return display;
        }
        else {
            return null;
        }
    }

    public JsonElement getPotionContents()
    {
        if (itemMeta instanceof PotionMeta potionMeta) {
            JsonObject potionObject = new JsonObject();
            potionObject.addProperty("potion", potionMeta.getBasePotionType().getKey().getKey());
            return potionObject;
        }
        return null;
    }

    public JsonElement getComponentsElement()
    {
        JsonObject object = new JsonObject();
        JsonElement enchantObject = getEnchantElement();
        if (enchantObject != null) {
            object.add(NamespacedKey.minecraft("enchantments").toString(), enchantObject);
        }
        JsonElement potionObject = getPotionContents();
        if (potionObject != null) {
            object.add(NamespacedKey.minecraft("potion_contents").toString(), potionObject);
        }
        if (object.isEmpty())
            return null;
        return object;
    }

    public JsonElement asJsonElement()
    {
        return asJsonObject();
    }

    public JsonObject asJsonObject()
    {
        JsonObject object = new JsonObject();
        object.addProperty("id", stack.getType().getKey().toString());
        object.addProperty("count", stack.getAmount());
        JsonElement components = getComponentsElement();
        if (components != null) {
            object.add("components", components);
        }
        return object;
    }

    @Override
    public String toString()
    {
        return asJsonElement().toString();
    }
}
