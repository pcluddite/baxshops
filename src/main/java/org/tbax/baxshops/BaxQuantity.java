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
package org.tbax.baxshops;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.versioning.LegacyPlayerUtil;
import org.tbax.bukkit.errors.CommandErrorException;
import org.tbax.bukkit.errors.PrematureAbortException;

import java.util.Arrays;

@SuppressWarnings("unused")
public class BaxQuantity
{
    private String argument;
    private ItemStack stack;
    private Iterable<ItemStack> inventory;
    private Player player;

    public BaxQuantity(@NotNull String arg, @NotNull Player player, @NotNull Iterable<ItemStack> inv, ItemStack item)
    {
        argument = arg;
        stack = item;
        inventory = inv;
        this.player = player;
    }

    public void setItem(ItemStack stack)
    {
        this.stack = stack;
    }

    public ItemStack getItem()
    {
        return stack;
    }

    public void setInventory(@NotNull Iterable<ItemStack> inv)
    {
        inventory = inv;
    }

    public void setInventory(@NotNull ItemStack[] inv)
    {
        setInventory(Arrays.asList(inv));
    }

    public @NotNull Iterable<ItemStack> getInventory()
    {
        return inventory;
    }

    public int getQuantity() throws PrematureAbortException
    {
        try {
            return Integer.parseInt(argument);
        }
        catch (NumberFormatException e) {
            if (isAll()) {
                return getAmountInInventory();
            }
            else if (isMost()) {
                return stack.getAmount() - 1;
            }
            else if (isStack()) {
                return stack.getMaxStackSize();
            }
            else if (isFill()) {
                ItemStack playerStack = LegacyPlayerUtil.getItemInHand(player);
                if (playerStack == null || stack.getType() != playerStack.getType()) {
                    int idx = player.getInventory().first(stack.getType());
                    if (idx < 0) {
                        return stack.getMaxStackSize();
                    }
                    else {
                        playerStack = player.getInventory().getItem(idx);
                    }
                }
                return stack.getMaxStackSize() - playerStack.getAmount();
            }
            throw new CommandErrorException("'" + argument + "' is not a valid quantity", e);
        }
    }

    public boolean isQuantityNotAny()
    {
        return isQuantityNotAny(argument);
    }

    public boolean isQuantity()
    {
        return isQuantity(argument);
    }

    public static boolean isQuantityNotAny(String qty)
    {
        return isFiniteQty(qty) || isAbstractQtyNotAny(qty);
    }

    public static boolean isQuantity(String qty)
    {
        return isQuantityNotAny(qty) || isAny(qty);
    }

    private int getAmountInInventory()
    {
        int qty = 0;
        for(ItemStack stack : inventory) {
            if (stack != null && stack.isSimilar(this.stack)) {
                qty += stack.getAmount();
            }
        }
        return qty;
    }

    public boolean isAll()
    {
        return isAll(argument);
    }

    public boolean isAny()
    {
        return isAny(argument);
    }

    public boolean isMost()
    {
        return isMost(argument);
    }

    public boolean isStack()
    {
        return isStack(argument);
    }

    public boolean isFill()
    {
        return isFill(argument);
    }

    public boolean isAbstractQtyNotAny()
    {
        return isAbstractQtyNotAny(argument);
    }

    public boolean isAbstractQty()
    {
        return isAbstractQty(argument);
    }

    public boolean isFiniteQty()
    {
        return isFiniteQty(argument);
    }

    public static boolean isAll(String argument)
    {
        return "all".equalsIgnoreCase(argument);
    }

    public static boolean isAny(String argument)
    {
        return "any".equalsIgnoreCase(argument);
    }

    public static boolean isMost(String argument)
    {
        return "most".equalsIgnoreCase(argument);
    }

    public static boolean isStack(String argument)
    {
        return "stack".equalsIgnoreCase(argument);
    }

    public static boolean isFill(String argument)
    {
        return "fill".equals(argument);
    }

    public static boolean isAbstractQtyNotAny(String argument)
    {
        return isAll(argument) || isFill(argument) || isMost(argument) || isStack(argument);
    }

    public static boolean isAbstractQty(String argument)
    {
        return isAbstractQtyNotAny(argument) || isAny(argument);
    }

    public static boolean isFiniteQty(String argument)
    {
        try {
            Integer.parseInt(argument);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }
}
