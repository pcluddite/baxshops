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
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.*;
import org.tbax.baxshops.items.ItemUtil;
import org.tbax.bukkit.CommandHelp;
import org.tbax.bukkit.CommandHelpArgument;
import org.tbax.bukkit.commands.CmdActor;
import org.tbax.bukkit.commands.CommandArgument;
import org.tbax.bukkit.errors.PrematureAbortException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class CmdEmpty extends ShopCommand
{
    @Override
    public @org.jetbrains.annotations.Nullable String getAction()
    {
        return "empty";
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_OWNER;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "remove all shop inventory");
        help.setLongDescription("Takes out all shop inventory and adds it to yours");
        help.setArgs(
                new CommandHelpArgument("entry", "the entry to start removing items at", 1)
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
        BaxShop shop = actor.getShop();
        assert shop != null;
        int startIdx = 0;

        if (actor.getNumArgs() == 2)
            startIdx = actor.getArg(1).asEntryIndex();

        if (shop.isEmpty())
            actor.exitError("This shop has no inventory");

        if (shop.hasFlagInfinite())
            actor.exitError("You cannot empty the inventory of an infinite shop");

        for (int idx = startIdx; idx < shop.size(); ++idx) {
            BaxEntry entry = shop.getEntry(idx);

            if (entry.getAmount() <= 0)
                continue;

            ItemStack stack = entry.toItemStack();
            int overflow = PlayerUtil.giveItem(actor.getPlayer(), stack, false);
            entry.subtract(stack.getAmount() - overflow);
            if (overflow > 0) {
                if (stack.getAmount() > overflow) {
                    actor.exitMessage(Resources.SOME_ROOM, stack.getAmount() - overflow, ItemUtil.getName(stack));
                }
                else {
                    actor.exitError(Resources.NO_ROOM_FOR_ITEM, stack.getAmount(), ItemUtil.getName(stack));
                }
            }
            else {
                actor.sendInfo("%s was added to your inventory", Format.itemName(stack.getAmount(), entry.getName()));
            }
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CmdActor actor, @NotNull Command command,
                                      @NotNull String alias, List<? extends CommandArgument> args)
    {
        ShopCmdActor shopActor = (ShopCmdActor)actor;
        if (shopActor.getShop() != null && args.size() == 2) {
            return IntStream.range(1, shopActor.getShop().size() + 1)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.toList());
        }
        return super.onTabComplete(actor, command, alias, args);
    }
}