package dev.ovrex.api.event.impl.player;

import dev.ovrex.api.event.Event;
import dev.ovrex.api.player.ProxyPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlayerDisconnectEvent extends Event {

    private final ProxyPlayer player;

}
