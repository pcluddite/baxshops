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
package org.tbax.baxshops.commands;

import org.bukkit.command.Command;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.*;
import org.tbax.baxshops.items.ItemUtil;
import org.tbax.bukkit.CommandHelp;
import org.tbax.bukkit.CommandHelpArgument;
import org.tbax.bukkit.commands.CmdActor;
import org.tbax.bukkit.commands.CommandArgument;
import org.tbax.bukkit.errors.PrematureAbortException;

import java.util.Arrays;
import java.util.List;

public final class CmdTake extends ShopCommand
{
    @Override
    public @org.jetbrains.annotations.Nullable String getAction()
    {
        return "take";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "t" };
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_OWNER;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
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
    public boolean hasValidArgCount(@NotNull CmdActor actor)
    {
        BaxShop shop;
        if (actor.getNumArgs() == 3 || actor.getNumArgs() == 2)
            return true;
        shop = ((ShopCmdActor)actor).getShop();
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
    public boolean requiresPlayer(@NotNull CmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onShopCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        BaxShop shop = actor.getShop();
        BaxEntry entry;
        assert shop != null;
        if (actor.getNumArgs() == 1) {
            actor.appendArgs("1", "1");
        }
        else if (actor.getNumArgs() == 2) {
            actor.appendArg("1");
        }

        entry = actor.getArg(1).asEntry();
        BaxQuantity amt = actor.getArg(2).asShopQty(entry);

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
    public List<String> onTabComplete(@NotNull CmdActor actor, @NotNull Command command,
                                      @NotNull String alias, List<? extends CommandArgument> args)
    {
        ShopCmdActor shopActor = (ShopCmdActor)actor;
        if (shopActor.getShop() != null) {
            if (args.size() == 2) {
                return shopActor.getShop().getAllItemAliases();
            }
            else if (args.size() == 3) {
                return Arrays.asList("all", "fill", "most", "stack", "1");
            }
        }
        return super.onTabComplete(actor, command, alias, args);
    }
}