package dev.ovrex.api.server;

import dev.ovrex.api.player.ProxyPlayer;

import java.net.InetSocketAddress;
import java.util.Collection;

public interface BackendServer {
    String getName();

    InetSocketAddress getAddress();

    Collection<ProxyPlayer> getPlayers();

    String getServerType();

    boolean isAvailable();
    void addPlayer(ProxyPlayer player);
    void removePlayer(ProxyPlayer player);
}
