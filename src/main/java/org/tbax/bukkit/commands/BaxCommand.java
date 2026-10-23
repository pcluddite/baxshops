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

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tbax.bukkit.CommandHelp;
import org.tbax.bukkit.errors.PrematureAbortException;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class BaxCommand implements TabCompleter
{
    public abstract String getCommand();

    public abstract @Nullable String getAction();

    public abstract String getPermission();

    public abstract @NotNull CommandHelp getHelp(@NotNull CmdActor actor);

    public abstract boolean hasValidArgCount(@NotNull CmdActor actor);

    public abstract boolean requiresPlayer(@NotNull CmdActor actor);

    public abstract void onCommand(@NotNull CmdActor actor) throws PrematureAbortException;

    public @NotNull String[] getAliases()
    {
        return new String[0];
    }

    public boolean hasPermission(@NotNull CmdActor actor)
    {
        return actor.hasPermission(getPermission());
    }

    public boolean useAlternative(CmdActor actor)
    {
        return false;
    }

    public @NotNull Class<? extends BaxCommand> getAlternative()
    {
        return this.getClass();
    }

    public abstract boolean requiresAdmin();

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, String[] args)
    {
        return onTabComplete((CmdActor)sender, command, alias, ((CmdActor)sender).getArgs());
    }

    public List<String> onTabComplete(@NotNull CmdActor actor, @NotNull Command command,
                                      @NotNull String alias, List<? extends CommandArgument> args)
    {
        return Collections.emptyList();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getCommand(), getAction());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof BaxCommand other) {
            return equals(other);
        }
        return false;
    }

    public boolean equals(BaxCommand other)
    {
        if (other == null) return false;
        if (other == this) return true;
        if (other.getClass() != getClass()) return false;
        return Objects.equals(other.getCommand(), getCommand())
                && Objects.equals(other.getAction(), getAction());
    }
}