package dev.ovrex.network.handler.impl;

import dev.ovrex.network.connection.ConnectionState;
import dev.ovrex.network.connection.MinecraftConnection;
import dev.ovrex.network.connection.PlayerConnection;
import dev.ovrex.network.handler.PacketHandler;
import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.enums.ProtocolState;
import dev.ovrex.network.packet.impl.play.AcknowledgeConfigurationPacket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerSwitchHandler implements PacketHandler {

    private final PlayerConnection playerConnection;
    private final Runnable onClientAcknowledged;

    public ServerSwitchHandler(PlayerConnection playerConnection,
                               Runnable onClientAcknowledged) {
        this.playerConnection = playerConnection;
        this.onClientAcknowledged = onClientAcknowledged;
    }

    @Override
    public void handle(MinecraftConnection connection, Packet packet) {
        if (packet instanceof AcknowledgeConfigurationPacket) {
            log.info("Client {} acknowledged StartConfiguration during server switch",
                    playerConnection.getUsername());

            connection.setDecoderState(ProtocolState.CONFIGURATION);
            connection.setConnectionState(ConnectionState.CONFIGURATION);


            onClientAcknowledged.run();
        }

    }

    @Override
    public void onDisconnect(MinecraftConnection connection) {
        log.debug("Client {} disconnected during server switch",
                playerConnection.getUsername());
    }
}