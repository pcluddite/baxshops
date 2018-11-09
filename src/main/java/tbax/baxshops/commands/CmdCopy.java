/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
 **/

package tbax.baxshops.commands;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import tbax.baxshops.Main;
import tbax.baxshops.Resources;
import tbax.baxshops.help.CommandHelp;

public class CmdCopy extends BaxShopCommand
{
    @Override
    public String getName()
    {
        return "copy";
    }

    @Override
    public String getPermission()
    {
        return "shops.owner";
    }

    @Override
    public CommandHelp getHelp()
    {
        return new CommandHelp("shop copy", null, null, "Copies the shop using a sign from the player's inventory");
    }

    @Override
    public boolean hasValidArgCount(ShopCmdActor actor)
    {
        return actor.getNumArgs() == 1;
    }

    @Override
    public boolean requiresSelection(ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresOwner(ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresPlayer(ShopCmdActor actor)
    {
        return true;
    }

    @Override
    public boolean requiresItemInHand(ShopCmdActor actor)
    {
        return false;
    }

    @Override
    public void onCommand(ShopCmdActor actor) throws PrematureAbortException
    {
        if (!actor.isAdmin()) {
            PlayerInventory inv = actor.getPlayer().getInventory();
            ItemStack sign = new ItemStack(Material.SIGN, 1);
            if (!inv.containsAtLeast(sign, 1)) {
                actor.exitError("You need a sign to copy a shop.");
            }
            inv.removeItem(sign);
        }

        int i = Main.giveItem(actor.getPlayer(), actor.getSelection().toItem());
        if (i > 0) {
            actor.sendMessage(Resources.NO_ROOM);
            actor.getPlayer().getInventory().addItem(new ItemStack(Material.SIGN, 1));
        }
    }
}
