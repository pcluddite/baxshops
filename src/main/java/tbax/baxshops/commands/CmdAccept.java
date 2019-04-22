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
package tbax.baxshops.commands;

import org.jetbrains.annotations.NotNull;
import tbax.baxshops.CommandHelp;
import tbax.baxshops.Resources;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.notification.Claimable;
import tbax.baxshops.notification.Notification;
import tbax.baxshops.notification.Request;

import java.util.Deque;

public final class CmdAccept extends BaxShopCommand
{
    @NotNull
    @Override
    public String getName()
    {
        return "accept";
    }

    @NotNull
    @Override
    public String[] getAliases()
    {
        return new String[]{"yes","a"};
    }

    @Override
    public String getPermission()
    {
        return null;
    }

    @Override
    public CommandHelp getHelp(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        CommandHelp help = super.getHelp(actor);
        help.setDescription("accept your most recent notification");
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull ShopCmdActor actor)
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
    public boolean requiresPlayer(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        Deque<Notification> notifications = actor.getNotifications();
        if (notifications.isEmpty()) {
            actor.exitError(Resources.NOT_FOUND_NOTE);
        }
        else {
            Notification n = notifications.getFirst();
            if (n instanceof Request) {
                Request r = (Request) n;
                if (r.accept(actor)) {
                    notifications.removeFirst();
                }
            } else if (n instanceof Claimable) {
                Claimable c = (Claimable) n;
                if (c.claim(actor)) {
                    notifications.removeFirst();
                }
            }
            actor.sendMessage("Last notification was accepted");
        }
        ShopPlugin.showNotificationCount(actor.getPlayer());
    }
}
