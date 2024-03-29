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
package org.tbax.baxshops.nms.world.item;

import org.tbax.baxshops.nms.RuntimeObject;
import org.tbax.baxshops.nms.network.chat.IChatBaseComponent;

import java.lang.reflect.Method;

public final class NmsItemStack extends RuntimeObject
{
    private Object runtimeObject;

    public NmsItemStack(Object runtimeObject)
    {
        this.runtimeObject = runtimeObject;
    }

    @Override
    public String __pkg_name()
    {
        return "net.minecraft.world.item";
    }

    @Override
    public String __class_name()
    {
        return "ItemStack";
    }

    private static Method getNameMethod = null;
    public IChatBaseComponent getName() throws ReflectiveOperationException
    {
        if (getNameMethod == null) {
            getNameMethod = __method("w");
        }
        return new IChatBaseComponent(getNameMethod.invoke(runtimeObject));
    }

    @Override
    public Object __object()
    {
        return runtimeObject;
    }
}
