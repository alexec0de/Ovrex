package dev.ovrex.core.impl;

import dev.ovrex.api.player.ProxyPlayer;
import dev.ovrex.api.server.BackendServer;
import dev.ovrex.core.utility.ChatUtility;
import dev.ovrex.network.NetworkServer;
import dev.ovrex.network.backend.BackendConnector;
import dev.ovrex.network.connection.ConnectionState;
import dev.ovrex.network.connection.MinecraftConnection;
import dev.ovrex.network.connection.PlayerConnection;
import dev.ovrex.network.handler.PacketHandler;
import dev.ovrex.network.handler.impl.ConfigurationHandler;
import dev.ovrex.network.handler.impl.PlayHandler;
import dev.ovrex.network.handler.impl.ServerSwitchHandler;
import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.impl.play.DisconnectPacket;
import dev.ovrex.network.packet.impl.play.StartConfigurationPacket;
import dev.ovrex.network.packet.impl.play.SystemChatMessagePacket;
import dev.ovrex.network.packet.impl.play.UnknownPacket;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Slf4j @Getter
@RequiredArgsConstructor
public class OvrexProxyPlayer implements ProxyPlayer {
    private final PlayerConnection playerConnection;
    private final BackendConnector backendConnector;
    private final OvrexServerManager serverManager;
    private volatile BackendServer currentServer;
    private final Consumer<OvrexProxyPlayer> onDisconnect;
    private final BiFunction<OvrexProxyPlayer, String, Boolean> commandHandler;



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

    public CompletableFuture<Void> firstConnect(BackendServer server) {
        final BackendConnector backendConnector1 = new BackendConnector(new NioEventLoopGroup());
        return backendConnector1.connect(playerConnection, server.getAddress(), server.getName())
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
    public CompletableFuture<Void> connect(BackendServer server) {
        MinecraftConnection clientConn = playerConnection.getClientConnection();
        CompletableFuture<Void> switchFuture = new CompletableFuture<>();

        boolean isInitialConnect = currentServer == null;

        if (isInitialConnect) {
            connectToBackend(server, switchFuture);
        } else {
            if (playerConnection.hasBackend()) {
                playerConnection.getBackendConnection().pause();
            }

            clientConn.sendPacket(new StartConfigurationPacket());

            replaceHandler(clientConn, new ServerSwitchHandler(
                    playerConnection,
                    () -> {
                        if (currentServer != null) {
                            currentServer.removePlayer(this);
                        }
                        playerConnection.disconnectFromBackend();
                        connectToBackend(server, switchFuture);
                    }
            ));
        }

        return switchFuture;
    }

    private void connectToBackend(BackendServer server, CompletableFuture<Void> switchFuture) {
        backendConnector
                .connect(playerConnection, server.getAddress(), server.getName())
                .thenAccept(backendConnection -> {
                    playerConnection.setBackendConnection(backendConnection);
                    playerConnection.setCurrentServerName(server.getName());
                    currentServer = server;
                    server.addPlayer(this);

                    MinecraftConnection clientConn = playerConnection.getClientConnection();
                    replaceHandler(clientConn, new ConfigurationHandler(
                            playerConnection,
                            this::onConfigurationComplete
                    ));

                    log.info("Player {} connected to {}", getUsername(), server.getName());
                    switchFuture.complete(null);
                })
                .exceptionally(throwable -> {
                    log.error("Failed to connect {} to {}: {}",
                            getUsername(), server.getName(), throwable.getMessage());
                    sendMessage("§cFailed to connect to " + server.getName());
                    switchFuture.completeExceptionally(throwable);
                    return null;
                });
    }

    private void onConfigurationComplete(MinecraftConnection conn, PlayerConnection pc) {
        log.info("Player {} configuration complete after server switch", pc.getUsername());
        replaceHandler(conn, new PlayHandler(
                pc,
                (connection, packet) -> forwardPacketToBackend(packet),
                this::handleDisconnect,
                this::handleCommand
        ));
    }
    private void replaceHandler(MinecraftConnection connection, PacketHandler newHandler) {
        NetworkServer.ConnectionHandler handler =
                (NetworkServer.ConnectionHandler) connection.getChannel()
                        .pipeline().get("handler");
        if (handler != null) {
            handler.setHandler(newHandler);
        }
    }

    private void forwardPacketToBackend(Packet packet) {
        if (playerConnection.hasBackend()) {
            playerConnection.getBackendConnection().sendPacket(packet);
        }
    }

    private void handleDisconnect() {
        log.info("Player {} disconnected", getUsername());
        if (currentServer != null) {
            currentServer.removePlayer(this);
        }
        playerConnection.disconnectFromBackend();

        if (onDisconnect != null) {
            onDisconnect.accept(this);
        }
    }

    private boolean handleCommand(PlayerConnection pc, String command) {
        if (commandHandler != null) {
            return commandHandler.apply(this, command);
        }
        return false;
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

        if (playerConnection.getClientConnection().getConnectionState() != ConnectionState.PLAY) {
            log.debug("Cannot send message to {} - not in PLAY state yet", getUsername());
            return;
        }

        final SystemChatMessagePacket packet = new SystemChatMessagePacket(message, false, playerConnection.getProtocolVersion());
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
