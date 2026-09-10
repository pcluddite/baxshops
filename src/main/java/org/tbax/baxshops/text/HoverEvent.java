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
import com.google.gson.JsonPrimitive;
import org.bukkit.inventory.ItemStack;
import org.tbax.baxshops.items.ItemUtil;

public final class HoverEvent
{
    private String event;
    private JsonElement value;
    private JsonObject contents;

    private HoverEvent(String event, String value)
    {
        this.event = event;
        this.value = new JsonPrimitive(value);
    }

    private HoverEvent(String event, ChatComponent value)
    {
        this.event = event;
        this.value = value.toJsonObject();
    }

    private HoverEvent(String event, JsonObject contents)
    {
        this.event = event;
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
        return new HoverEvent("show_text", text);
    }

    public static HoverEvent showText(ChatComponent text)
    {
        return new HoverEvent("show_text", text);
    }

    public static HoverEvent showItem(ItemStack stack)
    {
        return new HoverEvent("show_item", ItemUtil.getNBTTag(stack));
    }
}
