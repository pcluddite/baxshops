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

import org.bukkit.ChatColor;
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
import java.util.Collections;
import java.util.List;

public final class CmdRestockFromInventory extends ShopCommand
{
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
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public @org.jetbrains.annotations.Nullable String getAction()
    {
        return "restockfrominventory";
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_OWNER;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "search for an item in your inventory to restock");
        help.setLongDescription("Searches for an item matching a shop entry to restock the shop. This is different " +
                "from '/shop restock' in that '/shop restock' only sells items you are currently holding");
        help.setArgs(
                new CommandHelpArgument("entry", "the entry that you are restocking", true),
                new CommandHelpArgument("qty", "the amount to restock", 1)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull CmdActor actor)
    {
        return actor.getNumArgs() == 3 || actor.getNumArgs() == 2;
    }

    @Override
    public boolean requiresPlayer(@NotNull CmdActor actor)
    {
        return true;
    }

    @Override
    public void onShopCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        if (actor.getNumArgs() == 2) {
            actor.appendArg("1");
        }

        if (actor.getShop().hasFlagInfinite()) {
            actor.exitError("This shop does not need to be restocked.");
        }

        BaxEntry entry = actor.getArg(1).asEntry();

        ItemStack stack = null;
        for(int index = 0; stack == null && index < actor.getInventory().getSize(); ++index) {
            if (entry.isSimilar(actor.getInventory().getItem(index), actor.getShop().hasFlagSmartStack())) {
                stack = actor.getInventory().getItem(index);
            }
        }
        if (stack == null)
            actor.exitError("You do not have any in your inventory to restock");

        BaxQuantity qty =  new BaxQuantity(actor.getArg(2).asString(), actor.getPlayer(), actor.getInventory(), stack);
        List<BaxEntry> taken = PlayerUtil.takeQtyFromInventory(qty, actor.getShop(), Collections.emptyList());

        BaxEntry takenItem = taken.get(0);
        entry.add(takenItem.getAmount());
        if (!(qty.isAll() || qty.isMost()) && takenItem.getAmount() < qty.getQuantity()) {
            actor.sendInfo("Could only restock with " + ChatColor.RED + "%d %s" + ChatColor.RESET + ". The shop now has %s.",
                    takenItem.getAmount(), ItemUtil.getName(takenItem), Format.number(entry.getAmount())
            );
        }
        else {
            actor.sendInfo("Restocked with %s in inventory. The shop now has %s.",
                    Format.itemName(takenItem.getAmount(), ItemUtil.getName(entry)), Format.number(entry.getAmount())
            );
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CmdActor actor, @NotNull Command command,
                                      @NotNull String alias, List<? extends CommandArgument> args)
    {
        ShopCmdActor shopActor = (ShopCmdActor)actor;
        if (shopActor.getShop() != null) {
            if (actor.getNumArgs() == 2) {
                return shopActor.getShop().getAllItemAliases();
            }
            else if (actor.getNumArgs() == 3) {
                return Arrays.asList("all", "most", "stack");
            }
        }
        return Collections.emptyList();
    }
}
