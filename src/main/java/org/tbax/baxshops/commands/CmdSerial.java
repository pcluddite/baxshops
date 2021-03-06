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

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxEntry;
import org.tbax.bukkit.CommandHelp;
import org.tbax.bukkit.CommandHelpArgument;
import org.tbax.bukkit.commands.CmdActor;
import org.tbax.bukkit.errors.PrematureAbortException;
import org.tbax.baxshops.Permissions;

public final class CmdSerial extends ShopCommand
{
    @Override
    public @org.jetbrains.annotations.Nullable String getAction()
    {
        return "serial";
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_TRADER_BUY;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "show the shop entry as a YAML string");
        help.setLongDescription("Show what the shop entry would look like as a serialized YAML string");
        help.setArgs(
                new CommandHelpArgument("entry", "the shop entry to serialize", true)
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
        BaxEntry entry = actor.getArg(1).asEntry();
        YamlConfiguration config = new YamlConfiguration();
        config.set("entry", entry);
        actor.getSender().sendMessage(config.saveToString());
    }
}