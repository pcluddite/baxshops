/*
 * Copyright © 2012 Nathan Dinsmore and Sam Lazarus
 * Modifications Copyright © Timothy Baxendale
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package tbax.shops.notification;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.serialization.StateLoader;
import org.tbax.baxshops.serialization.states.StateLoader_00050;
import org.tbax.baxshops.serialization.states.StateLoader_00100;
import org.tbax.baxshops.serialization.states.StateLoader_00200;
import org.tbax.baxshops.serialization.states.StateLoader_00210;
import tbax.shops.Shop;
import tbax.shops.ShopEntry;

@Deprecated
public class BuyNotification implements Notification
{
    private static final long serialVersionUID = 1L;
    public ShopEntry entry;
    public Shop shop;
    public int shopId;
    public String buyer;
    public static final String JSON_TYPE_ID = "BuyNote";

    public BuyNotification(final Shop shop, final ShopEntry entry, final String buyer)
    {
        this.shop = shop;
        this.entry = entry;
        this.buyer = buyer;
    }

    public BuyNotification(StateLoader_00100 state00200, JsonObject o)
    {
        buyer = o.get("buyer").getAsString();
        shopId = o.get("shop").getAsInt();
        if (state00200 instanceof StateLoader_00210) {
            entry = new ShopEntry((StateLoader_00210)state00200, o.get("entry").getAsJsonObject());
        }
        else if (state00200 instanceof StateLoader_00200) {
            entry = new ShopEntry((StateLoader_00200)state00200, o.get("entry").getAsJsonObject());
        }
        else {
            entry = new ShopEntry(state00200, o.get("entry").getAsJsonObject());
        }
    }

    @Override
    public @NotNull Class<? extends org.tbax.bukkit.notification.Notification> getNewNoteClass()
    {
        return org.tbax.baxshops.notification.BuyNotification.class;
    }

    @Override
    public @NotNull org.tbax.bukkit.notification.Notification getNewNote(StateLoader stateLoader)
    {
        if (stateLoader instanceof StateLoader_00050) {
            return new org.tbax.baxshops.notification.BuyNotification(
                    ((StateLoader_00050)stateLoader).getBaxShop(shop).getId(),
                    stateLoader.getPlayerSafe(null, buyer),
                    stateLoader.getPlayerSafe(null, shop.owner),
                    entry.update((StateLoader_00050)stateLoader)
            );
        }
        else {
            return new org.tbax.baxshops.notification.BuyNotification(
                    stateLoader.getShop(null, shopId).getId(),
                    stateLoader.getPlayerSafe(null, buyer),
                    stateLoader.getPlayerSafe(null, ((StateLoader_00100)stateLoader).getShopOwner(shopId)),
                    entry.update((StateLoader_00100)stateLoader)
            );
        }
    }
}
