package dev.ovrex.network.handler.impl;

import dev.ovrex.network.connection.ConnectionState;
import dev.ovrex.network.connection.MinecraftConnection;
import dev.ovrex.network.handler.PacketHandler;
import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.enums.ProtocolState;
import dev.ovrex.network.packet.impl.handshake.HandshakePacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.BiConsumer;

@Slf4j
@RequiredArgsConstructor
public class HandshakeHandler implements PacketHandler {
    private final BiConsumer<MinecraftConnection, HandshakePacket> onHandshake;

    @Override
    public void handle(MinecraftConnection connection, Packet packet) {
        if (packet instanceof HandshakePacket handshake) {
            log.debug("Handshake from {} (protocol: {}, next: {})",
                    connection.getRemoteAddress(), handshake.getProtocolVersion(), handshake.getNextState());

            if (handshake.getNextState() == 1) {
                connection.setDecoderState(ProtocolState.STATUS);
                connection.setConnectionState(ConnectionState.STATUS);
            } else if (handshake.getNextState() == 2) {
                connection.setDecoderState(ProtocolState.LOGIN);
                connection.setConnectionState(ConnectionState.LOGIN);
            }

            onHandshake.accept(connection, handshake);
        }
    }

    @Override
    public void onDisconnect(MinecraftConnection connection) {
    }
}
