package dev.ovrex.api.player;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface PlayerManager {
    Optional<ProxyPlayer> getPlayer(String username);

    Optional<ProxyPlayer> getPlayer(UUID uuid);

    Collection<ProxyPlayer> getAllPlayers();

    int getPlayerCount();
}
