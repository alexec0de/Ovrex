package dev.ovrex.network.handler.impl;

import dev.ovrex.network.connection.ConnectionState;
import dev.ovrex.network.connection.MinecraftConnection;
import dev.ovrex.network.connection.PlayerConnection;
import dev.ovrex.network.handler.PacketHandler;
import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.enums.ProtocolState;
import dev.ovrex.network.packet.impl.configuration.FinishConfigurationAckPacket;
import lombok.extern.slf4j.Slf4j;

import java.util.function.BiConsumer;

@Slf4j
public class ConfigurationHandler implements PacketHandler {
    private final PlayerConnection playerConnection;
    private final BiConsumer<MinecraftConnection, PlayerConnection> onConfigurationComplete;

    public ConfigurationHandler(PlayerConnection playerConnection,
                                BiConsumer<MinecraftConnection, PlayerConnection> onConfigurationComplete) {
        this.playerConnection = playerConnection;
        this.onConfigurationComplete = onConfigurationComplete;
    }

    @Override
    public void handle(MinecraftConnection connection, Packet packet) {
        if (packet instanceof FinishConfigurationAckPacket) {
            log.info("Client {} acknowledged FinishConfiguration, switching to PLAY",
                    playerConnection.getUsername());

            connection.setDecoderState(ProtocolState.PLAY);
            connection.setConnectionState(ConnectionState.PLAY);

            onConfigurationComplete.accept(connection, playerConnection);

        } else {
            if (playerConnection.hasBackend()) playerConnection.getBackendConnection().sendPacket(packet);
        }
    }

    @Override
    public void onDisconnect(MinecraftConnection connection) {
        log.debug("Client disconnected during configuration: {}",
                playerConnection.getUsername());
    }
}
