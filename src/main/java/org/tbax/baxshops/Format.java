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

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tbax.baxshops.text.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Methods for formatting strings
 */
@SuppressWarnings("unused")
public final class Format
{   
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    public static final DateFormat FILE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

    private Format()
    {
    }

    public static @Nullable Date parseFileDate(String dateStr)
    {
        try {
            return FILE_DATE_FORMAT.parse(dateStr);
        }
        catch (ParseException e) {
            return null;
        }
    }

    public static @NotNull String date(@NotNull Date dt)
    {
        return DATE_FORMAT.format(dt);
    }

    @Deprecated
    public static @NotNull String money(@NotNull String in)
    {
        return reset(ChatColor.GREEN + in);
    }

    @Deprecated
    public static @NotNull String money(double d)
    {
        return money(ShopPlugin.getEconomy().format(d));
    }

    @Deprecated
    public static @NotNull String money2(@NotNull String in)
    {
        return reset(ChatColor.DARK_GREEN + in);
    }

    @Deprecated
    public static @NotNull String money2(double d)
    {
        return money2(ShopPlugin.getEconomy().format(d));
    }

    @Deprecated
    public static @NotNull String number(@NotNull String n)
    {
        return reset(ChatColor.AQUA + n);
    }

    @Deprecated
    public static @NotNull String number(int n)
    {
        return number(Integer.toString(n));
    }

    @Deprecated
    public static @NotNull String error(@NotNull String err)
    {
        return reset(ChatColor.RED + err);
    }

    @Deprecated
    public static @NotNull String warning(@NotNull String msg)
    {
        return reset(ChatColor.GOLD + msg);
    }

    @Deprecated
    public static @NotNull String location(@NotNull String loc)
    {
        return reset(ChatColor.GOLD + loc);
    }

    @Deprecated
    public static @NotNull String location(@NotNull Location loc)
    {
        return location(String.format("(%d,%d,%d)", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) + ChatColor.RESET;
    }

    @Deprecated
    public static @NotNull String itemName(int amount, @NotNull String name)
    {
        return itemName(String.format("%d %s", amount, name));
    }

    @Deprecated
    public static @NotNull String itemName(@NotNull String name)
    {
        return reset(ChatColor.GREEN + name);
    }

    @Deprecated
    public static @NotNull String username(@NotNull UUID uuid)
    {
        return username(ShopPlugin.getOfflinePlayer(uuid).getName());
    }

    @Deprecated
    public static @NotNull String username2(@NotNull UUID uuid)
    {
        return username2(ShopPlugin.getOfflinePlayer(uuid).getName());
    }

    @Deprecated
    public static @NotNull String username(@NotNull String name)
    {
        return reset(ChatColor.DARK_BLUE + name);
    }

    @Deprecated
    public static @NotNull String username2(@NotNull String name)
    {
        return reset(ChatColor.LIGHT_PURPLE + name);
    }

    @Deprecated
    public static @NotNull String command(@NotNull String cmd)
    {
        int space = cmd.indexOf(' ');
        if (space < 0) return reset(ChatColor.GOLD + cmd);
        return reset(ChatColor.GOLD + cmd.substring(0, space) + ChatColor.GRAY + cmd.substring(space));
    }

    @Deprecated
    public static @NotNull String retailPrice(@NotNull String price)
    {
        return reset(ChatColor.DARK_GREEN + price);
    }

    @Deprecated
    public static @NotNull String retailPrice(double price)
    {
        return retailPrice(String.format("(%s)", ShopPlugin.getEconomy().format(price)));
    }

    @Deprecated
    public static @NotNull String refundPrice(@NotNull String price)
    {
        return reset(ChatColor.BLUE + price);
    }

    @Deprecated
    public static @NotNull String refundPrice(double price)
    {
        return refundPrice(String.format("(%s)", ShopPlugin.getEconomy().format(price)));
    }

    @Deprecated
    public static @NotNull String enchantments(@NotNull String enchant)
    {
        return reset(ChatColor.DARK_PURPLE + enchant);
    }

    @Deprecated
    public static @NotNull String bullet(@NotNull String b)
    {
        return reset(ChatColor.GRAY + b);
    }

    @Deprecated
    public static @NotNull String bullet(int b)
    {
        return bullet(Integer.toString(b));
    }

    @Deprecated
    public static @NotNull String listname(@NotNull String name)
    {
        return reset(ChatColor.WHITE + name);
    }

    @Deprecated
    public static @NotNull String flag(@NotNull String flag)
    {
        return reset(ChatColor.YELLOW + flag);
    }

    @Deprecated
    public static @NotNull String keyword(@NotNull String word)
    {
        return reset(ChatColor.GREEN + word);
    }

    @Deprecated
    private static @NotNull String reset(@NotNull String format)
    {
        return format + ChatColor.RESET;
    }

    public static @NotNull String listOr(List<? extends CharSequence> elements)
    {
        return String.join(", ", elements.subList(0, elements.size() - 1)) + " or " + elements.get(elements.size() - 1);
    }

    public static @NotNull String listAnd(List<? extends CharSequence> elements)
    {
        return String.join(", ", elements.subList(0, elements.size() - 1)) + " and " + elements.get(elements.size() - 1);
    }

    private static final String[] NUMERALS = { "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I" };
    private static final int[] NUMBERS = { 1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1 };
    
    /**
     * Converts a number to a Roman numeral
     * @param n the number to convert to a numeral
     * @return a string of the roman numeral
     */
    public static String toNumeral(int n)
    {
        StringBuilder sb = new StringBuilder();
        int numeralIdx = 0;
        do {
            int x = n / NUMBERS[numeralIdx];
            for (int i = 0; i < x; ++i) {
                sb.append(NUMERALS[numeralIdx]);
            }
            n -= x * NUMBERS[numeralIdx];
        }
        while (++numeralIdx < NUMERALS.length && n > 0);
        return sb.toString();
    }
    
    public static String toFriendlyName(String name)
    {
        if (name == null || name.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean upper = true;
        for(int index = 0; index < name.length(); ++index) {
            char c = name.charAt(index);
            switch(c) {
                case '(': // end of name
                    return sb.toString();
                case '_': // make this char a space
                case ' ':
                    upper = true;
                    sb.append(' ');
                    break;
                default:
                    if (upper) {
                        sb.append(Character.toUpperCase(c));
                        upper = false;
                    }
                    else {
                        sb.append(Character.toLowerCase(c));
                    }
                    break;
            }
        }
        return sb.toString();
    }
    
    public static @NotNull String toAnsiColor(@NotNull String message) // obnoxious method to convert minecraft message colors to ansi colors
    {
        StringBuilder sb = new StringBuilder();
        boolean has_ansi = false;
        for(int index = 0; index < message.length(); ++index) {
            char c = message.charAt(index);
            if (c == ChatColor.COLOR_CHAR && ++index < message.length()) {
                c = Character.toLowerCase(message.charAt(index));
                sb.append((char)27);
                sb.append("[0;");
                switch(c) {
                    case '0': sb.append("30"); break;
                    case '1': sb.append("34"); break;
                    case '2': sb.append("32"); break;
                    case '3': sb.append("36"); break;
                    case '4': sb.append("31"); break;
                    case '5': sb.append("35"); break;
                    case '6': sb.append("33"); break;
                    case '7': sb.append("37"); break;
                    case '8': sb.append("37"); break;
                    case '9': sb.append("36"); break;
                    case 'a': sb.append("32"); break;
                    case 'b': sb.append("36"); break;
                    case 'c': sb.append("31"); break;
                    case 'd': sb.append("35"); break;
                    case 'e': sb.append("33"); break;
                    case 'f': sb.append("37"); break;
                    default:
                        sb.append("37"); break;
                }
                sb.append("m");
                if (!has_ansi) {
                    has_ansi = true;
                }
            }
            else {
                sb.append(c);
            }
        }
        if (has_ansi) {
            sb.append((char)27);
            sb.append("[0m"); // reset the color
        }
        return sb.toString();
    }

    /**
     * Generates a chat header with the given title
     * @param title the text in the header
     * @return the chat header
     * @deprecated Use {@link FormatText#header(String)}
     */
    @Deprecated
    public static @NotNull String header(String title)
    {
        return ChatColor.GRAY.toString() +
            "------------ " +
            ChatColor.WHITE +
            title +
            ChatColor.GRAY +
            " ------------" +
            ChatColor.RESET;
    }

    @Deprecated
    public static @NotNull ChatComponent header(int page, int maxPages, String command)
    {
        ChatComponent text = ChatComponent.of("", TextColor.GRAY);
        if (page > 1) {
            text.append(ChatComponent.of("<<", TextColor.GOLD, ChatTextStyle.UNDERLINED)
                    .hoverEvent(HoverEvent.showText(ChatColor.AQUA + "Previous"))
                    .clickEvent(ClickEvent.runCommand(command + " " + (page - 1))));
        }
        else {
            text.append(ChatComponent.of("<<", TextColor.DARK_GRAY));
        }
        text.append(" --------- ").append(String.format("Showing page %d of %d", page, maxPages), TextColor.WHITE).append(" --------- ");

        if (page < maxPages) {
            text.append(ChatComponent.of(">>", TextColor.GOLD, ChatTextStyle.UNDERLINED)
                    .hoverEvent(HoverEvent.showText(ChatColor.AQUA + "Next"))
                    .clickEvent(ClickEvent.runCommand(command + " " + (page + 1))));
        }
        else {
            text.append(ChatComponent.of(">>", TextColor.DARK_GRAY));
        }
        return text;
    }

    public static @NotNull String stripColor(@NotNull String str)
    {
        StringBuilder sb = new StringBuilder(str.length());
        for (int idx = 0; idx < str.length(); ++idx) {
            char c = str.charAt(idx);
            if (c == ChatColor.COLOR_CHAR) {
                ++idx;
            }
            else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static int countVisibleCharacters(String message)
    {
        int count = 0;
        for(int i = 0; i < message.length(); ++i) {
            if (message.charAt(i) == ChatColor.COLOR_CHAR) {
                ++i;
            }
            else {
                ++count;
            }
        }
        return count;
    }

    public static @NotNull String wordWrap(String input)
    {
        final int MAX_LINE_LENGTH = 50;

        StringBuilder output = new StringBuilder(input.length() * 2);
        StringBuilder word = new StringBuilder(input.length());
        StringBuilder formats = new StringBuilder(input.length());

        int lineCount = 0, wordCount = 0;

        for(int index = 0; index < input.length() + 1; ++index) {
            char c = index < input.length() ? input.charAt(index) : '\0';
            ChatColor format;
            if (Character.isWhitespace(c) || c == '\0') {
                if (lineCount + wordCount > MAX_LINE_LENGTH) {
                    output.append('\n');
                    output.append(formats);
                    lineCount = 0;
                }
                output.append(word);
                if (c != '\0') output.append(c);
                if (c == '\n') {
                    output.append(formats);
                    lineCount = 0;
                }
                lineCount += wordCount;
                word.setLength(0);
                wordCount = 0;
            }
            else {
                if (c == ChatColor.COLOR_CHAR && index + 1 < input.length() && (format = ChatColor.getByChar(c = input.charAt(index + 1))) != null) {
                    word.append(ChatColor.COLOR_CHAR);
                    if (format == ChatColor.RESET) {
                        formats.setLength(0);
                    }
                    else {
                        formats.append(format);
                    }
                    ++index;
                }
                word.append(c);
                ++wordCount;
            }
        }
        return output.toString();
    }
}
