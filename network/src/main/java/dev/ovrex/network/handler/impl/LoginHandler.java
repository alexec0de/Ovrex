package dev.ovrex.network.handler.impl;

import com.google.gson.JsonObject;
import dev.ovrex.network.connection.ConnectionState;
import dev.ovrex.network.connection.MinecraftConnection;
import dev.ovrex.network.connection.PlayerConnection;
import dev.ovrex.network.handler.PacketHandler;
import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.enums.ProtocolState;
import dev.ovrex.network.packet.impl.login.LoginAcknowledgedPacket;
import dev.ovrex.network.packet.impl.login.LoginDisconnectPacket;
import dev.ovrex.network.packet.impl.login.LoginStartPacket;
import dev.ovrex.network.packet.impl.login.LoginSuccessPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.function.BiFunction;

@Slf4j
public class LoginHandler implements PacketHandler {
    private final boolean onlineMode;
    private final BiFunction<MinecraftConnection, PlayerConnection, Boolean> onLoginComplete;

    private PlayerConnection pendingConnection;
    private boolean loginSuccessSent = false;

    public LoginHandler(boolean onlineMode,
                        BiFunction<MinecraftConnection, PlayerConnection, Boolean> onLoginComplete) {
        this.onlineMode = onlineMode;
        this.onLoginComplete = onLoginComplete;
    }

    @Override
    public void handle(MinecraftConnection connection, Packet packet) {
        if (packet instanceof LoginStartPacket loginStart) {
            handleLoginStart(connection, loginStart);
        } else if (packet instanceof LoginAcknowledgedPacket) {
            handleLoginAcknowledged(connection);
        }
    }

    private void handleLoginStart(MinecraftConnection connection, LoginStartPacket loginStart) {
        String username = loginStart.getUsername();
        log.info("Login attempt from: {}", username);

        UUID uuid;
        if (loginStart.getPlayerUUID() != null
                && loginStart.getPlayerUUID().getMostSignificantBits() != 0
                && loginStart.getPlayerUUID().getLeastSignificantBits() != 0) {
            uuid = loginStart.getPlayerUUID();
        } else {
            uuid = generateOfflineUUID(username);
        }

        pendingConnection = new PlayerConnection(connection);
        pendingConnection.setUsername(username);
        pendingConnection.setUuid(uuid);
        pendingConnection.setProtocolVersion(connection.getProtocolVersion());

        LoginSuccessPacket success = new LoginSuccessPacket(uuid, username);
        connection.sendPacket(success);
        loginSuccessSent = true;

        log.debug("Login success sent to {}, waiting for LoginAcknowledged", username);
    }

    private void handleLoginAcknowledged(MinecraftConnection connection) {
        if (!loginSuccessSent || pendingConnection == null) {
            log.warn("Received LoginAcknowledged without pending login");
            connection.close();
            return;
        }

        log.debug("Login acknowledged by {}, switching to CONFIGURATION", pendingConnection.getUsername());

        connection.setDecoderState(ProtocolState.CONFIGURATION);
        connection.setConnectionState(ConnectionState.CONFIGURATION);

        Boolean accepted = onLoginComplete.apply(connection, pendingConnection);
        if (!accepted) {
            String reason = "{\"text\":\"Connection refused by proxy\"}";
            connection.sendPacketAndClose(new LoginDisconnectPacket(reason));
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
