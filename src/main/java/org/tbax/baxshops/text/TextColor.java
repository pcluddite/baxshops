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
import org.bukkit.ChatColor;

public enum TextColor
{
    BLACK       (0x000000, "30"),
    DARK_BLUE   (0x0000AA, "34"),
    DARK_GREEN  (0x00AA00, "32"),
    DARK_AQUA   (0x00AAAA, "36"),
    DARK_RED    (0xAA0000, "31"),
    DARK_PURPLE (0xAA00AA, "35"),
    GOLD        (0xFFAA00, "33"),
    GRAY        (0xAAAAAA, "37"),
    DARK_GRAY   (0x555555, "37"),
    BLUE        (0x5555FF, "36"),
    GREEN       (0x55FF55, "32"),
    AQUA        (0x55FFFF, "36"),
    RED         (0xFF5555, "31"),
    LIGHT_PURPLE(0xFF55FF, "35"),
    YELLOW      (0xFFFF55, "33"),
    WHITE       (0xFFFFFF, "37");

    private final int hexColor;
    private final String ansiColor;

    TextColor(int hexColor, String ansiColor)
    {
        this.hexColor = hexColor;
        this.ansiColor = (char)27 + "[0;" + ansiColor + "m";
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

    public net.kyori.adventure.text.format.TextColor getKyoriColor() {
        return NamedTextColor.NAMES.value(name());
    }

    @Override
    public String toString()
    {
        return name().toLowerCase();
    }
}
