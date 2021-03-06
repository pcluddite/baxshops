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

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.Format;
import org.tbax.bukkit.notification.Claimable;
import org.tbax.bukkit.notification.StandardNote;
import org.tbax.bukkit.serialization.SafeMap;

import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public final class SaleRejection extends StandardNote implements Claimable
{
    public SaleRejection(UUID shopId, OfflinePlayer buyer, OfflinePlayer seller, BaxEntry entry)
    {
        super(shopId, buyer, seller, entry);
    }

    public SaleRejection(UUID shopId, UUID buyer, UUID seller, BaxEntry entry)
    {
        super(shopId, buyer, seller, entry);
    }

    public SaleRejection(Map<String, Object> args)
    {
        super(args);
    }

    @Override
    public void upgrade00300(@NotNull SafeMap map)
    {
        legacyBuyer = map.getString("buyer");
        legacySeller = map.getString("seller");
        shopId = BaxShop.DUMMY_UUID;
        entry = map.getBaxEntry("entry");
    }

    @Override
    public @NotNull String getMessage(CommandSender sender)
    {
        if (getSeller().equals(sender)) {
            return String.format("%s rejected your request to sell %s",
                Format.username(buyer), entry.getFormattedName()
            );
        }
        else {
            return getMessage();
        }
    }

    @Override
    public @NotNull String getMessage()
    {
        return String.format("%s rejected %s's request to sell %s",
            Format.username(buyer), Format.username2(seller), entry.getFormattedName()
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

    public static SaleRejection deserialize(Map<String, Object> args)
    {
        return new SaleRejection(args);
    }

    public static SaleRejection valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
