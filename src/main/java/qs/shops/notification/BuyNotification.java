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
package qs.shops.notification;

import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.serialization.StateLoader;
import org.tbax.baxshops.serialization.states.StateLoader_00000;
import qs.shops.Shop;
import qs.shops.ShopEntry;

/**
 * A BuyNotification notifies a shop owner that someone bought an item
 * from him/her.
 */
@Deprecated
public class BuyNotification implements Notification {
	private static final long serialVersionUID = 1L;
	/**
	 * An entry for the offered item
	 */
	public ShopEntry entry;
	/**
	 * The shop to which the item is being sold
	 */
	public Shop shop;
	/**
	 * The seller of the item
	 */
	public String buyer;
	
	/**
	 * Constructs a new notification.
	 * @param shop the shop to which the seller was selling
	 * @param entry an entry for the item (note: not the one in the shop)
	 */
	public BuyNotification(Shop shop, ShopEntry entry, String buyer) {
		this.shop = shop;
		this.entry = entry;
		this.buyer = buyer;
	}

	// begin modified class

	@Override
	public @NotNull Class<? extends org.tbax.bukkit.notification.Notification> getNewNoteClass()
	{
		return org.tbax.baxshops.notification.BuyNotification.class;
	}

	@Override
	public @NotNull org.tbax.bukkit.notification.Notification getNewNote(StateLoader stateLoader)
	{
		return new org.tbax.baxshops.notification.BuyNotification(
				((StateLoader_00000)stateLoader).getBaxShopId(shop),
				stateLoader.getPlayerSafe(null, buyer),
				stateLoader.getPlayerSafe(null, shop.owner),
				entry.update()
		);
	}
}
