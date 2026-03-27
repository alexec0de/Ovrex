package dev.ovrex.network.backend;

import dev.ovrex.network.connection.BackendConnection;
import dev.ovrex.network.connection.PlayerConnection;
import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.enums.PacketDirection;
import dev.ovrex.network.packet.enums.ProtocolState;
import dev.ovrex.network.packet.impl.handshake.HandshakePacket;
import dev.ovrex.network.packet.impl.login.LoginDisconnectPacket;
import dev.ovrex.network.packet.impl.login.LoginStartPacket;
import dev.ovrex.network.packet.impl.login.LoginSuccessPacket;
import dev.ovrex.network.packet.impl.login.SetCompressionPacket;
import dev.ovrex.network.pipeline.MinecraftDecoder;
import dev.ovrex.network.pipeline.MinecraftEncoder;
import dev.ovrex.network.pipeline.PacketLengthDecoder;
import dev.ovrex.network.pipeline.PacketLengthEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class BackendConnector {

    private final EventLoopGroup workerGroup;

    public BackendConnector(EventLoopGroup workerGroup) {
        this.workerGroup = workerGroup;
    }

    public CompletableFuture<BackendConnection> connect(PlayerConnection playerConnection,
                                                        InetSocketAddress address,
                                                        String serverName) {
        CompletableFuture<BackendConnection> future = new CompletableFuture<>();

        Bootstrap bootstrap = new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("length-decoder", new PacketLengthDecoder());
                        pipeline.addLast("length-encoder", new PacketLengthEncoder());
                        pipeline.addLast("mc-decoder", new MinecraftDecoder(PacketDirection.CLIENTBOUND, ProtocolState.LOGIN));
                        pipeline.addLast("mc-encoder", new MinecraftEncoder());
                        pipeline.addLast("backend-login-handler", new BackendLoginHandler(playerConnection, serverName, future));
                    }
                });

        bootstrap.connect(address).addListener((ChannelFutureListener) connectFuture -> {
            if (!connectFuture.isSuccess()) {
                log.error("Failed to connect to backend {}: {}", serverName, connectFuture.cause().getMessage());
                future.completeExceptionally(connectFuture.cause());
                return;
            }

            Channel channel = connectFuture.channel();

            final HandshakePacket handshake = new HandshakePacket();
            handshake.setProtocolVersion(playerConnection.getProtocolVersion());
            handshake.setServerAddress(address.getHostString());
            handshake.setServerPort(address.getPort());
            handshake.setNextState(2);
            channel.writeAndFlush(handshake);

            // Send login start
            final LoginStartPacket loginStart = new LoginStartPacket(
                    playerConnection.getUsername(),
                    playerConnection.getUuid()
            );
            channel.writeAndFlush(loginStart);
        });

        return future;
    }

    private static class BackendLoginHandler extends SimpleChannelInboundHandler<Packet> {

        private final PlayerConnection playerConnection;
        private final String serverName;
        private final CompletableFuture<BackendConnection> future;

        BackendLoginHandler(PlayerConnection playerConnection, String serverName,
                            CompletableFuture<BackendConnection> future) {
            this.playerConnection = playerConnection;
            this.serverName = serverName;
            this.future = future;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
            if (packet instanceof LoginSuccessPacket) {
                log.info("Backend login success for {} on {}", playerConnection.getUsername(), serverName);

                final MinecraftDecoder decoder = ctx.pipeline().get(MinecraftDecoder.class);
                if (decoder != null) {
                    decoder.setState(ProtocolState.PLAY);
                }

                final BackendConnection backendConnection = new BackendConnection(ctx.channel(), serverName);
                ctx.pipeline().replace(this, "backend-handler", new BackendHandler(playerConnection));

                future.complete(backendConnection);
            } else if (packet instanceof LoginDisconnectPacket disconnect) {
                log.warn("Backend login rejected for {}: {}", playerConnection.getUsername(), disconnect.getReason());
                future.completeExceptionally(new RuntimeException("Login rejected: " + disconnect.getReason()));
                ctx.close();
            } else if (packet instanceof SetCompressionPacket) {
                // TODO: Enable compression on backend connection
                log.debug("Backend requested compression, ignoring for now");
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("Backend login error for {}: {}", playerConnection.getUsername(), cause.getMessage());
            future.completeExceptionally(cause);
            ctx.close();
        }
    }
}
