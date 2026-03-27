package dev.ovrex.core.impl;

import dev.ovrex.api.player.ProxyPlayer;
import dev.ovrex.api.server.BackendServer;
import dev.ovrex.core.utility.ChatUtility;
import dev.ovrex.network.backend.BackendConnector;
import dev.ovrex.network.connection.PlayerConnection;
import dev.ovrex.network.packet.impl.play.DisconnectPacket;
import dev.ovrex.network.packet.impl.play.SystemChatMessagePacket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j @Getter
@RequiredArgsConstructor
public class OvrexProxyPlayer implements ProxyPlayer {
    private final PlayerConnection playerConnection;
    private final BackendConnector backendConnector;
    private final OvrexServerManager serverManager;
    private volatile BackendServer currentServer;



    @Override
    public UUID getUniqueId() {
        return playerConnection.getUuid();
    }

    @Override
    public String getUsername() {
        return playerConnection.getUsername();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return playerConnection.getClientConnection().getRemoteAddress();
    }

    @Override
    public Optional<BackendServer> getCurrentServer() {
        return Optional.ofNullable(currentServer);
    }

    @Override
    public CompletableFuture<Void> connect(BackendServer server) {

        return backendConnector.connect(playerConnection, server.getAddress(), server.getName())
                .thenAccept(backendConnection -> {

                    if (currentServer != null) {
                        currentServer.removePlayer(this);
                    }
                    playerConnection.disconnectFromBackend();


                    playerConnection.setBackendConnection(backendConnection);
                    playerConnection.setCurrentServerName(server.getName());
                    currentServer = server;
                    server.addPlayer(this);

                    log.info("Player {} connected to backend: {}", getUsername(), server.getName());
                })
                .exceptionally(throwable -> {
                    log.error("Failed to connect {} to {}: {}", getUsername(), server.getName(), throwable.getMessage());
                    sendMessage("§cFailed to connect to " + server.getName());
                    return null;
                });
    }

    @Override
    public void disconnect(String reason) {
        String jsonReason = "{\"text\":\"" + reason + "\"}";
        playerConnection.getClientConnection().sendPacketAndClose(new DisconnectPacket(jsonReason));

        if (currentServer != null) {
            currentServer.removePlayer(this);
        }
        playerConnection.disconnectFromBackend();
    }

    @Override
    public int getProtocolVersion() {
        return playerConnection.getProtocolVersion();
    }

    @Override
    public boolean isConnected() {
        return playerConnection.getClientConnection().isActive();
    }

    @Override
    public void sendMessage(String message) {
        if (!isConnected()) {
            return;
        }

        final String jsonMessage = ChatUtility.convertToJsonChat(message);
        final SystemChatMessagePacket packet = new SystemChatMessagePacket(jsonMessage, false);
        playerConnection.getClientConnection().sendPacket(packet);
    }

    @Override
    public boolean hasPermission(String permission) {
        return true; // TODO: Not permission system
    }

    public void cleanup() {
        if (currentServer != null) {
            currentServer.removePlayer(this);
        }
        playerConnection.disconnectFromBackend();
    }
}
