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
package org.tbax.baxshops.internal.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.*;
import org.tbax.baxshops.commands.BaxShopCommand;
import org.tbax.baxshops.commands.ShopCmdActor;
import org.tbax.baxshops.errors.PrematureAbortException;
import org.tbax.baxshops.BaxQuantity;
import org.tbax.baxshops.internal.Permissions;
import org.tbax.baxshops.internal.Resources;
import org.tbax.baxshops.internal.items.ItemUtil;

import java.util.Arrays;
import java.util.List;

public final class CmdTake extends BaxShopCommand
{
    @Override
    public @NotNull String getName()
    {
        return "take";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "take", "t" };
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_OWNER;
    }

    @Override
    public CommandHelp getHelp(@NotNull ShopCmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "take an item from the shop");
        help.setLongDescription("Take an item from a shop. /shop buy is a synonym for this if you are the shop owner.");
        help.setArgs(
                new CommandHelpArgument("entry", "the item to take from the shop", true),
                new CommandHelpArgument("quantity", "the quantity to take from the shop", 1)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
    {
        BaxShop shop;
        if (actor.getNumArgs() == 3 || actor.getNumArgs() == 2)
            return true;
        shop = actor.getShop();
        return actor.getNumArgs() == 1 && shop != null && shop.size() == 1;
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresOwner(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresPlayer(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        BaxShop shop = actor.getShop();
        BaxEntry entry;
        assert shop != null;
        if (actor.getNumArgs() == 1) {
            actor.appendArgs(1, 1);
        }
        else if (actor.getNumArgs() == 2) {
            actor.appendArg(1);
        }

        entry = actor.getArgEntry(1);
        BaxQuantity amt = actor.getArgShopQty(2, entry);

        if (!shop.hasFlagInfinite()) {
            if (amt.getQuantity() > entry.getAmount()) {
                actor.exitError(Resources.NO_SUPPLIES);
            }
            else if (entry.getAmount() == 0) {
                actor.exitError("There's no more %s in the shop", entry.getName());
            }
        }
        if (amt.getQuantity() < 0) {
            actor.exitError(Resources.INVALID_DECIMAL, "amount to take");
        }
        else if (amt.getQuantity() == 0) {
            actor.exitWarning("You took nothing");
        }

        ItemStack stack = entry.toItemStack();
        stack.setAmount(amt.getQuantity());
        entry.subtract(amt.getQuantity());

        int overflow = actor.giveItem(stack);
        if (overflow > 0) {
            entry.add(overflow);
            actor.sendMessage(Resources.SOME_ROOM, stack.getAmount() - overflow, ItemUtil.getName(stack));
        }
        else {
            actor.sendMessage("%s %s added to your inventory.",
                    Format.itemName(stack.getAmount(), ItemUtil.getName(stack)),
                    amt.getQuantity() == 1 ? "was" : "were"
            );
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        ShopCmdActor actor = (ShopCmdActor)sender;
        if (actor.getShop() != null) {
            if (args.length == 2) {
                return actor.getShop().getAllItemAliases();
            }
            else if (args.length == 3) {
                return Arrays.asList("all", "fill", "most", "stack");
            }
        }
        return super.onTabComplete(sender, command, alias, args);
    }
}