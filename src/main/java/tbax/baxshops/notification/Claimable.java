/* 
 * The MIT License
 *
 * Copyright © 2013-2017 Timothy Baxendale (pcluddite@hotmail.com) and 
 * Copyright © 2012 Nathan Dinsmore and Sam Lazarus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package tbax.baxshops.notification;

import org.bukkit.entity.Player;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.Main;
import tbax.baxshops.Resources;

/**
 * A Claimable represents a notification which must wait for
 * certain conditions to be true before completing an action.
 * When a Claimable notification is sent, the notification is
 * automatically claimed if the user is online.
 */
public abstract class Claimable implements Notification
{
    /**
     * An entry for the offered item
     */
    protected BaxEntry entry;
    
    /**
     * Attempts to claim this notification.
     * @param player the player who is claiming the notification
     * @return true if the notification could be claimed, false otherwise
     */
    public boolean claim(Player player)
    {
        if (Main.tryGiveItem(player, entry.toItemStack())) {
            player.sendMessage(Resources.ITEM_ADDED);
            return true;
        }
        else {
            Main.sendError(player, Resources.NO_ROOM);
            return false;
        }
    }
}
