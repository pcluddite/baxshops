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

import org.jetbrains.annotations.NotNull;
import org.tbax.bukkit.CommandHelp;
import org.tbax.bukkit.commands.CmdActor;
import org.tbax.bukkit.errors.PrematureAbortException;
import org.tbax.baxshops.Permissions;
import org.tbax.baxshops.Resources;
import org.tbax.baxshops.ShopPlugin;
import org.tbax.bukkit.notification.Notification;
import org.tbax.bukkit.notification.Request;
import org.tbax.bukkit.serialization.StoredPlayer;

public final class CmdReject extends ShopCommand
{
    @Override
    public @org.jetbrains.annotations.Nullable String getAction()
    {
        return "reject";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "no" };
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_OWNER;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "reject your latest notification");
        help.setLongDescription("Reject your most recent notification and remove it from your notification queue");
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull CmdActor actor)
    {
        return actor.getNumArgs() == 1;
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        return false;
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
    public void onShopCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        StoredPlayer storedPlayer = actor.getStoredPlayer();
        if (!storedPlayer.hasNotes()) {
            actor.exitError(Resources.NOT_FOUND_NOTE);
        }
        else {
            Notification n = storedPlayer.peekNote();
            if (n instanceof Request) {
                Request r = (Request)n;
                if (r.reject(actor)) {
                    storedPlayer.dequeueNote();
                }
                ShopPlugin.showNotificationCount(actor.getPlayer());
            }
            else {
                actor.sendError("Your current notification is not a request");
            }
        }
    }
}
