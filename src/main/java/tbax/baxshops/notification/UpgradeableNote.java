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
package tbax.baxshops.notification;

import org.jetbrains.annotations.NotNull;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.serialization.Reflector;
import tbax.baxshops.serialization.SafeMap;

public interface UpgradeableNote extends Notification
{
    @Deprecated
    void deserialize00300(@NotNull SafeMap map);

    @Deprecated
    void deserialize00400(@NotNull SafeMap map);

    default void deserialize00410(@NotNull SafeMap map)
    {
        deserialize00400(map);
    }

    default void deserialize(@NotNull SafeMap map)
    {
        try {
            Reflector.getDeserializer(getClass()).invoke(this, map);
        }
        catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }
}
