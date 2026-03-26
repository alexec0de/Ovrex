package dev.ovrex.api.player;

import dev.ovrex.api.command.CommandSender;
import dev.ovrex.api.server.BackendServer;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ProxyPlayer extends CommandSender {

    UUID getUniqueId();
    String getUsername();
    InetSocketAddress getRemoteAddress();
    Optional<BackendServer> getCurrentServer();
    CompletableFuture<Void> connect(BackendServer server);
    void disconnect(String reason);
    int getProtocolVersion();
    boolean isConnected();

}
