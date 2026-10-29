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
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.text.ChatTextColor;
import org.tbax.baxshops.text.ChatTextStyle;
import org.tbax.bukkit.Pair;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Methods for formatting text components. Intended as a replacement for {@link Format}.
 */
public class FormatText
{
    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

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
        String commandName = space < 0 ? command : command.substring(0, space);
        Component component = Component.text(commandName, NamedTextColor.GOLD)
                .clickEvent(ClickEvent.suggestCommand("/" + commandName));
        if (space > -1) {
            component = component.appendSpace()
                    .append(Component.text(command.substring(space + 1), NamedTextColor.GRAY));
        }
        return component;
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
    public static @NotNull Component header(@NotNull String title)
    {
        return Component.text("------------ ", NamedTextColor.GRAY)
                .append(Component.text(title, NamedTextColor.WHITE))
                .append(Component.text(" ------------", NamedTextColor.GRAY));
    }

    public static @NotNull Component header(int page, int maxPages, @NotNull String command)
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

    public static @NotNull String listOr(@NotNull List<? extends CharSequence> elements)
    {
        return listOr(elements, false);
    }

    public static @NotNull String listOr(@NotNull List<? extends CharSequence> elements, boolean oxford)
    {
        return list(elements, "or", oxford);
    }

    public static @NotNull String listAnd(@NotNull List<? extends CharSequence> elements)
    {
        return listAnd(elements, false);
    }

    public static @NotNull String listAnd(@NotNull List<? extends CharSequence> elements, boolean oxford)
    {
        return list(elements, "and", oxford);
    }

    public static @NotNull String list(@NotNull List<? extends CharSequence> elements, String conjunction)
    {
        return list(elements, conjunction, false);
    }

    public static @NotNull String list(@NotNull List<? extends CharSequence> elements, String conjunction,
                                       boolean oxford)
    {
        if (elements.isEmpty())
            return "";
        if (elements.size() == 1)
            return elements.get(0).toString();
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(", ", elements.subList(0, elements.size() - 1)));
        if (oxford && elements.size() > 2) // do not append comma on list with two elements
            sb.append(',');
        sb.append(' ').append(conjunction).append(' ');
        sb.append(elements.get(elements.size() - 1));
        return sb.toString();
    }

    public static @NotNull String toNumeral(int n)
    {
        return Format.toNumeral(n);
    }

    public static @NotNull String toFriendlyName(@NotNull String name)
    {
        return Format.toFriendlyName(name);
    }

    private static @NotNull List<Component> flattenComponent(@NotNull Component component)
    {
        if (component.children().isEmpty())
            return Collections.singletonList(component);

        List<Component> result = new ArrayList<>();
        Deque<Pair<Component, Style>> stack = new ArrayDeque<>();
        stack.push(new Pair<>(component, component.style()));

        while (!stack.isEmpty()) {
            Pair<Component, Style> entry = stack.pop();
            Component comp = entry.getFirst();
            Style inherited = entry.getSecond();

            // merged = inherited.merge(comp.style())
            Style merged = (inherited == null ? Style.empty() : inherited).merge(comp.style());

            if (comp instanceof TextComponent tc) {
                if (!tc.content().isEmpty()) {
                    result.add(Component.text(tc.content(), merged));
                }
            }

            // push children in reverse order so they are processed in original order
            List<Component> children = comp.children();
            for (int i = children.size() - 1; i >= 0; --i) {
                Component child = children.get(i);
                // pass merged as the inherited style for the child (same as recursive)
                stack.push(new Pair<>(child, merged));
            }
        }

        return result;
    }

    public static @NotNull String toAnsiColor(@NotNull Component component)
    {
        StringBuilder sb = new StringBuilder();
        boolean hasAnsi = false;
        List<Component> componentList = flattenComponent(component);
        for(Component c : componentList) {
            if (!(c instanceof TextComponent current))
                continue;
            TextColor color = current.color();
            if (color != null) {
                ChatTextColor chatTextColor;
                try {
                    chatTextColor = ChatTextColor.getChatTextColor(color);
                }
                catch (IllegalArgumentException e) {
                    chatTextColor = ChatTextColor.WHITE;
                }
                sb.append(chatTextColor.getAnsiColor());
                hasAnsi = true;
            }
            sb.append(current.content());
        }
        if (hasAnsi) {
            sb.append(ChatTextColor.RESET.getAnsiColor()); // reset the color
        }
        return sb.toString();
    }

    public static @NotNull String extractText(@NotNull Component component)
    {
        List<Component> componentList = flattenComponent(component);
        StringBuilder sb = new StringBuilder();
        for(Component c : componentList) {
            if (c instanceof TextComponent current) {
                sb.append(current.content());
            }
        }
        return sb.toString();
    }

    public static @NotNull Component stripColor(@NotNull Component component)
    {
        return Component.text(Format.stripColor(extractText(component)));
    }

    public static @NotNull String toChatString(@NotNull Component component)
    {
        List<Component> componentList = flattenComponent(component);
        StringBuilder sb = new StringBuilder();
        for (Component c : componentList) {
            if (!(c instanceof TextComponent current))
                continue;

            if (current.color() != null) {
                try {
                    sb.append(ChatTextColor.getChatTextColor(current.color()));
                }
                catch (IllegalArgumentException e) {
                    sb.append(ChatTextColor.WHITE.getChatColor());
                }
            }

            for (Map.Entry<TextDecoration, TextDecoration.State> entry : current.decorations().entrySet()) {
                if (entry.getValue() == TextDecoration.State.TRUE) {
                    sb.append(ChatTextStyle.valueOf(entry.getKey().name()).getChatColorStyle());
                }
            }
        }
        return sb.toString();
    }

    public static @NotNull Component wordWrap(@NotNull Component input)
    {
        final int MAX_LINE_LENGTH = 50;
        return wordWrap(input, MAX_LINE_LENGTH);
    }

    public static @NotNull Component wordWrap(@NotNull Component input, int maxLineLength) {
        List<Component> output = new ArrayList<>();
        StringBuilder currentWord = new StringBuilder();
        Style currentStyle = Style.empty();

        int lineLength = 0;
        int wordLength = 0;

        List<Component> segments = flattenComponent(input);

        for (Component segment : segments) {
            String text = extractText(segment);
            Style style = segment.style();

            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);

                if (Character.isWhitespace(c)) {
                    // Wrap if needed
                    if (lineLength + wordLength > maxLineLength) {
                        output.add(Component.newline());
                        lineLength = 0;
                    }

                    // Emit the word
                    if (!currentWord.isEmpty()) {
                        output.add(Component.text(currentWord.toString(), currentStyle));
                    }

                    // Emit the whitespace
                    output.add(Component.text(String.valueOf(c), style));

                    lineLength += wordLength;
                    currentWord.setLength(0);
                    wordLength = 0;
                    currentStyle = style;
                }
                else {
                    // Continue building the word
                    currentWord.append(c);
                    wordLength++;
                    currentStyle = style;
                }
            }
        }

        // Emit final word
        if (!currentWord.isEmpty()) {
            output.add(Component.text(currentWord.toString(), currentStyle));
        }

        // Combine into a single component
        Component result = Component.empty();
        for (Component c : output) {
            result = result.append(c);
        }

        return result;
    }


    public static @NotNull String toHexString(int value)
    {
        return toHexString(value, 6); // 6 is max hex digits in a byte
    }

    public static @NotNull String toHexString(int value, int width)
    {
        return "0x" + padLeft(Integer.toHexString(value).toUpperCase(), width, '0');
    }

    public static @NotNull String padLeft(@NotNull String s, int width)
    {
        return padLeft(s, width, ' ');
    }

    public static @NotNull String padLeft(@NotNull String s, int width, char padChar)
    {
        if (s.length() >= width) return s;
        StringBuilder sb = new StringBuilder(width);
        while (sb.length() < width - s.length()) {
            sb.append(padChar);
        }
        sb.append(s);
        return sb.toString();
    }

    public static @NotNull String padRight(@NotNull String s, int width)
    {
        return padRight(s, width, ' ');
    }

    public static @NotNull String padRight(@NotNull String s, int width, char padChar)
    {
        if (s.length() >= width) return s;
        StringBuilder sb = new StringBuilder(width);
        sb.append(s);
        while (sb.length() < width - s.length()) {
            sb.append(padChar);
        }
        return sb.toString();
    }

    public static boolean isUUID(@NotNull String str)
    {
        return UUID_PATTERN.matcher(str).matches();
    }
}
