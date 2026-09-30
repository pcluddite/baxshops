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
package org.tbax.baxshops.commands;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tbax.baxshops.BaxShop;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.Permissions;
import org.tbax.baxshops.ShopPlugin;
import org.tbax.bukkit.CommandHelp;
import org.tbax.bukkit.commands.CmdActor;
import org.tbax.bukkit.errors.PrematureAbortException;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CmdDelete extends ShopCommand
{
    // playerId: shopId
    private final Map<UUID, UUID> confirmationMap = new ConcurrentHashMap<>();

    @Override
    public @Nullable String getAction()
    {
        return "delete";
    }

    @Override
    public @NotNull String[] getAliases()
    {
        return new String[] { "del" };
    }

    @Override
    public String getPermission()
    {
        return Permissions.SHOP_OWNER;
    }

    @Override
    public @NotNull CommandHelp getHelp(@NotNull CmdActor actor)
    {
        CommandHelp help = new CommandHelp(this, "delete a shop");
        help.setLongDescription("Deletes the selected shop and turns it into a normal sign. Only owners and ops may do this. " +
                "If the shop has multiple locations, only that location will be deleted. If this is the only location of the shop, " +
                "all shop inventory must be removed prior to it being deleted. Infinite shops may be deleted without inventory being removed.");
        return help;
    }

    @Override
    public boolean hasValidArgCount(@NotNull CmdActor actor)
    {
        if (actor instanceof ShopCmdActor) {
            ShopCmdActor shopActor = ((ShopCmdActor)actor);
            if (needsToConfirm(shopActor.getPlayer(), shopActor.getShop())
                    || (actor.getNumArgs() == 2 && "all".equalsIgnoreCase(shopActor.getArg(1).asString()))) {
                return actor.getNumArgs() == 2;
            }
        }
        return actor.getNumArgs() == 1;
    }

    @Override
    public boolean requiresSelection(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresOwner(@NotNull ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresPlayer(@NotNull CmdActor actor)
    {
        return false;
    }

    @Override
    public boolean requiresItemInHand(@NotNull ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onShopCommand(@NotNull ShopCmdActor actor) throws PrematureAbortException
    {
        BaxShop shop = actor.getShop();
        Player player = actor.getPlayer();
        if (needsToConfirm(player, shop)) {
            String yesNo = actor.getArg(1).asEnum("yes", "no");
            if ("yes".equalsIgnoreCase(yesNo)) {
                if (ShopPlugin.getBaxConfig().hasBackupOnDelete()) {
                    ShopPlugin.getStateFile().writeToDisk(ShopPlugin.getState());
                    ShopPlugin.getStateFile().backup();
                }
                removeShop(actor, shop);
                actor.sendWarning("This shop has been deleted. If this is a mistake, try loading a backup.");
            }
            else {
                actor.sendMessage("Shop will not be deleted");
            }
            confirmationMap.remove(player.getUniqueId());
            ShopPlugin.clearSelection(actor.getPlayer());
        }
        else if (shop.getLocations().size() == 1) {
            if (shop.isEmpty()) {
                removeShop(actor, actor.getShop());
                actor.sendInfo("%s's shop has been deleted", Format.username(shop.getOwner().getName()));
                ShopPlugin.clearSelection(actor.getPlayer());
            }
            else {
                actor.sendError("This is the last location of this shop, and it still has inventory.");
                actor.sendError("Any inventory will not be recovered. Are you sure you want to delete it?");
                actor.sendInfo("Type %s to confirm or %s", Format.command("/shop delete yes"), Format.command("/shop delete no"));
                confirmationMap.put(player.getUniqueId(), shop.getId());
            }
        }
        else if (actor.getNumArgs() == 2 && "all".equalsIgnoreCase(actor.getArg(1).asString())) {
            actor.sendError("You are about to delete all locations of this shop");
            actor.sendError("Any inventory will not be recovered. Are you sure you want to delete it?");
            actor.sendInfo("Type %s to confirm or %s", Format.command("/shop delete yes"), Format.command("/shop delete no"));
            confirmationMap.put(player.getUniqueId(), shop.getId());
        }
        else {
            ShopPlugin.removeLocation(shop.getId(), actor.getSelection().getLocation());
            changeSignText(actor, actor.getSelection().getLocation());
            actor.sendInfo("This shop location has been closed.");
            ShopPlugin.clearSelection(actor.getPlayer());
        }
    }

    private void removeShop(ShopCmdActor actor, BaxShop shop)
    {
        for (Location loc : new ArrayList<>(shop.getLocations())) {
            changeSignText(actor, loc);
            ShopPlugin.removeLocation(shop.getId(), loc);
        }
        ShopPlugin.removeShop(shop.getId());
    }

    private void changeSignText(ShopCmdActor actor, Location loc)
    {
        BaxShop shop = actor.getShop();
        assert shop != null;
        try {
            Block b = loc.getBlock();
            Sign sign = (Sign)b.getState();
            sign.setLine(0, "This shop has");
            sign.setLine(1, "been closed by");
            sign.setLine(2, (shop.getOwner().equals(actor.getPlayer()) ? "the owner" : "an admin") + ".");
            sign.setLine(3, "");
            sign.update();
        }
        catch (NullPointerException | ClassCastException e) {
            actor.sendError("Unable to change the sign text at " + Format.location(loc));
        }
    }

    private boolean needsToConfirm(OfflinePlayer player, BaxShop shop)
    {
        if (player == null || shop == null)
            return false;
        UUID shopToConfirm = confirmationMap.get(player.getUniqueId());
        return shopToConfirm != null && shopToConfirm.equals(shop.getId());
    }

    @Override
    public void clearStaticData(UUID playerId)
    {
        confirmationMap.remove(playerId);
    }
}