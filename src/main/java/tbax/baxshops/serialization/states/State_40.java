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
package tbax.baxshops.serialization.states;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.BaxShop;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.notification.NoteSet;
import tbax.baxshops.serialization.StateLoader;
import tbax.baxshops.serialization.StoredPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class State_40 implements StateLoader
{
    public static final double VERSION = 4.0;
    private ShopPlugin plugin;

    public State_40(@NotNull ShopPlugin plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public @NotNull Collection<BaxShop> buildShops(@NotNull FileConfiguration state)
    {
        List<BaxShop> shops = new ArrayList<>();
        if (!state.isList("shops")) {
            return shops;
        }
        for (Object o : state.getList("shops")) {
            if (o instanceof BaxShop) {
                shops.add((BaxShop)o);
            }
            else {
                plugin.getLogger().warning("Could not readFromDisk BaxShop of type " + o.getClass());
            }
        }
        return shops;
    }

    @Override
    public @NotNull Collection<StoredPlayer> buildPlayers(@NotNull FileConfiguration state)
    {
        List<StoredPlayer> players = new ArrayList<>();
        if (!state.isList("players")) {
            return players;
        }
        for(Object o : state.getList("players")) {
            if (o instanceof StoredPlayer) {
                players.add((StoredPlayer)o);
            }
            else {
                plugin.getLogger().warning("Could not readFromDisk StoredPlayer of type " + o.getClass());
            }
        }
        return players;
    }

    @Override
    public @NotNull Collection<NoteSet> buildNotifications(@NotNull FileConfiguration state)
    {
        List<NoteSet> notes = new ArrayList<>();
        if (!state.isList("notes")) {
            return notes;
        }
        for (Object o : state.getList("notes")) {
            if (o instanceof NoteSet) {
                notes.add((NoteSet)o);
            }
            else {
                plugin.getLogger().warning("Could not readFromDisk NoteSet of type " + o.getClass());
            }
        }
        return notes;
    }

    @Override
    public @NotNull ShopPlugin getPlugin()
    {
        return plugin;
    }
}
