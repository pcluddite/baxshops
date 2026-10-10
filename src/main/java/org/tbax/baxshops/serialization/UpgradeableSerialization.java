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
package org.tbax.baxshops.serialization;

import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.ShopPlugin;
import org.tbax.bukkit.serialization.SafeMap;
import org.tbax.bukkit.serialization.SerializationException;
import org.tbax.bukkit.serialization.annotations.DoNotSerialize;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class UpgradeableSerialization
{
    private static final NumberFormat verFormat = new DecimalFormat("000.00");

    private UpgradeableSerialization()
    {
    }

    public static String getVersionString(double ver)
    {
        return verFormat.format(ver).replace(".", "");
    }

    public static Class<?> getClass(String pkg, String cls) throws ReflectiveOperationException
    {
        return Class.forName(pkg + "." + cls);
    }

    public static StateLoader getStateLoader(ShopPlugin plugin, double ver) throws ReflectiveOperationException
    {
        String verStr = getVersionString(ver);
        Class<?> stateClass = getClass("org.tbax.baxshops.serialization.states", "StateLoader_" + verStr);
        return (StateLoader)stateClass.getConstructor(ShopPlugin.class).newInstance(plugin);
    }

    public static Method getUpgrader(Class<? extends UpgradeableSerializable> cls) throws ReflectiveOperationException
    {
        String verStr = getVersionString(State.getLoadedState());
        return cls.getMethod("upgrade" + verStr, SafeMap.class);
    }

    public static void upgrade(@NotNull UpgradeableSerializable obj, @NotNull Map<String, Object> args)
    {
        upgrade(obj, new SafeMap(args));
    }

    public static void upgrade(@NotNull UpgradeableSerializable obj, @NotNull SafeMap map)
    {
        try {
            getUpgrader(obj.getClass()).invoke(obj, map);
        }
        catch (ReflectiveOperationException e) {
            throw new SerializationException(e.getMessage(), e);
        }
    }

    public static Map<String, Object> serialize(UpgradeableSerializable obj)
    {
        SafeMap map = new SafeMap();
        for (Field field : getFields(obj.getClass())) {
            SerialField serialField = new SerialField(obj.getClass(), field);
            try {
                if (serialField.shouldSerialize(obj)) {
                    serialField.putMap(map, obj);
                }
            }
            catch (ReflectiveOperationException e) {
                throw new RuntimeException(obj.getClass().getCanonicalName() + " caused a serialization error", e);
            }
        }
        return map;
    }

    private static List<Field> getFields(Class<?> clazz)
    {
        List<Field> allFields = new ArrayList<>();
        do {
            Arrays.stream(clazz.getDeclaredFields())
                    .filter(f -> !Modifier.isStatic(f.getModifiers())
                                 && f.getAnnotation(DoNotSerialize.class) == null)
                    .forEach(allFields::add);
        }
        while((clazz = clazz.getSuperclass()) != null && !Object.class.equals(clazz));
        return allFields;
    }

    public static void deserialize(UpgradeableSerializable obj, Map<String, Object> args)
    {
        SafeMap map;
        if (args instanceof SafeMap) {
            map = (SafeMap)args;
        }
        else {
            map = new SafeMap(args);
        }
        for (Field field : getFields(obj.getClass())) {
            SerialField serialField = new SerialField(obj.getClass(), field);
            try {
                serialField.getMap(map, obj);
            }
            catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
