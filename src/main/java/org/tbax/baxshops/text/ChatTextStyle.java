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

import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;

public enum ChatTextStyle
{
    BOLD("bold", ChatColor.BOLD),
    ITALIC("italic", ChatColor.ITALIC),
    UNDERLINED("underlined", ChatColor.UNDERLINE),
    STRIKETHROUGH("strikethrough", ChatColor.STRIKETHROUGH),
    OBFUSCATED("obfuscated", ChatColor.MAGIC);

    private final String style;
    private final ChatColor chatColorStyle;

    ChatTextStyle(String style, ChatColor chatColorStyle)
    {
        this.style = style;
        this.chatColorStyle = chatColorStyle;
    }

    @Override
    public String toString()
    {
        return style;
    }

    public ChatColor getChatColorStyle()
    {
        return chatColorStyle;
    }

    public TextDecoration getKyoriDecoration()
    {
        return TextDecoration.valueOf(name());
    }
}
