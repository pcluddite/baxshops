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

import java.util.HashMap;
import java.util.Map;

public final class RuntimeObject
{
    public static final String CRAFTBUKKIT_PACKAGE;

    static
    {
        String packageName = "org.bukkit.craftbukkit";
        String runtimePackageName = Bukkit.getServer().getClass().getPackage().getName();
        if (runtimePackageName.equals(packageName)) {
            // version is not part of package name
            CRAFTBUKKIT_PACKAGE = packageName;
        }
        else {
            // version is part of package name
            CRAFTBUKKIT_PACKAGE = packageName + "." + runtimePackageName.split("\\.")[3];
        }
    }

    private static final Map<String, Class<?>> CLASS_CACHE = new HashMap<>();

    public static Class<?> getCraftbukkitClass(String className) throws ReflectiveOperationException
    {
        return getRuntimeClass(CRAFTBUKKIT_PACKAGE + "." + className);
    }

    public static Class<?> getRuntimeClass(String className) throws ReflectiveOperationException
    {
        Class<?> cls = CLASS_CACHE.get(className);
        if (cls == null) {
            CLASS_CACHE.put(className, cls = Class.forName(className));
        }
        return cls;
    }
}
