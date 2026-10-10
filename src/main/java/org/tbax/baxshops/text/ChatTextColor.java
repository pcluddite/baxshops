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
package org.tbax.baxshops.text;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.FormatText;

public enum ChatTextColor
{
    BLACK       (0x000000, "0;30"),
    DARK_BLUE   (0x0000AA, "0;34"),
    DARK_GREEN  (0x00AA00, "0;32"),
    DARK_AQUA   (0x00AAAA, "0;36"),
    DARK_RED    (0xAA0000, "0;31"),
    DARK_PURPLE (0xAA00AA, "0;35"),
    GOLD        (0xFFAA00, "0;33"),
    GRAY        (0xAAAAAA, "0;37"),
    DARK_GRAY   (0x555555, "0;37"),
    BLUE        (0x5555FF, "0;36"),
    GREEN       (0x55FF55, "0;32"),
    AQUA        (0x55FFFF, "0;36"),
    RED         (0xFF5555, "0;31"),
    LIGHT_PURPLE(0xFF55FF, "0;35"),
    YELLOW      (0xFFFF55, "0;33"),
    WHITE       (0xFFFFFF, "0;37"),
    RESET       (0x000000, "0") {
        @Override
        public NamedTextColor getKyoriColor()
        {
            return null;
        }
    };

    private final int hexColor;
    private final String ansiColor;

    ChatTextColor(int hexColor, String ansiColor)
    {
        this.hexColor = hexColor;
        this.ansiColor = (char)27 + "[" + ansiColor + "m";
    }

    public ChatColor getChatColor()
    {
        return ChatColor.valueOf(name());
    }

    public int getHexColor()
    {
        return hexColor;
    }

    public String getAnsiColor()
    {
        return ansiColor;
    }

    public NamedTextColor getKyoriColor() {
        return NamedTextColor.NAMES.value(name().toLowerCase());
    }

    @Override
    public String toString()
    {
        return name().toLowerCase();
    }

    public static ChatTextColor getChatTextColor(@NotNull ChatColor color)
    {
        return ChatTextColor.valueOf(color.name());
    }

    public static ChatTextColor getChatTextColor(char colorChar)
    {
        ChatColor color = ChatColor.getByChar(Character.toLowerCase(colorChar));
        if (color == null)
            throw new IllegalArgumentException("Unknown color: " + ChatColor.COLOR_CHAR + colorChar);
        return getChatTextColor(color);
    }

    public static ChatTextColor getChatTextColor(@NotNull TextColor color)
    {
        NamedTextColor namedTextColor = NamedTextColor.namedColor(color.value());
        if (namedTextColor == null)
            throw new IllegalArgumentException("Unknown color: " + FormatText.toHexString(color.value()));
        return getChatTextColor(namedTextColor);
    }

    public static ChatTextColor getChatTextColor(@NotNull NamedTextColor color)
    {
        return ChatTextColor.valueOf(color.name().toUpperCase());
    }
}
