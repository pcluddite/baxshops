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
package org.tbax.baxshops.nms;

import org.bukkit.Bukkit;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class RuntimeObject
{
    public static final String CRAFTBUKKIT_PACKAGE;

    static
    {
        String packageName = "org.bukkit.craftbukkit";
        String runtimePackageName = Bukkit.getServer().getClass().getPackage().getName();
        if (runtimePackageName.equals(packageName)) {
            CRAFTBUKKIT_PACKAGE = packageName;
        }
        else {
            CRAFTBUKKIT_PACKAGE = packageName + "." + runtimePackageName.split("\\.")[3];
        }
    }

    private final static Map<String, Class<?>> classCache = new HashMap<>();

    public abstract String __pkg_name();

    public String __class_name()
    {
        Class<? extends RuntimeObject> cls = getClass();
        return cls.getName().substring(cls.getName().lastIndexOf('.') + 1);
    }

    public final Class<?> __class() throws ReflectiveOperationException
    {
        return __class(__pkg_name() + "." + __class_name());
    }

    public static Class<?> __class(String className) throws ReflectiveOperationException
    {
        Class<?> cls = classCache.get(className);
        if (cls == null) {
            classCache.put(className, cls = Class.forName(className));
        }
        return cls;
    }

    protected final Method __method(String name, Class<?>... parameterTypes) throws ReflectiveOperationException
    {
        return __class().getDeclaredMethod(name, parameterTypes);
    }

    public abstract Object __object() throws ReflectiveOperationException;

    public static Class<?> getCraftbukkitClass(String className) throws ReflectiveOperationException
    {
        return __class(CRAFTBUKKIT_PACKAGE + "." + className);
    }
}
