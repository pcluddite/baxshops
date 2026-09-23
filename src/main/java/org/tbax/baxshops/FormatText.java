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
package org.tbax.baxshops;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Methods for formatting text components. Intended as a replacement for {@link Format}.
 */
public class FormatText
{
    public static @NotNull Component money(@NotNull String amount)
    {
        return Component.text(amount, NamedTextColor.GREEN);
    }

    public static @NotNull Component money(double amount)
    {
        return money(ShopPlugin.getEconomy().format(amount));
    }

    public static @NotNull Component money2(@NotNull String amount)
    {
        return Component.text(amount, NamedTextColor.DARK_GREEN);
    }

    public static @NotNull Component money2(double amount)
    {
        return money2(ShopPlugin.getEconomy().format(amount));
    }

    public static @NotNull Component number(@NotNull String value)
    {
        return Component.text(value, NamedTextColor.AQUA);
    }

    public static @NotNull Component number(int value)
    {
        return number(Integer.toString(value));
    }

    public static @NotNull Component error(@NotNull String message)
    {
        return Component.text(message, NamedTextColor.RED);
    }

    public static @NotNull Component warning(@NotNull String message)
    {
        return Component.text(message, NamedTextColor.GOLD);
    }

    public static @NotNull Component location(@NotNull String location)
    {
        return Component.text(location, NamedTextColor.GOLD);
    }

    public static @NotNull Component location(@NotNull Location loc)
    {
        return location(String.format("(%d,%d,%d)", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    }

    public static @NotNull Component itemName(int amount, @NotNull String name)
    {
        return itemName(String.format("%d %s", amount, name));
    }

    public static @NotNull Component itemName(@NotNull String name)
    {
        return Component.text(name, NamedTextColor.GREEN);
    }

    public static @NotNull Component username(@NotNull UUID uuid)
    {
        return username(ShopPlugin.getOfflinePlayer(uuid).getName());
    }

    public static @NotNull Component username(@NotNull String name)
    {
        return Component.text(name, NamedTextColor.DARK_BLUE);
    }

    public static @NotNull Component username2(@NotNull UUID uuid)
    {
        return username2(ShopPlugin.getOfflinePlayer(uuid).getName());
    }

    public static @NotNull Component username2(@NotNull String name)
    {
        return Component.text(name, NamedTextColor.DARK_PURPLE);
    }

    public static @NotNull Component command(@NotNull String command)
    {
        int space = command.indexOf(' ');
        if (space < 0)
            return Component.text(command, NamedTextColor.GOLD);
        return Component.text(command.substring(0, space), NamedTextColor.GOLD)
                .append(Component.text(command.substring(space), NamedTextColor.GRAY));
    }

    public static @NotNull Component retailPrice(@NotNull String price)
    {
        return Component.text(price, NamedTextColor.DARK_GREEN);
    }

    public static @NotNull Component retailPrice(double price)
    {
        return retailPrice(String.format("(%s)", ShopPlugin.getEconomy().format(price)));
    }

    public static @NotNull Component refundPrice(@NotNull String price)
    {
        return Component.text(price, NamedTextColor.BLUE);
    }

    public static @NotNull Component refundPrice(double price)
    {
        return refundPrice(String.format("(%s)", ShopPlugin.getEconomy().format(price)));
    }

    public static @NotNull Component enchantments(@NotNull String enchant)
    {
        return Component.text(enchant, NamedTextColor.DARK_PURPLE);
    }

    public static @NotNull Component bullet(@NotNull String b)
    {
        return Component.text(b, NamedTextColor.DARK_GREEN);
    }

    public static @NotNull Component bullet(int b)
    {
        return bullet(Integer.toString(b));
    }

    public static @NotNull Component listname(@NotNull String name)
    {
        return Component.text(name, NamedTextColor.WHITE);
    }

    public static @NotNull Component flag(@NotNull String flag)
    {
        return Component.text(flag, NamedTextColor.YELLOW);
    }

    public static @NotNull Component keyword(@NotNull String word)
    {
        return Component.text(word, NamedTextColor.GREEN);
    }

    /**
     * Generates a chat header with the given title
     * @param title the text in the header
     * @return the chat header
     */
    public static @NotNull Component header(String title)
    {
        return Component.text("------------ ", NamedTextColor.GRAY)
                .append(Component.text(title, NamedTextColor.WHITE))
                .append(Component.text(" ------------", NamedTextColor.GRAY));
    }

    public static @NotNull Component header(int page, int maxPages, String command)
    {
        Component text = Component.text("", NamedTextColor.GRAY);
        if (page > 1) {
            text = text.append(Component.text("<<", NamedTextColor.GOLD, TextDecoration.UNDERLINED)
                    .hoverEvent(HoverEvent.showText(Component.text("Previous", NamedTextColor.AQUA)))
                    .clickEvent(ClickEvent.runCommand(command + " " + (page - 1))));
        }
        else {
            text = text.append(Component.text("<<", NamedTextColor.DARK_GRAY));
        }
        text = text.append(Component.text(" --------- ", NamedTextColor.DARK_GRAY))
                .append(Component.text(String.format("Showing page %d of %d", page, maxPages), NamedTextColor.WHITE))
                .append(Component.text(" --------- ", NamedTextColor.DARK_GRAY));

        if (page < maxPages) {
            text = text.append(Component.text(">>", NamedTextColor.GOLD, TextDecoration.UNDERLINED)
                    .hoverEvent(HoverEvent.showText(Component.text("Next", NamedTextColor.AQUA)))
                    .clickEvent(ClickEvent.runCommand(command + " " + (page + 1))));
        }
        else {
            text = text.append(Component.text(">>", NamedTextColor.DARK_GRAY));
        }
        return text;
    }
}
