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

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.PlayerUtil;
import org.tbax.baxshops.Resources;
import org.tbax.baxshops.items.ItemUtil;
import org.tbax.baxshops.serialization.UpgradeableSerializable;
import org.tbax.baxshops.serialization.UpgradeableSerialization;
import org.tbax.bukkit.commands.CmdActor;
import org.tbax.bukkit.errors.PrematureAbortException;
import org.tbax.bukkit.notification.Claimable;
import org.tbax.bukkit.serialization.annotations.DoNotSerialize;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class HeadlessShopClaim implements Claimable, UpgradeableSerializable
{
    private Date date = new Date();
    private BaxShop shop;

    @DoNotSerialize
    private Material signType;

    public HeadlessShopClaim(Map<String, Object> args)
    {
        UpgradeableSerialization.deserialize(this, args);
    }

    public HeadlessShopClaim(@NotNull BaxShop shop)
    {
        this.shop = shop;
    }

    @Override
    public @NotNull String getMessage(CommandSender sender)
    {
        if (getRecipient().equals(sender)) {
            return "You have a shop with inventory that has no locations.";
        }
        return getMessage();
    }

    @Override
    public boolean claim(CmdActor actor)
    {
        ItemStack stack;
        try {
            stack = PlayerUtil.findSign(actor.getPlayer());
        }
        catch (PrematureAbortException e) {
            actor.sendMessage(e.getComponent());
            return false;
        }
        if (stack != null) {
            signType = stack.getType();
        }
        else if (!actor.isAdmin()) {
            actor.sendError(Resources.NOT_FOUND_SIGN, "to claim this shop");
            return false;
        }
        if(Claimable.super.claim(actor)) {
            if (!actor.isAdmin()) {
                actor.getPlayer().getInventory().remove(new ItemStack(getSignType(), 1));
            }
            return true;
        }
        return false;
    }

    @Override
    public @NotNull String getMessage()
    {
        return null;
    }

    @Override
    public @Nullable Date getSentDate()
    {
        return date;
    }

    @Override
    public void setSentDate(@Nullable Date sentDate)
    {
        date = sentDate;
    }

    @Override
    public @NotNull UUID getRecipientId()
    {
        return shop.getOwnerId();
    }

    @Override
    public void setRecipient(@NotNull OfflinePlayer player)
    {
        shop.setOwner(player);
    }

    public static HeadlessShopClaim deserialize(Map<String, Object> args)
    {
        return new HeadlessShopClaim(args);
    }

    public static HeadlessShopClaim valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }

    public Material getSignType()
    {
        if (signType == null)
            return ItemUtil.getDefaultSignType();
        return signType;
    }

    @Override
    public BaxEntry getEntry()
    {
        return new BaxEntry(shop.toItem(getSignType()));
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) return true;
        if (!(obj instanceof HeadlessShopClaim)) return false;
        return shop.getId().equals(((HeadlessShopClaim)obj).shop.getId());
    }
}
