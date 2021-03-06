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
package org.tbax.baxshops.serialization.states;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.ShopPlugin;
import org.tbax.baxshops.notification.NoteSet;
import org.tbax.baxshops.serialization.State;
import org.tbax.baxshops.serialization.StateLoader;
import org.tbax.bukkit.notification.Notification;
import org.tbax.bukkit.serialization.StoredPlayer;

import java.io.File;
import java.util.Collection;
import java.util.Deque;

@Deprecated
public abstract class LoaderWithNotes implements StateLoader
{
    public abstract  @NotNull Collection<NoteSet> buildNotifications(@NotNull FileConfiguration state);

    @Override
    public State loadState(@NotNull File stateLocation)
    {
        FileConfiguration stateConfig = readFile(stateLocation);
        State savedState = new State(getPlugin());

        if (stateConfig == null) {
            return savedState;
        }

        ShopPlugin.logInfo("Loading shop data...");
        Collection<BaxShop> shops = buildShops(stateConfig);
        sanitizeShopData(shops);
        savedState.setShops(shops);

        ShopPlugin.logInfo("Loading player data...");
        Collection<StoredPlayer> players = buildPlayers(stateConfig);
        sanitizePlayerData(players);
        savedState.setPlayers(players);

        ShopPlugin.logInfo("Loading notifications...");
        Collection<NoteSet> notes = buildNotifications(stateConfig);

        for (NoteSet noteSet : notes) {
            Deque<Notification> deque = noteSet.getNotifications();
            StoredPlayer player = getPlayer(savedState, noteSet.getRecipient());
            for (Notification note : deque) {
                player.queueNote(note);
            }
        }

        return savedState;
    }

    @Override
    public FileConfiguration readFile(@NotNull File stateLocation)
    {
        return YamlConfiguration.loadConfiguration(stateLocation);
    }
}
