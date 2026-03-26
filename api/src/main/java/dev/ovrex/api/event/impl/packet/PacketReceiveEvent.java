package dev.ovrex.api.event.impl.packet;

import dev.ovrex.api.event.Cancellable;
import dev.ovrex.api.event.Event;
import dev.ovrex.api.player.ProxyPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class PacketReceiveEvent extends Event implements Cancellable {
    private final ProxyPlayer player;
    private final int packetId;
    private final byte[] data;
    @Setter
    private boolean cancelled;
}
