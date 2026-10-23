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
package org.tbax.baxshops.notification;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.PlayerUtil;
import org.tbax.baxshops.ShopPlugin;
import org.tbax.bukkit.commands.CmdActor;
import org.tbax.bukkit.errors.PrematureAbortException;
import org.tbax.bukkit.notification.Request;
import org.tbax.bukkit.notification.StandardNote;

import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public final class SaleRequest extends StandardNote implements Request
{
    public SaleRequest(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        super(shopId, buyer, seller, entry);
    }

    public SaleRequest(UUID shopId, UUID buyer, UUID seller, BaxEntry entry)
    {
        super(shopId, buyer, seller, entry);
    }

    public SaleRequest(Map<String, Object> args)
    {
        super(args);
    }

    @Override
    public boolean accept(CmdActor acceptingActor)
    {
        try {
            PlayerUtil.sellItem(shopId, buyer, seller, entry);
            ShopPlugin.sendNotification(seller, new SaleNotification(shopId, buyer, seller, entry));
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
        SaleRejection rejection = new SaleRejection(shopId, buyer, seller, entry);
        ShopPlugin.sendNotification(seller, rejection);
        rejectingActor.sendError("Offer rejected");
        return true;
    }

    @Override
    public @NotNull String getMessage(CommandSender sender)
    {
        if (getBuyer().equals(sender)) {
            return String.format("%s wants to sell you %s for %s.",
                Format.username(seller), entry.getFormattedName(), entry.getFormattedSellPrice()
            );
        }
        else {
            return getMessage();
        }
    }

    @Override
    public @NotNull String getMessage()
    {
        return String.format("%s wants to sell %s to %s for %s.",
            Format.username(seller), entry.getFormattedName(), Format.username2(buyer), entry.getFormattedSellPrice()
        );
    }

    @Override
    public @NotNull UUID getRecipientId()
    {
        return buyer;
    }

    @Override
    public void setRecipient(@NotNull OfflinePlayer player)
    {
        buyer = player.getUniqueId();
    }

    public static SaleRequest deserialize(Map<String, Object> args)
    {
        return new SaleRequest(args);
    }

    public static SaleRequest valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
