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
package org.tbax.bukkit.commands;

import org.bukkit.OfflinePlayer;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.FormatText;
import org.tbax.baxshops.ShopPlugin;
import org.tbax.bukkit.errors.CommandErrorException;
import org.tbax.bukkit.errors.PrematureAbortException;
import org.tbax.bukkit.serialization.StoredPlayer;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public class CommandArgument
{
    private final String arg;

    public CommandArgument(String arg)
    {
        this.arg = arg;
    }

    public String asString()
    {
        return arg;
    }
    
    public int asInteger() throws PrematureAbortException
    {
        return asInteger(String.format("'%s' should be a whole number", arg));
    }

    public int asInteger(String errMsg) throws PrematureAbortException
    {
        try {
            return Integer.parseInt(arg);
        }
        catch(NumberFormatException e) {
            throw new CommandErrorException(e, errMsg);
        }
    }
    
    public boolean isInteger()
    {
        try {
            Integer.parseInt(arg);
            return true;
        }
        catch(NumberFormatException e) {
            return false;
        }
    }

    public long asLong() throws PrematureAbortException
    {
        return asLong(String.format("'%s' should be a whole number", arg));
    }

    public long asLong(String errMsg) throws PrematureAbortException
    {
        try {
            return Long.parseLong(arg);
        }
        catch(NumberFormatException e) {
            throw new CommandErrorException(e, errMsg);
        }
    }

    public boolean isLong()
    {
        try {
            Long.parseLong(arg);
            return true;
        }
        catch(NumberFormatException e) {
            return false;
        }
    }

    public short asShort() throws PrematureAbortException
    {
        return asShort(String.format("'%s' should be a whole number", arg));
    }

    public short asShort(String errMsg) throws PrematureAbortException
    {
        try {
            return Short.parseShort(arg);
        }
        catch(NumberFormatException e) {
            throw new CommandErrorException(e, errMsg);
        }
    }

    public boolean isShort()
    {
        try {
            Short.parseShort(arg);
            return true;
        }
        catch(NumberFormatException e) {
            return false;
        }
    }

    public double asDouble() throws PrematureAbortException
    {
        return asShort(String.format("'%s' should be a number", arg));
    }

    public double asDouble(String errMsg) throws PrematureAbortException
    {
        try {
            return Double.parseDouble(arg);
        }
        catch(NumberFormatException e) {
            throw new CommandErrorException(e, errMsg);
        }
    }

    public boolean isDouble()
    {
        try {
            Double.parseDouble(arg);
            return true;
        }
        catch(NumberFormatException e) {
            return false;
        }
    }

    public double asRoundedDouble() throws PrematureAbortException
    {
        return Math.round(100d * asDouble()) / 100d;
    }

    public double asRoundedDouble(String errMsg) throws PrematureAbortException
    {
        return Math.round(100d * asDouble(errMsg)) / 100d;
    }

    public boolean asBoolean() throws PrematureAbortException
    {
        return asBoolean(String.format("'%s' should be either 'true' or 'false'", arg));
    }

    public boolean asBoolean(String errMsg) throws PrematureAbortException
    {
        if (isBoolean()) {
            return "true".equalsIgnoreCase(arg)
                    || "yes".equalsIgnoreCase(arg)
                    || "1".equalsIgnoreCase(arg);
        } else {
            throw new CommandErrorException(errMsg);
        }
    }

    public boolean isBoolean()
    {
        return "true".equalsIgnoreCase(arg) || "false".equalsIgnoreCase(arg)
                || "yes".equalsIgnoreCase(arg) || "no".equalsIgnoreCase(arg)
                || "1".equalsIgnoreCase(arg) || "0".equalsIgnoreCase(arg);
    }

    public String asEnum(String... options) throws PrematureAbortException
    {
        return asEnum(Arrays.asList(options));
    }

    public String asEnum(List<String> options) throws PrematureAbortException
    {
        for (String a : options) {
            if (a.equalsIgnoreCase(arg)) {
                return a;
            }
        }
        throw new CommandErrorException("'" + arg + "' must be either " + Format.listOr(options));
    }

    public UUID asUuid() throws PrematureAbortException
    {
        return asUuid(String.format("'%s' is not a UUID", arg));
    }

    public UUID asUuid(String errMsg) throws PrematureAbortException
    {
        try {
            return UUID.fromString(arg);
        }
        catch (IllegalArgumentException e) {
            throw new CommandErrorException(e, errMsg);
        }
    }

    public boolean isUuid()
    {
        return FormatText.isUUID(arg);
    }

    public OfflinePlayer asPlayer() throws PrematureAbortException
    {
        if (isUuid()) {
            return ShopPlugin.getState().getOfflinePlayer(asUuid());
        }
        else {
            List<StoredPlayer> players = ShopPlugin.getOfflinePlayer(arg);
            if (players.isEmpty()) {
                return null;
            }
            else if (players.size() > 1) {
                StringBuilder sb = new StringBuilder();
                sb.append("There are multiple players with that name. Please specify the UUID instead.\n");
                for (StoredPlayer player : players) {
                    sb.append(Format.keyword(player.getUniqueId().toString())).append('\n');
                }
                throw new CommandErrorException(sb.toString());
            }
            return players.get(0);
        }
    }

    public OfflinePlayer asPlayerSafe() throws PrematureAbortException
    {
        OfflinePlayer player = asPlayer();
        if (player == null)
            return ShopPlugin.getOfflinePlayerSafe(arg).get(0);
        if (player == StoredPlayer.ERROR)
            return ShopPlugin.getOfflinePlayer(asUuid("The player could not be found"));
        return player;
    }

    @Override
    public String toString()
    {
        return arg;
    }
}
