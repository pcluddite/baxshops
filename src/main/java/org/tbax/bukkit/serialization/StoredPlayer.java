/*
 * Copyright (C) Timothy Baxendale
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
package org.tbax.bukkit.serialization;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tbax.baxshops.ShopPlugin;
import org.tbax.baxshops.versioning.LegacyOfflinePlayer;
import org.tbax.bukkit.notification.Notification;
import org.tbax.bukkit.serialization.annotations.SerializeNonNull;
import org.tbax.bukkit.serialization.annotations.SerializedAs;
import org.tbax.baxshops.serialization.BaxConfig;
import org.tbax.baxshops.serialization.UpgradeableSerializable;
import org.tbax.baxshops.serialization.UpgradeableSerialization;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class StoredPlayer extends LegacyOfflinePlayer implements UpgradeableSerializable
{
    public static final UUID DUMMY_UUID = ShopPlugin.getBaxConfig().getDummyId();
    public static final String DUMMY_NAME = ShopPlugin.getBaxConfig().getDummyName();
    public static final StoredPlayer DUMMY = new StoredPlayer(DUMMY_NAME, DUMMY_UUID, "WORLD");

    public static final UUID ERROR_UUID = ShopPlugin.getBaxConfig().getErrorId();
    public static final String ERROR_NAME = ShopPlugin.getBaxConfig().getErrorName();
    public static final StoredPlayer ERROR = new StoredPlayer(ERROR_NAME, ERROR_UUID, "ERROR");

    private UUID uuid = UUID.randomUUID();
    private Deque<Notification> notifications = new ArrayDeque<>();

    @SerializedAs("legacy")
    private boolean legacyPlayer;

    @SerializedAs("name")
    private String lastSeenName;

    @SerializeNonNull
    private String special;

    public StoredPlayer(String name, UUID uuid)
    {
        this.uuid = uuid;
        this.lastSeenName = name;
    }

    public StoredPlayer(Player player)
    {
        legacyPlayer = false;
        uuid = player.getUniqueId();
        lastSeenName = player.getName();
    }

    public StoredPlayer(String name)
    {
        legacyPlayer = true;
        lastSeenName = name;
    }

    private StoredPlayer(String name, UUID uuid, String special)
    {
        this.uuid = uuid;
        this.lastSeenName = name;
        this.special = special;
    }

    public StoredPlayer(Map<String, Object> args)
    {
        UpgradeableSerialization.upgrade(this, args);
    }

    @Override
    public void upgrade00400(@NotNull SafeMap map)
    {
        uuid = map.getUUID("uuid", UUID.randomUUID());
        lastSeenName = map.getString("name", uuid.toString());
        legacyPlayer = map.getBoolean("legacy", true);
    }

    @Override
    public void upgrade00480(@NotNull SafeMap map)
    {
        uuid = map.getUUID("uuid", UUID.randomUUID());
        lastSeenName = map.getString("name", uuid.toString());
        legacyPlayer = map.getBoolean("legacy", true);
        notifications = map.getDeque("notifications", new ArrayDeque<>());
        if (BaxConfig.DEFAULT_DUMMY_ID.equals(uuid)) {
            special = "WORLD";
        }
        else if (BaxConfig.DEFAULT_ERROR_ID.equals(uuid)) {
            special = "ERROR";
        }
    }

    public OfflinePlayer getOfflinePlayer()
    {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player.isOnline()) {
            lastSeenName = player.getPlayer().getName();
        }
        return player;
    }

    public boolean isLegacyPlayer()
    {
        return legacyPlayer;
    }

    public void convertLegacy(Player player)
    {
        this.legacyPlayer = false;
        uuid = player.getUniqueId();
        lastSeenName = player.getName();
    }

    @Override
    public boolean isOnline()
    {
        return getOfflinePlayer().isOnline();
    }

    @Override
    public String getName()
    {
        if (getOfflinePlayer().isOnline()) {
            return lastSeenName = getOfflinePlayer().getPlayer().getName();
        }
        else {
            if (lastSeenName == null)
                lastSeenName = ERROR_NAME;
            return lastSeenName;
        }
    }

    @Override
    public @NotNull UUID getUniqueId()
    {
        if (uuid == null)
            uuid = ERROR_UUID;
        return uuid;
    }

    @Override
    public @NotNull PlayerProfile getPlayerProfile()
    {
        return getOfflinePlayer().getPlayerProfile();
    }

    public Collection<Notification> getNotifications()
    {
        return Collections.unmodifiableCollection(notifications);
    }

    public void queueNote(Notification notification)
    {
        notification.setRecipient(this);
        notifications.add(notification);
    }

    public Notification dequeueNote()
    {
        return notifications.removeFirst();
    }

    public Notification peekNote()
    {
        return notifications.peekFirst();
    }

    public int getNotificationCount()
    {
        return notifications.size();
    }

    public boolean hasNotes()
    {
        return !notifications.isEmpty();
    }

    public void clearNotes()
    {
        notifications.clear();
    }

    @Override
    public boolean isBanned()
    {
        return getOfflinePlayer().isBanned();
    }

    @Override
    public @Nullable BanEntry<PlayerProfile> ban(@Nullable String reason, @Nullable Date expires, @Nullable String source)
    {
        return getOfflinePlayer().ban(reason, expires, source);
    }

    @Override
    public @Nullable BanEntry<PlayerProfile> ban(@Nullable String reason, @Nullable Instant expires, @Nullable String source)
    {
        return getOfflinePlayer().ban(reason, expires, source);
    }

    @Override
    public @Nullable BanEntry<PlayerProfile> ban(@Nullable String reason, @Nullable Duration duration, @Nullable String source)
    {
        return getOfflinePlayer().ban(reason, duration, source);
    }

    @Override
    public boolean isWhitelisted()
    {
        return getOfflinePlayer().isWhitelisted();
    }

    @Override
    public void setWhitelisted(boolean value)
    {
        getOfflinePlayer().setWhitelisted(value);
    }

    @Override
    public Player getPlayer()
    {
        return getOfflinePlayer().getPlayer();
    }

    @Override
    public long getFirstPlayed()
    {
        return getOfflinePlayer().getFirstPlayed();
    }

    @Override
    public long getLastPlayed()
    {
        return getOfflinePlayer().getLastPlayed();
    }

    @Override
    public boolean hasPlayedBefore()
    {
        return getOfflinePlayer().hasPlayedBefore();
    }

    @Override
    public Location getBedSpawnLocation()
    {
        return getOfflinePlayer().getBedSpawnLocation();
    }

    @Override
    public @Nullable Location getRespawnLocation()
    {
        return getOfflinePlayer().getRespawnLocation();
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic) throws IllegalArgumentException
    {
        getOfflinePlayer().incrementStatistic(statistic);
    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic) throws IllegalArgumentException
    {
        getOfflinePlayer().decrementStatistic(statistic);
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, int amount) throws IllegalArgumentException
    {
        getOfflinePlayer().incrementStatistic(statistic, amount);
    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, int amount) throws IllegalArgumentException
    {
        getOfflinePlayer().decrementStatistic(statistic, amount);
    }

    @Override
    public void setStatistic(@NotNull Statistic statistic, int newValue) throws IllegalArgumentException
    {
        getOfflinePlayer().setStatistic(statistic, newValue);
    }

    @Override
    public int getStatistic(@NotNull Statistic statistic) throws IllegalArgumentException
    {
        return 0;
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull Material material) throws IllegalArgumentException
    {
        getOfflinePlayer().incrementStatistic(statistic, material);
    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull Material material) throws IllegalArgumentException
    {
        getOfflinePlayer().decrementStatistic(statistic, material);
    }

    @Override
    public int getStatistic(@NotNull Statistic statistic, @NotNull Material material) throws IllegalArgumentException
    {
        return getOfflinePlayer().getStatistic(statistic, material);
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull Material material, int amount) throws IllegalArgumentException
    {
        getOfflinePlayer().incrementStatistic(statistic, material, amount);
    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull Material material, int amount) throws IllegalArgumentException
    {
        getOfflinePlayer().decrementStatistic(statistic, material, amount);
    }

    @Override
    public void setStatistic(@NotNull Statistic statistic, @NotNull Material material, int newValue) throws IllegalArgumentException
    {
        getOfflinePlayer().setStatistic(statistic, material, newValue);
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) throws IllegalArgumentException
    {
        getOfflinePlayer().incrementStatistic(statistic, entityType);
    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) throws IllegalArgumentException
    {
        getOfflinePlayer().decrementStatistic(statistic, entityType);
    }

    @Override
    public int getStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType) throws IllegalArgumentException
    {
        return getOfflinePlayer().getStatistic(statistic, entityType);
    }

    @Override
    public void incrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int amount) throws IllegalArgumentException
    {
        getOfflinePlayer().incrementStatistic(statistic, entityType, amount);
    }

    @Override
    public void decrementStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int amount)
    {
        getOfflinePlayer().decrementStatistic(statistic, entityType, amount);
    }

    @Override
    public void setStatistic(@NotNull Statistic statistic, @NotNull EntityType entityType, int newValue)
    {
        getOfflinePlayer().setStatistic(statistic, entityType, newValue);
    }

    @Override
    public @Nullable Location getLastDeathLocation()
    {
        return getOfflinePlayer().getLastDeathLocation();
    }

    @Override
    public @Nullable Location getLocation()
    {
        return getOfflinePlayer().getLocation();
    }

    @Override
    public boolean isOp()
    {
        return getOfflinePlayer().isOp();
    }

    @Override
    public void setOp(boolean value)
    {
        getOfflinePlayer().setOp(value);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj instanceof OfflinePlayer)
            return equals((OfflinePlayer) obj);
        return false;
    }

    public boolean equals(OfflinePlayer player)
    {
        if (player == null)
            return false;
        return uuid.equals(player.getUniqueId());
    }

    @Override
    public String toString()
    {
        return getName();
    }

    public static StoredPlayer deserialize(Map<String, Object> args)
    {
        return new StoredPlayer(args);
    }

    public static StoredPlayer valueOf(Map<String, Object> args)
    {
        return deserialize(args);
    }

    public String getSpecial()
    {
        return special;
    }

    public boolean isSpecial()
    {
        return special != null;
    }

    public boolean isErrorUser()
    {
        return "ERROR".equals(getSpecial());
    }

    public boolean isDummyUser()
    {
        return "WORLD".equals(getSpecial());
    }

    public void queueAll(Collection<? extends Notification> notifications)
    {
        for(Notification n : notifications) {
            queueNote(n);
        }
    }
}
