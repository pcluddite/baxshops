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
package org.tbax.baxshops;

import com.google.common.base.Objects;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSignOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.tbax.bukkit.errors.CommandErrorException;
import org.tbax.bukkit.errors.CommandWarningException;
import org.tbax.bukkit.errors.PrematureAbortException;
import org.tbax.baxshops.items.ItemUtil;
import org.tbax.baxshops.text.ChatComponent;
import org.tbax.baxshops.text.ClickEvent;
import org.tbax.baxshops.text.HoverEvent;
import org.tbax.bukkit.serialization.StoredPlayer;

import java.util.UUID;

@SuppressWarnings("unused")
public class EventListener implements Listener
{
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event)
    {
        if (event.isCancelled()) return;
        try {
            BaxShop shop = ShopPlugin.getShop(event.getBlock().getLocation());
            if (shop != null) {
                if (Objects.equal(event.getPlayer(), shop.getOwner()) || event.getPlayer().hasPermission("shops.admin")) {
                    if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                        event.setCancelled(true);
                    }
                    else if (shop.getLocations().size() == 1) {
                        throw new CommandWarningException("This is the only location for this shop. It cannot be destroyed.");
                    }
                    else {
                        event.setCancelled(true);
                        ItemStack shopStack = shop.toItem(event.getBlock().getLocation());
                        event.getBlock().setType(Material.AIR);
                        if (PlayerUtil.hasRoomForItem(event.getPlayer(), shopStack)) {
                            PlayerUtil.giveItem(event.getPlayer(), shopStack);
                            ShopPlugin.sendInfo(event.getPlayer(),
                                String.format("%s shop has been added to your inventory",
                                    Objects.equal(event.getPlayer(), shop.getOwner()) ? Format.username("Your")
                                        : Format.username(shop.getOwner().getName()) + "'s"
                                )
                            );
                        }
                        else {
                            event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), shopStack);
                        }
                        ShopPlugin.removeLocation(shop.getId(), event.getBlock().getLocation());
                        ShopPlugin.clearSelection(event.getPlayer());
                    }
                }
                else {
                    throw new CommandErrorException("You don't have permission to remove this shop.");
                }
            }
            for (Block block : ItemUtil.getSignOnBlock(event.getBlock())) {
                if (ShopPlugin.getShop(block.getLocation()) != null) {
                    throw new CommandErrorException("You cannot break this block because there is a shop on it.");
                }
            }
        }
        catch (PrematureAbortException e) {
            event.setCancelled(true);
            ShopPlugin.sendMessage(event.getPlayer(), e.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        Block b = event.getClickedBlock();
        Player player = event.getPlayer();

        if (b == null) {
            return;
        }
        
        BaxShop shop = ShopPlugin.getShop(b.getLocation());
        if (shop == null) {
            if (b.hasMetadata("shopid")) {
                shop = ShopPlugin.getShop(UUID.fromString(b.getMetadata("shopid").get(0).asString()));
                if (shop == null) {
                    ShopPlugin.sendMessage(event.getPlayer(),"This shop has been closed.");
                    return;
                }
            }
            else {
                return;
            }
        }

        boolean isOwner = shop.getOwner().equals(player);

        ShopSelection selection = ShopPlugin.getSelection(player);
        selection.setLocation(b.getLocation());

        if (selection.getShop() == shop) {
            int pages = shop.getPages();
            if (pages == 0) {
                selection.setPage(0);
            }
            else {
                int delta = event.getAction() == Action.LEFT_CLICK_BLOCK ? 1 : -1;
                selection.setPage((((selection.getPage() + delta) % pages) + pages) % pages);
            }
            player.sendMessage("");
            player.sendMessage("");
        }
        else {
            ShopPlugin.clearStaticData(player.getUniqueId());
            if (ShopPlugin.getStateFile().getConfig().isLogNotes()) {
                ShopPlugin.logInfo(String.format("%s selected shop %s", player.getName(), shop.getId().toString()));
            }
            selection.setIsOwner(isOwner);
            selection.setShop(shop);
            selection.setPage(0);
            selection.showIntro(player);
        }

        selection.showListing(player);
    }
	
    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplosion(EntityExplodeEvent event)
    {
        for (int i = event.blockList().size() - 1; i >= 0; --i) {
            Block b = event.blockList().get(i);
            if (ShopPlugin.getShop(b.getLocation()) != null) {
                event.blockList().remove(b);
            }
            else {
                for (Block block : ItemUtil.getSignOnBlock(b)) {
                    if (ShopPlugin.getShop(block.getLocation()) != null) {
                        event.blockList().remove(b);
                        break;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPhysicsEvent(BlockPhysicsEvent e)
    {
        for (Block block : ItemUtil.getSignOnBlock(e.getBlock())) {
            if (ShopPlugin.getShop(block.getLocation()) != null) {
                e.setCancelled(true);
                break;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        try {
            ItemStack item = event.getItemInHand();
            if (!ItemUtil.isShop(item) || event.isCancelled())
                return;
            BaxShop shop = ItemUtil.fromItem(item);
            if (shop == null) {
                throw new CommandErrorException("This shop has been closed and can't be placed.");
            }
            if (!ShopPlugin.addLocation(event.getBlockPlaced().getLocation(), shop)) {
                throw new CommandErrorException(Resources.SHOP_EXISTS);
            }
            String[] lines = ItemUtil.extractSignText(item);
            if (lines.length > 0) {
                Sign sign = (Sign) event.getBlockPlaced().getState();
                if (lines.length < 3) {
                    sign.setLine(0, "");
                    sign.setLine(1, lines[0]);
                    sign.setLine(2, lines.length > 1 ? lines[1] : "");
                    sign.setLine(3, "");
                } else {
                    sign.setLine(0, lines[0]);
                    sign.setLine(1, lines[1]);
                    sign.setLine(2, lines[2]);
                    sign.setLine(3, lines.length > 3 ? lines[3] : "");
                }
                sign.setEditable(true);
                sign.update();
            }
        }
        catch (PrematureAbortException e) {
            event.setCancelled(true);
            ShopPlugin.sendMessage(event.getPlayer(), e.getMessage());
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onSignChange(SignChangeEvent event)
    {
        BaxShop shop = ShopPlugin.getShop(event.getBlock().getLocation());
        if (shop != null) {
            for(String line : event.getLines()) {
                if (!line.isEmpty()) {
                    return;
                }
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        StoredPlayer player = ShopPlugin.getState().joinPlayer(event.getPlayer());
        if (player.hasNotes()) {
            ChatComponent.of("You have new notifications. Use ")
                    .append(ChatComponent.of(Format.command("/shop notifications"))
                            .hoverEvent(HoverEvent.showText(ChatColor.AQUA + "Show next notification"))
                            .clickEvent(ClickEvent.runCommand("/shop notifications")))
                    .append(" to view them")
                    .sendTo(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event)
    {
        Player pl = event.getPlayer();
        ShopSelection s = ShopPlugin.getSelection(pl);
        if (s.getShop() != null) {
            Location shopLoc = s.getLocation();
            Location pLoc = event.getTo();
            if (shopLoc != null && shopLoc.getWorld() != pl.getWorld() || shopLoc.distanceSquared(pLoc) > Resources.SHOP_RANGE) {
                if (ShopPlugin.getStateFile().getConfig().isLogNotes()) {
                    ShopPlugin.logInfo(String.format("%s left shop %s", pl.getName(), s.getShop().getId().toString()));
                }
                if (s.isOwner()) {
                    pl.sendMessage("[Left " + Format.username("your") + " shop]");
                }
                else {
                    pl.sendMessage("[Left " + Format.username(s.getShop().getOwner().getName()) + "'s shop]");
                }
                ShopPlugin.clearSelection(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onSignOpen(PlayerSignOpenEvent event)
    {
        BaxShop shop = ShopPlugin.getShop(event.getSign().getLocation().getBlock().getLocation());
        if (shop != null) {
            event.setCancelled(true);
        }
    }
}
