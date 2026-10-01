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

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.*;
import org.tbax.baxshops.text.*;
import org.tbax.bukkit.CommandHelp;
import org.tbax.bukkit.CommandHelpArgument;
import org.tbax.bukkit.commands.BaxCommand;
import org.tbax.bukkit.commands.CmdActor;
import org.tbax.bukkit.errors.PrematureAbortException;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class CmdHelp extends ShopCommand
{
    @Override
    public @org.jetbrains.annotations.Nullable String getAction()
    {
        return "help";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "h" };
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_TRADER;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "show a list of commands");
        help.setArgs(
                new CommandHelpArgument("action", "get help on a /shop action, e.g. /shop h buy", false)
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
        if (actor.getNumArgs() == 1) {
            actor.appendArg("1"); // show page 1 by default
        }
        if (actor.getArg(1).isInteger()) {
            actor.getSender().sendMessage("Use this to lookup information on specific commands.");
            actor.getSender().sendMessage(String.format("To lookup a command, use: %s", Format.command("/shop help <command>")));
            showHelpList(actor, actor.getArg(1).asInteger() - 1);
        }
        else {
            BaxCommand cmd = ShopPlugin.getCommands().get(actor.getArg(1).asString());
            if (cmd == null) {
                actor.exitError(Resources.INVALID_SHOP_ACTION, actor.getArg(1));
            }
            CommandHelp help = cmd.getHelp(actor);
            if (cmd == null) {
                actor.exitError("No documentation could be found for /shop %s", actor.getArg(1));
            }
            else if (!cmd.hasPermission(actor)) {
                actor.sendError("You do not have permission to view the documentation for this command");
            }
            actor.getSender().sendMessage(help.toString().split("\\n"));
        }
    }

    private void showHelpList(@NotNull CmdActor actor, int page)
    {
        List<BaxCommand> commands = ShopPlugin.getCommands().values().stream()
                .filter(cmd -> cmd.hasPermission(actor))
                .distinct()
                .sorted(Comparator.comparing(BaxCommand::getAction))
                .collect(Collectors.toList());
        int pages = (int)Math.ceil((double)commands.size() / ShopSelection.ITEMS_PER_PAGE);
        Format.header(page + 1, pages, "/shop help").sendTo(actor.getSender());
        int i = page * ShopSelection.ITEMS_PER_PAGE,
                stop = (page + 1) * ShopSelection.ITEMS_PER_PAGE,
                max = Math.min(stop, commands.size());
        for (; i < max; ++i) {
            CommandHelp help = commands.get(i).getHelp(actor);
            ChatComponent.of("").append(
                    ChatComponent.of(help.getAction(), ChatTextColor.GOLD, ChatTextStyle.UNDERLINED)
                            .hoverEvent(HoverEvent.showText(ChatColor.GRAY + "Click for more info"))
                            .clickEvent(ClickEvent.runCommand("/shop help " + help.getAction())))
                    .append(": ")
                    .append(help.getShortDescription())
            .sendTo(actor.getSender());
        }
        for (; i < stop; i++) {
            actor.getSender().sendMessage("");
        }
    }
}