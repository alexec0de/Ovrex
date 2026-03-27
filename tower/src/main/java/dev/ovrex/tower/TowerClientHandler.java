package dev.ovrex.tower;

import com.google.gson.JsonObject;
import dev.ovrex.api.server.ServerManager;
import dev.ovrex.tower.auth.TowerAuthenticator;
import dev.ovrex.tower.enums.TowerTypeProtocol;
import dev.ovrex.tower.heartbeat.HeartbeatManager;
import dev.ovrex.tower.model.TowerServerInfo;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class TowerClientHandler extends SimpleChannelInboundHandler<String>  {
    private final TowerAuthenticator authenticator;
    private final ServerManager serverManager;
    private final HeartbeatManager heartbeatManager;
    private final Map<Channel, TowerServerInfo> registeredServers;

    private boolean authenticated = false;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        JsonObject json;
        try {
            json = TowerProtocol.deserialize(msg);
        } catch (Exception e) {
            log.warn("Invalid message from {}: {}", ctx.channel().remoteAddress(), msg);
            return;
        }
        try {
            final TowerTypeProtocol type = TowerTypeProtocol.valueOf(json.get("type").getAsString().toUpperCase());

            switch (type) {
                case TowerTypeProtocol.AUTH -> handleAuth(ctx, json);
                case TowerTypeProtocol.REGISTER -> handleRegister(ctx, json);
                case TowerTypeProtocol.HEARTBEAT -> handleHeartbeat(ctx);
                case TowerTypeProtocol.DISCONNECT -> handleDisconnect(ctx);
                default -> log.warn("Unknown message type: {}", type);
            }
        } catch (IllegalArgumentException e) {
            log.warn("Error message type : {}", e.getMessage());
            ctx.close();
        }
    }

    private void handleAuth(ChannelHandlerContext ctx, JsonObject json) {
        String login = json.get("login").getAsString();
        String password = json.get("password").getAsString();

        if (authenticator.authenticate(login, password)) {
            authenticated = true;
            heartbeatManager.registerChannel(ctx.channel());
            sendResponse(ctx, true, "Authentication successful");
            log.info("Tower client authenticated: {} from {}", login, ctx.channel().remoteAddress());
        } else {
            sendResponse(ctx, false, "Authentication failed");
            ctx.close();
            log.warn("Tower authentication failed for: {} from {}", login, ctx.channel().remoteAddress());
        }
    }

    private void handleRegister(ChannelHandlerContext ctx, JsonObject json) {
        if (!authenticated) {
            sendResponse(ctx, false, "Not authenticated");
            return;
        }

        final TowerServerInfo info = TowerServerInfo.builder()
                .name(json.get("name").getAsString())
                .host(json.get("host").getAsString())
                .port(json.get("port").getAsInt())
                .type(json.get("serverType").getAsString())
                .maxPlayers(json.has("maxPlayers") ? json.get("maxPlayers").getAsInt() : 100)
                .build();

        serverManager.registerServer(info.getName(),
                new InetSocketAddress(info.getHost(), info.getPort()),
                info.getType());

        registeredServers.put(ctx.channel(), info);

        sendResponse(ctx, true, "Server registered: " + info.getName());
        log.info("Tower server registered: {}", info);
    }

    private void handleHeartbeat(ChannelHandlerContext ctx) {
        if (!authenticated) return;
        heartbeatManager.updateHeartbeat(ctx.channel());
        sendResponse(ctx, true, "pong");
    }

    private void handleDisconnect(ChannelHandlerContext ctx) {
        cleanupChannel(ctx.channel());
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        cleanupChannel(ctx.channel());
    }

    private void cleanupChannel(Channel channel) {
        heartbeatManager.removeChannel(channel);
        TowerServerInfo info = registeredServers.remove(channel);
        if (info != null) {
            serverManager.unregisterServer(info.getName());
            log.info("Tower server unregistered (disconnected): {}", info.getName());
        }
    }

    private void sendResponse(ChannelHandlerContext ctx, boolean success, String message) {
        JsonObject response = TowerProtocol.createResponse(success, message);
        ctx.writeAndFlush(TowerProtocol.serialize(response) + "\n");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Tower client error from {}: {}", ctx.channel().remoteAddress(), cause.getMessage());
        cleanupChannel(ctx.channel());
        ctx.close();
    }
}
