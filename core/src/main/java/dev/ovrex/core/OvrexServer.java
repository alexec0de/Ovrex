package dev.ovrex.core;

import dev.ovrex.api.OvrexAPI;
import dev.ovrex.api.command.CommandSender;
import dev.ovrex.api.event.impl.packet.PacketReceiveEvent;
import dev.ovrex.api.event.impl.player.PlayerConnectEvent;
import dev.ovrex.api.event.impl.player.PlayerDisconnectEvent;
import dev.ovrex.api.server.BackendServer;
import dev.ovrex.core.command.EndCommand;
import dev.ovrex.core.command.ServerCommand;
import dev.ovrex.core.config.ConfigLoader;
import dev.ovrex.core.config.OvrexConfig;
import dev.ovrex.core.impl.*;
import dev.ovrex.network.NetworkServer;
import dev.ovrex.network.backend.BackendConnector;
import dev.ovrex.network.connection.MinecraftConnection;
import dev.ovrex.network.connection.PlayerConnection;
import dev.ovrex.network.handler.PacketHandler;
import dev.ovrex.network.handler.impl.HandshakeHandler;
import dev.ovrex.network.handler.impl.LoginHandler;
import dev.ovrex.network.handler.impl.PlayHandler;
import dev.ovrex.network.handler.impl.StatusHandler;
import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.impl.play.UnknownPacket;
import dev.ovrex.plugin.DefaultServiceRegistry;
import dev.ovrex.plugin.JavaPluginManager;
import dev.ovrex.tower.TowerServer;
import dev.ovrex.tower.auth.TowerAuthenticator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
@Slf4j
@Getter
public class OvrexServer implements OvrexAPI {
    private static final String VERSION = "26.0-snapshot-1";

    private final OvrexConfig config;
    private final OvrexPlayerManager playerManager;
    private final OvrexServerManager serverManager;
    private final OvrexEventBus eventBus;
    private final OvrexCommandManager commandManager;
    private final OvrexScheduler scheduler;
    private final JavaPluginManager pluginManager;
    private final DefaultServiceRegistry serviceRegistry;

    private final NetworkServer networkServer;
    private final TowerServer towerServer;
    private final BackendConnector backendConnector;
    private final TowerAuthenticator towerAuthenticator;

    private final AtomicBoolean running = new AtomicBoolean(false);

    public OvrexServer() {
        this.config = ConfigLoader.load();

        this.playerManager = new OvrexPlayerManager();
        this.serverManager = new OvrexServerManager();
        this.eventBus = new OvrexEventBus();
        this.commandManager = new OvrexCommandManager();
        this.scheduler = new OvrexScheduler();
        this.serviceRegistry = new DefaultServiceRegistry();

        this.networkServer = new NetworkServer(this::createHandlerChain);
        this.backendConnector = new BackendConnector(networkServer.getWorkerGroup());

        this.towerAuthenticator = new TowerAuthenticator();
        this.towerServer = new TowerServer(towerAuthenticator, serverManager);

        this.pluginManager = new JavaPluginManager(new File("plugins"), this);
    }

    public void start() {
        if (!running.compareAndSet(false, true)) {
            log.warn("Proxy is already running");
            return;
        }

        log.info("Starting Ovrex v{}", VERSION);

        registerStaticServers();
        configureTowerAuth();

        final InetSocketAddress bindAddress = new InetSocketAddress(
                config.getBindAddress(), config.getBindPort());
        networkServer.bind(bindAddress).join();

        final InetSocketAddress towerAddress = new InetSocketAddress(
                config.getTowerBindAddress(), config.getTowerPort());
        towerServer.start(towerAddress).join();

        serverManager.setDefaultServerName(config.getDefaultServer());

        registerCommands();

        pluginManager.loadPlugins();
        pluginManager.enablePlugins();

        log.info("Ovrex v{} started successfully!", VERSION);
        log.info("Listening on {}:{}", config.getBindAddress(), config.getBindPort());
        log.info("Tower on {}:{}", config.getTowerBindAddress(), config.getTowerPort());

        startConsoleReader();
    }

    private PacketHandler createHandlerChain(MinecraftConnection connection) {
        return new HandshakeHandler((conn, handshake) -> {
            if (handshake.getNextState() == 1) {
                final StatusHandler statusHandler = new StatusHandler(
                        playerManager::getPlayerCount,
                        config.getMaxPlayers(),
                        config.getMotd()
                );
                replaceHandler(conn, statusHandler);
            } else if (handshake.getNextState() == 2) {
                final LoginHandler loginHandler = new LoginHandler(
                        config.isOnlineMode(),
                        (c, playerConn) -> onLoginComplete(c, playerConn, handshake.getProtocolVersion())
                );
                replaceHandler(conn, loginHandler);
            }
        });
    }

    private void replaceHandler(MinecraftConnection connection, PacketHandler newHandler) {
        NetworkServer.ConnectionHandler handler =
                (NetworkServer.ConnectionHandler) connection.getChannel().pipeline().get("handler");
        if (handler != null) {
            handler.setHandler(newHandler);
        }
    }

    private boolean onLoginComplete(MinecraftConnection connection, PlayerConnection playerConnection,
                                    int protocolVersion) {
        playerConnection.setProtocolVersion(protocolVersion);

        // Check if already online
        if (playerManager.isOnline(playerConnection.getUsername())) {
            log.warn("Player {} is already connected, rejecting", playerConnection.getUsername());
            return false;
        }

        final OvrexProxyPlayer player = new OvrexProxyPlayer(
                playerConnection, backendConnector, serverManager);


        PlayerConnectEvent connectEvent = eventBus.fire(new PlayerConnectEvent(player));
        if (connectEvent.isCancelled()) {
            log.info("Connection cancelled for {} : {}", player.getUsername(), connectEvent.getCancelReason());
            return false;
        }

        playerManager.addPlayer(player);

        PlayHandler playHandler = new PlayHandler(
                playerConnection,
                (pc, packet) -> forwardPacketToBackend(player, packet),
                () -> handlePlayerDisconnect(player)
        );
        replaceHandler(connection, playHandler);

        Optional<BackendServer> defaultServer = serverManager.getDefaultServer();
        if (defaultServer.isPresent()) {
            player.connect(defaultServer.get());
        } else {
            log.warn("No default server available for {}", player.getUsername());
            player.disconnect("§cNo available servers");
        }

        log.info("Player {} logged in from {}", player.getUsername(), connection.getRemoteAddress());
        return true;
    }

    private void forwardPacketToBackend(OvrexProxyPlayer player, Packet packet) {
        if (packet instanceof UnknownPacket unknown) {
            PacketReceiveEvent event = eventBus.fire(
                    new PacketReceiveEvent(player, unknown.getId(), unknown.getData()));
            if (event.isCancelled()) return;
        }

        if (player.getPlayerConnection().hasBackend()) {
            player.getPlayerConnection().getBackendConnection().sendPacket(packet);
        }
    }

    private void handlePlayerDisconnect(OvrexProxyPlayer player) {
        eventBus.fire(new PlayerDisconnectEvent(player));
        playerManager.removePlayer(player);
        log.info("Player {} disconnected", player.getUsername());
    }

    private void registerStaticServers() {
        config.getServers().forEach((name, entry) -> {
            serverManager.registerServer(name,
                    new InetSocketAddress(entry.getAddress(), entry.getPort()),
                    entry.getType());
        });
    }

    private void configureTowerAuth() {
        config.getTowerAuth().forEach((login, auth) ->
                towerAuthenticator.addCredential(login, auth.getPassword()));

        if (config.getTowerAuth().isEmpty()) {
            towerAuthenticator.addCredential("admin", "changeme");
        }
    }

    private void registerCommands() {
        commandManager.register(new ServerCommand(serverManager));
        commandManager.register(new EndCommand(this));
    }

    private void startConsoleReader() {
        Thread consoleThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            final CommandSender consoleSender = new CommandSender() {
                @Override
                public void sendMessage(String message) {
                    log.info(message.replaceAll("§[0-9a-fk-or]", ""));
                }

                @Override
                public boolean hasPermission(String permission) {
                    return true;
                }
            };

            while (running.get()) {
                try {
                    if (scanner.hasNextLine()) {
                        String line = scanner.nextLine().trim();
                        if (!line.isEmpty()) {
                            if (!commandManager.execute(consoleSender, line)) {
                                log.info("Unknown command: {}", line);
                            }
                        }
                    }
                } catch (Exception e) {
                    if (running.get()) {
                        log.error("Console reader error", e);
                    }
                }
            }
        }, "Console-Reader");
        consoleThread.setDaemon(true);
        consoleThread.start();
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public void shutdown() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        log.info("Shutting down MinecraftProxy...");

        // Disconnect all players
        playerManager.getAllPlayers().forEach(player ->
                player.disconnect("§cProxy is shutting down"));

        // Disable plugins
        pluginManager.disablePlugins();

        // Shutdown systems
        scheduler.shutdown();
        eventBus.shutdown();
        towerServer.shutdown();
        networkServer.shutdown();

        log.info("MinecraftProxy shut down successfully");
    }
}
