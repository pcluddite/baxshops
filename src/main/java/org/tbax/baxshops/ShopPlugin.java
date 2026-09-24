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

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.tbax.baxshops.commands.*;
import org.tbax.baxshops.items.ItemUtil;
import org.tbax.baxshops.notification.*;
import org.tbax.baxshops.serialization.BaxConfig;
import org.tbax.baxshops.serialization.State;
import org.tbax.baxshops.serialization.StateFile;
import org.tbax.baxshops.text.ChatComponent;
import org.tbax.baxshops.text.ClickEvent;
import org.tbax.baxshops.text.HoverEvent;
import org.tbax.bukkit.commands.BaxCommand;
import org.tbax.bukkit.errors.PrematureAbortException;
import org.tbax.bukkit.notification.Claimable;
import org.tbax.bukkit.notification.GeneralNotification;
import org.tbax.bukkit.notification.Notification;
import org.tbax.bukkit.notification.Request;
import org.tbax.bukkit.serialization.StoredPlayer;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class ShopPlugin extends JavaPlugin
{
    private static CommandMap commands;
    private static Economy econ;
    private static Logger log;
    private static State state;
    private static StateFile stateFile;

    private final static String[] RAW_COMMANDS = { "buy", "sell", "restock", "restockall" };

    /**
     * A map containing each player's currently selected shop and other
     * selection data
     */
    private final static Map<UUID, ShopSelection> selectedShops = new HashMap<>();

    private static CommandMap initCommands()
    {
        CommandMap commands = new CommandMap();
        try {
            commands.add(CmdAccept.class);
            commands.add(CmdAdd.class);
            commands.add(CmdAlphabetize.class);
            commands.add(CmdBackup.class);
            commands.add(CmdBuy.class);
            commands.add(CmdClaim.class);
            commands.add(CmdCopy.class);
            commands.add(CmdCreate.class);
            commands.add(CmdDelete.class);
            commands.add(CmdEmpty.class);
            commands.add(CmdFlag.class);
            commands.add(CmdHelp.class);
            commands.add(CmdInfo.class);
            commands.add(CmdList.class);
            commands.add(CmdLollipop.class);
            commands.add(CmdNotifications.class);
            commands.add(CmdPage.class);
            commands.add(CmdReject.class);
            commands.add(CmdReload.class);
            commands.add(CmdRemove.class);
            commands.add(CmdRestock.class);
            commands.add(CmdRestockFromInventory.class);
            commands.add(CmdSave.class);
            commands.add(CmdSelect.class);
            commands.add(CmdSell.class);
            commands.add(CmdSellFromInventory.class);
            commands.add(CmdSerial.class);
            commands.add(CmdSet.class);
            commands.add(CmdSetAmnt.class);
            commands.add(CmdSetAngle.class);
            commands.add(CmdSetDur.class);
            commands.add(CmdSetFoodLevel.class);
            commands.add(CmdSetIndex.class);
            commands.add(CmdSetOwner.class);
            commands.add(CmdSign.class);
            commands.add(CmdSignAll.class);
            commands.add(CmdSkip.class);
            commands.add(CmdTake.class);
            commands.add(CmdTeleport.class);
            return commands;
        }
        catch (IllegalAccessException | InstantiationException e) {
            log.log(Level.SEVERE, "Could not instantiate CommandMap", e);
            return null;
        }
    }

    public static Map<String, ShopCommand> getCommands()
    {
        return commands;
    }

    public static int showNotificationCount(Player player)
    {
        StoredPlayer storedPlayer = state.getOfflinePlayer(player.getUniqueId());
        Collection<Notification> notifications = storedPlayer.getNotifications();
        if (notifications.isEmpty()) {
            ShopPlugin.sendMessage(player, "You have no new notifications");
            return 0;
        }
        else {
            int size = notifications.size();
            ShopPlugin.sendMessage(player, String.format("You have %s notification%s", Format.number(size), size == 1 ? "" : "s"));
            return size;
        }
    }

    /**
     * Shows a player his/her most recent notification
     *
     * @param player the player
     */
    public static void showNotification(Player player)
    {
        StoredPlayer storedPlayer = state.getOfflinePlayer(player.getUniqueId());
        Notification n = storedPlayer.peekNote();
        if (n.getSentDate() == null) {
            sendInfo(player, n.getMessage(player));
        }
        else {
            sendInfo(player, String.format("%s[%tD %2$tl:%2$tM %2$tp]%s %s", ChatColor.DARK_GRAY,
                    n.getSentDate(), ChatColor.RESET, n.getMessage(player)));
        }
        if (n instanceof Request) {
            ChatComponent request = ChatComponent.of("Use ")
                    .append(ChatComponent.of(Format.command("/shop accept"))
                            .clickEvent(ClickEvent.runCommand("/shop accept"))
                            .hoverEvent(HoverEvent.showText(ChatColor.AQUA + "Accept this request")))
                    .append(" or ")
                    .append(ChatComponent.of(Format.command("/shop reject"))
                            .clickEvent(ClickEvent.runCommand("/shop reject"))
                            .hoverEvent(HoverEvent.showText(ChatColor.AQUA + "Decline this request")))
                    .append(" to manage this request.");
            request.sendTo(player);
        }
        else if (n instanceof Claimable) {
            ChatComponent request = ChatComponent.of("Use ")
                    .append(ChatComponent.of(Format.command("/shop claim"))
                            .clickEvent(ClickEvent.runCommand("/shop claim"))
                            .hoverEvent(HoverEvent.showText(ChatColor.AQUA + "Claim this item")))
                    .append(" to claim this and remove the notification.");
            request.sendTo(player);
        }
        else {
            storedPlayer.dequeueNote();
        }
    }

    /**
     * Sends a notification to a player.
     *
     * @param player the player
     * @param n the notification
     */
    public static void sendNotification(OfflinePlayer player, Notification n)
    {
        sendNotification(player, n, stateFile.getConfig().isLogNotes());
    }

    /**
     * Sends a notification to a player.
     *
     * @param player the player
     * @param n the notification
     * @param logNote should show it in the log
     */
    public static void sendNotification(OfflinePlayer player, Notification n, boolean logNote)
    {
        StoredPlayer storedPlayer = state.getOfflinePlayer(player.getUniqueId());
        if (logNote)
            log.info(Format.toAnsiColor(n.getMessage(null)));

        if (!StoredPlayer.DUMMY.equals(player))
            storedPlayer.queueNote(n);

        if (player.isOnline()) {
            if (storedPlayer.getNotificationCount() == 1) {
                showNotification(player.getPlayer());
            }
            else {
                showNotificationCount(player.getPlayer());
            }
        }
    }

    public static ShopSelection getSelection(OfflinePlayer player)
    {
        ShopSelection selected = selectedShops.get(player.getUniqueId());
        if (selected == null) {
            selected = new ShopSelection();
            selectedShops.put(player.getUniqueId(), selected);
        }
        return selected;
    }

    public static void clearSelection(OfflinePlayer player)
    {
        selectedShops.remove(player.getUniqueId());
        ShopPlugin.clearStaticData(player.getUniqueId());
    }

    public static void sendNotification(UUID playerId, Notification note)
    {
        sendNotification(state.getOfflinePlayer(playerId), note);
    }

    public static void addShop(BaxShop shop)
    {
        state.addShop(shop);
    }

    public static void removeShop(UUID shopId)
    {
        state.removeShop(shopId);
    }

    public static void removeLocation(UUID shopId, Location loc)
    {
        state.removeLocation(shopId, loc);
    }

    public static StoredPlayer getOfflinePlayer(UUID uuid)
    {
        return state.getOfflinePlayer(uuid);
    }

    public static BaxShop getShop(UUID shopId)
    {
        return state.getShop(shopId);
    }

    public static BaxShop getShopByShortId2(String shortId2)
    {
        return state.getShopByShortId2(shortId2);
    }

    @Deprecated
    public static BaxShop getShopByShortId(String shortId)
    {
        return state.getShopByShortId(shortId);
    }

    public static @NotNull List<StoredPlayer> getOfflinePlayerSafe(String playerName)
    {
        return state.getOfflinePlayerSafe(playerName);
    }

    public static @NotNull List<StoredPlayer> getOfflinePlayer(String playerName)
    {
        return state.getOfflinePlayer(playerName);
    }

    public static BaxShop getShop(Location location)
    {
        return state.getShop(location);
    }

    public static boolean addLocation(Location location, BaxShop shop)
    {
        return state.addLocation(shop, location);
    }

    public static boolean runCommand(ShopCmdActor actor)
    {
        if (actor.getNumArgs() == 0) {
            return false;
        }

        ShopCommand cmd = commands.get(actor.getAction());
        if (cmd == null) {
            actor.sendError(Resources.INVALID_SHOP_ACTION, actor.getAction());
            return true;
        }
        else if (cmd.useAlternative(actor)) {
            try {
                cmd = (ShopCommand)cmd.getAlternative().newInstance();
                actor.setAction(cmd.getAction());
            }
            catch (InstantiationException | IllegalAccessException | ClassCastException e) {
                e.printStackTrace();
            }
        }
        return runCommand(cmd, actor);
    }

    public static boolean runCommand(ShopCommand cmd, ShopCmdActor actor)
    {
        try {
            if (!cmd.hasValidArgCount(actor)) {
                actor.getSender().sendMessage(cmd.getHelp(actor).toString().split("\\n"));
                logPlayerMessage(actor.getPlayer(), "Command help was sent to player");
            }
            else if(!cmd.hasPermission(actor)) {
                actor.sendError("You do not have permission to use this command");
            }
            else if(cmd.requiresPlayer(actor) && actor.getPlayer() == null) {
                actor.sendError("You must be a player to use this command.");
            }
            else if(cmd.requiresSelection(actor) && actor.getShop() == null) {
                actor.sendError(Resources.NOT_FOUND_SELECTED);
            }
            else if (!cmd.allowsExclusion(actor) && actor.getExcluded() != null) {
                actor.sendError("You cannot exclude anything");
            }
            else if(cmd.requiresOwner(actor) && !(actor.isOwner() || actor.isAdmin())) {
                actor.sendError("You must be the owner of the shop to use /shop %s", actor.getAction());
            }
            else if(cmd.requiresItemInHand(actor) && actor.getItemInHand() == null) {
                actor.sendError("You need to be holding an item to perform this action");
            }
            else {
                cmd.onCommand(actor);
            }
        }
        catch(PrematureAbortException e) {
            sendInfo(actor.getSender(), e.getMessage());
        }
        return true;
    }

    public static Economy getEconomy()
    {
        return econ;
    }

    public static void sendInfo(@NotNull CommandSender sender, String message)
    {
        sendMessage(sender, message);
        if (stateFile.getConfig().isLogNotes() && sender instanceof Player) {
            logPlayerMessage((Player)sender, message);
        }
    }

    public static void sendInfo(@NotNull CommandSender sender, String[] message)
    {
        for (String line : message) {
            sendInfo(sender, line);
        }
    }

    public static void sendMessage(@NotNull CommandSender sender, String message)
    {
        sender.sendMessage(Format.wordWrap(message));
    }

    public static void logPlayerMessage(Player pl, String message)
    {
        StringBuilder sb = new StringBuilder();
        if (pl != null) {
            sb.append((char)27).append("[0;35m");
            sb.append("[");
            sb.append(pl.getName()).append("] ");
            sb.append((char)27);
            sb.append("[0m");
        }
        sb.append(Format.toAnsiColor(message));
        log.info(sb.toString());
    }

    public static State getState()
    {
        return state;
    }

    public static void logInfo(String msg)
    {
        log.info(Format.toAnsiColor(msg));
    }

    public static void logWarning(String msg)
    {
        log.warning(Format.toAnsiColor(msg));
    }

    public static void logSevere(String msg)
    {
        log.severe(Format.toAnsiColor(msg));
    }

    public static Collection<StoredPlayer> getRegisteredPlayers()
    {
        return state.getRegisteredPlayers();
    }

    public static void logIf(boolean condition, String message)
    {
        if (condition)
            logPossibleError(message);
    }

    public static void logPossibleError(String msg)
    {
        logWarning("Possible error: " + msg);
    }

    public static BaxConfig getBaxConfig()
    {
        return getStateFile().getConfig();
    }

    @Override
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        log = getLogger();
        stateFile = new StateFile(this);

        if (!enableVault()) {
            log.severe("BaxShops could not use this server's economy! Make sure Vault is installed!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if ((commands = initCommands()) == null) {
            log.severe("BaxShops failed to initialize its commands");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        loadConfigurationSerializable();
        ItemUtil.loadEnchants(this);

        saveDefaultConfig();
        try {
            state = State.readFromDisk(this);
        }
        catch (Exception e) {
            log.log(Level.SEVERE, "An exception occurred trying to read saved data. BaxShops cannot load.", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // run an initial save 5 minutes after starting, then a recurring save
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this,
                () -> stateFile.writeToDisk(getState()),
                6000L, stateFile.getConfig().getBackupInterval() * 1200L
        );

        log.info("BaxShops has loaded successfully!");
    }

    public void loadConfigurationSerializable()
    {
        ConfigurationSerialization.registerClass(BaxEntry.class, "tbax.baxshops.BaxEntry");
        ConfigurationSerialization.registerClass(BaxShop.class, "tbax.baxshops.BaxShop");
        ConfigurationSerialization.registerClass(BuyClaim.class, "tbax.baxshops.notification.BuyClaim");
        ConfigurationSerialization.registerClass(BuyNotification.class, "tbax.baxshops.notification.BuyNotification");
        ConfigurationSerialization.registerClass(BuyNotification.class, "org.tbax.baxshops.internal.notification.BuyNotification");
        ConfigurationSerialization.registerClass(BuyNotification.class, "org.tbax.baxshops.notification.internal.BuyNotification");
        ConfigurationSerialization.registerClass(BuyRejection.class, "tbax.baxshops.notification.BuyRejection");
        ConfigurationSerialization.registerClass(BuyRejection.class, "org.tbax.baxshops.internal.notification.BuyRejection");
        ConfigurationSerialization.registerClass(BuyRejection.class, "org.tbax.baxshops.notification.internal.BuyRejection");
        ConfigurationSerialization.registerClass(BuyRequest.class, "tbax.baxshops.notification.BuyRequest");
        ConfigurationSerialization.registerClass(BuyRequest.class, "org.tbax.baxshops.internal.notification.BuyRequest");
        ConfigurationSerialization.registerClass(BuyRequest.class, "org.tbax.baxshops.notification.internal.BuyRequest");
        ConfigurationSerialization.registerClass(DeletedShopClaim.class, "tbax.baxshops.notification.DeletedShopClaim");
        ConfigurationSerialization.registerClass(DeletedShopClaim.class, "org.tbax.baxshops.notification.internal.DeletedShopClaim");
        ConfigurationSerialization.registerClass(DeletedShopClaim.class, "org.tbax.baxshops.internal.notification.DeletedShopClaim");
        ConfigurationSerialization.registerClass(GeneralNotification.class, "org.tbax.baxshops.internal.notification.GeneralNotification");
        ConfigurationSerialization.registerClass(GeneralNotification.class, "org.tbax.baxshops.notification.internal.GeneralNotification");
        ConfigurationSerialization.registerClass(HeadlessShopClaim.class, "org.tbax.baxshops.internal.notification.HeadlessShopClaim");
        ConfigurationSerialization.registerClass(HeadlessShopClaim.class, "org.tbax.baxshops.notification.internal.HeadlessShopClaim");
        ConfigurationSerialization.registerClass(LollipopNotification.class, "tbax.baxshops.notification.LollipopNotification");
        ConfigurationSerialization.registerClass(LollipopNotification.class, "org.tbax.baxshops.internal.notification.LollipopNotification");
        ConfigurationSerialization.registerClass(LollipopNotification.class, "org.tbax.baxshops.notification.internal.LollipopNotification");
        ConfigurationSerialization.registerClass(NoteSet.class, "tbax.baxshops.notifications.NoteSet");
        ConfigurationSerialization.registerClass(SaleClaim.class, "tbax.baxshops.notification.SaleClaim");
        ConfigurationSerialization.registerClass(SaleClaim.class, "org.tbax.baxshops.internal.notification.SaleClaim");
        ConfigurationSerialization.registerClass(SaleClaim.class, "org.tbax.baxshops.notification.internal.SaleClaim");
        ConfigurationSerialization.registerClass(SaleNotification.class, "tbax.baxshops.notification.SaleNotification");
        ConfigurationSerialization.registerClass(SaleNotification.class, "org.tbax.baxshops.internal.notification.SaleNotification");
        ConfigurationSerialization.registerClass(SaleNotification.class, "org.tbax.baxshops.notification.internal.SaleNotification");
        ConfigurationSerialization.registerClass(SaleNotificationAuto.class, "tbax.baxshops.notification.SaleNotificationAuto");
        ConfigurationSerialization.registerClass(SaleNotificationAutoClaim.class, "tbax.baxshops.notification.SaleNotificationAutoClaim");
        ConfigurationSerialization.registerClass(SaleRejection.class, "tbax.baxshops.notification.SaleRejection");
        ConfigurationSerialization.registerClass(SaleRejection.class, "org.tbax.baxshops.internal.notification.SaleRejection");
        ConfigurationSerialization.registerClass(SaleRejection.class, "org.tbax.baxshops.notification.internal.SaleRejection");
        ConfigurationSerialization.registerClass(SaleRequest.class, "tbax.baxshops.notification.SaleRequest");
        ConfigurationSerialization.registerClass(SaleRequest.class, "org.tbax.baxshops.internal.notification.SaleRequest");
        ConfigurationSerialization.registerClass(SaleRequest.class, "org.tbax.baxshops.notification.internal.SaleRequest");
        ConfigurationSerialization.registerClass(SellRequest.class, "tbax.baxshops.notification.SellRequest");
        ConfigurationSerialization.registerClass(StoredPlayer.class, "tbax.baxshops.serialization.StoredPlayer");
        ConfigurationSerialization.registerClass(StoredPlayer.class, "org.tbax.baxshops.serialization.StoredPlayer");
    }

    @Override
    public void onDisable()
    {
        log.info("Saving BaxShops...");
        stateFile.writeToDisk(getState());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args)
    {
        ShopCmdActor actor = new ShopCmdActor(sender, command, args);
        adjustRawCommands(actor);
        return runCommand(actor);
    }

    private void adjustRawCommands(ShopCmdActor actor)
    {
        for(String rawCommand : RAW_COMMANDS) {
            if (rawCommand.equalsIgnoreCase(actor.getCommandName())) {
                actor.getArgs().add(0, new ShopCmdArg(actor, actor.getCommandName()));
                actor.setCommandName("shop");
                break;
            }
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
                                      @NotNull String[] args)
    {
        ShopCmdActor actor = new ShopCmdActor(sender, command, args);
        adjustRawCommands(actor);
        List<ShopCmdArg> argList = actor.getArgs();

        if (actor.getNumArgs() == 1) {
            return commands.entrySet().stream()
                .filter(c -> c.getKey().equals(c.getValue().getAction())
                        && c.getValue().hasPermission(actor)
                        && c.getKey().startsWith(actor.getArg(0).asString()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        }
        else if (actor.getNumArgs() > 1) {
            BaxCommand cmd = commands.get(actor.getArg(0).asString());
            if (cmd == null) return Collections.emptyList();
            String[] argWords = actor.getArg(actor.getNumArgs() - 1).asString().toLowerCase().split("_");
            String lastWord = argWords[argWords.length - 1];
            List<String> suggestions = cmd.onTabComplete(actor, command, alias, argList);
            List<String> filtered = new ArrayList<>(suggestions.size());
            for(String suggestion : suggestions) {
                if (Arrays.stream(suggestion.split("_")).anyMatch(word -> word.startsWith(lastWord))) {
                    filtered.add(suggestion);
                }
            }
            if (argWords.length > 1) {
                for (int i = filtered.size() - 1; i >= 0; --i) {
                    List<String> suggestionWords = Arrays.asList(filtered.get(i).split("_"));
                    boolean found = false;
                    for (int j = 0; j < argList.size() - 1 && !found; ++j) {
                        if (suggestionWords.contains(argWords[j])) {
                            found = true;
                        }
                    }
                    if (!found) {
                        filtered.remove(i);
                    }
                }
            }
            return filtered;
        }
        return Collections.emptyList();
    }

    /**
     * Sets up Vault.
     *
     * @return true on success, false otherwise
     */
    private boolean enableVault()
    {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        econ = rsp.getProvider();
        return true;
    }

    public static StateFile getStateFile()
    {
        return stateFile;
    }

    public static void clearStaticData(UUID playerId)
    {
        for(ShopCommand command : commands.values()) {
            command.clearStaticData(playerId);
        }
    }
}
