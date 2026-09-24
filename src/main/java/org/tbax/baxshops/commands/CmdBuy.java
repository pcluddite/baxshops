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
import org.tbax.baxshops.notification.BuyNotification;
import org.tbax.baxshops.notification.BuyRequest;
import org.tbax.bukkit.CommandHelp;
import org.tbax.bukkit.CommandHelpArgument;
import org.tbax.bukkit.MathUtil;
import org.tbax.bukkit.commands.BaxCommand;
import org.tbax.bukkit.commands.CmdActor;
import org.tbax.bukkit.commands.CommandArgument;
import org.tbax.bukkit.errors.PrematureAbortException;

import java.util.Arrays;
import java.util.List;

public final class CmdBuy extends ShopCommand
{
    @Override
    public @org.jetbrains.annotations.Nullable String getAction()
    {
        return "buy";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "b" };
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_TRADER_BUY;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "buy an item from a shop");
        help.setLongDescription("Buy an item from the selected shop. You will be charged the purchase amount with the funds credited to the owner.");
        help.setArgs(
                new CommandHelpArgument("item", "the name of the item or an entry number in the shop", true),
                new CommandHelpArgument("quantity", "the quantity you wish to buy", 1)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull CmdActor actor)
    {
        if (actor.getNumArgs() == 1) {
            return ((ShopCmdActor)actor).getShop() != null && ((ShopCmdActor)actor).getShop().size() == 1;
        }
        return actor.getNumArgs() == 2 || actor.getNumArgs() == 3;
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
        return false;
    }

    @Override
    public boolean useAlternative(CmdActor actor)
    {
        return ((ShopCmdActor)actor).isOwner();
    }

    @Override
    public @NotNull Class<? extends BaxCommand> getAlternative()
    {
        return CmdTake.class;
    }

    @Override
    public void onShopCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException // tested OK 3/16/19
    {
        if (actor.getNumArgs() == 1) {
            actor.appendArgs("1", "1");
        }
        if (actor.getNumArgs() == 2) {
            actor.appendArgs("1");
        }

        BaxShop shop = actor.getShop();
        BaxEntry entry = actor.getArg(1).asEntry();
        if (!entry.canBuy())
            actor.exitError("%s is not for sale", entry.getName());

        BaxQuantity amount = actor.getArg(2).asShopQty(entry);
        if (amount.getQuantity() == 0) {
            actor.exitWarning("You purchased nothing");
        }
        else if (amount.getQuantity() < 0) {
            actor.exitError(Resources.INVALID_DECIMAL, "amount to buy");
        }
        else if (entry.getAmount() < amount.getQuantity() && !shop.hasFlagInfinite()) {
            actor.exitError(Resources.NO_SUPPLIES);
        }

        String itemName = ItemUtil.getName(entry);
        double price = MathUtil.multiply(amount.getQuantity(), entry.getRetailPrice());

        if (!ShopPlugin.getEconomy().has(actor.getPlayer(), price)) {
            actor.exitError("You do not have enough money");
        }

        BaxEntry purchased = new BaxEntry(entry);
        purchased.setAmount(amount.getQuantity());

        if (shop.hasFlagBuyRequests()) {
            if (!shop.hasFlagInfinite()) {
                entry.subtract(amount.getQuantity());
            }

            BuyRequest request = new BuyRequest(shop.getId(), actor.getPlayer(), shop.getOwner(), purchased);
            ShopPlugin.sendNotification(shop.getOwner(), request);
            actor.sendMessage("Your request to buy %s for %s has been sent.", Format.itemName(purchased.getAmount(), itemName), Format.money(price));
        }
        else {
            int overflow = actor.giveItem(purchased.toItemStack());
            if (overflow > 0) {
                price = MathUtil.multiply((amount.getQuantity() - overflow), entry.getRetailPrice());
                actor.sendMessage(Resources.SOME_ROOM + ". You were charged %s.", amount.getQuantity() - overflow, itemName, Format.money(price));
            }
            else {
                actor.sendMessage("You bought %s for %s.", Format.itemName(purchased.getAmount(), itemName), Format.money(price));
            }
            ShopPlugin.getEconomy().withdrawPlayer(actor.getPlayer(), price);
            if (!shop.hasFlagInfinite()) {
                entry.subtract(amount.getQuantity() - overflow);
            }

            ShopPlugin.getEconomy().depositPlayer(shop.getOwner(), price);

            purchased.subtract(overflow);

            actor.sendMessage(Resources.CURRENT_BALANCE, Format.money2(ShopPlugin.getEconomy().getBalance(actor.getPlayer())));
            ShopPlugin.sendNotification(shop.getOwner(), new BuyNotification(shop.getId(), actor.getPlayer(), shop.getOwner(), purchased));
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