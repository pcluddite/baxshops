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
package org.tbax.baxshops.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.Permissions;
import org.tbax.baxshops.ShopSelection;
import org.tbax.bukkit.CommandHelp;
import org.tbax.bukkit.CommandHelpArgument;
import org.tbax.bukkit.commands.CmdActor;
import org.tbax.bukkit.errors.PrematureAbortException;

import java.util.ArrayList;
import java.util.List;

public final class CmdList extends ShopCommand
{
    @Override
    public @org.jetbrains.annotations.Nullable String getAction()
    {
        return "list";
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_OWNER;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "list all shop locations");
        help.setLongDescription("List all locations (x, y, z) for this shop");
        help.setArgs(
                new CommandHelpArgument("page", "the page number", 1)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull CmdActor actor)
    {
        return actor.getNumArgs() == 1 || actor.getNumArgs() == 2;
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
        if (actor.getNumArgs() == 1) {
            actor.appendArg("1"); // show page 1 by default
        }

        if (actor.getShop().getLocations().size() > 1) {
            int page = actor.getArg(1).asInteger() - 1;
            if (page < 0 || page > actor.getShop().getLocations().size()) {
                actor.exitError("That's not a valid page number");
            }
            actor.sendMessage("Shop Locations");
            showLocList(actor, page);
        }
        else {
            actor.sendMessage(ChatColor.YELLOW + "This shop has no other locations.");
        }
    }

    private void showLocList(@NotNull ShopCmdActor actor, int page)
    {
        List<Location> locations = new ArrayList<>(actor.getShop().getLocations());
        int pages = (int)Math.ceil((double)locations.size() / ShopSelection.ITEMS_PER_PAGE);
        actor.getSender().sendMessage(Format.header(String.format("Showing page %d of %d", page + 1, pages)));
        int i = page * ShopSelection.ITEMS_PER_PAGE,
                stop = (page + 1) * ShopSelection.ITEMS_PER_PAGE,
                max = Math.min(stop, locations.size());
        for (; i < max; ++i) {
            Location loc = locations.get(i);
            actor.sendInfo("%-3s %-16s %-18s %s",
                    ChatColor.WHITE.toString() + (i + 1) + ".",
                    Format.location(loc),
                    ChatColor.LIGHT_PURPLE + actor.getShop().getSignTextString(loc),
                    (actor.getSelection().getLocation().equals(loc) ? ChatColor.LIGHT_PURPLE + " (current)" : ""));
        }
        for (; i < stop; i++) {
            actor.getSender().sendMessage("");
        }
    }
}