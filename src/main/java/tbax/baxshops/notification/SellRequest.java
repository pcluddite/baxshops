/* 
 * The MIT License
 *
 * Copyright © 2015 Timothy Baxendale (pcluddite@hotmail.com) and 
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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Calendar;
import java.util.Date;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tbax.baxshops.BaxEntry;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Main;
import static tbax.baxshops.Main.sendError;
import tbax.baxshops.Resources;
import tbax.baxshops.serialization.ItemNames;

/**
 * A SellRequest notifies a shop owner that someone has requested
 * to sell him/her an item.
 * SellRequests expire after five days.
 */
public class SellRequest implements Request, TimedNotification {
    private static final long serialVersionUID = 1L;
    /**
     * An entry for the offered item
     */
    public BaxEntry entry;
    /**
     * The shop to which the item is being sold
     */
    public BaxShop shop;
    /**
     * The date at which the request expires
     */
    public long expirationDate;
    /**
     * The seller of the item
     */
    public String seller;
    
    /**
     * Constructs a new notification.
     * @param shop the shop to which the seller was selling
     * @param entry an entry for the item (note: not the one in the shop)
     * @param seller the seller of the item
     */
    public SellRequest(BaxShop shop, BaxEntry entry, String seller) {
        this.shop = shop;
        this.entry = entry;
        this.seller = seller;

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, Resources.EXPIRE_TIME_DAYS);
        this.expirationDate = c.getTimeInMillis();
    }

    @Override
    public String getMessage(Player player) {
        return player == null || !player.getName().equals(shop.owner) ?
                String.format("%s wants to sell %s %d %s for $%.2f",
                                seller, shop.owner, entry.getAmount(), ItemNames.getItemName(entry),
                                entry.refundPrice * entry.getAmount()) :
                String.format("%s wants to sell you §e%d %s§F for §a$%.2f§F",
                                seller, entry.getAmount(), ItemNames.getItemName(entry),
                                entry.refundPrice * entry.getAmount());
    }
    
    @Override
    public boolean accept(Player player) {
        
        double price = Main.roundTwoPlaces(entry.getAmount() * entry.refundPrice);

        ItemStack item = entry.toItemStack();
        
        Economy econ = Main.econ;

        if (!econ.has(shop.owner, price)) {
            Main.sendError(player, Resources.NO_MONEY);
            return false;
        }
        
        econ.withdrawPlayer(shop.owner, price);
        econ.depositPlayer(seller, price);

        if (shop.sellToShop) {
            sellToShop(item);
        }
        else if (!Main.giveToPlayer(player, item)) {
            sendError(player, Resources.NO_ROOM);
            return false;
        }

        SaleNotification n = new SaleNotification(shop, entry, seller);
        Main.instance.state.sendNotification(seller, n);
        
        player.sendMessage("§aOffer accepted");
        player.sendMessage(String.format(Resources.CURRENT_BALANCE, Main.econ.format(Main.econ.getBalance(player.getName()))));
        
        return true;
    }
    
    public boolean autoAccept() {
        double price = Main.roundTwoPlaces(entry.getAmount() * entry.refundPrice);
        
        Economy econ = Main.econ;
        
        if (!econ.has(shop.owner, price)) {
            return false;
        }
        
        econ.withdrawPlayer(shop.owner, price);
        econ.depositPlayer(seller, price);
        
        Notification buyerNote;
        if (shop.sellToShop) {
            sellToShop(entry.toItemStack());
            buyerNote = new GeneralNotification(
                SaleNotificationAuto.getMessage(shop.owner, shop, entry, seller)
            );
            Main.instance.state.sendNotification(shop.owner, buyerNote, false);
            Main.instance.log.info(SaleNotificationAuto.getMessage(null, shop, entry, seller));
        }
        else {
            buyerNote = new SaleNotificationAuto(shop, entry, seller);
            Main.instance.state.sendNotification(shop.owner, buyerNote);
        }
        return true;
    }
    
    private void sellToShop(ItemStack item) {
        BaxEntry shopEntry = shop.findEntry(item.getType(), item.getDurability());
        if (shopEntry == null) {
            shopEntry = new BaxEntry();
            shopEntry.setItem(item);
            shop.addEntry(shopEntry);
        }
        if (shop.infinite) {
            shopEntry.infinite = true;
        }
        else {
            shopEntry.add(item.getAmount());
        }
    }
    
    @Override
    public boolean reject(Player player) {
        SaleRejection n = new SaleRejection(shop, entry, seller);
        Main.instance.state.sendNotification(seller, n);
        player.sendMessage("§cOffer rejected");
        return true;
    }

    @Override
    public long expirationDate() {
        return expirationDate;
    }

    public static final String TYPE_ID = "SellRequest";
    
    @Override
    public JsonElement toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("type", TYPE_ID);
        o.addProperty("seller", seller);
        o.addProperty("shop", shop.uid);
        o.addProperty("expires", expirationDate);
        o.add("entry", entry.toJson());
        return o;
    }
    
    public SellRequest() {
    }
    
    public static SellRequest fromJson(JsonObject o) {
        SellRequest request = new SellRequest();
        request.seller = o.get("seller").getAsString();
        request.shop = Main.instance.state.getShop(o.get("shop").getAsInt());
        request.expirationDate = o.get("expires").getAsLong();
        request.entry = new BaxEntry(o.get("entry").getAsJsonObject());
        return request;
    }
}
