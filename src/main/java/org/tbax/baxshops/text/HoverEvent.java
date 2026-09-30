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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.inventory.ItemStack;

public final class HoverEvent
{
    private static final String EVENT_SHOW_TEXT = "show_text";
    private static final String EVENT_SHOW_ITEM = "show_item";

    private final String event;
    private final JsonElement value;
    private final JsonObject contents;

    private HoverEvent(String event, String value)
    {
        this.event = event;
        this.value = new JsonPrimitive(value);
        this.contents = null;
    }

    private HoverEvent(String event, ChatComponent value)
    {
        this.event = event;
        this.value = value.toJsonObject();
        this.contents = null;
    }

    private HoverEvent(String event, JsonObject contents)
    {
        this.event = event;
        this.value = null;
        this.contents = contents;
    }

    public String getEvent()
    {
        return event;
    }

    public String getValue()
    {
        if (value.isJsonPrimitive()) {
            return value.getAsString();
        }
        else {
            return value.toString();
        }
    }

    public JsonObject toJsonObject()
    {
        JsonObject object = new JsonObject();
        object.addProperty("action", event);
        if (value != null) {
            object.add("value", value);
        }
        if (contents != null) {
            object.add("contents", contents);
        }
        return object;
    }

    @Override
    public String toString()
    {
        return toJsonObject().toString();
    }

    public static HoverEvent showText(String text)
    {
        return new HoverEvent(EVENT_SHOW_TEXT, text);
    }

    public static HoverEvent showText(ChatComponent text)
    {
        return new HoverEvent(EVENT_SHOW_TEXT, text);
    }

    public static HoverEvent showItem(ItemStack item)
    {
        GsonComponentSerializer serializer = GsonComponentSerializer.gson();
        String jsonString = serializer.serialize(Component.empty().hoverEvent(item.asHoverEvent()));
        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
        return new HoverEvent(EVENT_SHOW_ITEM, json.get("contents").getAsJsonObject());
    }
}
