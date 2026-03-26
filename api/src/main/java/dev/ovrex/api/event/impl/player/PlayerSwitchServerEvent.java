package dev.ovrex.api.event.impl.player;

import dev.ovrex.api.event.Event;
import dev.ovrex.api.player.ProxyPlayer;
import dev.ovrex.api.server.BackendServer;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PlayerSwitchServerEvent extends Event {

    private final ProxyPlayer player;
    private final BackendServer previousServer;
    private final BackendServer newServer;

}
