package dev.ovrex.core.impl;

import dev.ovrex.api.player.ProxyPlayer;
import dev.ovrex.api.server.BackendServer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@RequiredArgsConstructor
@Getter
public class OvrexBackendServer implements BackendServer {
    private final String name;
    private final InetSocketAddress address;
    private final String serverType;
    private final Set<ProxyPlayer> playerSet = ConcurrentHashMap.newKeySet();
    @Setter
    private volatile boolean available = true;

    @Override
    public Collection<ProxyPlayer> getPlayers() {
        return Collections.unmodifiableSet(playerSet);
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void addPlayer(ProxyPlayer player) {
        playerSet.add(player);
    }

    @Override
    public void removePlayer(ProxyPlayer player) {
        playerSet.remove(player);
    }
}
