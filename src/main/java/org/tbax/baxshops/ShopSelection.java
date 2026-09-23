/*
 * Copyright (C) 2013-2019 Timothy Baxendale
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
package org.tbax.baxshops;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.tbax.baxshops.text.*;

/**
 * A ShopSelection represents a user's selected shop.
 * Shops are selected by right- or left-clicking on them in-game.
 */
public final class ShopSelection
{
    public static final int ITEMS_PER_PAGE = 7;
    
    /**
     * The selected shop
     */
    private BaxShop shop;

    /**
     * The displayed inventory page number
     */
    private int page = 0;

    /**
     * Whether the player who selected this shop owns it 
     */
    private boolean owner;

    private Location location;

    public ShopSelection()
    {
    }

    public void setLocation(Location value)
    {
        location = value;
    }

    public Location getLocation()
    {
        return location;
    }

    public boolean isOwner()
    {
        return owner;
    }

    public void setIsOwner(boolean value)
    {
        owner = value;
    }

    public void setShop(BaxShop value)
    {
        shop = value;
    }

    public BaxShop getShop()
    {
        return shop;
    }

    public void setPage(int value)
    {
        page = value;
    }

    public int getPage()
    {
        return page;
    }
        
    public void showListing(CommandSender sender)
    {
        int pages = shop.getPages();
        if (pages == 0) {
            sender.sendMessage(Format.header("Empty"));
            sender.sendMessage("");
            sender.sendMessage("This shop has no items");
            int stop = ITEMS_PER_PAGE - 2;
            if (owner) {
                sender.sendMessage("Use /shop add to add items");
                stop--;
            }
            for (int i = 0; i < stop; ++i) {
                sender.sendMessage("");
            }
        }
        else {
            Format.header(page + 1, pages, "/shop page").sendTo(sender);
            int i = page * ITEMS_PER_PAGE,
                    stop = (page + 1) * ITEMS_PER_PAGE,
                    max = Math.min(stop, shop.size());
            for (; i < max; i++) {
                sender.sendMessage(shop.getEntry(i).toChatComponent(i + 1, shop.hasFlagInfinite()));
            }
            for (; i < stop; i++) {
                sender.sendMessage("");
            }
        }
    }

    public ItemStack toItem(Material signType)
    {
        return shop.toItem(location, signType);
    }

    public void showIntro(CommandSender sender)
    {
        ChatComponent msg = new ChatComponent("Welcome to ", TextColor.WHITE)
                .append(owner ? "your " : shop.getOwner().getName() + "'s ", TextColor.DARK_BLUE)
                .append("shop")
                .appendLine()
                .append(ChatComponent.of("For help with shops, type ", TextColor.GRAY)
                        .append(ChatComponent.of("/shop help", ChatTextStyle.UNDERLINED)
                                .clickEvent(ClickEvent.runCommand("/shop help"))
                                .hoverEvent(HoverEvent.showText(ChatColor.AQUA + "Get help with shops"))
                ));
        msg.sendTo(sender);
    }
}
