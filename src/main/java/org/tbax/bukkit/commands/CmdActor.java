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

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tbax.baxshops.PlayerUtil;
import org.tbax.bukkit.errors.CommandErrorException;
import org.tbax.bukkit.errors.CommandMessageException;
import org.tbax.bukkit.errors.CommandWarningException;
import org.tbax.bukkit.errors.PrematureAbortException;
import org.tbax.baxshops.ShopPlugin;
import org.tbax.baxshops.versioning.LegacyPlayerUtil;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public interface CmdActor extends CommandSender
{
    default @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin)
    {
        return getSender().addAttachment(plugin);
    }

    default  @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String s, boolean b)
    {
        return getSender().addAttachment(plugin, s, b);
    }

    default @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String s, boolean b, int i)
    {
        return getSender().addAttachment(plugin, s, b, i);
    }

    default  @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, int i)
    {
        return getSender().addAttachment(plugin, i);
    }

    default void appendArg(String arg)
    {
        appendArg(new CommandArgument(arg));
    }

    default void appendArgs(String... args)
    {
        for (String arg : args) {
            appendArg(arg);
        }
    }

    void appendArg(CommandArgument arg);

    default void appendArgs(CommandArgument... args)
    {
        for (CommandArgument arg : args) {
            appendArg(arg);
        }
    }

    default void exitError(String format, Object... args) throws PrematureAbortException
    {
        throw new CommandErrorException(String.format(format, args));
    }

    default void exitMessage(String format, Object... args) throws PrematureAbortException
    {
        throw new CommandMessageException(String.format(format, args));
    }

    default void exitWarning(String format, Object... args) throws PrematureAbortException
    {
        throw new CommandWarningException(String.format(format, args));
    }

    default String getAction()
    {
        return getArgs().size() > 0 ? getArg(0).asString().toLowerCase() : "";
    }

    CommandArgument getArg(int index);

    @NotNull List<? extends CommandArgument> getArgs();

    Command getCommand();

    String getCommandName();

    default  @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions()
    {
        return getSender().getEffectivePermissions();
    }

    default PlayerInventory getInventory()
    {
        if (getPlayer() == null)
            return null;
        return getPlayer().getInventory();
    }

    default ItemStack getItemInHand()
    {
        if (getPlayer() == null)
            return null;
        ItemStack item = LegacyPlayerUtil.getItemInHand(getPlayer());
        if (item == null || item.getType() == Material.AIR)
            return null;
        return item;
    }

    @Override
    default @NotNull String getName()
    {
        return getSender().getName();
    }

    default int getNumArgs()
    {
        return getArgs().size();
    }

    default Player getPlayer()
    {
        try {
            return (Player)getSender();
        }
        catch (ClassCastException e) {
            return null;
        }
    }

    CommandSender getSender();

    @Override
    default @NotNull Server getServer()
    {
        return getSender().getServer();
    }

    default int getSpaceForItem(ItemStack stack)
    {
        return PlayerUtil.getSpaceForItem(getPlayer(), stack);
    }

    default int giveItem(ItemStack item) throws PrematureAbortException
    {
        return PlayerUtil.giveItem(getPlayer(), item);
    }

    default int giveItem(ItemStack item, boolean allOrNothing) throws PrematureAbortException
    {
        return PlayerUtil.giveItem(getPlayer(), item, allOrNothing);
    }

    default boolean hasPermission(@NotNull String permission)
    {
        return getSender().hasPermission(permission);
    }

    default boolean hasPermission(@NotNull Permission permission)
    {
        return getSender().hasPermission(permission);
    }

    default boolean hasRoomForItem(ItemStack stack)
    {
        return PlayerUtil.hasRoomForItem(getPlayer(), stack);
    }

    default boolean isAdmin()
    {
        return isOp();
    }

    default boolean isPermissionSet(@NotNull String permission)
    {
        return getSender().isPermissionSet(permission);
    }

    default boolean isPermissionSet(@NotNull Permission permission)
    {
        return getSender().isPermissionSet(permission);
    }

    @Override
    default boolean isOp()
    {
        return getSender().isOp();
    }

    default void logError(String format, Object... args)
    {
        ShopPlugin.logSevere(String.format(format, args));
    }

    default void logMessage(String format, Object... args)
    {
        ShopPlugin.logInfo(String.format(format, args));
    }

    default void logWarning(String format, Object... args)
    {
        ShopPlugin.logWarning(String.format(format, args));
    }

    default void recalculatePermissions()
    {
        getSender().recalculatePermissions();
    }

    default void removeAttachment(@NotNull PermissionAttachment permissionAttachment)
    {
        getSender().removeAttachment(permissionAttachment);
    }

    default void sendMessage(@Nullable UUID sender, @NotNull String message)
    {
        getSender().sendMessage(sender, message);
    }

    default void sendMessage(@Nullable UUID sender, @NotNull String... messages)
    {
        getSender().sendMessage(sender, messages);
    }

    default @NotNull Spigot spigot()
    {
        return getSender().spigot();
    }

    default void sendError(String msg)
    {
        ShopPlugin.sendInfo(getSender(), ChatColor.RED + msg);
    }

    default void sendError(String format, Object... args)
    {
        ShopPlugin.sendInfo(getSender(), ChatColor.RED + String.format(format, args));
    }

    @Override
    default void sendMessage(@NotNull String msg)
    {
        ShopPlugin.sendInfo(getSender(), msg);
    }

    @Override
    default void sendMessage(@NotNull String[] strings)
    {
        ShopPlugin.sendInfo(getSender(), strings);
    }

    default void sendMessage(String format, Object... args)
    {
        sendMessage(String.format(format, args));
    }

    default void sendWarning(String msg)
    {
        ShopPlugin.sendInfo(getSender(), ChatColor.GOLD + msg);
    }

    default void sendWarning(String format, Object... args)
    {
        ShopPlugin.sendInfo(getSender(), ChatColor.GOLD + String.format(format, args));
    }

    default void setAction(String action)
    {
        setArg(0, action);
    }

    default void setArg(int index, String value)
    {
        setArg(index, new CommandArgument(value));
    }

    void setArg(int index, CommandArgument value);

    void setCommandName(String name);

    default void setItemInHand(ItemStack stack)
    {
        LegacyPlayerUtil.setItemInHand(getPlayer(), stack);
    }

    @Override
    default void setOp(boolean b)
    {
        getSender().setOp(b);
    }

    default boolean tryGiveItem(ItemStack stack)
    {
        return PlayerUtil.tryGiveItem(getPlayer(), stack);
    }
}
