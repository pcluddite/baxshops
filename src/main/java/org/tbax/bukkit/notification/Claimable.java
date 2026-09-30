/*
 * Copyright (C) Timothy Baxendale
 * Portions derived from Shops Copyright (c) 2012 Nathan Dinsmore and Sam Lazarus.
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
package org.tbax.bukkit.notification;

import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.Resources;
import org.tbax.bukkit.commands.CmdActor;
import org.tbax.bukkit.errors.PrematureAbortException;

public interface Claimable extends Notification
{
    BaxEntry getEntry();

    default boolean claim(CmdActor actor)
    {
        BaxEntry entry = getEntry();
        try {
            int overflow = actor.giveItem(entry.toItemStack(), false);
            if (overflow > 0) {
                actor.sendInfo(Resources.SOME_ROOM, entry.getAmount() - overflow, entry.getName());
                entry.setAmount(overflow);
                return false;
            }
            return true;
        }
        catch (PrematureAbortException e) {
            actor.sendMessage(e.getMessage());
            return false;
        }
    }
}
