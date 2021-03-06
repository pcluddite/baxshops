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

import org.bukkit.command.Command;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxEntry;
import org.tbax.bukkit.CommandHelp;
import org.tbax.bukkit.CommandHelpArgument;
import org.tbax.bukkit.commands.CmdActor;
import org.tbax.bukkit.commands.CommandArgument;
import org.tbax.bukkit.errors.PrematureAbortException;
import org.tbax.baxshops.Permissions;
import org.tbax.baxshops.Resources;

import java.util.List;

public final class CmdSetIndex extends ShopCommand
{
    @Override
    public @org.jetbrains.annotations.Nullable String getAction()
    {
        return "setindex";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "setorder", "reorder" };
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_OWNER;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "change entry order");
        help.setLongDescription("Change the order of an entry in the shop");
        help.setArgs(
                new CommandHelpArgument("old-index", "the current index of the item", true),
                new CommandHelpArgument("new-index", "the new index of the item", true)
        );
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull CmdActor actor)
    {
        return actor.getNumArgs() == 3;
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
        assert actor.getShop() != null;
        int oldIndex = actor.getShop().indexOf(actor.getArg(1).asEntry());
        int newIndex = actor.getArg(2).asInteger(String.format(Resources.INVALID_DECIMAL, "new index"));
        if (newIndex > actor.getShop().size()) {
            actor.exitError("You must choose a new index that is less than the number of items in the shop!");
        }
        if (newIndex < 1) {
            actor.exitError("The new index must be greater than 0.");
        }
        if (newIndex == oldIndex) {
            actor.exitWarning("The index has not been changed.");
        }
        BaxEntry entry = actor.getShop().removeEntryAt(oldIndex);
        if (actor.getShop().size() < newIndex) {
            actor.getShop().add(entry);
        }
        else {
            actor.getShop().addEntry(newIndex - 1, entry);
        }
        actor.sendMessage("The index for this item was successfully changed.");
    }

    @Override
    public List<String> onTabComplete(@NotNull CmdActor actor, @NotNull Command command,
                                      @NotNull String alias, List<? extends CommandArgument> args)
    {
        ShopCmdActor shopActor = (ShopCmdActor)actor;
        if (args.size() == 2 && shopActor.getShop() != null) {
            return shopActor.getShop().getAllItemAliases();
        }
        else {
            return super.onTabComplete(actor, command, alias, args);
        }
    }
}