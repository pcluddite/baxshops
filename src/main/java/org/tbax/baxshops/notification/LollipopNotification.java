/*
 * Copyright (C) 2013-2019 Timothy Baxendale
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
import org.tbax.baxshops.Format;
import org.tbax.baxshops.ShopPlugin;
import org.tbax.baxshops.serialization.SafeMap;
import org.tbax.baxshops.serialization.StoredPlayer;
import org.tbax.baxshops.serialization.UpgradeableSerializable;
import org.tbax.baxshops.serialization.UpgradeableSerialization;
import org.tbax.baxshops.serialization.annotations.SerializeMethod;
import org.tbax.baxshops.serialization.states.State_00300;

import java.util.*;

/**
 * A LollipopNotification notifies a player that someone sent him/her a
 * lollipop.
 */
public final class LollipopNotification implements Notification, UpgradeableSerializable
{
    public static final String DEFAULT_TASTINESS = "";
    public static final String[] STOCK_ADJECTIVES =  {
        "disgusting",
        "bad",
        "icky",
        "bland",
        "OK",
        "good",
        "great",
        "tasty",
        "delicious",
        "wonderful"
    };

    @SerializeMethod(getter = "getSender")
    private UUID sender;

    @SerializeMethod(getter = "getRecipientId")
    private UUID recipient;

    private String tastiness;
    private Date date;

    public LollipopNotification(Map<String, Object> args)
    {
        UpgradeableSerialization.upgrade(this, args);
    }

    public LollipopNotification(OfflinePlayer sender, OfflinePlayer recipient, String tastiness)
    {
        this.sender = sender.getUniqueId();
        this.recipient = recipient.getUniqueId();
        this.tastiness = tastiness;
        this.date = new Date();
    }

    @Override
    public void upgrade00300(@NotNull SafeMap map)
    {
        sender = State_00300.getPlayerId(map.getString("sender"));
        recipient = StoredPlayer.ERROR_UUID;
        tastiness = getStockAdjective(map.getDouble("tastiness"));
    }

    @Override
    public void upgrade00400(@NotNull SafeMap map)
    {
        sender = map.getUUID("sender");
        recipient = StoredPlayer.ERROR_UUID;
        tastiness = getStockAdjective(map.getDouble("tastiness"));
    }

    @Override
    public void upgrade00410(@NotNull SafeMap map)
    {
        sender = map.getUUID("sender");
        recipient = map.getUUID("recipient");
        tastiness = getStockAdjective(map.getDouble("tastiness"));
        date = map.getDate("date");
    }

    @Override
    public void upgrade00411(@NotNull SafeMap map)
    {
        sender = map.getUUID("sender");
        recipient = map.getUUID("recipient");
        tastiness = map.getString("tastiness");
        date = map.getDate("date");
    }

    public OfflinePlayer getSender()
    {
        return ShopPlugin.getOfflinePlayer(sender);
    }

    @Override
    public @NotNull String getMessage(CommandSender sender)
    {
        if (getRecipient().equals(sender)) {
            return String.format("%s sent you %s lollipop",
                    Format.username2(getSender().getName()),
                    Format.keyword(getAdornedTastiness())
            );
        }
        else {
            return getMessage();
        }
    }

    @Override
    public @NotNull UUID getRecipientId()
    {
        return recipient;
    }

    @Override
    public void setRecipient(@NotNull OfflinePlayer player)
    {
        recipient = player.getUniqueId();
    }

    @Override
    public @NotNull String getMessage()
    {
        return String.format("%s sent %s %s lollipop",
                Format.username(getSender().getName()),
                Format.username2(getRecipient().getName()),
                getAdornedTastiness()
        );
    }

    @Override
    public Date getSentDate()
    {
        return date;
    }

    public static LollipopNotification deserialize(Map<String, Object> args)
    {
        return new LollipopNotification(args);
    }

    public static LollipopNotification valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }

    public static String getStockAdjective(double tastiness)
    {
        tastiness = Math.max(tastiness, 0) % 101;
        if (tastiness >= 55.0 && tastiness < 60.0)
            return "better-than-average";
        if (tastiness >= 40 && tastiness < 50)
            return "";
        String adjective = "";
        for (int i = 0; i < STOCK_ADJECTIVES.length; ++i) {
            int j = i > 3 ? i + 1 : i; // skip 4
            if (tastiness >= (j * 10)) {
                adjective = STOCK_ADJECTIVES[i];
            }
        }
        return adjective;
    }

    public String getTastiness()
    {
        return tastiness;
    }

    public String getAdornedTastiness()
    {
        if (tastiness == null || "".equals(tastiness))
            return "a";
        if (Arrays.asList('a', 'e', 'i', 'o', 'u').contains(Character.toLowerCase(tastiness.charAt(0)))) {
            return "an " + tastiness;
        }
        else {
            return "a " + tastiness;
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LollipopNotification that = (LollipopNotification) o;
        return Objects.equals(sender, that.sender) &&
                Objects.equals(recipient, that.recipient) &&
                Objects.equals(tastiness, that.tastiness);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(sender, recipient, tastiness);
    }
}
