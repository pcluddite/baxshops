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
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.ShopPlugin;
import org.tbax.baxshops.notification.NoteSet;
import org.tbax.bukkit.notification.Notification;
import org.tbax.bukkit.serialization.StoredPlayer;

import java.util.*;

public class StateLoader_00410 extends StateLoader_00400
{
    public static final double VERSION = 4.1;

    public StateLoader_00410(ShopPlugin plugin)
    {
        super(plugin);
    }

    @Override
    public @NotNull Collection<NoteSet> buildNotifications(@NotNull FileConfiguration state)
    {
        List<NoteSet> notes = new ArrayList<>();
        if (!state.isConfigurationSection("notes")) {
            return notes;
        }

        NoteSet errorNotes = new NoteSet(StoredPlayer.ERROR_UUID);

        for (Map.Entry entry : state.getConfigurationSection("notes").getValues(false).entrySet()) {
            UUID playerId;
            try {
                playerId = UUID.fromString(entry.getKey().toString());
            }
            catch (IllegalArgumentException e) {
                playerId = StoredPlayer.ERROR_UUID;
                ShopPlugin.logWarning("UUID " + entry.getKey() + " is invalid. Notes will be assigned to an error user.");
            }
            if (entry.getValue() instanceof List) {
                Deque<Notification> pending = new ArrayDeque<>(((List) entry.getValue()).size());
                for (Object o : (List) entry.getValue()) {
                    if (o instanceof Notification) {
                        pending.add((Notification) o);
                    }
                    else {
                        ShopPlugin.logWarning("Could not load Notification of type " + entry.getValue().getClass());
                    }
                }
                if (playerId.equals(StoredPlayer.ERROR_UUID)) {
                    errorNotes.getNotifications().addAll(pending);
                }
                else {
                    notes.add(new NoteSet(playerId, pending));
                }
            }
            else {
                ShopPlugin.logWarning("Could not load notification list for " + entry.getKey());
            }
        }

        if (!errorNotes.getNotifications().isEmpty()) {
            notes.add(errorNotes);
        }

        return notes;
    }
}
