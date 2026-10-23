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
package org.tbax.baxshops;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tbax.baxshops.items.ItemUtil;
import org.tbax.baxshops.notification.DeletedShopClaim;
import org.tbax.baxshops.notification.SaleClaim;
import org.tbax.baxshops.versioning.LegacyPlayerUtil;
import org.tbax.bukkit.MathUtil;
import org.tbax.bukkit.errors.CommandErrorException;
import org.tbax.bukkit.errors.CommandWarningException;
import org.tbax.bukkit.errors.PrematureAbortException;
import org.tbax.bukkit.serialization.StoredPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Methods for dealing with interactions with players
 */
@SuppressWarnings("unused")
public final class PlayerUtil
{
    private PlayerUtil()
    {
    }

    /**
     * gives an ItemStack to a player
     * @param player recipient of the ItemStack
     * @param item item to give the player
     * @return the amount that could not be added to the player's inventory
     * @throws PrematureAbortException thrown if no space is available
     */
    public static int giveItem(@NotNull Player player, @NotNull ItemStack item) throws PrematureAbortException
    {
        return giveItem(player, item, false);
    }

    /**
     * gives an ItemStack to a player
     * @param player recipient of the ItemStack
     * @param item item to give the player
     * @param allOrNothing if true, there must be space for the full amount of the ItemStack
     * @return the amount that could not be added to the player's inventory
     * @throws PrematureAbortException thrown if no space is available
     */
    public static int giveItem(@NotNull Player player, @NotNull ItemStack item, boolean allOrNothing) throws PrematureAbortException
    {
        int space = getSpaceForItem(player, item);
        if (space == 0 || (allOrNothing && space < item.getAmount())) {
            throw new CommandErrorException(String.format(Resources.NO_ROOM_FOR_ITEM, item.getAmount(), ItemUtil.getName(item)));
        }

        int overflow = Math.max(item.getAmount() - space, 0);
        int fullStacks;
        item = item.clone();
        item.setAmount(Math.min(item.getAmount(), space));

        fullStacks = item.getAmount() / item.getMaxStackSize();
        int leftover = item.getAmount() - fullStacks * item.getMaxStackSize();

        if (leftover > 0) {
            item.setAmount(leftover);
            player.getInventory().addItem(item);
        }

        while(fullStacks-- > 0) {
            ItemStack stack = item.clone();
            stack.setAmount(stack.getMaxStackSize());
            player.getInventory().addItem(stack);
        }

        return overflow;
    }

    /**
     * gets the amount of space in the player's inventory for a given item
     * @param player the player whose inventory to check
     * @param item item to check
     * @return the amount available to add in the player's inventory
     */
    public static int getSpaceForItem(@NotNull Player player, @NotNull ItemStack item)
    {
        ItemStack[] contents = LegacyPlayerUtil.getInventoryContents(player);
        int max = item.getMaxStackSize();
        int space = 0;

        for (ItemStack content : contents) {
            if (content == null || content.getType() == Material.AIR) {
                space += max;
            }
            else if (content.isSimilar(item)) {
                space += max - content.getAmount();
            }
        }
        return space;
    }

    /**
     * determines whether there is enough space available in a player's inventory for the full amount of an ItemStack
     * @param player the player whose inventory to check
     * @param item item to check
     * @return true if there is enough space, otherwise false
     */
    public static boolean hasRoomForItem(@NotNull Player player, @NotNull ItemStack item)
    {
        return item.getAmount() <= getSpaceForItem(player, item);
    }

    /**
     * gives an ItemStack to a player
     * @apiNote This differs from giveItem() in that it does not throw an exception, but sends the message to the player
     * @param player the player
     * @param item the item
     * @return true if the item was given, otherwise false
     */
    public static boolean tryGiveItem(@NotNull Player player, @NotNull ItemStack item)
    {
        return tryGiveItem(player, item, true);
    }

    /**
     * gives an ItemStack to a player
     * @apiNote This differs from giveItem() in that it does not throw an exception, but sends the message to the player
     * @param player the player
     * @param item the item
     * @param allOrNothing if true, there must be space for the full amount of the ItemStack
     * @return true if the item was given, otherwise false
     */
    public static boolean tryGiveItem(@NotNull Player player, @NotNull ItemStack item, boolean allOrNothing)
    {
        try {
            giveItem(player, item, allOrNothing);
            return true;
        }
        catch (CommandErrorException | CommandWarningException e) {
            ShopPlugin.sendMessage(player, e.getComponent());
            return false;
        }
        catch (PrematureAbortException e) {
            ShopPlugin.sendMessage(player, e.getComponent());
            return true;
        }
    }

    /**
     * Sells a BaxEntry to a player
     * @param shopId the UUID of the shop this entry is a part of
     * @param buyer the player who is purchasing the item
     * @param seller the player who is selling the item
     * @param entry the BaxEntry that is being sold
     * @throws PrematureAbortException thrown if the seller has insufficient funds
     */
    public static void sellItem(@NotNull UUID shopId, @NotNull OfflinePlayer buyer, @NotNull OfflinePlayer seller, @NotNull BaxEntry entry) throws PrematureAbortException
    {
        sellItem(ShopPlugin.getShop(shopId), buyer, seller, entry);
    }

    /**
     * Sells a BaxEntry to a player
     * @param shop the shop this entry is a part of
     * @param buyer the player who is purchasing the item
     * @param seller the player who is selling the item
     * @param entry the BaxEntry that is being sold
     * @throws PrematureAbortException thrown if the seller has insufficient funds
     */
    public static void sellItem(BaxShop shop, @NotNull OfflinePlayer buyer, @NotNull OfflinePlayer seller, @NotNull BaxEntry entry) throws PrematureAbortException
    {
        double price = MathUtil.multiply(entry.getRefundPrice(), entry.getAmount());

        if (!buyer.equals(StoredPlayer.DUMMY) || ShopPlugin.getBaxConfig().hasDepositDummy()) { // don't charge the dummy
            if (!ShopPlugin.getEconomy().has(buyer, price)) {
                throw new CommandErrorException(Resources.NO_MONEY_SELLER);
            }
            ShopPlugin.getEconomy().withdrawPlayer(buyer, price);
        }
        ShopPlugin.getEconomy().depositPlayer(seller, price);

        if (shop == null) {
            DeletedShopClaim deletedClaim = new DeletedShopClaim(buyer, entry);
            ShopPlugin.sendNotification(buyer, deletedClaim);
        }
        else if (shop.hasFlagSellToShop()) {
            ItemStack item = entry.toItemStack();
            BaxEntry shopEntry = shop.find(item);
            if (shopEntry == null) {
                shopEntry = new BaxEntry();
                shopEntry.setItem(item);
            }
            if (!shop.hasFlagInfinite()) {
                shopEntry.add(item.getAmount());
            }
        }
        else {
            ItemStack item = entry.toItemStack();
            if (buyer.isOnline() && hasRoomForItem(buyer.getPlayer(), item)) {
                giveItem(buyer.getPlayer(), item, true);
            }
            else {
                SaleClaim claim = new SaleClaim(shop.getId(), buyer, seller, entry);
                ShopPlugin.sendNotification(buyer, claim);
            }
        }
    }

    /**
     * Sells a BaxEntry to a player
     * @param shopId the UUID of the shop this entry is a part of
     * @param buyer the UUID of the player who is purchasing the item
     * @param seller the UUID of the player who is selling the item
     * @param entry the BaxEntry that is being sold
     * @throws PrematureAbortException thrown if the seller has insufficient funds
     */
    public static void sellItem(@NotNull UUID shopId, @NotNull UUID buyer, @NotNull UUID seller, @NotNull BaxEntry entry) throws PrematureAbortException
    {
        sellItem(shopId, ShopPlugin.getOfflinePlayer(buyer), ShopPlugin.getOfflinePlayer(seller), entry);
    }

    /**
     * Removes a BaxQuantity from an inventory
     * @param qty the BaxQuantity to remove
     * @return a list of BaxEntries with the removed items
     * @throws PrematureAbortException thrown when 'any' is specified
     */
    public static @NotNull List<BaxEntry> takeQtyFromInventory(@NotNull BaxQuantity qty) throws PrematureAbortException
    {
        return takeQtyFromInventory(qty, null, Collections.emptyList());
    }

    /**
     * Removes a BaxQuantity from an inventory
     * @param qty the BaxQuantity to remove
     * @param shop the shop to use if 'any' is specified, or for the returned BaxEntry
     * @return a list of BaxEntries with the removed items
     * @throws PrematureAbortException thrown when any is specified and shop is null or inv is null
     */
    public static @NotNull List<BaxEntry> takeQtyFromInventory(@NotNull BaxQuantity qty, @Nullable BaxShop shop,
                                                               @Nullable List<BaxEntry> exclude) throws PrematureAbortException
    {
        if (qty.isAny()) {
            if (!(qty.getInventory() instanceof PlayerInventory))
                throw new CommandErrorException("'any' cannot be used for this action");
            return takeAnyFromInventory(shop, (PlayerInventory)qty.getInventory(), exclude);
        }

        BaxEntry clone = null;
        boolean smartStack = false;
        if (shop != null) {
            clone = shop.find(qty.getItem());
            smartStack = shop.hasFlagSmartStack();
        }
        if (clone == null) {
            clone = new BaxEntry(qty.getItem());
        }
        else {
            clone = new BaxEntry(clone);
        }
        clone.setAmount(takeFromInventory(qty.getInventory(), clone.getItemStack(), qty.getQuantity(), smartStack));
        return Collections.singletonList(clone);
    }

    /**
     * Gets the list of items that <i>would</i> be removed if takeQtyFromInventory() was run
     * @param qty the BaxQuantity to remove
     * @param shop the shop to use if 'any' is specified, or for the returned BaxEntry
     * @return a list of BaxEntries with the removed items
     * @throws PrematureAbortException thrown when any is specified and shop is null or inv is null
     */
    public static List<BaxEntry> peekQtyFromInventory(@NotNull BaxQuantity qty, BaxShop shop, @Nullable List<BaxEntry> exclude)
            throws PrematureAbortException
    {
        if (qty.isAny()) {
            if (!(qty.getInventory() instanceof PlayerInventory))
                throw new CommandErrorException("'any' cannot be used for this action");
            return peekAnyFromInventory(shop, (PlayerInventory)qty.getInventory(), exclude);
        }

        BaxEntry clone = null;
        boolean smartStack = false;
        if (shop != null) {
            clone = shop.find(qty.getItem());
            smartStack = shop.hasFlagSmartStack();
        }
        if (clone == null) {
            clone = new BaxEntry(qty.getItem());
        }
        else {
            clone = new BaxEntry(clone);
        }
        clone.setAmount(peekFromInventory(qty.getInventory(), clone.getItemStack(), qty.getQuantity(), smartStack));
        return Collections.singletonList(clone);
    }

    private static @NotNull List<BaxEntry> takeAnyFromInventory(@NotNull BaxShop shop, @NotNull PlayerInventory inv,
                                                                @Nullable List<BaxEntry> exclude)
    {
        ArrayList<BaxEntry> list = new ArrayList<>();

        if (exclude == null)
            exclude = Collections.emptyList();

        for (BaxEntry entry : shop) {

            if (exclude.contains(entry))
                continue;

            BaxEntry curr = new BaxEntry(entry);
            curr.setAmount(0);
            for (int x = 0; x < inv.getSize(); ++x) {
                ItemStack item = inv.getItem(x);
                if (item != null && curr.isSimilar(item, shop.hasFlagSmartStack())) {
                    curr.add(item.getAmount());
                    inv.setItem(x, null);
                }
            }
            if (curr.getAmount() > 0) {
                list.add(curr);
            }
        }

        return list;
    }

    private static @NotNull List<BaxEntry> peekAnyFromInventory(@NotNull BaxShop shop, @NotNull PlayerInventory inv,
                                                                @Nullable List<BaxEntry> exclude)
    {
        List<BaxEntry> list = new ArrayList<>();

        if (exclude == null)
            exclude = Collections.emptyList();

        for (BaxEntry entry : shop) {

            if (exclude.contains(entry))
                continue;

            BaxEntry curr = new BaxEntry(entry);
            curr.setAmount(0);
            for (int x = 0; x < inv.getSize(); ++x) {
                ItemStack item = inv.getItem(x);
                if (item != null && curr.isSimilar(item, shop.hasFlagSmartStack())) {
                    curr.add(item.getAmount());
                }
            }
            if (curr.getAmount() > 0) {
                list.add(curr);
            }
        }

        return list;
    }

    /**
     * Searches for an ItemStack and removes a specified amount from the Iterable. When the amount removes the entire
     * quantity of an ItemStack, the item is removed if the Iterable passed is an Inventory object or set to zero
     * @param inventory the Iterable containing the ItemStack inventory
     * @param item the item
     * @param amt the amount to remove
     * @return the actual quantity removed
     */
    public static int takeFromInventory(@NotNull Iterable<ItemStack> inventory, @NotNull ItemStack item,
                                        int amt, boolean smartStack)
    {
        int qty = 0;
        if (inventory instanceof Inventory) {
            Inventory inv = (Inventory)inventory;
            if (inv instanceof PlayerInventory) {
                ItemStack hand = LegacyPlayerUtil.getItemInHand((PlayerInventory)inv);
                if (ItemUtil.isSimilar(hand, item, smartStack)) {
                    if (amt < hand.getAmount()) {
                        hand.setAmount(hand.getAmount() - amt);
                        qty += amt;
                    }
                    else {
                        qty += hand.getAmount();
                        LegacyPlayerUtil.setItemInHand((PlayerInventory)inv, null);
                    }
                }
            }

            for (int x = 0; x < inv.getSize() && qty < amt; ++x) {
                ItemStack other = inv.getItem(x);
                if (other != null && ItemUtil.isSimilar(other, item, smartStack)) {
                    if (amt - qty < other.getAmount()) {
                        other.setAmount(other.getAmount() - (amt - qty));
                        qty = amt;
                    }
                    else {
                        qty += other.getAmount();
                        inv.setItem(x, null);
                    }
                }
            }
        }
        else {
            for(ItemStack invItem : inventory) {
                if (ItemUtil.isSimilar(invItem, item, smartStack)) {
                    if (amt - qty < invItem.getAmount()) {
                        invItem.setAmount(invItem.getAmount() - (amt - qty));
                        qty = amt;
                    }
                    else {
                        qty += invItem.getAmount();
                        invItem.setAmount(0);
                    }
                }
            }
        }

        return qty;
    }

    /**
     * Performs the same function as takeFromInventory() without removing any items
     * @param inventory the Iterable containing the ItemStack inventory
     * @param item the item
     * @param amt the amount to remove
     * @return the actual quantity removed
     */
    public static int peekFromInventory(@NotNull Iterable<ItemStack> inventory, @NotNull ItemStack item,
                                        int amt, boolean smartStack)
    {
        int qty = 0;
        for(ItemStack invItem : inventory) {
            if (ItemUtil.isSimilar(invItem, item, smartStack)) {
                if (amt - qty < invItem.getAmount()) {
                    qty = amt;
                }
                else {
                    qty += invItem.getAmount();
                }
            }
        }
        return qty;
    }

    public static ItemStack findInInventory(@NotNull Player player, List<ItemStack> items)
    {
        if (items.stream().anyMatch(LegacyPlayerUtil.getItemInHand(player)::isSimilar)) {
            return LegacyPlayerUtil.getItemInHand(player);
        }

        for(ItemStack itemStack : player.getInventory()) {
            return itemStack;
        }
        return null;
    }

    public static ItemStack findSign(@NotNull Player player) throws PrematureAbortException
    {
        ItemStack itemInHand = LegacyPlayerUtil.getItemInHand(player);
        if (ItemUtil.isSign(itemInHand) && !ItemUtil.isShop(itemInHand)) {
            return itemInHand;
        }

        Inventory inventory = player.getInventory();
        for (int idx = 0; idx < inventory.getSize(); ++idx) {
            ItemStack item = inventory.getItem(idx);
            if (ItemUtil.isSign(item) && !ItemUtil.isShop(item)) {
                return item;
            }
        }

        return null;
    }
}
