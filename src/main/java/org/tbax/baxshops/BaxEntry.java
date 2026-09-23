/*
 * Copyright (C) Timothy Baxendale
 * Portions derived from Shops Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus.
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
package org.tbax.baxshops;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.items.EnchantMap;
import org.tbax.baxshops.items.ItemUtil;
import org.tbax.baxshops.serialization.UpgradeableSerializable;
import org.tbax.baxshops.serialization.UpgradeableSerialization;
import org.tbax.bukkit.MathUtil;
import org.tbax.bukkit.serialization.SafeMap;

import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public class BaxEntry implements UpgradeableSerializable
{
    private static final int CAN_BUY = 1;
    private static final int CAN_SELL = 2;

    private ItemStack stack = new ItemStack(Material.AIR);
    private double retailPrice = Integer.MAX_VALUE;
    private double refundPrice = 0;
    private int buySell = CAN_BUY;
    private int quantity = 0;
    
    public BaxEntry()
    {
    }

    public BaxEntry(@NotNull BaxEntry other)
    {
        quantity = other.quantity;
        refundPrice = other.refundPrice;
        retailPrice = other.retailPrice;
        buySell = other.buySell;
        stack = other.stack.clone();
    }

    public BaxEntry(@NotNull ItemStack item)
    {
        setItem(item);
        quantity = item.getAmount();
    }

    public BaxEntry(Map<String, Object> args)
    {
        UpgradeableSerialization.upgrade(this, args);
    }

    @Override @SuppressWarnings("unchecked")
    public void upgrade00300(@NotNull SafeMap map)
    {
        retailPrice = map.getDouble("retailPrice", 10000);
        refundPrice = map.getDouble("refundPrice", 0);
        if (map.get("stack") instanceof Map) {
            stack = ItemStack.deserialize((Map) map.get("stack"));
        }
        quantity = map.getInteger("quantity");
        buySell = CAN_BUY | (refundPrice >= 0 ? CAN_SELL : 0);
    }

    @Override @SuppressWarnings("unchecked")
    public void upgrade00421(@NotNull SafeMap map)
    {
        retailPrice = map.getDouble("retailPrice", retailPrice);
        refundPrice = map.getDouble("refundPrice", 0);
        quantity = map.getInteger("quantity", 0);
        if (map.get("stack") instanceof Map) {
            stack = ItemStack.deserialize((Map) map.get("stack"));
        }
        buySell = CAN_BUY | (refundPrice >= 0 ? CAN_SELL : 0);
    }

    @Override
    public void upgrade00450(@NotNull SafeMap map)
    {
        stack = map.getItemStack("stack", stack);
        retailPrice = map.getDouble("retailPrice", retailPrice);
        refundPrice = map.getDouble("refundPrice", 0);
        quantity = map.getInteger("quantity", 0);
        buySell = CAN_BUY | (refundPrice >= 0 ? CAN_SELL : 0);
    }

    @Override
    public void upgrade00451(@NotNull SafeMap map)
    {
        stack = map.getItemStack("stack", stack);
        retailPrice = map.getDouble("retailPrice", retailPrice);
        refundPrice = Math.max(map.getDouble("refundPrice", refundPrice), 0);
        quantity = map.getInteger("quantity", 0);
        buySell = map.getBoolean("canBuy", true) ? CAN_BUY : 0;
        buySell = buySell | (map.getBoolean("canSell", false) ? CAN_SELL : 0);
    }

    public boolean canBuy()
    {
        return (buySell & CAN_BUY) == CAN_BUY;
    }

    public void canBuy(boolean value)
    {
        if (value) {
            buySell = buySell | CAN_BUY;
        }
        else {
            buySell = buySell & ~CAN_BUY;
        }
    }

    public boolean canSell()
    {
        return (buySell & CAN_SELL) == CAN_SELL;
    }

    public void canSell(boolean value)
    {
        if (value) {
            buySell = buySell | CAN_SELL;
        }
        else {
            buySell = buySell & ~CAN_SELL;
        }
    }

    public double getRetailPrice()
    {
        return retailPrice;
    }

    public void setRetailPrice(double price)
    {
        retailPrice = price;
    }

    public double getRefundPrice()
    {
        return refundPrice;
    }

    public void setRefundPrice(double price)
    {
        refundPrice = price;
    }

    public Material getType()
    {
        return stack.getType();
    }

    public void add(int amt)
    {
        setAmount(getAmount() + amt);
    }
    
    public void subtract(int amt)
    {
        setAmount(getAmount() - amt);
    }
    
    public void setItem(@NotNull ItemStack item)
    {
        stack = item.clone();
    }

    public void setItem(@NotNull ItemStack item, int qty)
    {
        setItem(item);
        quantity = qty;
    }
    
    public void setItem(Material type)
    {
        stack = new ItemStack(type, getAmount());
    }
    
    public void setItem(Material type, int damage)
    {
        stack = new ItemStack(type, getAmount());
        setDurability(damage);
    }
        
    /**
     * clones this entry's item stack and sets its amount to this entry's quantity
     * If the entry quantity is equal to zero, the material type may be AIR
     * @return an ItemStack
     */
    public @NotNull ItemStack toItemStack()
    {
        ItemStack stack = this.stack.clone();
        stack.setAmount(quantity);
        if (quantity == 0)
            stack.setType(this.stack.getType());
        return stack;
    }
    
    /**
     * gets a reference to the ItemStack that this entry points to. the amount is not guaranteed to be the entry
     * quantity
     * @return the ItemStack
     */
    public @NotNull ItemStack getItemStack()
    {
        return stack;
    }
    
    public Map<Enchantment, Integer> getEnchantments()
    {
        return EnchantMap.getEnchants(stack);
    }
    
    public boolean hasItemMeta()
    {
        return stack.hasItemMeta();
    }
    
    public ItemMeta getItemMeta()
    {
        return stack.getItemMeta();
    }

    public void setAmount(int amt)
    {
        quantity = amt;
        ShopPlugin.logIf(quantity < 0, "shop has an item with a quantity of less than 0");
    }
    
    public int getAmount()
    {
        return quantity;
    }
    
    public int getDurability()
    {
        return ItemUtil.getDurability(stack);
    }

    public void setDurability(int durability)
    {
        ItemUtil.setDurability(stack, durability);
    }
    
    public int getDamagePercent()
    {
        return (int)Math.round((getDurability() * 100d) / stack.getType().getMaxDurability());
    }
    
    public void setDamagePercent(double pct)
    {
        double damage = (pct / 100d) * stack.getType().getMaxDurability();
        setDurability((int)damage);
    }

    public @NotNull String getName()
    {
        return ItemUtil.getName(this);
    }

    public @NotNull String getFormattedName()
    {
        return Format.itemName(getAmount(), getName());
    }

    public @NotNull String getFormattedSellPrice()
    {
        return Format.money(MathUtil.multiply(refundPrice, getAmount()));
    }

    public @NotNull String getFormattedSellPrice2()
    {
        return Format.money2(MathUtil.multiply(refundPrice, getAmount()));
    }

    public @NotNull String getFormattedBuyPrice()
    {
        return Format.money(MathUtil.multiply(retailPrice, getAmount()));
    }

    public @NotNull String getFormattedBuyPrice2()
    {
        return Format.money2(MathUtil.multiply(retailPrice, getAmount()));
    }
    
    @Override
    public String toString()
    {   
        return getItemStack().toString();
    }

    public String toString(int index, boolean infinite)
    {
        return toChatComponent(index, infinite).toString();
    }

    public Component toChatComponent(int index, boolean infinite)
    {
        Component component;
        boolean strikethrough = false;

        if (infinite) {
            component = Component.text(Format.bullet(index) + ". ", NamedTextColor.GRAY);
        }
        else if (getAmount() <= 0) {
            component = Component.text(index + " (0) ", NamedTextColor.RED, TextDecoration.STRIKETHROUGH);
            strikethrough = true;
        }
        else {
            component = Component.text(index + ". (" + getAmount() + ") ", NamedTextColor.GRAY);
        }

        if(stack.getType() == Material.ENCHANTED_BOOK && EnchantMap.isEnchanted(stack)) {
            String text = ItemUtil.getName(this);
            if (!strikethrough) {
                text = Format.enchantments(text);
            }
            Component name = Component.text(text)
                    .hoverEvent(getItemStack().asHoverEvent())
                    .clickEvent(ClickEvent.runCommand("/shop info " + index));
            component = component.append(name);
        }
        else {
            String text = ItemUtil.getName(this);
            if (!strikethrough) {
                text = Format.listname(text);
            }
            Component name = Component.text(text)
                    .hoverEvent(getItemStack().asHoverEvent())
                    .clickEvent(ClickEvent.runCommand("/shop info " + index));
            if (EnchantMap.isEnchanted(getItemStack())) {
                text = " (" + EnchantMap.abbreviatedListString(getItemStack()) + ")";
                if (!strikethrough) {
                    text = Format.enchantments(text);
                }
                name = name.append(Component.text(text));
            }
            component = component.append(name);
        }

        String potionInfo = ItemUtil.getPotionInfo(getItemStack());
        if (!potionInfo.isEmpty()) {
            potionInfo = " " + potionInfo;
            if (strikethrough) {
                potionInfo = Format.stripColor(potionInfo);
            }
            component = component.append(Component.text(potionInfo));
        }

        if (stack.getType().getMaxDurability() > 0 && getDurability() > 0) {
            Component damageComponent = Component.text(" (Damage: " + getDamagePercent() + "%)");
            if (!strikethrough) {
                damageComponent = damageComponent.color(NamedTextColor.YELLOW);
            }
            component = component.append(damageComponent);
        }

        if (canBuy()) {
            component = component.appendSpace();
            Component buyComponent = Component.text(Format.retailPrice(retailPrice))
                    .clickEvent(ClickEvent.suggestCommand("/buy " + index + " "))
                    .hoverEvent(HoverEvent.showText(Component.text("Purchase for " + Format.money(retailPrice))));
            component = component.append(buyComponent);
        }

        if (canSell()) {
            component = component.appendSpace();
            Component sellComponent = Component.text(Format.refundPrice(refundPrice))
                    .clickEvent(ClickEvent.suggestCommand("/shop sellfrominventory " + index + " "))
                    .hoverEvent(HoverEvent.showText(Component.text("Sell for " + Format.money(refundPrice))));
            component = component.append(sellComponent);
        }

        if (!canBuy() && !canSell()) {
            component = component.appendSpace();
            Component nfs = Component.text("(Not for Sale)");
            if (!strikethrough) {
                nfs = nfs.color(NamedTextColor.DARK_RED);
            }
            component = component.append(nfs);
        }

        return component;
    }
    
    public static BaxEntry deserialize(Map<String, Object> args)
    {
        return new BaxEntry(args);
    }
    
    public static BaxEntry valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }

    public boolean isSimilar(ItemStack item)
    {
        if (item == null)
            return false;
        return stack.isSimilar(item);
    }

    public boolean isSimilar(ItemStack item, boolean smartStack)
    {
        return ItemUtil.isSimilar(item, stack, smartStack);
    }

    public boolean isSimilar(BaxEntry entry)
    {
        if (entry == null)
            return false;
        if (Double.compare(entry.retailPrice, retailPrice) != 0) return false;
        if (Double.compare(entry.refundPrice, refundPrice) != 0) return false;
        return stack.isSimilar(entry.stack);
    }

    public boolean isSimilar(BaxEntry entry, boolean smartStack)
    {
        return ItemUtil.isSimilar(entry.stack, stack, smartStack);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(retailPrice, refundPrice, quantity);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj instanceof BaxEntry)
            return equals((BaxEntry)obj);
        if (obj instanceof ItemStack)
            return equals((ItemStack)obj);
        return false;
    }

    public boolean equals(BaxEntry entry)
    {
        if (entry == null) return false;
        if (this == entry) return true;
        if (Double.compare(entry.retailPrice, retailPrice) != 0) return false;
        if (Double.compare(entry.refundPrice, refundPrice) != 0) return false;
        if (buySell != entry.buySell) return false;
        return stack.isSimilar(entry.stack) && quantity == entry.quantity;
    }

    public boolean equals(BaxEntry entry, boolean smartStack)
    {
        if (!smartStack) return equals(entry);
        if (!equals(entry)) {
            return entry.getType() == getType()
                    && entry.getAmount() == quantity
                    && buySell == entry.buySell
                    && retailPrice == entry.retailPrice
                    && refundPrice == entry.refundPrice
                    && (ItemUtil.isSameBanner(entry.stack, stack)
                        || ItemUtil.isSameBook(entry.stack, stack));
        }
        return true;
    }

    public boolean equals(ItemStack stack)
    {
        if (stack == null)
            return false;
        return this.stack.isSimilar(stack) && stack.getAmount() == quantity;
    }

    public String getAlias()
    {
        String name = ItemUtil.getName(this).toLowerCase();
        return name.replace(' ', '_');
    }
}
