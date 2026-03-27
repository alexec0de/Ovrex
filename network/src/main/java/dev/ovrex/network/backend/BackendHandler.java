package dev.ovrex.network.backend;

import dev.ovrex.network.connection.PlayerConnection;
import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.impl.play.DisconnectPacket;
import dev.ovrex.network.packet.impl.play.KeepAlivePacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class BackendHandler extends SimpleChannelInboundHandler<Packet> {
    private final PlayerConnection playerConnection;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        if (packet instanceof DisconnectPacket disconnect) {
            log.info("Backend disconnected player {} with reason: {}",
                    playerConnection.getUsername(), disconnect.getReason());
            playerConnection.getClientConnection().sendPacket(disconnect);
            return;
        }

        if (packet instanceof KeepAlivePacket) {
            playerConnection.getClientConnection().sendPacket(packet);
            return;
        }

        playerConnection.getClientConnection().sendPacket(packet);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("Backend connection closed for {}", playerConnection.getUsername());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.warn("Backend exception for {}: {}", playerConnection.getUsername(), cause.getMessage());
        ctx.close();
    }
}
