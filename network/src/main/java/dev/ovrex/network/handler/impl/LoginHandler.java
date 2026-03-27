package dev.ovrex.network.handler.impl;

import com.google.gson.JsonObject;
import dev.ovrex.network.connection.ConnectionState;
import dev.ovrex.network.connection.MinecraftConnection;
import dev.ovrex.network.connection.PlayerConnection;
import dev.ovrex.network.handler.PacketHandler;
import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.enums.ProtocolState;
import dev.ovrex.network.packet.impl.login.LoginDisconnectPacket;
import dev.ovrex.network.packet.impl.login.LoginStartPacket;
import dev.ovrex.network.packet.impl.login.LoginSuccessPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.function.BiFunction;

@Slf4j
@RequiredArgsConstructor
public class LoginHandler implements PacketHandler {
    private final boolean onlineMode;
    private final BiFunction<MinecraftConnection, PlayerConnection, Boolean> onLoginComplete;

    @Override
    public void handle(MinecraftConnection connection, Packet packet) {
        if (packet instanceof LoginStartPacket loginStart) {
            String username = loginStart.getUsername();
            log.info("Login attempt from: {}", username);

            if (onlineMode) {
                handleOnlineMode(connection, username);
            } else {
                handleOfflineMode(connection, username, loginStart.getPlayerUUID());
            }
        }
    }

    private void handleOnlineMode(MinecraftConnection connection, String username) {
        // В production здесь должна быть верификация через Mojang API
        // Для упрощения используем offline-mode flow
        handleOfflineMode(connection, username, null);
    }

    private void handleOfflineMode(MinecraftConnection connection, String username, UUID clientUUID) {
        UUID uuid = clientUUID != null ? clientUUID : generateOfflineUUID(username);

        final PlayerConnection playerConnection = new PlayerConnection(connection);
        playerConnection.setUsername(username);
        playerConnection.setUuid(uuid);

        final LoginSuccessPacket success = new LoginSuccessPacket(uuid, username);
        connection.sendPacket(success);

        connection.setDecoderState(ProtocolState.PLAY);
        connection.setConnectionState(ConnectionState.PLAY);

        final Boolean accepted = onLoginComplete.apply(connection, playerConnection);
        if (!accepted) {
            final JsonObject reason = new JsonObject();
            reason.addProperty("text", "Connection refused by proxy");
            connection.sendPacketAndClose(new LoginDisconnectPacket(reason.toString()));
        }
    }

    private UUID generateOfflineUUID(String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes());
    }

    @Override
    public void onDisconnect(MinecraftConnection connection) {
        log.debug("Login disconnected: {}", connection.getRemoteAddress());
    }
}
