/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.serialization.states;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.BaxShop;
import tbax.baxshops.BaxShopFlag;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.notification.*;
import tbax.baxshops.serialization.*;

import java.util.*;

public class State_30 implements StateLoader
{
    public static final double VERSION = 3.0;
    private static Map<Long, UUID> legacyIds = new HashMap<>();
    private static PlayerMap players = new PlayerMap();
    private ShopPlugin plugin;

    public State_30(@NotNull ShopPlugin plugin)
    {
        this.plugin = plugin;
    }

    public static int flagMapToFlag(SafeMap args)
    {
        int flags = BaxShopFlag.NONE;
        if (args.containsKey("buyRequests")) {
            flags = BaxShopFlag.setFlag(flags, BaxShopFlag.BUY_REQUESTS, args.getBoolean("buyRequests", false));
        }
        if (args.containsKey("infinite")) {
            flags = BaxShopFlag.setFlag(flags, BaxShopFlag.INFINITE, args.getBoolean("infinite", false));
        }
        if (args.containsKey("notify")) {
            flags = BaxShopFlag.setFlag(flags, BaxShopFlag.NOTIFY, args.getBoolean("notify", true));
        }
        if (args.containsKey("sellRequests")) {
            flags = BaxShopFlag.setFlag(flags, BaxShopFlag.SELL_REQUESTS, args.getBoolean("sellRequests", true));
        }
        if (args.containsKey("sellToShop")) {
            flags = BaxShopFlag.setFlag(flags, BaxShopFlag.SELL_TO_SHOP, args.getBoolean("sellToShop", false));
        }
        return flags;
    }

    public static UUID getShopId(long legacyId)
    {
        return legacyIds.get(legacyId);
    }

    public static OfflinePlayer getPlayer(String playerName)
    {
        if (playerName == null)
            return StoredPlayer.ERROR;
        return players.get(playerName).get(0);
    }

    public static UUID getPlayerId(String playerName)
    {
        if (playerName == null)
            return StoredPlayer.ERROR_UUID;
        return players.get(playerName).get(0).getUniqueId();
    }

    public static Collection<StoredPlayer> getPlayers()
    {
        return players.values();
    }

    public static void addLegacyShop(long legacyId, UUID id)
    {
        legacyIds.put(legacyId, id);
    }

    public static void invalidateMaps()
    {
        legacyIds.clear();
        players.clear();
        legacyIds = null;
        players = null;
    }

    @Override
    public @NotNull Configuration loadConfig(@NotNull FileConfiguration config)
    {
        Configuration ret = StateLoader.super.loadConfig(config);
        ret.setDeathTaxEnabled(config.contains("DeathTax"));
        if (ret.isDeathTaxEnabled()) {
            String goesTo = ret.getDeathTaxGoesTo();
            ret.setDeathTaxGoesTo(getPlayerId(goesTo).toString());
        }
        invalidateMaps();
        return ret;
    }

    @Override
    public @NotNull Collection<BaxShop> buildShops(@NotNull FileConfiguration state)
    {
        List<BaxShop> shops = new ArrayList<>();
        if (!state.isList("shops")) {
            return shops;
        }
        for (Object o : state.getList("shops")) {
            if (o instanceof BaxShop) {
                BaxShop shop = (BaxShop) o;
                addLegacyShop(shop.getLegacyId(), shop.getId());
                shops.add(shop);
            } else {
                plugin.getLogger().warning("Could not load BaxShop of type " + o.getClass());
            }
        }
        return shops;
    }

    @Override
    public @NotNull Collection<StoredPlayer> buildPlayers(@NotNull FileConfiguration state)
    {
        return getPlayers();
    }

    @Override
    public @NotNull Collection<NoteSet> buildNotifications(@NotNull FileConfiguration state)
    {
        List<NoteSet> noteSets = new ArrayList<>();
        if (!state.isConfigurationSection("notes")) {
            return noteSets;
        }
        for (Map.Entry entry : state.getConfigurationSection("notes").getValues(false).entrySet()) {
            OfflinePlayer player = getPlayer(entry.getKey().toString());
            if (!(entry.getValue() instanceof List)) {
                plugin.getLogger().warning("Could not load notifications of type " + entry.getValue().getClass());
            }
            else {
                Deque<Notification> pending = new ArrayDeque<>(((List) entry.getValue()).size());
                for (Object o : (List) entry.getValue()) {
                    if (o instanceof Notification) {
                        pending.add((Notification) o);
                    }
                    else if (o instanceof DeprecatedNote) {
                        pending.add(((DeprecatedNote) o).getNewNote());
                    }
                    else {
                        plugin.getLogger().warning("Could not load Notification of type " + entry.getValue().getClass());
                    }
                }
                if (StoredPlayer.DUMMY.equals(player)) {
                    Deque<Notification> errors = new ArrayDeque<>();
                    while (!pending.isEmpty()) {
                        Notification n = pending.remove();
                        if (n instanceof Claimable || n instanceof Request) {
                            errors.add(n);
                        }
                    }
                    if (!errors.isEmpty()) {
                        plugin.getLogger().warning("There is one or more claim or request notification assigned to the dummy player. " +
                            "These cannot be honored and will be assigned to an error user. The configuration file will need to be fixed manually.");
                        noteSets.add(new NoteSet(StoredPlayer.ERROR_UUID, errors));
                    }
                }
                else {
                    noteSets.add(new NoteSet(player.getUniqueId(), pending));
                }
            }
        }
        return noteSets;
    }

    @Override
    public @NotNull ShopPlugin getPlugin()
    {
        return plugin;
    }
}
