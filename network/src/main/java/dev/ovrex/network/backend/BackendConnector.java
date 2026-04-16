package dev.ovrex.network.backend;

import dev.ovrex.network.connection.BackendConnection;
import dev.ovrex.network.connection.PlayerConnection;
import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;
import dev.ovrex.network.packet.enums.PacketDirection;
import dev.ovrex.network.packet.enums.ProtocolState;
import dev.ovrex.network.packet.impl.configuration.ClientboundKnownPacksPacket;
import dev.ovrex.network.packet.impl.configuration.FinishConfigurationAckPacket;
import dev.ovrex.network.packet.impl.configuration.FinishConfigurationPacket;
import dev.ovrex.network.packet.impl.handshake.HandshakePacket;
import dev.ovrex.network.packet.impl.login.*;
import dev.ovrex.network.packet.impl.play.UnknownPacket;
import dev.ovrex.network.pipeline.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
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
                        pipeline.addLast("mc-decoder", new MinecraftDecoder(
                                PacketDirection.CLIENTBOUND, ProtocolState.LOGIN));
                        pipeline.addLast("mc-encoder", new MinecraftEncoder());
                        pipeline.addLast("backend-login-handler",
                                new BackendLoginHandler(playerConnection, serverName, future));
                    }
                });

        bootstrap.connect(address).addListener((ChannelFutureListener) cf -> {
            if (!cf.isSuccess()) {
                log.error("Failed to connect to backend {}: {}", serverName, cf.cause().getMessage());
                future.completeExceptionally(cf.cause());
                return;
            }

            Channel channel = cf.channel();

            HandshakePacket handshake = new HandshakePacket();
            handshake.setProtocolVersion(playerConnection.getProtocolVersion());
            handshake.setServerAddress(address.getHostString());
            handshake.setServerPort(address.getPort());
            handshake.setNextState(2);
            channel.writeAndFlush(handshake);

            LoginStartPacket loginStart = new LoginStartPacket(
                    playerConnection.getUsername(),
                    playerConnection.getUuid()
            );
            channel.writeAndFlush(loginStart);
        });

        return future;
    }

    @Slf4j
    private static class BackendLoginHandler extends SimpleChannelInboundHandler<Packet> {

        private enum Phase { LOGIN, CONFIGURATION, DONE }

        private final PlayerConnection playerConnection;
        private final String serverName;
        private final CompletableFuture<BackendConnection> future;

        private Phase phase = Phase.LOGIN;

        BackendLoginHandler(PlayerConnection playerConnection, String serverName,
                            CompletableFuture<BackendConnection> future) {
            this.playerConnection = playerConnection;
            this.serverName = serverName;
            this.future = future;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
            switch (phase) {
                case LOGIN -> handleLoginPhase(ctx, packet);
                case CONFIGURATION -> handleConfigurationPhase(ctx, packet);
                case DONE -> {}
            }
        }

        private void handleLoginPhase(ChannelHandlerContext ctx, Packet packet) {
            if (packet instanceof SetCompressionPacket setCompression) {
                int threshold = setCompression.getThreshold();
                log.info("Backend {} enabling compression threshold={}", serverName, threshold);
                enableCompression(ctx, threshold);

            } else if (packet instanceof LoginSuccessPacket) {
                log.info("Backend {} login success for {}", serverName, playerConnection.getUsername());

                ctx.writeAndFlush(new LoginAcknowledgedPacket());

                MinecraftDecoder decoder = ctx.pipeline().get(MinecraftDecoder.class);
                if (decoder != null) {
                    decoder.setState(ProtocolState.CONFIGURATION);
                }

                phase = Phase.CONFIGURATION;
                log.debug("Backend {} now in CONFIGURATION", serverName);

            } else if (packet instanceof LoginDisconnectPacket disconnect) {
                log.warn("Backend {} rejected {}: {}",
                        serverName, playerConnection.getUsername(), disconnect.getReason());
                future.completeExceptionally(
                        new RuntimeException("Rejected: " + disconnect.getReason()));
                ctx.close();
            }
        }

        private void handleConfigurationPhase(ChannelHandlerContext ctx, Packet packet) {
            int packetId = packet.getId();

            if (packet instanceof FinishConfigurationPacket) {
                log.info("Backend {} sent FinishConfiguration", serverName);

                playerConnection.getClientConnection().sendPacket(new FinishConfigurationPacket());

                ctx.writeAndFlush(new FinishConfigurationAckPacket());

                MinecraftDecoder decoder = ctx.pipeline().get(MinecraftDecoder.class);
                if (decoder != null) {
                    decoder.setState(ProtocolState.PLAY);
                }

                phase = Phase.DONE;

                BackendConnection backendConnection = new BackendConnection(ctx.channel(), serverName);
                ctx.pipeline().replace(this, "backend-handler",
                        new BackendHandler(playerConnection));

                future.complete(backendConnection);
                log.info("Backend {} fully connected for {}", serverName, playerConnection.getUsername());

            } else if (packet instanceof ClientboundKnownPacksPacket knownPacks) {
                log.debug("Backend {} sent KnownPacks, responding", serverName);

                playerConnection.getClientConnection().sendPacket(knownPacks);

                respondKnownPacks(ctx);

            } else if (packetId == 0x04) {
                log.debug("Backend {} config keepalive, responding", serverName);
                respondConfigKeepAlive(ctx, packet);

            } else {
                playerConnection.getClientConnection().sendPacket(packet);
                log.debug("Backend {} -> client: config 0x{}",
                        serverName, Integer.toHexString(packetId));
            }
        }

        private void respondKnownPacks(ChannelHandlerContext ctx) {
            final PacketBuffer buf = PacketBuffer.alloc();
            buf.writeVarInt(0x07);
            buf.writeVarInt(0);
            ByteBuf raw = buf.getHandle();

            ctx.writeAndFlush(new UnknownPacket(0x07, new byte[0]) {
                @Override
                public void write(PacketBuffer buffer) {
                    buffer.writeVarInt(0);
                }
            });

            raw.release();
        }

        private void respondConfigKeepAlive(ChannelHandlerContext ctx, Packet packet) {
            if (packet instanceof UnknownPacket unknown) {
                byte[] data = unknown.getData();
                ctx.writeAndFlush(new UnknownPacket(0x04, data));
            }
        }

        private void enableCompression(ChannelHandlerContext ctx, int threshold) {
            ChannelPipeline pipeline = ctx.pipeline();

            if (pipeline.get("compression-decoder") == null) {
                pipeline.addAfter("length-decoder", "compression-decoder",
                        new CompressionDecoder(threshold));
            }

            if (pipeline.get("compression-encoder") == null) {
                pipeline.addAfter("length-encoder", "compression-encoder",
                        new CompressionEncoder(threshold));
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("Backend {} error for {}: {}",
                    serverName, playerConnection.getUsername(), cause.getMessage());
            if (!future.isDone()) {
                future.completeExceptionally(cause);
            }
            ctx.close();
        }
    }
}
