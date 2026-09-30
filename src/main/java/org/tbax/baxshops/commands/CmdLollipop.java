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

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.Permissions;
import org.tbax.baxshops.Resources;
import org.tbax.baxshops.ShopPlugin;
import org.tbax.baxshops.notification.LollipopNotification;
import org.tbax.bukkit.CommandHelp;
import org.tbax.bukkit.CommandHelpArgument;
import org.tbax.bukkit.commands.CmdActor;
import org.tbax.bukkit.commands.CommandArgument;
import org.tbax.bukkit.errors.PrematureAbortException;
import org.tbax.bukkit.serialization.StoredPlayer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class CmdLollipop extends ShopCommand
{
    @Override
    public @org.jetbrains.annotations.Nullable String getAction()
    {
        return "lollipop";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "lol", "lolly" };
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_TRADER;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "hand out a lollipop");
        help.setLongDescription("Give a lollipop to your friends");
        help.setArgs(
                new CommandHelpArgument("player", "player to send lollipop", true),
                new CommandHelpArgument("tastiness", "the tastiness", false)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull CmdActor actor)
    {
        return actor.getNumArgs() == 3 || actor.getNumArgs() == 2;
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
        String tastiness = LollipopNotification.DEFAULT_TASTINESS;
        if (actor.getNumArgs() == 3) {
            if (actor.getArg(2).isDouble()) {
                tastiness = LollipopNotification.getStockAdjective(actor.getArg(2).asDouble());
            }
            else {
                tastiness = actor.getArg(2).asString();
            }
        }

        if (tastiness.length() > 30)
            actor.exitError("Your adjective is too long");

        OfflinePlayer sender = actor.getPlayer() == null ? StoredPlayer.DUMMY : actor.getPlayer();
        StoredPlayer recipient = actor.getArg(1).asPlayer();
        if (recipient == null)
            actor.exitError(Resources.NOT_REGISTERED_PLAYER, actor.getArg(1), "receive a lollipop");

        List<LollipopNotification> otherPops = recipient.getNotifications().stream()
                .filter(n -> n instanceof LollipopNotification)
                .map(n -> (LollipopNotification)n)
                .filter(n -> n.getSender().equals(sender))
                .collect(Collectors.toList());

        if (otherPops.isEmpty()) {
            LollipopNotification lol = new LollipopNotification(sender, recipient, tastiness);
            ShopPlugin.sendNotification(recipient, lol);
            actor.sendInfo("You sent %s lollipop to %s", lol.getAdornedTastiness(), Format.username2(recipient.getName()));
        }
        else {
            actor.sendError("%s has to eat your %s lollipop before you can send another",
                    recipient.getName(),
                    "".equals(otherPops.get(0).getTastiness()) ? "other" : otherPops.get(0).getTastiness()
            );
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CmdActor actor, @NotNull Command command,
                                      @NotNull String alias, List<? extends CommandArgument> args)
    {
        if (actor.getNumArgs() == 2) {
            return ShopPlugin.getRegisteredPlayers().stream()
                    .map(StoredPlayer::getName)
                    .collect(Collectors.toList());
        }
        else if (actor.getNumArgs() == 3) {
            return Arrays.asList(LollipopNotification.STOCK_ADJECTIVES);
        }
        else {
            return super.onTabComplete(actor, command, alias, args);
        }
    }
}
