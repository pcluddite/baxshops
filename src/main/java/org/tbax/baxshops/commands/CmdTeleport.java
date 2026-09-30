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

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.Permissions;
import org.tbax.baxshops.ShopSelection;
import org.tbax.bukkit.CommandHelp;
import org.tbax.bukkit.CommandHelpArgument;
import org.tbax.bukkit.commands.CmdActor;
import org.tbax.bukkit.commands.CommandArgument;
import org.tbax.bukkit.errors.PrematureAbortException;

import java.util.Arrays;
import java.util.List;

public final class CmdTeleport extends ShopCommand
{
    @Override
    public @org.jetbrains.annotations.Nullable String getAction()
    {
        return "teleport";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "tp" };
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_ADMIN;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "teleport to a shop location");
        help.setLongDescription("Teleport to a specific shop location. Use /shop list for a list of locations. This can only be done by an admin.");
        help.setArgs(
                new CommandHelpArgument("index", "the index of the shop location", true)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull CmdActor actor)
    {
        return actor.getNumArgs() == 2;
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
    public void onShopCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        ShopSelection selection = actor.getSelection();

        int loc = actor.getArg(1).asInteger("Expected a location number. For a list of locations, use /shop list.");
        if (loc < 1 || loc > selection.getShop().getLocations().size()) {
            actor.exitError("That shop location does not exist.");
        }

        Location old = selection.getLocation();
        selection.setLocation((Location)selection.getShop().getLocations().toArray()[loc - 1]);
        if (actor.getPlayer().teleport(selection.getLocation())) {
            actor.sendInfo("Teleported you to %s", Format.location(selection.getLocation()));
        }
        else {
            selection.setLocation(old);
            actor.exitError("Unable to teleport you to that location.");
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CmdActor actor, @NotNull Command command,
                                      @NotNull String alias, List<? extends CommandArgument> args)
    {
        ShopCmdActor shopActor = (ShopCmdActor)actor;
        if (actor.isAdmin() && actor.getNumArgs() == 2 && shopActor.getShop() != null) {
            String[] nums = new String[shopActor.getShop().getLocations().size()];
            for (int i = 0; i < nums.length; ++i) {
                nums[i] = String.valueOf(i + 1);
            }
            return Arrays.asList(nums);
        }
        return super.onTabComplete(actor, command, alias, args);
    }
}