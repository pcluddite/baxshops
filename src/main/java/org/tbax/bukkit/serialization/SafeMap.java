/*
 * Copyright (C) 2013-2019 Timothy Baxendale
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
package org.tbax.bukkit.serialization;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.Format;

import java.text.ParseException;
import java.util.*;

@SuppressWarnings("unused")
public class SafeMap implements Map<String, Object>
{
    private final Map<String, Object> argMap;

    public SafeMap(Map<String, Object> map)
    {
        argMap = map;
    }

    public SafeMap()
    {
        argMap = new HashMap<>();
    }

    public boolean getBoolean(String key)
    {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue)
    {
        try {
            return (boolean) getOrDefault(key, defaultValue);
        }
        catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public Object put(String key, boolean value)
    {
        return argMap.put(key, value);
    }

    public int getInteger(String key)
    {
        return getInteger(key, 0);
    }

    public int getInteger(String key, int defaultValue)
    {
        try {
            return (int) getOrDefault(key, defaultValue);
        }
        catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public Object put(String key, int value)
    {
        return argMap.put(key, value);
    }

    public String getString(String key)
    {
        return getString(key, "");
    }

    public String getString(String key, String defaultValue)
    {
        try {
            return (String) getOrDefault(key, defaultValue);
        }
        catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public double getDouble(String key)
    {
        return getDouble(key, 0);
    }

    public double getDouble(String key, double defaultValue)
    {
        try {
            return (double) getOrDefault(key, defaultValue);
        }
        catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public Object put(String key, double value)
    {
        return argMap.put(key, value);
    }

    public UUID getUUID(String key)
    {
        return getUUID(key, null);
    }

    public UUID getUUID(String key, UUID defaultValue)
    {
        String value = getString(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return UUID.fromString(value);
        }
        catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    public Object put(String key, UUID value)
    {
        return argMap.put(key, value == null ? null : value.toString());
    }

    public Object put(String key, OfflinePlayer value)
    {
        if (value == null) {
            return argMap.put(key, null);
        }
        else {
            return put(key, value.getUniqueId());
        }
    }

    public <E> List<E> getList(String key)
    {
        return getList(key, new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    public <E> List<E> getList(String key, List<E> defaultValue)
    {
        try {
            return (List<E>)getOrDefault(key, defaultValue);
        }
        catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public <E> Set<E> getSet(String key)
    {
        return getSet(key, new HashSet<>());
    }

    @SuppressWarnings("unchecked")
    public <E> Set<E> getSet(String key, Set<E> defaultValue)
    {
        try {
            return new HashSet<>((List<E>) getOrDefault(key, defaultValue));
        }
        catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public <E> Object put(String key, Set<E> value)
    {
        return argMap.put(key, value == null ? new ArrayList<>() : new ArrayList<>(value));
    }

    public <E> Deque<E> getDeque(String key)
    {
        return getDeque(key, new ArrayDeque<>());
    }

    @SuppressWarnings("unchecked")
    public <E> Deque<E> getDeque(String key, Deque<E> defaultValue)
    {
        try {
            return new ArrayDeque<>((List<E>) getOrDefault(key, defaultValue));
        }
        catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public <E> Object put(String key, Deque<E> value)
    {
        return argMap.put(key, value == null ? new ArrayList<>() : new ArrayList<>(value));
    }

    public ItemStack getItemStack(String key)
    {
        return getItemStack(key, null);
    }

    public ItemStack getItemStack(String key, ItemStack defaultValue)
    {
        try {
            return (ItemStack)getOrDefault(key, defaultValue);
        }
        catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public Object put(String key, ItemStack value)
    {
        return argMap.put(key, value);
    }

    public Date getDate(String key)
    {
        return getDate(key, null);
    }

    public Date getDate(String key, Date defaultValue)
    {
        try {
            return Format.DATE_FORMAT.parse((String)get(key));
        }
        catch (ClassCastException | ParseException | NullPointerException e) {
            return defaultValue;
        }
    }

    public Object put(String key, Date value)
    {
        return argMap.put(key, value == null ? null : Format.date(value));
    }

    @Override
    public int size()
    {
        return argMap.size();
    }

    @Override
    public boolean isEmpty()
    {
        return argMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return argMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return argMap.containsValue(value);
    }

    @Override
    public Object get(Object key)
    {
        return get((String)key, null);
    }

    public Object get(String key, Object defaultValue)
    {
        if (argMap.containsKey(key))
            return argMap.get(key);
        return defaultValue;
    }

    @Override
    public @Nullable Object put(String key, Object value)
    {
        return argMap.put(key, value);
    }

    @Override
    public Object remove(Object key)
    {
        return argMap.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ?> m)
    {
        argMap.putAll(m);
    }

    @Override
    public void clear()
    {
        argMap.clear();
    }

    @Override
    public @NotNull Set<String> keySet()
    {
        return argMap.keySet();
    }

    @Override
    public @NotNull Collection<Object> values()
    {
        return argMap.values();
    }

    @Override
    public @NotNull Set<Entry<String, Object>> entrySet()
    {
        return argMap.entrySet();
    }

    public BaxEntry getBaxEntry(String key)
    {
        return getBaxEntry(key, null);
    }

    public BaxEntry getBaxEntry(String key, BaxEntry defaultValue)
    {
        try {
            return (BaxEntry)getOrDefault(key, defaultValue);
        }
        catch (ClassCastException e) {
            return defaultValue;
        }
    }
    public BaxShop getBaxShop(String key)
    {
        return getBaxShop(key, null);
    }

    public BaxShop getBaxShop(String key, BaxShop defaultValue)
    {
        try {
            return (BaxShop)getOrDefault(key, defaultValue);
        }
        catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public Object put(String key, BaxShop value)
    {
        return argMap.put(key, value);
    }

    public long getLong(String key)
    {
        return getLong(key, 0);
    }

    public long getLong(String key, long defaultValue)
    {
        try {
            return (long)getOrDefault(key, defaultValue);
        }
        catch (ClassCastException e) {
            try {
                return (int)getOrDefault(key, defaultValue);
            }
            catch (ClassCastException e1) {
                return defaultValue;
            }
        }
    }

    public Object put(String key, long value)
    {
        return argMap.put(key, value);
    }
}
