package dev.ovrex.api.event.impl.player;

import dev.ovrex.api.event.Cancellable;
import dev.ovrex.api.event.Event;
import dev.ovrex.api.player.ProxyPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class PlayerConnectEvent extends Event implements Cancellable {

    private final ProxyPlayer player;

    @Setter
    private boolean cancelled;

    @Setter
    private String cancelReason = "Connection refused";

}
