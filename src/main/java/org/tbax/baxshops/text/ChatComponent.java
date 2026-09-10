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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.ShopPlugin;

import java.lang.reflect.Method;
import java.util.*;

@SuppressWarnings({ "unused", "UnusedReturnValue" })
public final class ChatComponent
{
    private List<ChatComponent> siblings = new ArrayList<>();
    private Set<ChatTextStyle> styles = new HashSet<>();
    private TextColor color = null;
    private ClickEvent clickEvent = null;
    private HoverEvent hoverEvent = null;

    private String text;

    public ChatComponent(String text)
    {
        this.text = text;
    }

    public ChatComponent(String text, TextColor color)
    {
        this.text = text;
        this.color = color;
    }

    public ChatComponent(String text, ChatTextStyle style)
    {
        this.text = text;
        addStyle(style);
    }

    public ChatComponent(String text, ClickEvent clickEvent)
    {
        this.text = text;
        this.clickEvent = clickEvent;
    }

    public ChatComponent(String text, Collection<ChatTextStyle> styles)
    {
        this.text = text;
        for (ChatTextStyle style : styles) {
            addStyle(style);
        }
    }

    public ChatComponent(String text, ChatTextStyle... styles)
    {
        this(text, Arrays.asList(styles));
    }

    public ChatComponent(String text, TextColor color, Collection<ChatTextStyle> styles)
    {
        this.text = text;
        this.color = color;
        for (ChatTextStyle style : styles) {
            addStyle(style);
        }
    }

    public ChatComponent(String text, TextColor color, ChatTextStyle... styles)
    {
        this(text, color, Arrays.asList(styles));
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public TextColor getColor()
    {
        return color;
    }

    public void setColor(TextColor color)
    {
        this.color = color;
    }

    public Set<ChatTextStyle> getStyles()
    {
        return Collections.unmodifiableSet(styles);
    }

    public boolean addStyle(@NotNull ChatTextStyle style)
    {
        return styles.add(style);
    }

    public boolean removeStyle(@NotNull ChatTextStyle style)
    {
        return styles.remove(style);
    }

    public List<ChatComponent> getSiblings()
    {
        return Collections.unmodifiableList(siblings);
    }

    public void addSibling(ChatComponent chatComponent)
    {
        siblings.add(chatComponent);
    }

    public boolean removeSibling(ChatComponent chatComponent)
    {
        return siblings.remove(chatComponent);
    }

    public ChatComponent removeSibling(int index)
    {
        return siblings.remove(index);
    }

    public ChatComponent append(String text)
    {
        addSibling(new ChatComponent(text));
        return this;
    }

    public ChatComponent append(String text, TextColor color)
    {
        addSibling(new ChatComponent(text, color));
        return this;
    }

    public ChatComponent append(String text, ChatTextStyle style)
    {
        addSibling(new ChatComponent(text, style));
        return this;
    }

    public ChatComponent append(String text, Collection<ChatTextStyle> styles)
    {
        addSibling(new ChatComponent(text, styles));
        return this;
    }

    public ChatComponent append(String text, ChatTextStyle... styles)
    {
        return append(text, Arrays.asList(styles));
    }

    public ChatComponent append(String text, TextColor color, Collection<ChatTextStyle> styles)
    {
        addSibling(new ChatComponent(text, color, styles));
        return this;
    }

    public ChatComponent append(String text, TextColor color, ChatTextStyle... styles)
    {
        return append(text, color, Arrays.asList(styles));
    }

    public ChatComponent append(ChatComponent component)
    {
        addSibling(component);
        return this;
    }

    public ChatComponent appendLine()
    {
        return append("\n");
    }

    public ChatComponent clickEvent(ClickEvent clickEvent)
    {
        this.clickEvent = clickEvent;
        return this;
    }

    public ChatComponent hoverEvent(HoverEvent hoverEvent)
    {
        this.hoverEvent = hoverEvent;
        return this;
    }

    public void sendTo(CommandSender sender)
    {
        if (sender instanceof Player) {
            sendTo((Player)sender);
        }
        else {
            sender.sendMessage(toPlainString());
        }
    }

    public void sendTo(Player player)
    {
        try {
            Component component = GsonComponentSerializer.gson().deserialize(toString());
            Method sendMessage = Player.class.getMethod("sendMessage", Class.forName("net.kyori.adventure.text.Component"));
            sendMessage.invoke(player, component);
        } catch (ReflectiveOperationException e) {
            ShopPlugin.logSevere("Reflection error at " +  e.getMessage());
            player.sendMessage(toPlainString());
        }
    }

    public ClickEvent getClickEvent()
    {
        return clickEvent;
    }

    public void setClickEvent(ClickEvent event)
    {
        this.clickEvent = event;
    }

    public HoverEvent getHoverEvent()
    {
        return hoverEvent;
    }

    public void setHoverEvent(HoverEvent hoverEvent)
    {
        this.hoverEvent = hoverEvent;
    }

    public String toPlainString()
    {
        StringBuilder sb = new StringBuilder(text.length());
        if (color != null) {
            sb.append(color.getChatColor());
        }
        for (ChatTextStyle style : styles) {
            sb.append(style.getChatColorStyle());
        }
        sb.append(text);
        for (ChatComponent sib : siblings) {
            sb.append(sib.toPlainString());
            sb.append(ChatColor.RESET);
            if (color != null) {
                sb.append(color.getChatColor());
            }
            for (ChatTextStyle style : styles) {
                sb.append(style.getChatColorStyle());
            }
        }
        return sb.toString();
    }

    public JsonElement toJsonObject()
    {
        JsonObject obj = new JsonObject();
        if (text != null) {
            obj.addProperty("text", text);
        }
        if (color != null) {
            obj.addProperty("color", color.name().toLowerCase());
        }
        for (ChatTextStyle style : styles) {
            obj.addProperty(style.toString(), "true");
        }
        if (clickEvent != null) {
            obj.add("clickEvent", clickEvent.toJsonObject());
        }
        if (hoverEvent != null) {
            obj.add("hoverEvent", hoverEvent.toJsonObject());
        }
        if (!siblings.isEmpty()) {
            JsonArray array = new JsonArray();
            for (ChatComponent sib : siblings) {
                array.add(sib.toJsonObject());
            }
            obj.add("extra", array);
        }
        return obj;
    }

    @Override
    public String toString()
    {
        return toJsonObject().toString();
    }

    public static ChatComponent of(String text)
    {
        return new ChatComponent(text);
    }

    public static ChatComponent of(String text, TextColor color)
    {
        return new ChatComponent(text, color);
    }

    public static ChatComponent of(String text, ChatTextStyle style)
    {
        return new ChatComponent(text, style);
    }

    public static ChatComponent of(String text, ClickEvent clickEvent)
    {
        return new ChatComponent(text, clickEvent);
    }

    public static ChatComponent of(String text, Collection<ChatTextStyle> styles)
    {
        return new ChatComponent(text, styles);
    }

    public static ChatComponent of(String text, ChatTextStyle... styles)
    {
        return new ChatComponent(text, styles);
    }

    public static ChatComponent of(String text, TextColor color, Collection<ChatTextStyle> styles)
    {
        return new ChatComponent(text, color, styles);
    }

    public static ChatComponent of(String text, TextColor color, ChatTextStyle... styles)
    {
        return new ChatComponent(text, color, styles);
    }
}
