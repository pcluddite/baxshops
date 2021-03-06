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

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PlayerMap implements Map<UUID, StoredPlayer>
{
    private Map<UUID, StoredPlayer> players = new HashMap<>();
    private Map<String, Set<UUID>> playerNames = new HashMap<>();
    private Map<UUID, UUID> survivorship = new HashMap<>();

    public PlayerMap()
    {
        put(StoredPlayer.DUMMY);
        put(StoredPlayer.ERROR);
    }

    public PlayerMap(Collection<? extends StoredPlayer> players)
    {
        for (StoredPlayer player : players) {
            if (player.isDummyUser() && !StoredPlayer.DUMMY_UUID.equals(player.getUniqueId())) {
                survivorship.put(player.getUniqueId(), StoredPlayer.DUMMY_UUID);
                StoredPlayer.DUMMY.queueAll(player.getNotifications());
            }
            else if (player.isErrorUser() && !StoredPlayer.ERROR_UUID.equals(player.getUniqueId())) {
                survivorship.put(player.getUniqueId(), StoredPlayer.ERROR_UUID);
                StoredPlayer.ERROR.queueAll(player.getNotifications());
            }
            else {
                put(player);
            }
        }
        put(StoredPlayer.DUMMY);
        put(StoredPlayer.ERROR);
    }

    @Override
    public int size()
    {
        return players.size();
    }

    @Override
    public boolean isEmpty()
    {
        return players.isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        if (key instanceof UUID)
            return containsKey((UUID)key);
        if (key instanceof String)
            return containsKey((String)key);
        throw new ClassCastException();
    }

    public boolean containsKey(UUID playerId)
    {
        return players.containsKey(playerId);
    }

    public boolean containsKey(String playerName)
    {
        return playerNames.containsKey(playerName);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return players.containsValue(value);
    }

    @Override
    public StoredPlayer get(Object key)
    {
        if (key instanceof UUID)
            return get((UUID)key);
        if (key instanceof String) {
            List<StoredPlayer> players = get((String) key);
            if (players.isEmpty())
                return null;
            if (players.size() > 1)
                throw new ClassCastException();
            return players.get(0);
        }
        throw new ClassCastException();
    }

    public StoredPlayer get(UUID playerId)
    {
        return players.get(survivorId(playerId));
    }

    public List<StoredPlayer> get(String playerName)
    {
        Set<UUID> uuids = playerNames.get(playerName);
        if (uuids == null) return Collections.emptyList();
        StoredPlayer[] ret = new StoredPlayer[uuids.size()];
        int x = 0;
        for (UUID uuid : uuids) {
            ret[x++] = get(uuid);
        }
        return Arrays.asList(ret);
    }

    public List<StoredPlayer> getOrCreate(String playerName)
    {
        List<StoredPlayer> storedPlayers = get(playerName);
        if (storedPlayers.isEmpty()) {
            StoredPlayer player = new StoredPlayer(playerName);
            Set<UUID> uuids = new HashSet<>();
            uuids.add(player.getUniqueId());
            players.put(player.getUniqueId(), player);
            playerNames.put(player.getName(), uuids);
            return Collections.singletonList(player);
        }
        else {
            return storedPlayers;
        }
    }

    @Override
    public StoredPlayer put(UUID key, StoredPlayer value)
    {
        if(key != null && value != null && !key.equals(value.getUniqueId()))
            throw new UnsupportedOperationException();
        StoredPlayer player = players.put(key, value);
        Set<UUID> uuids = playerNames.remove(value.getName());
        if (uuids == null) {
            uuids = new HashSet<>();
        }
        uuids.add(value.getUniqueId());
        playerNames.put(value.getName(), uuids);
        return player;
    }

    @Override
    public StoredPlayer remove(Object key)
    {
        if (key instanceof UUID)
            return remove((UUID)key);
        if (key instanceof String) {
            List<StoredPlayer> ret = remove((String) key);
            if (ret == null || ret.isEmpty())
                return null;
            return ret.get(0);
        }
        return null;
    }

    public StoredPlayer remove(UUID playerId)
    {
        StoredPlayer player = players.remove(playerId);
        if (player != null)
            playerNames.remove(player.getName());
        return player;
    }

    public List<StoredPlayer> remove(String playerName)
    {
        Set<UUID> uuids = playerNames.remove(playerName);
        if (uuids == null)
            return null;
        StoredPlayer[] ret = new StoredPlayer[uuids.size()];
        for(int x = 0; x < uuids.size(); ++x) {
            ret[x] = remove(get(x));
        }
        return Arrays.asList(ret);
    }

    @Override
    public void putAll(@NotNull Map<? extends UUID, ? extends StoredPlayer> m)
    {
        players.putAll(m);
        playerNames.clear();
        for(StoredPlayer value : players.values()) {
            Set<UUID> uuids = playerNames.remove(value.getName());
            if (uuids == null)
                uuids = new HashSet<>();
            uuids.add(value.getUniqueId());
            playerNames.put(value.getName(), uuids);
        }
    }

    @Override
    public void clear()
    {
        players.clear();
        playerNames.clear();
    }

    @NotNull
    @Override
    public Set<UUID> keySet()
    {
        return players.keySet();
    }

    @NotNull
    @Override
    public Collection<StoredPlayer> values()
    {
        return players.values();
    }

    @NotNull
    @Override
    public Set<Entry<UUID, StoredPlayer>> entrySet()
    {
        return players.entrySet();
    }

    /**
     * Find and convert any legacy players and return the old UUID
     * @param player the online player
     * @return the former UUID of the player
     */
    public UUID convertLegacy(Player player)
    {
        Set<UUID> uuids = playerNames.get(player.getName());
        if (uuids == null || uuids.isEmpty())
            return null;

        for (UUID legacyId : uuids) {
            StoredPlayer storedPlayer = get(legacyId);
            if (storedPlayer != null && storedPlayer.isLegacyPlayer()) {
                players.remove(legacyId);
                storedPlayer.convertLegacy(player);
                put(storedPlayer);
                survivorship.put(legacyId, storedPlayer.getUniqueId());
                return legacyId;
            }
        }        
        return null;
    }

    public StoredPlayer put(StoredPlayer storedPlayer)
    {
        return put(storedPlayer.getUniqueId(), storedPlayer);
    }

    public UUID survivorId(UUID playerId)
    {
        UUID id = survivorship.get(playerId);
        if (id == null)
            return playerId;
        return id;
    }
}
