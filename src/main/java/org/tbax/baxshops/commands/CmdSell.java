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
import org.tbax.baxshops.notification.SaleRequest;
import org.tbax.bukkit.CommandHelp;
import org.tbax.bukkit.CommandHelpArgument;
import org.tbax.bukkit.MathUtil;
import org.tbax.bukkit.commands.BaxCommand;
import org.tbax.bukkit.commands.CmdActor;
import org.tbax.bukkit.commands.CommandArgument;
import org.tbax.bukkit.errors.PrematureAbortException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class CmdSell extends ShopCommand
{
    @Override
    public @org.jetbrains.annotations.Nullable String getAction()
    {
        return "sell";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "s" };
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_TRADER_SELL;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "sell an item");
        help.setLongDescription("Sell an item to a shop or send a request if sell requests for the shop are active");
        help.setArgs(
                new CommandHelpArgument("quantity", "the quantity to you wish to sell", false)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull CmdActor actor)
    {
        return actor.getNumArgs() == 2 || actor.getNumArgs() == 1;
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresOwner(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresPlayer(@NotNull CmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return actor.getNumArgs() < 2 || !BaxQuantity.isAny(actor.getArg(1).asString());
    }

    @Override
    public boolean useAlternative(CmdActor actor)
    {
        return ((ShopCmdActor)actor).isOwner();
    }

    @Override
    public @NotNull Class<? extends BaxCommand> getAlternative()
    {
        return CmdRestock.class;
    }

    @Override
    public boolean allowsExclusion(ShopCmdActor actor)
    {
        return actor.getNumArgs() == 2 && BaxQuantity.isAny(actor.getArg(1).asString());
    }

    @Override
    public void onShopCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        if (actor.getNumArgs() == 1) {
            assert actor.getItemInHand() != null;
            actor.appendArg(String.valueOf(actor.getItemInHand().getAmount()));
        }

        BaxShop shop = actor.getShop();
        assert shop != null;
        if (requiresItemInHand(actor) && !shop.contains(actor.getItemInHand()))
            actor.exitError("The owner of the shop isn't buying %s", ItemUtil.getName(actor.getItemInHand()));
        List<BaxEntry> items = actor.getArg(1).peekFromInventory().stream()
                .filter(BaxEntry::canSell)
                .collect(Collectors.toList());

        if (items.isEmpty()) {
            if (requiresItemInHand(actor)) {
                actor.exitError("The owner of the shop isn't buying %s", ItemUtil.getName(actor.getItemInHand()));
            }
            else {
                actor.exitWarning("You did not have anything to sell at this shop.");
            }
        }

        double total = 0.0;
        for (BaxEntry entry : items) {
            if (entry.getAmount() > 0) {
                double price = sell(actor, entry);
                if (price >= 0.0) {
                    total = MathUtil.add(total, price);
                }
            }
        }
        if (total > 0.0) {
            actor.sendInfo("You earned %s.", Format.money(total));
            actor.sendInfo(Resources.CURRENT_BALANCE, Format.money2(ShopPlugin.getEconomy().getBalance(actor.getPlayer())));
        }
        else if (shop.hasFlagSellRequests()) {
            actor.sendInfo("Your money will be deposited when the buyer accepts the sale.");
        }
    }

    static double sell(ShopCmdActor actor, BaxEntry entry) throws PrematureAbortException
    {
        BaxShop shop = actor.getShop();

        String name = ItemUtil.getName(entry.getItemStack());
        double price = MathUtil.multiply(entry.getAmount(), entry.getRefundPrice());
        if (shop.hasFlagSellRequests()) {
            SaleRequest request = new SaleRequest(shop.getId(), shop.getOwner(), actor.getPlayer(), entry);
            ShopPlugin.sendNotification(shop.getOwner(), request);
            actor.sendInfo("Your request to sell %s for %s has been sent.",
                    Format.itemName(entry.getAmount(), name), Format.money(price)
            );
            return 0;
        }
        else {
            PlayerUtil.sellItem(shop, shop.getOwner(), actor.getPlayer(), entry);
            PlayerUtil.takeFromInventory(actor.getPlayer().getInventory(), entry.getItemStack(), entry.getAmount(), actor.getShop().hasFlagSmartStack());
            actor.sendInfo(
                    "You have sold %s for %s to %s.",
                    Format.itemName(entry.getAmount(), name),
                    Format.money(price),
                    Format.username(shop.getOwner().getName())
            );
            return price;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CmdActor actor, @NotNull Command command,
                                      @NotNull String alias, List<? extends CommandArgument> args)
    {
        if (actor.getNumArgs() == 2) {
            return Arrays.asList("all", "any", "most", "stack");
        }
        return super.onTabComplete(actor, command, alias, args);
    }
}