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
package org.tbax.baxshops.notification;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.*;
import org.tbax.bukkit.MathUtil;
import org.tbax.bukkit.commands.CmdActor;
import org.tbax.bukkit.errors.PrematureAbortException;
import org.tbax.bukkit.notification.Request;
import org.tbax.bukkit.notification.StandardNote;

import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public final class BuyRequest extends StandardNote implements Request
{
    public BuyRequest(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        super(shopId, buyer, seller, entry);
    }

    public BuyRequest(UUID shopId, UUID buyer, UUID seller, BaxEntry entry)
    {
        super(shopId, buyer, seller, entry);
    }

    public BuyRequest(Map<String, Object> args)
    {
        super(args);
    }

    @Override
    public boolean accept(CmdActor acceptingActor)
    {
        try {
            double price = MathUtil.multiply(entry.getAmount(), entry.getRetailPrice());

            Economy econ = ShopPlugin.getEconomy();

            if (!econ.has(getBuyer(), price)) {
                acceptingActor.exitError(Resources.NO_MONEY_SELLER);
            }

            econ.withdrawPlayer(getBuyer(), price);
            econ.depositPlayer(getSeller(), price);

            BuyClaim n = new BuyClaim(shopId, buyer, seller, entry);
            ShopPlugin.sendNotification(getBuyer(), n);

            acceptingActor.sendMessage("Offer accepted");
            acceptingActor.sendMessage(String.format(Resources.CURRENT_BALANCE, Format.money2(ShopPlugin.getEconomy().getBalance(acceptingActor.getPlayer()))));
            return true;
        }
        catch (PrematureAbortException e) {
            acceptingActor.sendMessage(e.getComponent());
            return false;
        }
    }

    @Override
    public boolean reject(CmdActor rejectingActor)
    {
        BaxShop shop = ShopPlugin.getShop(shopId);
        if (shop == null) {
            DeletedShopClaim shopDeleted = new DeletedShopClaim(buyer, entry);
            ShopPlugin.sendNotification(getBuyer(), shopDeleted);
        }
        else if (!shop.hasFlagInfinite()) {
            BaxEntry shopEntry = shop.find(entry.getItemStack());
            if (shopEntry == null) {
                shop.add(entry);
            }
            else {
                shopEntry.add(entry.getAmount());
            }
        }

        BuyRejection n = new BuyRejection(shopId, buyer, seller, entry);
        ShopPlugin.sendNotification(buyer, n);
        rejectingActor.sendError("Offer rejected");
        return true;
    }

    @Override
    public @NotNull String getMessage(CommandSender sender)
    {
        if (getSeller().equals(sender)) {
            return String.format("%s wants to buy %s from you for %s.",
                Format.username(buyer), entry.getFormattedName(), entry.getFormattedBuyPrice()
            );
        }
        else {
            return getMessage();
        }
    }

    @Override
    public @NotNull String getMessage()
    {
        return String.format("%s wants to buy %s from %s for %s.",
            Format.username(buyer), entry.getFormattedName(), Format.username2(seller), entry.getFormattedBuyPrice()
        );
    }

    @Override
    public @NotNull UUID getRecipientId()
    {
        return seller;
    }

    @Override
    public void setRecipient(@NotNull OfflinePlayer player)
    {
        seller = player.getUniqueId();
    }

    public static BuyRequest deserialize(Map<String, Object> args)
    {
        return new BuyRequest(args);
    }

    public static BuyRequest valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
