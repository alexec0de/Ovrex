package dev.ovrex.network.handler.impl;

import dev.ovrex.network.connection.MinecraftConnection;
import dev.ovrex.network.connection.PlayerConnection;
import dev.ovrex.network.handler.PacketHandler;
import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.impl.play.KeepAlivePacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.BiConsumer;

@Slf4j
@RequiredArgsConstructor
public class PlayHandler implements PacketHandler {

    private final PlayerConnection playerConnection;
    private final BiConsumer<PlayerConnection, Packet> packetForwarder;
    private final Runnable onDisconnectCallback;

    @Override
    public void handle(MinecraftConnection connection, Packet packet) {
        if (packet instanceof KeepAlivePacket) {
            if (playerConnection.hasBackend()) {
                playerConnection.getBackendConnection().sendPacket(packet);
            }
            return;
        }

        packetForwarder.accept(playerConnection, packet);
    }

    @Override
    public void onDisconnect(MinecraftConnection connection) {
        log.info("Player disconnected: {}", playerConnection.getUsername());
        onDisconnectCallback.run();
    }

    @Override
    public void onException(MinecraftConnection connection, Throwable cause) {
        log.warn("Exception in play handler for {}: {}", playerConnection.getUsername(), cause.getMessage());
    }
}
