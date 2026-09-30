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

import java.util.List;

public final class CmdRemove extends ShopCommand
{
    @Override
    public @org.jetbrains.annotations.Nullable String getAction()
    {
        return "remove";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "rm" };
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_OWNER;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "remove an item from the shop");
        help.setLongDescription("Remove an entry from a shop. The current stock will be added to your inventory.");
        help.setArgs(
                new CommandHelpArgument("entry", "the name or entry number of the item to remove", true)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull CmdActor actor)
    {
        return actor.getNumArgs() == 2;
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
        BaxEntry entry = actor.getArg(1).asEntry();

        if (entry == null) {
            actor.exitError(Resources.NOT_FOUND_SHOPITEM);
        }
        assert shop != null;
        if (!shop.hasFlagInfinite() && entry.getAmount() > 0) {
            ItemStack stack = entry.toItemStack();
            int overflow = actor.giveItem(stack, false);
            entry.subtract(stack.getAmount() - overflow);
            if (overflow > 0) {
                actor.sendInfo(Resources.SOME_ROOM, stack.getAmount() - overflow, entry.getName());
            }
            else {
                actor.sendInfo("%s %s added to your inventory.",
                        Format.itemName(stack.getAmount(), ItemUtil.getName(entry)),
                        stack.getAmount() == 1 ? "was" : "were");
            }
        }
        if (!shop.hasFlagInfinite() && entry.getAmount() > 0) {
            actor.sendWarning("The shop entry was not removed");
        }
        else {
            shop.remove(entry);
            actor.sendMessage("The shop entry was removed.");
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CmdActor actor, @NotNull Command command,
                                      @NotNull String alias, List<? extends CommandArgument> args)
    {
        ShopCmdActor shopActor = (ShopCmdActor)actor;
        if (args.size() == 2 && shopActor.getShop() != null) {
            return shopActor.getShop().getAllItemAliases();
        }
        else {
            return super.onTabComplete(actor, command, alias, args);
        }
    }
}