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

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.serialization.SafeMap;
import tbax.baxshops.serialization.StoredPlayer;
import tbax.baxshops.serialization.states.State_30;

import java.util.Map;

@Deprecated
public class SellRequest implements DeprecatedNote
{
    private String seller;
    private String buyer;
    private long shopId;
    private long expirationDate;
    private BaxEntry entry;

    public SellRequest(Map<String, Object> args)
    {
        SafeMap map = new SafeMap(args);
        seller = map.getString("seller");
        buyer = map.getString("buyer");
        shopId = map.getInteger("shop");
        entry = map.getBaxEntry("entry");
        expirationDate = map.getLong("expires");
    }

    @Override
    public @NotNull SaleRequest getNewNote()
    {
        return new SaleRequest(State_30.getShopId(shopId),
            getBuyer(),
            getSeller(),
            entry);
    }

    public OfflinePlayer getBuyer()
    {
        return buyer == null ? StoredPlayer.ERROR : State_30.getPlayer(buyer);
    }

    public OfflinePlayer getSeller()
    {
        return seller == null ? StoredPlayer.ERROR : State_30.getPlayer(seller);
    }

    @Override
    public @NotNull Class<? extends Notification> getNewNoteClass()
    {
        return SaleRequest.class;
    }

    @Override
    public Map<String, Object> serialize()
    {
        throw new NotImplementedException();
    }

    public static SellRequest deserialize(Map<String, Object> args)
    {
        return new SellRequest(args);
    }

    public static SellRequest valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }
}
