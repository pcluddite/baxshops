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

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.BaxShop;
import org.tbax.bukkit.commands.CmdActor;
import org.tbax.bukkit.commands.CommandArgument;
import org.tbax.bukkit.errors.PrematureAbortException;
import org.tbax.baxshops.Permissions;
import org.tbax.baxshops.ShopPlugin;
import org.tbax.baxshops.ShopSelection;
import org.tbax.bukkit.serialization.StoredPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public final class ShopCmdActor implements CmdActor
{
    private final CommandSender sender;
    private final Command command;
    private String name;
    private String excluded;

    private List<ShopCmdArg> args;

    public ShopCmdActor(CommandSender sender, Command command, String[] args)
    {
        this.sender = sender;
        this.command = command;
        this.name = command.getName();
        setArgs(args);
    }

    private void setArgs(String[] argsArray)
    {
        args = new ArrayList<>(argsArray.length);
        int idx = 0;
        for (; idx < argsArray.length - 1; ++idx) {
            String arg = argsArray[idx];
            if (Arrays.asList("-e", "--exclude", "--except").contains(arg)) {
                excluded = argsArray[++idx];
            }
            else {
                args.add(new ShopCmdArg(this, arg));
            }
        }
        if (idx < argsArray.length)
            args.add(new ShopCmdArg(this, argsArray[idx]));
    }

    @Override
    public void appendArg(String arg)
    {
        args.add(new ShopCmdArg(this, arg));
    }

    @Override
    public void appendArg(CommandArgument arg)
    {
        if (arg instanceof ShopCmdArg) {
            args.add((ShopCmdArg)arg);
        }
        throw new ClassCastException();
    }

    public @Nullable List<BaxEntry> getExcluded() throws PrematureAbortException
    {
        if (excluded == null)
            return null;
        return Collections.singletonList(getShop().getEntryFromString(excluded, "Excluded item not found in shop"));
    }

    @Override
    public @NotNull List<ShopCmdArg> getArgs()
    {
        return args;
    }

    @Override
    public CommandSender getSender()
    {
        return sender;
    }

    @Override
    public Command getCommand()
    {
        return command;
    }

    @Override
    public boolean isAdmin()
    {
        return sender.hasPermission(Permissions.SHOP_ADMIN);
    }

    public boolean isOwner()
    {
        ShopSelection selection = getSelection();
        return selection != null && selection.isOwner();
    }

    public @Nullable ShopSelection getSelection()
    {
        if (getPlayer() == null) {
            if (getSender() == Bukkit.getConsoleSender()) {
                return ShopPlugin.getSelection(StoredPlayer.DUMMY);
            }
            else {
                return null;
            }
        }
        else {
            return ShopPlugin.getSelection(getPlayer());
        }
    }

    @Override
    public ShopCmdArg getArg(int index)
    {
        return args.get(index);
    }

    public BaxShop getShop()
    {
        if (getSelection() != null)
            return getSelection().getShop();
        return null;
    }

    @Override
    public void setCommandName(String name)
    {
        this.name = name;
    }

    @Override
    public String getCommandName()
    {
        return name;
    }

    @Override
    public void setArg(int index, CommandArgument value)
    {
        if (value instanceof ShopCmdArg) {
            setArg(index, (ShopCmdArg)value);
        }
        else {
            setArg(index, new ShopCmdArg(this, value.asString()));
        }
    }

    public void setArg(int index, ShopCmdArg value)
    {
        args.set(index, value);
    }

    public StoredPlayer getStoredPlayer()
    {
        return ShopPlugin.getState().getOfflinePlayer(getPlayer().getUniqueId());
    }
}
