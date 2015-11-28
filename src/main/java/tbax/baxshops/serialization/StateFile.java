/* 
 * The MIT License
 *
 * Copyright © 2013-2015 Timothy Baxendale (pcluddite@hotmail.com) and 
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
package tbax.baxshops.serialization;

import tbax.baxshops.*;
import tbax.baxshops.notification.*;
import java.io.*;
import java.util.*;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 *
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 */
public final class StateFile {

    public static final String JSON_FILE_PATH = "shops.json";
    public static final String JSONBAK_FILE_PATH = "backups/%d.json";
    public static final String YAML_FILE_PATH = "shops.yml";
    public static final String YAMLBAK_FILE_PATH = "backups/%d.yml";
    
    public static final double STATE_VERSION = 3.0; // state file format version
    
    /**
     * A map of locations to their shop ids, accessed by their location in the world
     */
    public HashMap<Location, Integer> locations = new HashMap<>();
    
    /**
     * A map of ids map to their shops
     */
    public HashMap<Integer, BaxShop> shops = new HashMap<>();
    /**
     * A map containing each player's notifications
     */
    public HashMap<String, ArrayDeque<Notification>> pending = new HashMap<>();
    
    private final Logger log;
    private final Main main;
    
    public StateFile(Main main)
    {
        this.main = main;
        this.log = Main.log;
    }
    
    public BaxShop getShop(int uid)
    {
        return shops.get(uid);
    }
    
    public BaxShop getShop(Location loc)
    {
        Integer uid = locations.get(loc);
        if (uid == null) {
            return null;
        }
        return shops.get(uid);
    }
    
    public void load()
    {
        ItemNames.loadItemNames(main);
        ItemNames.loadDamageable(main);
        
        loadState();
        
        if (shops == null) {
            shops = new HashMap<>();
            log.warning("BaxShops could not load saved data. This is expected upon installation. If this is not BaxShop's first run, try restarting with one of the backups.");
        }
    }
    
    /**
     * Attempts to back up the shops.json save file.
     * @return a boolean indicating success
     */
    public boolean backup()
    {
        File stateLocation = new File(main.getDataFolder(), JSON_FILE_PATH);
        if (stateLocation.exists()) {
            long timestamp = new Date().getTime();
            File backupFolder = new File(main.getDataFolder(), "backups");
            if (!backupFolder.exists()) {
                backupFolder.mkdirs();
            }

            File[] backups = backupFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File f, String name) {
                    return name.endsWith(".yml");
                }
            });
            int b = main.getConfig().getInt("Backups", 15);
            if (b > 0 && backups.length >= b) {
                File delete = null;
                long oldest = Long.MAX_VALUE;
                for (File f : backups) {
                    String name = f.getName();
                    int i = name.indexOf('.');
                    try {
                        long compare = Long.parseLong(name.substring(0, i));
                        if (compare < oldest) {
                            oldest = compare;
                            delete = f;
                        }
                    } catch (NumberFormatException e) {
                    }
                }
                if (delete != null) {
                    delete.delete();
                }
            }

            try {
                File backup = new File(main.getDataFolder(), String.format(YAMLBAK_FILE_PATH, timestamp));
                OutputStream out;
                try (InputStream in = new FileInputStream(stateLocation)) {
                    out = new FileOutputStream(backup);
                    byte[] buf = new byte[1024];
                    int i;
                    while ((i = in.read(buf)) > 0) {
                        out.write(buf, 0, i);
                    }
                }
                out.close();
            } catch (FileNotFoundException e) {
                log.warning("Backup failed");
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                log.warning("Backup failed");
                e.printStackTrace();
                return false;
            }
            return true;
        }
        log.warning("Aborting backup: shops.yml not found");
        return false;
    }

    /*
    * Loads all shops from shop.json file
    */
    public boolean loadStateJson()
    {
        File stateLocation = new File(main.getDataFolder(), JSON_FILE_PATH);
        if (!stateLocation.exists()) {
            return false;
        }
        try {
            try (JsonReader jr = new JsonReader(new FileReader(stateLocation))) {
                JsonParser p = new JsonParser();
                JsonElement e = p.parse(jr);
                if (e.isJsonObject()) {
                    JsonObject full = e.getAsJsonObject();
                    double version = 0.0;
                    if (full.has("version")) {
                        version = full.get("version").getAsDouble();
                        log.info("State file is of version " + version);
                    }
                    else {
                        log.info("State file has no version.");
                    }
                    if (version != STATE_VERSION) {
                        log.info("This will be converted to version " + STATE_VERSION + " when saved.");
                    }
                    loadShops(version, full.get("shops").getAsJsonObject());
                    loadNotes(version, full.get("notes").getAsJsonObject());
                }
                else {
                    shops = null;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            shops = null;
        }
        return true;
    }
    
    private void loadShops(double version, JsonObject shopObject)
    {
        for(Map.Entry<String, JsonElement> entry : shopObject.entrySet()) {
            try {
                int uid = Integer.parseInt(entry.getKey());
                BaxShop shop = BaxShopDeserializer.deserialize(version, uid, entry.getValue().getAsJsonObject());
                shops.put(shop.uid, shop);
                for(Location loc : shop.getLocations()) {
                    locations.put(loc, shop.uid);
                }
            }
            catch(NumberFormatException e) {
                log.warning("Invalid shop id '" + entry.getKey() + "'! Shop has been skipped. If you believe this is an error, try loading a backup.");
            }
        }
    }
    
    private void loadNotes(double version, JsonObject noteObject)
    {
        for(Map.Entry<String, JsonElement> entry : noteObject.entrySet()) {
            ArrayDeque<Notification> notes = new ArrayDeque<>();
            if (entry.getValue().isJsonArray()) {
                for(JsonElement e : entry.getValue().getAsJsonArray()) {
                    if (e.isJsonObject()) {
                        Notification n = loadNote(version, e.getAsJsonObject());
                        if (n != null) {
                            notes.add(n);
                        }
                    }
                    else {
                        log.warning("Invalid notification type. Skipping.");
                    }
                }
            }
            pending.put(entry.getKey(), notes);
        }
    }
    
    private Notification loadNote(double version, JsonObject o)
    {
        switch(o.get("type").getAsString()) {
            case BuyClaim.TYPE_ID:
                return BuyClaim.fromJson(version, o);
            case BuyNotification.TYPE_ID:
                return BuyNotification.fromJson(version, o);
            case BuyRejection.TYPE_ID:
                return BuyRejection.fromJson(version, o);
            case BuyRequest.TYPE_ID:
                return BuyRequest.fromJson(version, o);
            case DeathNotification.TYPE_ID:
                return DeathNotification.fromJson(version, o);
            case GeneralNotification.TYPE_ID:
                return GeneralNotification.fromJson(version, o);
            case LollipopNotification.TYPE_ID:
                return LollipopNotification.fromJson(version, o);
            case SaleNotification.TYPE_ID:
                return SaleNotification.fromJson(version, o);
            case SaleNotificationAuto.TYPE_ID:
                return SaleNotificationAuto.fromJson(version, o);
            case SaleRejection.TYPE_ID:
                return SaleRejection.fromJson(version, o);
            case SellRequest.TYPE_ID:
                return SellRequest.fromJson(version, o);
            default:
                log.warning("Unknown message type '" + o.get("type").getAsString() + "'. Skipped.");
                break;
        }
        return null;
    }
    
    /*
     * Loads all shops from shop.yml file
     */
    public boolean loadState()
    {
        File stateLocation = new File(main.getDataFolder(), YAML_FILE_PATH);
        if (!stateLocation.exists()) {
            log.info("YAML file did not exist. Trying to load JSON...");
            boolean ret = loadStateJson();
            if (ret) {
                File jsonFile = new File(main.getDataFolder(), JSON_FILE_PATH);
                jsonFile.delete();
            }
            else {
                log.info("JSON did not exist. Assuming new configuration.");
            }
            return ret;
        }
        FileConfiguration state = YamlConfiguration.loadConfiguration(stateLocation);
        ArrayList<BaxShop> shoplist = (ArrayList)state.getList("shops");
        for(BaxShop shop : shoplist) {
            addShop(null, shop);
        }
        ConfigurationSection yNotes = state.getConfigurationSection("notes");
        for(Map.Entry<String, Object> player : yNotes.getValues(false).entrySet()) {
            ArrayDeque<Notification> playerNotes = new ArrayDeque<>();
            List yPlayerNotes = (List)player.getValue();
            for(Object yNote : yPlayerNotes) {
                playerNotes.add((Notification)yNote);
            }
            pending.put(player.getKey(), playerNotes);
        }
        return true;
    }
    
    public boolean addShop(Player pl, BaxShop shop)
    {
        if (shop.uid < 0) {
            shop.uid = getUniqueId();
        }
        for(Location loc : shop.getLocations()) {
            if (!addLocation(pl, loc, shop)) {
                return false;
            }
        }
        shops.put(shop.uid, shop);
        return true;
    }
    
    public boolean addLocation(Player pl, Location loc, BaxShop shop)
    {
        Integer otherUid = locations.get(loc);
        if (otherUid == null) {
            locations.put(loc, shop.uid);
        }
        else if (otherUid != shop.uid) {
            Main.sendError(pl, "You can't create a new shop here! Another shop already exists on this block!");
            return false;
        }
        return true;
    }
    
    public boolean removeShop(Player pl, BaxShop shop)
    {
        for(Location loc : shop.getLocations()) {
            removeLocation(pl, loc);
        }
        pl.sendMessage(String.format("%s's shop has been deleted.", Format.username(shop.owner)));
        return true;
    }
    
    public boolean removeLocation(Player pl, Location loc)
    {
        Integer uid = locations.get(loc);
        if (uid != null) {
            BaxShop shop = shops.get(uid);
            Block b = loc.getBlock();
            Sign sign = (Sign) b.getState();
            sign.setLine(0, Resources.SIGN_CLOSED[0]);
            sign.setLine(1, Resources.SIGN_CLOSED[1]);
            sign.setLine(2, (pl.getName().equals(shop.owner) ? "the owner" : "an admin") + ".");
            sign.setLine(3, "");
            sign.update();
            shop.removeLocation(loc);
            locations.remove(loc);
            if (shop.getLocations().isEmpty()) {
               shops.remove(shop.uid); 
            }
        }
        return true;
    }
    
    /**
     * Gets a list of notifications for a player.
     *
     * @param pl the player
     * @return the player's notifications
     */
    public ArrayDeque<Notification> getNotifications(Player pl)
    {
        return getNotifications(pl.getName());
    }
	
    /**
     * Gets a list of notifications for a player.
     *
     * @param player the player
     * @return the player's notifications
     */
    public ArrayDeque<Notification> getNotifications(String player)
    {
        ArrayDeque<Notification> n = pending.get(player);
        if (n == null) {
            n = new ArrayDeque<>();
            pending.put(player, n);
        }
        return n;
    }
    
    /**
     * Shows a player his/her most recent notification. Also shows the
     * notification count.
     *
     * @param pl the player
     */
    public void showNotification(Player pl)
    {
        showNotification(pl, true);
    }

    /**
     * Shows a player his/her most recent notification.
     *
     * @param pl the player
     * @param showCount whether the notification count should be shown as well
     */
    public void showNotification(Player pl, boolean showCount)
    {
        ArrayDeque<Notification> notifications = getNotifications(pl);
        if (notifications.isEmpty()) {
            if (showCount) {
                pl.sendMessage("You have no notifications.");
            }
            return;
        }
        if (showCount) {
            int size = notifications.size();
            pl.sendMessage(String.format("You have %s %s.", Format.number(size), size == 1 ? "notification" : "notifications"));
        }

        Notification n = notifications.getFirst();
        pl.sendMessage(n.getMessage(pl));
        if (n instanceof BuyClaim) {
            if (((Claimable)n).claim(pl)) {
                notifications.removeFirst();
                return;
            }
        }
        if (n instanceof Request) {
            pl.sendMessage("Use " + Format.command("/shop accept") + " or " + Format.command("/shop reject") + " to manage this request.");
        } 
        else if (n instanceof Claimable) {
            pl.sendMessage("Use " + Format.command("/shop claim") + " to claim and remove this notification.");
        } 
        else {
            notifications.removeFirst();
        }
    }

    /**
     * Sends a notification to a player.
     *
     * @param pl the player
     * @param n the notification
     */
    public void sendNotification(Player pl, Notification n)
    {
        sendNotification(pl.getName(), n);
    }
    
    /**
     * Sends a notification to a player.
     *
     * @param player the player
     * @param n the notification
     */
    public void sendNotification(String player, Notification n)
    {
        sendNotification(player, n, main.getConfig().getBoolean("LogNotes"));
    }

    /**
     * Sends a notification to a player.
     *
     * @param player the player
     * @param n the notification
     * @param log_it should show it in the log
     */
    public void sendNotification(String player, Notification n, boolean log_it)
    {
        ArrayDeque<Notification> ns = getNotifications(player);
        if (log_it) {
            log.info(Main.toAnsiColor(n.getMessage(null)));
        }
        ns.add(n);
        Player pl = main.getServer().getPlayer(player);
        if (pl != null && pl.isOnline()) {
            showNotification(pl, false);
        }
    }
    
    private int getUniqueId()
    {
        int current = 1;
        while(shops.containsKey(current)) {
            ++current;
        }
        return current;
    }
	
    /**
     * Saves all shops as json
     */
    public void saveAllJson()
    {
        if (!backup()) {
            log.warning("Failed to back up BaxShops");
        }

        JsonObject state = new JsonObject();
        state.addProperty("version", STATE_VERSION);
        
        int errors = 0;
        
        JsonObject jShops = new JsonObject();
        for (Map.Entry<Integer, BaxShop> entry : shops.entrySet()) {
            try {
                BaxShop shop = entry.getValue();
                jShops.add(shop.uid + "", BaxShopSerializer.serialize(STATE_VERSION, shop));
            }
            catch(Exception e) { // A mortal sin, yes, but if an exception is thrown for one, the whole thing won't be saved.
                log.warning("A shop could not be saved due to the following error: " + e.getClass().getName());
                ++errors;
            }
        }
        state.add("shops", jShops);
        
        JsonObject jPending = new JsonObject();
        for(Map.Entry<String, ArrayDeque<Notification>> entry : pending.entrySet()) {
            JsonArray notes = new JsonArray();
            for(Notification n : entry.getValue()) {
                try {
                    notes.add(n.toJson(STATE_VERSION));
                }
                catch (Exception e) {
                    log.warning("A notification could not be saved due to the following error: " + e.getClass().getName());
                    ++errors;
                }
            }
            jPending.add(entry.getKey(), notes);
        }
        state.add("notes", jPending);
        
        if (errors > 0) {
            log.warning(String.format("BaxShops encountered %d error(s) when converting the shops and notifications to JSON. You may wish to load a backup next time.", errors));
        }
        
        try {
            File dir = main.getDataFolder();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File f = new File(dir, "shops.json");
            try (FileWriter fw = new FileWriter(f)) {
                fw.write(state.toString());
            }
        } catch (FileNotFoundException e) {
            log.warning("Save failed");
            e.printStackTrace();
        } catch (IOException e) {
            log.warning("Save failed");
            e.printStackTrace();
        }
    }
    
    /**
     * Saves all shops
     */
    public void saveAll()
    {
        if (!backup()) {
            log.warning("Failed to back up BaxShops");
        }

        FileConfiguration state = new YamlConfiguration();
        state.set("version", STATE_VERSION);
        
        state.set("shops", new ArrayList(shops.values()));
        
        HashMap<String, List<String>> yNotes = new HashMap<>();
        for(Map.Entry<String, ArrayDeque<Notification>> player : pending.entrySet()) {
            yNotes.put(player.getKey(), new ArrayList(player.getValue()));
        }
        state.createSection("notes", yNotes);
        
        try {
            File dir = main.getDataFolder();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            state.save(new File(dir, "shops.yml"));
        } catch (IOException e) {
            log.warning("Save failed");
            e.printStackTrace();
        }
    }
}
