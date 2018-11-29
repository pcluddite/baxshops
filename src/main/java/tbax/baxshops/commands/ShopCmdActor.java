/** +++====+++
 *  
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/

package tbax.baxshops.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import tbax.baxshops.*;
import tbax.baxshops.errors.CommandErrorException;
import tbax.baxshops.errors.CommandMessageException;
import tbax.baxshops.errors.CommandWarningException;
import tbax.baxshops.errors.PrematureAbortException;
import tbax.baxshops.serialization.ItemNames;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class ShopCmdActor
{
    private final CommandSender sender;
    private final Command command;
    private final Main main;
    private Player player;
    private String name;
    private String action;
    
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
            player = (Player)sender;
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

    public Player getPlayer()
    {
        return player;
    }
    
    public Main getPlugin()
    {
        return main;
    }
    
    public boolean isAdmin()
    {
        return sender.hasPermission("shops.admin");
    }

    public boolean isOwner()
    {
        return getShop() != null && sender.equals(getShop().getOwner());
    }

    public boolean hasPermission(String perm)
    {
        if (perm == null)
            return true;
        return sender.hasPermission(perm);
    }
    
    public boolean cmdIs(String... names)
    {
        for(int x = 0; x < names.length; ++x) {
            if (name.equalsIgnoreCase(names[x]))
                return true;
        }
        return false;
    }

    public ShopSelection getSelection()
    {
        return Main.getSelection(player);
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
            throw new CommandErrorException(e, errMsg);
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

    public double getArgRoundedDouble(int index) throws PrematureAbortException
    {
        return Math.round(100d * getArgDouble(index)) / 100d;
    }

    public double getArgRoundedDouble(int index, String errMsg) throws PrematureAbortException
    {
        return Math.round(100d * getArgDouble(index, errMsg)) / 100d;
    }

    public double getArgDouble(int index) throws PrematureAbortException
    {
        return getArgDouble(index, String.format("Expecting argument %d to be a number", index));
    }

    public double getArgDouble(int index, String errMsg) throws PrematureAbortException
    {
        try {
            return Double.parseDouble(args[index]);
        }
        catch (NumberFormatException e) {
            throw new CommandErrorException(e, errMsg);
        }
    }

    public short getArgShort(int index) throws PrematureAbortException
    {
        return getArgShort(index, String.format("Expecting argument %d to be a small whole number", index));
    }

    public short getArgShort(int index, String errMsg) throws PrematureAbortException
    {
        try {
            return Short.parseShort(args[index]);
        }
        catch (NumberFormatException e) {
            throw new CommandErrorException(e, errMsg);
        }
    }

    public boolean getArgBoolean(int index) throws PrematureAbortException
    {
        return getArgBoolean(index, String.format("Expecting argument %d to be yes/no", index));
    }

    public boolean getArgBoolean(int index, String errMsg) throws PrematureAbortException
    {
        if ("true".equalsIgnoreCase(args[index]) || "false".equalsIgnoreCase(args[index]))
            return "true".equalsIgnoreCase(args[index]);
        if ("yes".equalsIgnoreCase(args[index]) || "no".equalsIgnoreCase(args[index]))
            return "yes".equalsIgnoreCase(args[index]);
        if ("1".equalsIgnoreCase(args[index]) || "0".equalsIgnoreCase(args[index]))
            return "1".equalsIgnoreCase(args[index]);
        throw new CommandErrorException(errMsg);
    }

    public BaxEntry getArgEntry(int index) throws PrematureAbortException
    {
        return getArgEntry(index, Resources.NOT_FOUND_SHOPITEM);
    }

    public BaxEntry getArgEntry(int index, String errMsg) throws PrematureAbortException
    {
        BaxEntry entry = null;
        if (isArgInt(index)) {
            index = getArgInt(1) - 1;
            if (index < getShop().getInventorySize() && index >= 0) {
                entry = getShop().getEntryAt(index);
            }
        }
        else {
            entry = ItemNames.getItemFromAlias(getArg(1), getShop());
        }

        if (entry == null) {
            exitError(errMsg);
        }
        return entry;
    }

    public Logger getLogger()
    {
        return main.getLogger();
    }
    
    public BaxShop getShop()
    {
        if (getSelection() != null)
            return getSelection().getShop();
        return null;
    }
    
    public void setCmdName(String name)
    {
        this.name = name;
    }
    
    public String getCmdName()
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
        appendArgs(arg);
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

    public void exitError(String format, Object... args) throws PrematureAbortException
    {
        throw new CommandErrorException(String.format(format, args));
    }

    public void sendError(String format, Object... args)
    {
        getSender().sendMessage(ChatColor.RED + String.format(format, args));
    }

    public void sendWarning(String format, Object... args)
    {
        getSender().sendMessage(ChatColor.GOLD + String.format(format, args));
    }

    public void exitWarning(String format, Object... args) throws PrematureAbortException
    {
        throw new CommandWarningException(String.format(format, args));
    }

    public void sendMessage(String format, Object... args)
    {
        getSender().sendMessage(String.format(format, args));
    }

    public void exitMessage(String format, Object... args) throws PrematureAbortException
    {
        throw new CommandMessageException(String.format(format, args));
    }

    public void logError(String format, Object... args)
    {
        getLogger().severe(String.format(format, args));
    }

    public void logWarning(String format, Object... args)
    {
        getLogger().warning(String.format(format, args));
    }

    public void logMessage(String format, Object... args)
    {
        getLogger().info(String.format(format, args));
    }

    public ItemStack getItemInHand()
    {
        if (player == null)
            return null;
        return player.getInventory().getItemInMainHand();
    }

    public List<BaxEntry> takeArgFromInventory(String arg) throws PrematureAbortException
    {
        List<BaxEntry> ret = new ArrayList<>();
        int qty;

        if ("any".equalsIgnoreCase(arg))
            return takeAnyFromInventory();

        if (getShop() == null)
            throw new CommandErrorException(Resources.NOT_FOUND_SELECTED);

        if (getItemInHand() == null || getItemInHand().getType() == Material.AIR)
            throw new CommandErrorException(Resources.NOT_FOUND_HELDITEM);

        BaxEntry clone = getShop().findEntry(getItemInHand()).clone();

        if ("all".equalsIgnoreCase(arg)) {
            qty = takeFromInventory(clone.getItemStack(), Integer.MAX_VALUE);
        }
        else if ("most".equalsIgnoreCase(arg)) {
            qty = takeFromInventory(clone.getItemStack(), getItemInHand().getAmount() - 1);
        }
        else {
            int amt = 0;
            try {
                amt = Integer.parseInt(arg);
            }
            catch (NumberFormatException e) {
                exitError("%s is not a valid quantity", arg);
            }
            qty = takeFromInventory(clone.getItemStack(), amt);
        }
        clone.setAmount(qty);
        ret.add(clone);
        return ret;
    }

    private List<BaxEntry> takeAnyFromInventory()
    {
        BaxShop shop = getShop();
        PlayerInventory inv;
        ArrayList<BaxEntry> list = new ArrayList<>();

        if (shop == null || player == null)
            return list;

        inv = player.getInventory();

        for (BaxEntry entry : shop) {
            BaxEntry curr = entry.clone();
            for (int x = 0; x < inv.getSize(); ++x) {
                ItemStack item = inv.getItem(x);
                if (curr.isSimilar(item)) {
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

    public int takeFromInventory(ItemStack item, int amt)
    {
        PlayerInventory inv = player.getInventory();
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

    public PlayerInventory getInventory() {
        if (player == null)
            return null;
        return player.getInventory();
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

    public void setArg(int index, Object value)
    {
        args[index] = value.toString();
    }

    public int giveItem(ItemStack item) throws PrematureAbortException
    {
        return PlayerUtil.giveItem(player, item);
    }

    public int giveItem(ItemStack item, boolean allOrNothing) throws PrematureAbortException
    {
        return PlayerUtil.giveItem(player, item, allOrNothing);
    }

    public int getSpaceForItem(ItemStack stack)
    {
        return PlayerUtil.getSpaceForItem(player, stack);
    }

    public boolean hasRoomForItem(ItemStack stack)
    {
        return PlayerUtil.hasRoomForItem(player, stack);
    }

    public boolean tryGiveItem(ItemStack stack)
    {
        return PlayerUtil.tryGiveItem(player, stack);
    }
}
