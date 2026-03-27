package dev.ovrex.network;

import dev.ovrex.network.connection.MinecraftConnection;
import dev.ovrex.network.handler.PacketHandler;
import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.enums.PacketDirection;
import dev.ovrex.network.packet.enums.ProtocolState;
import dev.ovrex.network.pipeline.MinecraftDecoder;
import dev.ovrex.network.pipeline.MinecraftEncoder;
import dev.ovrex.network.pipeline.PacketLengthDecoder;
import dev.ovrex.network.pipeline.PacketLengthEncoder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.Getter;


import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Slf4j
public class NetworkServer {
    @Getter
    private final EventLoopGroup bossGroup;
    @Getter
    private final EventLoopGroup workerGroup;
    private Channel serverChannel;
    private final Function<MinecraftConnection, PacketHandler> handlerFactory;

    public NetworkServer(Function<MinecraftConnection, PacketHandler> handlerFactory) {
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
        this.handlerFactory = handlerFactory;
    }

    public CompletableFuture<Void> bind(InetSocketAddress address) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,
                        new WriteBufferWaterMark(512 * 1024, 2 * 1024 * 1024))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        MinecraftConnection connection = new MinecraftConnection(ch);
                        PacketHandler handler = handlerFactory.apply(connection);

                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("timeout", new ReadTimeoutHandler(30));
                        pipeline.addLast("length-decoder", new PacketLengthDecoder());
                        pipeline.addLast("length-encoder", new PacketLengthEncoder());
                        pipeline.addLast("mc-decoder", new MinecraftDecoder(PacketDirection.SERVERBOUND, ProtocolState.HANDSHAKE));
                        pipeline.addLast("mc-encoder", new MinecraftEncoder());
                        pipeline.addLast("handler", new ConnectionHandler(connection, handler));
                    }
                });

        bootstrap.bind(address).addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                serverChannel = channelFuture.channel();
                log.info("Network server bound to {}", address);
                future.complete(null);
            } else {
                log.error("Failed to bind to {}", address, channelFuture.cause());
                future.completeExceptionally(channelFuture.cause());
            }
        });

        return future;
    }

    public void shutdown() {
        if (serverChannel != null) {
            serverChannel.close().syncUninterruptibly();
        }
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    @ChannelHandler.Sharable
    private static class ConnectionHandler extends SimpleChannelInboundHandler<Packet> {

        private final MinecraftConnection connection;
        @Setter
        private volatile PacketHandler handler;

        ConnectionHandler(MinecraftConnection connection, PacketHandler handler) {
            this.connection = connection;
            this.handler = handler;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
            handler.handle(connection, packet);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            handler.onDisconnect(connection);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            handler.onException(connection, cause);
            ctx.close();
        }
    }
}
