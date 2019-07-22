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
package tbax.baxshops.serialization;

import org.jetbrains.annotations.NotNull;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.serialization.annotations.DoNotSerialize;
import tbax.baxshops.serialization.annotations.SerializeMethod;
import tbax.baxshops.serialization.annotations.SerializedAs;

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
        Class<?> stateClass = getClass("tbax.baxshops.serialization.states", "State_" + verStr);
        return (StateLoader)stateClass.getConstructor(ShopPlugin.class).newInstance(plugin);
    }

    public static Method getDeserializer(Class<? extends UpgradeableSerializable> cls) throws ReflectiveOperationException
    {
        String verStr = getVersionString(SavedState.getLoadedState());
        return cls.getMethod("deserialize" + verStr, SafeMap.class);
    }

    public static void deserialize(@NotNull UpgradeableSerializable obj, @NotNull Map<String, Object> args)
    {
        deserialize(obj, new SafeMap(args));
    }

    public static void deserialize(@NotNull UpgradeableSerializable obj, @NotNull SafeMap map)
    {
        try {
            getDeserializer(obj.getClass()).invoke(obj, map);
        }
        catch (ReflectiveOperationException e) {
            throw new SerializationException(e.getMessage(), e.getCause());
        }
    }

    public static Map<String, Object> serialize(UpgradeableSerializable obj)
    {
        SafeMap map = new SafeMap();
        for (Field field : getFields(obj.getClass())) {
            field.setAccessible(true);
            try {
                SerializedAs as = field.getAnnotation(SerializedAs.class);
                SerializeMethod m = field.getAnnotation(SerializeMethod.class);
                String name = field.getName();
                Class<?> type = field.getType();
                Object value;
                if (as != null) {
                    name = as.value();
                }
                if (m != null) {
                    Method method = obj.getClass().getDeclaredMethod(m.value());
                    method.setAccessible(true);
                    type = method.getReturnType();
                    value = method.invoke(obj);
                }
                else {
                    value = field.get(obj);
                }
                getMapPutter(type).invoke(map, name, value);
            }
            catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        return map;
    }

    private static Method getMapPutter(Class<?> clazz)
    {
        try {
            return SafeMap.class.getDeclaredMethod("put", String.class, clazz);
        }
        catch (NoSuchMethodException e) {
            try {
                return SafeMap.class.getDeclaredMethod("put", String.class, Object.class);
            }
            catch (NoSuchMethodException ex) {
                throw new RuntimeException(ex); // this shouldn't happen
            }
        }
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
}
