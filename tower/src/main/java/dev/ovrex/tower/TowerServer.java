package dev.ovrex.tower;

import dev.ovrex.api.server.ServerManager;
import dev.ovrex.tower.auth.TowerAuthenticator;
import dev.ovrex.tower.heartbeat.HeartbeatManager;
import dev.ovrex.tower.model.TowerServerInfo;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
public class TowerServer {


    private final TowerAuthenticator authenticator;
    private final ServerManager serverManager;
    private final HeartbeatManager heartbeatManager;
    private final Map<Channel, TowerServerInfo> registeredServers = new ConcurrentHashMap<>();

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    public TowerServer(TowerAuthenticator authenticator, ServerManager serverManager) {
        this.authenticator = authenticator;
        this.serverManager = serverManager;
        this.heartbeatManager = new HeartbeatManager(30000);
    }

    public CompletableFuture<Void> start(InetSocketAddress address) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(2);

        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("timeout", new ReadTimeoutHandler(60));
                        pipeline.addLast("frame-decoder", new LineBasedFrameDecoder(65536));
                        pipeline.addLast("string-decoder", new StringDecoder(StandardCharsets.UTF_8));
                        pipeline.addLast("string-encoder", new StringEncoder(StandardCharsets.UTF_8));
                        pipeline.addLast("handler", new TowerClientHandler(
                                authenticator, serverManager, heartbeatManager, registeredServers));
                    }
                });

        bootstrap.bind(address).addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                serverChannel = channelFuture.channel();
                heartbeatManager.start();
                log.info("Tower server started on {}", address);
                future.complete(null);
            } else {
                log.error("Failed to start Tower server on {}", address, channelFuture.cause());
                future.completeExceptionally(channelFuture.cause());
            }
        });

        return future;
    }

    public void shutdown() {
        heartbeatManager.shutdown();
        registeredServers.forEach((channel, info) -> {
            serverManager.unregisterServer(info.getName());
        });
        registeredServers.clear();

        if (serverChannel != null) {
            serverChannel.close().syncUninterruptibly();
        }
        if (workerGroup != null) workerGroup.shutdownGracefully();
        if (bossGroup != null) bossGroup.shutdownGracefully();

        log.info("Tower server shut down");
    }
}


