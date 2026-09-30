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
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.*;
import org.tbax.baxshops.items.ItemUtil;
import org.tbax.bukkit.CommandHelp;
import org.tbax.bukkit.CommandHelpArgument;
import org.tbax.bukkit.commands.CmdActor;
import org.tbax.bukkit.commands.CommandArgument;
import org.tbax.bukkit.errors.PrematureAbortException;

import java.util.List;

public final class CmdSet extends ShopCommand
{
    @Override
    public @org.jetbrains.annotations.Nullable String getAction()
    {
        return "set";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "setprice" };
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_OWNER;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "set a price for an item");
        help.setLongDescription("Change the buy or sell price for a shop item already in a shop");
        help.setArgs(
                new CommandHelpArgument("entry", "the item in the shop", true),
                new CommandHelpArgument("buy price", "the new price for buying a single item", true),
                new CommandHelpArgument("sell price", "the new price for selling a single item. If no price is specified, the item cannot be sold.", false)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull CmdActor actor)
    {
        return actor.getNumArgs() == 3 || actor.getNumArgs() == 4;
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
        return false;
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
        assert shop != null;

        BaxEntry entry = actor.getArg(1).asEntry();
        entry.canBuy(true);

        if (actor.getNumArgs() == 3) {
            actor.appendArg("0");
            entry.canSell(false);
        }
        else {
            entry.canSell(true);
        }

        double retailAmount = actor.getArg(2).asRoundedDouble(String.format(Resources.INVALID_DECIMAL, "buy price")),
                refundAmount = actor.getArg(3).asRoundedDouble(String.format(Resources.INVALID_DECIMAL, "sell price"));

        entry.setRetailPrice(retailAmount);
        entry.setRefundPrice(refundAmount);

        if (shop.hasFlagInfinite()) {
            actor.sendInfo("The price for %s was set.", Format.itemName(ItemUtil.getName(entry)));
        }
        else {
            actor.sendInfo("The price for %s was set.", Format.itemName(entry.getAmount(), ItemUtil.getName(entry)));
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