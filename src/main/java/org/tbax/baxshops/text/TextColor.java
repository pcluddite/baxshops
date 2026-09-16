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

import org.bukkit.ChatColor;

public enum TextColor
{
    BLACK       ("black"       , ChatColor.BLACK       , 0x000000),
    DARK_BLUE   ("dark_blue"   , ChatColor.DARK_BLUE   , 0x0000AA),
    DARK_GREEN  ("dark_green"  , ChatColor.DARK_GREEN  , 0x00AA00),
    DARK_AQUA   ("dark_aqua"   , ChatColor.DARK_AQUA   , 0x00AAAA),
    DARK_RED    ("dark_red"    , ChatColor.DARK_RED    , 0xAA0000),
    DARK_PURPLE ("dark_purple" , ChatColor.DARK_PURPLE , 0xAA00AA),
    GOLD        ("gold"        , ChatColor.GOLD        , 0xFFAA00),
    GRAY        ("gray"        , ChatColor.GRAY        , 0xAAAAAA),
    DARK_GRAY   ("dark_gray"   , ChatColor.DARK_GRAY   , 0x555555),
    BLUE        ("blue"        , ChatColor.BLUE        , 0x5555FF),
    GREEN       ("green"       , ChatColor.GREEN       , 0x55FF55),
    AQUA        ("aqua"        , ChatColor.AQUA        , 0x55FFFF),
    RED         ("red"         , ChatColor.RED         , 0xFF5555),
    LIGHT_PURPLE("light_purple", ChatColor.LIGHT_PURPLE, 0xFF55FF),
    YELLOW      ("yellow"      , ChatColor.YELLOW      , 0xFFFF55),
    WHITE       ("white"       , ChatColor.WHITE       , 0xFFFFFF);

    private final String value;
    private final ChatColor chatColor;
    private final int hexColor;

    TextColor(String value, ChatColor chatColor, int hexColor)
    {
        this.value = value;
        this.chatColor = chatColor;
        this.hexColor = hexColor;
    }

    public ChatColor getChatColor()
    {
        return chatColor;
    }

    public int getHexColor()
    {
        return hexColor;
    }

    @Override
    public String toString()
    {
        return value;
    }
}
