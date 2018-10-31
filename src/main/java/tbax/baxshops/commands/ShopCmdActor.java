/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import tbax.baxshops.BaxShop;
import tbax.baxshops.Main;
import tbax.baxshops.ShopSelection;
import tbax.baxshops.executer.CmdRequisite;
import tbax.baxshops.serialization.StateFile;

import java.util.*;
import java.util.logging.Logger;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public final class ShopCmdActor
{
    private final CommandSender sender;
    private final Command command;
    private final Main main;
    private Player pl;
    private String name;
    private String action;
    private CmdRequisite requisite;
    
    private String[] args;
    
    public ShopCmdActor(Main main, CommandSender sender, Command command, String[] args)
    {
        this.main = main;
        this.sender = sender;
        this.command = command;
        this.args = new String[args.length];
        System.arraycopy(args, 0, this.args, 0, args.length);
        this.name = command.getName();
        if (sender instanceof Player) {
            pl = (Player)sender;
        }
    }
    
    public CommandSender getSender()
    {
        return sender;
    }
    
    public Command getCommand()
    {
        return command;
    }
    
    public StateFile getState()
    {
        return Main.getState();
    }
    
    public Player getPlayer()
    {
        return pl;
    }
    
    public Main getMain()
    {
        return main;
    }
    
    public boolean isAdmin()
    {
        return sender.hasPermission("shops.admin");
    }

    public boolean hasPermission(String perm)
    {
        return sender.hasPermission(perm);
    }
    
    public ShopSelection getSelection()
    {
        return main.selectedShops.get(pl);
    }
    
    public int getNumArgs()
    {
        return args.length;
    }
    
    public String getArg(int index)
    {
        return args[index];
    }

    public int getArgInt(int index) throws PrematureAbortException
    {
        return getArgInt(index, String.format("Expecting argument %d to be a whole number", index));
    }

    public int getArgInt(int index, String errMsg) throws PrematureAbortException
    {
        try {
            return Integer.parseInt(args[index]);
        }
        catch(NumberFormatException e) {
            throw new PrematureAbortException(e, errMsg);
        }
    }

    public boolean isArgInt(int index)
    {
        try {
            Integer.parseInt(args[index]);
            return true;
        }
        catch(NumberFormatException e) {
            return false;
        }
    }

    public boolean isArgDouble(int index)
    {
        try {
            Double.parseDouble(args[index]);
            return true;
        }
        catch(NumberFormatException e) {
            return false;
        }
    }

    public double getArgDouble(int index) throws PrematureAbortException
    {
        return getArgDouble(index, String.format("Expecting argument %d to be a number", index));
    }

    public double getArgDouble(int index, String errMsg) throws  PrematureAbortException
    {
        try {
            return Double.parseDouble(args[index]);
        }
        catch (NumberFormatException e) {
            throw new PrematureAbortException(e, errMsg);
        }
    }

    public Logger getLogger()
    {
        return main.getLogger();
    }
    
    public BaxShop getShop()
    {
        if (getSelection() != null)
            return getSelection().shop;
        return null;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {
        return name;
    }
    
    /**
     * Gets the first argument (if present) in lower case
     * @return 
     */
    public String getAction()
    {
        if (action == null) { // lazy initialization
            action = args.length > 0 ? args[0].toLowerCase() : "";
        }
        return action;
    }
    
    /**
     * Inserts a new first argument in the argument list
     * @param action the new first argument
     */
    public void insertAction(String action)
    {
        String[] newArgs = new String[args.length + 1];
        System.arraycopy(args, 0, newArgs, 1, args.length);
        newArgs[0] = action;
        args = newArgs;
    }
    
    
    /**
     * Appends an argument to the end of the argument list
     * @param arg 
     */
    public void appendArg(Object arg)
    {
        String[] newArgs = new String[args.length + 1];
        System.arraycopy(args, 0, newArgs, 0, args.length);
        newArgs[args.length] = arg.toString();
        args = newArgs;
    }

    public void appendArgs(Object... newArgs)
    {
        String[] allArgs = new String[args.length + newArgs.length];
        System.arraycopy(args, 0, allArgs, 0, args.length);
        for(int x = 0; x < newArgs.length; ++x) {
            allArgs[x + args.length] = newArgs[x].toString();
        }
        args = allArgs;
    }

    public void exitError(String formatStr, Object... args) throws PrematureAbortException
    {
        throw new PrematureAbortException(String.format(formatStr, args));
    }

    public void sendMessage(String format, Object... args)
    {
        getSender().sendMessage(String.format(format, args));
    }

    public ItemStack getItemInHand()
    {
        return pl.getInventory().getItemInMainHand();
    }

    public List<ItemStack> takeArgFromInventory(ItemStack item, String arg) throws PrematureAbortException
    {
        List<ItemStack> ret = new ArrayList<>();
        int qty;
        if ("all".equalsIgnoreCase(arg)) {
            qty = takeFromInventory(item, Integer.MAX_VALUE);
            ret.add(item.clone());
            ret.get(0).setAmount(qty);
        }
        else if ("most".equalsIgnoreCase(arg)) {

        }
        else if ("any".equalsIgnoreCase(arg)) {

        }
        else {
            int amt;
            try {
                amt = Integer.parseInt(arg);
            }
            catch (NumberFormatException e) {
                throw new PrematureAbortException(e, String.format("%s is not a valid quantity", arg));
            }
            qty = takeFromInventory(item, amt);
            ret.add(item.clone());
            ret.get(0).setAmount(qty);
        }
        return ret;
    }

    public int takeFromInventory(ItemStack item, int amt)
    {
        PlayerInventory inv = pl.getInventory();
        ItemStack hand = getItemInHand();
        int qty = 0;
        if (hand != null && hand.isSimilar(item)) {
            qty += hand.getAmount();
            if (hand.getAmount() < amt) {
                hand.setAmount(hand.getAmount() - amt);
            }
            else {
                inv.setItemInMainHand(null);
            }
        }

        for(int x = 0; x < inv.getSize() && qty < amt; ++x) {
            ItemStack other = inv.getItem(x);
            if (other != null && other.isSimilar(item)) {
                qty += other.getAmount();
                if (other.getAmount() < amt) {
                    other.setAmount(other.getAmount() - amt);
                }
                else {
                    inv.setItem(x, null);
                }
            }
        }

        return qty;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(name);
        for (String s : args) {
            sb.append(" ");
            sb.append(s);
        }
        return sb.toString();
    }
}
