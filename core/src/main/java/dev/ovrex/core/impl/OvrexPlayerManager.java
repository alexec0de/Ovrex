package dev.ovrex.core.impl;

import dev.ovrex.api.player.PlayerManager;
import dev.ovrex.api.player.ProxyPlayer;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class OvrexPlayerManager implements PlayerManager {

    private final Map<UUID, OvrexProxyPlayer> playersByUuid = new ConcurrentHashMap<>();
    private final Map<String, OvrexProxyPlayer> playersByName = new ConcurrentHashMap<>();

    public void addPlayer(OvrexProxyPlayer player) {
        playersByUuid.put(player.getUniqueId(), player);
        playersByName.put(player.getUsername().toLowerCase(), player);
    }

    public void removePlayer(OvrexProxyPlayer player) {
        playersByUuid.remove(player.getUniqueId());
        playersByName.remove(player.getUsername().toLowerCase());
        player.cleanup();
    }

    @Override
    public Optional<ProxyPlayer> getPlayer(String username) {
        return Optional.ofNullable(playersByName.get(username.toLowerCase()));
    }

    @Override
    public Optional<ProxyPlayer> getPlayer(UUID uuid) {
        return Optional.ofNullable(playersByUuid.get(uuid));
    }

    @Override
    public Collection<ProxyPlayer> getAllPlayers() {
        return Collections.unmodifiableCollection(playersByUuid.values());
    }

    @Override
    public int getPlayerCount() {
        return playersByUuid.size();
    }

    public boolean isOnline(String username) {
        return playersByName.containsKey(username.toLowerCase());
    }

    public boolean isOnline(UUID uuid) {
        return playersByUuid.containsKey(uuid);
    }
}
