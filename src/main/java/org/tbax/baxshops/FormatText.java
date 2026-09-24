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
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Methods for formatting text components. Intended as a replacement for {@link Format}.
 */
public class FormatText
{
    public static @NotNull Component money(@NotNull String amount)
    {
        return money(Component.text(amount));
    }

    public static @NotNull Component money(@NotNull Component amount)
    {
        return amount.color(NamedTextColor.GREEN);
    }

    public static @NotNull Component money(double amount)
    {
        return money(ShopPlugin.getEconomy().format(amount));
    }

    public static @NotNull Component money2(@NotNull String amount)
    {
        return money2(Component.text(amount));
    }

    public static @NotNull Component money2(@NotNull Component amount)
    {
        return amount.color(NamedTextColor.DARK_GREEN);
    }

    public static @NotNull Component money2(double amount)
    {
        return money2(ShopPlugin.getEconomy().format(amount));
    }

    public static @NotNull Component number(@NotNull String value)
    {
        return number(Component.text(value));
    }

    public static @NotNull Component number(Component value)
    {
        return value.color(NamedTextColor.AQUA);
    }

    public static @NotNull Component number(int value)
    {
        return number(Integer.toString(value));
    }

    public static @NotNull Component error(@NotNull String message)
    {
        return error(Component.text(message));
    }

    public static @NotNull Component error(@NotNull Component message)
    {
        return message.color(NamedTextColor.RED);
    }

    public static @NotNull Component warning(@NotNull String message)
    {
        return warning(Component.text(message));
    }

    public static @NotNull Component warning(@NotNull Component message)
    {
        return message.color(NamedTextColor.GOLD);
    }

    public static @NotNull Component location(@NotNull String location)
    {
        return location(Component.text(location));
    }

    public static @NotNull Component location(@NotNull Component location)
    {
        return location.color(NamedTextColor.GOLD);
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
        return itemName(Component.text(name));
    }

    public static @NotNull Component itemName(@NotNull Component name)
    {
        return name.color(NamedTextColor.GREEN);
    }

    public static @NotNull Component username(@NotNull UUID uuid)
    {
        return username(ShopPlugin.getOfflinePlayer(uuid).getName());
    }

    public static @NotNull Component username(@NotNull String name)
    {
        return username(Component.text(name));
    }

    public static @NotNull Component username(@NotNull Component name)
    {
        return name.color(NamedTextColor.DARK_BLUE);
    }

    public static @NotNull Component username2(@NotNull UUID uuid)
    {
        return username2(ShopPlugin.getOfflinePlayer(uuid).getName());
    }

    public static @NotNull Component username2(@NotNull String name)
    {
        return username2(Component.text(name));
    }

    public static @NotNull Component username2(@NotNull Component name)
    {
        return name.color(NamedTextColor.DARK_PURPLE);
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
        return retailPrice(Component.text(price));
    }

    public static @NotNull Component retailPrice(@NotNull Component price)
    {
        return price.color(NamedTextColor.DARK_GREEN);
    }

    public static @NotNull Component retailPrice(double price)
    {
        return retailPrice(String.format("(%s)", ShopPlugin.getEconomy().format(price)));
    }

    public static @NotNull Component refundPrice(@NotNull String price)
    {
        return refundPrice(Component.text(price));
    }

    public static @NotNull Component refundPrice(@NotNull Component price)
    {
        return price.color(NamedTextColor.BLUE);
    }

    public static @NotNull Component refundPrice(double price)
    {
        return refundPrice(String.format("(%s)", ShopPlugin.getEconomy().format(price)));
    }

    public static @NotNull Component enchantments(@NotNull String enchant)
    {
        return enchantments(Component.text(enchant));
    }

    public static @NotNull Component enchantments(@NotNull Component enchant)
    {
        return enchant.color(NamedTextColor.DARK_PURPLE);
    }

    public static @NotNull Component bullet(@NotNull String b)
    {
        return bullet(Component.text(b));
    }

    public static @NotNull Component bullet(@NotNull Component b)
    {
        return b.color(NamedTextColor.GRAY);
    }

    public static @NotNull Component bullet(int b)
    {
        return bullet(Integer.toString(b));
    }

    public static @NotNull Component listname(@NotNull String name)
    {
        return listname(Component.text(name));
    }

    public static @NotNull Component listname(@NotNull Component name)
    {
        return name.color(NamedTextColor.WHITE);
    }

    public static @NotNull Component flag(@NotNull String flag)
    {
        return flag(Component.text(flag));
    }

    public static @NotNull Component flag(@NotNull Component flag)
    {
        return flag.color(NamedTextColor.YELLOW);
    }

    public static @NotNull Component keyword(@NotNull String word)
    {
        return keyword(Component.text(word));
    }

    public static @NotNull Component keyword(@NotNull Component word)
    {
        return word.color(NamedTextColor.GREEN);
    }

    public static @NotNull Component strikethrough(@NotNull String text)
    {
        return strikethrough(Component.text(text));
    }

    public static @NotNull Component strikethrough(@NotNull Component text)
    {
        return text.color(NamedTextColor.RED).decorate(TextDecoration.STRIKETHROUGH);
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

    private static @NotNull List<Component> getAllComponents(@NotNull Component component)
    {
        List<Component> components = new ArrayList<>();
        components.add(component);
        for (Component c : component.children()) {
            components.addAll(getAllComponents(c));
        }
        return components;
    }

    public static @NotNull String toAnsiColor(@NotNull TextComponent component) // obnoxious method to convert minecraft message colors to ansi colors
    {
        StringBuilder sb = new StringBuilder();
        boolean has_ansi = false;
        List<Component> componentList = getAllComponents(component);
        for(Component c : componentList) {
            if (!(c instanceof TextComponent))
                continue;
            TextColor color = c.color();
            if (color != null) {
                sb.append((char)27);
                sb.append("[0;");
                if (color.equals(NamedTextColor.BLACK)) {
                    sb.append("30");
                }
                else if (color.equals(NamedTextColor.DARK_BLUE)) {
                    sb.append("34");
                }
                else if (color.equals(NamedTextColor.DARK_GREEN)) {
                    sb.append("32");
                }
                else if (color.equals(NamedTextColor.DARK_AQUA)) {
                    sb.append("36");
                }
                else if (color.equals(NamedTextColor.DARK_RED)) {
                    sb.append("31");
                }
                else if (color.equals(NamedTextColor.DARK_PURPLE)) {
                    sb.append("35");
                }
                else if (color.equals(NamedTextColor.GOLD)) {
                    sb.append("33");
                }
                else if (color.equals(NamedTextColor.GRAY)) {
                    sb.append("37");
                }
                else if (color.equals(NamedTextColor.DARK_GRAY)) {
                    sb.append("37");
                }
                else if (color.equals(NamedTextColor.BLUE)) {
                    sb.append("36");
                }
                else if (color.equals(NamedTextColor.GREEN)) {
                    sb.append("32");
                }
                else if (color.equals(NamedTextColor.AQUA)) {
                    sb.append("36");
                }
                else if (color.equals(NamedTextColor.RED)) {
                    sb.append("31");
                }
                else if (color.equals(NamedTextColor.LIGHT_PURPLE)) {
                    sb.append("35");
                }
                else if (color.equals(NamedTextColor.YELLOW)) {
                    sb.append("33");
                }
                else {
                    sb.append("37");
                }
                sb.append("m");
                if (!has_ansi) {
                    has_ansi = true;
                }
            }
            sb.append(((TextComponent)c).content());
        }
        if (has_ansi) {
            sb.append((char)27);
            sb.append("[0m"); // reset the color
        }
        return sb.toString();
    }
}
