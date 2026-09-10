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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;

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
        if (stack.getEnchantments() != null && !stack.getEnchantments().isEmpty()) {
            JsonArray enchantArray = new JsonArray();
            for (Map.Entry<Enchantment, Integer> enchants : stack.getEnchantments().entrySet()) {
                JsonObject enchantMap = new JsonObject();
                enchantMap.addProperty("id", enchants.getKey().getKey().toString());
                enchantMap.addProperty("lvl", enchants.getValue());
                enchantArray.add(enchantMap);
            }
            return enchantArray;
        }
        return null;
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

    public JsonElement getTagElement()
    {
        JsonObject tag = new JsonObject();
        JsonElement enchantElement = getEnchantElement();
        if (enchantElement != null) {
            tag.add("Enchantments", enchantElement);
        }
        JsonElement displayElement = getDisplayElement();
        if (displayElement != null) {
            tag.add("display", displayElement);
        }
        if (stack.getType().getMaxDurability() > 0) {
            tag.addProperty("Damage", ItemUtil.getDurability(stack));
        }

        if (itemMeta instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta)itemMeta;
            PotionData potionData = potionMeta.getBasePotionData();
            PotionInfo potionInfo = ItemUtil.getNbtPotionInfo(potionMeta.getBasePotionData().getType());

            String name;
            if (potionInfo == null) {
                name = potionData.getType().name().toLowerCase();
            }
            else if (potionData.isExtended()) {
                name = potionInfo.getExtendedNbtName();
            }
            else if (potionData.isUpgraded()) {
                name = potionInfo.getUpgradedNbtName();
            }
            else {
                name = potionInfo.getNbtName();
            }

            tag.addProperty("Potion", "minecraft:" + name);
            JsonElement customPotionEffectsElement = getCustomPotionEffectsElement();
            if (customPotionEffectsElement != null) {
                tag.add("CustomPotionEffects", customPotionEffectsElement);
            }

            if (potionMeta.hasColor()) {
                tag.addProperty("CustomPotionColor", potionMeta.getColor().asRGB());
            }
        }

        if (tag.size() > 0) {
            return tag;
        } else {
            return null;
        }
    }

    public JsonElement getCustomPotionEffectsElement()
    {
        if (itemMeta instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta)itemMeta;
            if (potionMeta.getCustomEffects() != null && !potionMeta.getCustomEffects().isEmpty()) {
                JsonArray customPotionEffectElement = new JsonArray();
                for (PotionEffect effect : potionMeta.getCustomEffects()) {
                    JsonObject effectElement = new JsonObject();
                    effectElement.addProperty("Id", effect.getType().getName());
                    effectElement.addProperty("Amplifier", effect.getAmplifier());
                    effectElement.addProperty("Duration", effect.getDuration());
                    customPotionEffectElement.add(effectElement);
                }
                return customPotionEffectElement;
            }
        }
        return null;
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
        JsonElement tag = getTagElement();
        if (tag != null) {
            object.addProperty("nbt", tag.toString());
        }
        return object;
    }

    @Override
    public String toString()
    {
        return asJsonElement().toString();
    }
}
