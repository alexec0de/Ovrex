package dev.ovrex.network.handler.impl;

import dev.ovrex.network.connection.MinecraftConnection;
import dev.ovrex.network.connection.PlayerConnection;
import dev.ovrex.network.handler.PacketHandler;
import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.impl.play.KeepAlivePacket;
import dev.ovrex.network.packet.impl.play.KeepAliveServerboundPacket;
import dev.ovrex.network.packet.impl.play.ServerboundChatCommandPacket;
import dev.ovrex.network.packet.impl.play.ServerboundChatPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@Slf4j
@RequiredArgsConstructor
public class PlayHandler implements PacketHandler {

    private final PlayerConnection playerConnection;
    private final BiConsumer<PlayerConnection, Packet> packetForwarder;
    private final Runnable onDisconnectCallback;
    private final BiFunction<PlayerConnection, String, Boolean> commandHandler;

    @Override
    public void handle(MinecraftConnection connection, Packet packet) {
        if (packet instanceof KeepAliveServerboundPacket) {
            if (playerConnection.hasBackend()) {
                playerConnection.getBackendConnection().sendPacket(packet);
            }
            return;
        }

        if (packet instanceof ServerboundChatCommandPacket chatCommand) {
            final String command = chatCommand.getCommand();
            log.debug("Player {} executed command: /{}", playerConnection.getUsername(), command);

            if (commandHandler != null && commandHandler.apply(playerConnection, command)) {
                return;
            }

            packetForwarder.accept(playerConnection, packet);
            return;
        }

        if (packet instanceof ServerboundChatPacket chatPacket) {
            final String message = chatPacket.getMessage();

            if (message.startsWith("/")) {
                final String command = message.substring(1);
                log.debug("Player {} chat command: /{}", playerConnection.getUsername(), command);

                if (commandHandler != null && commandHandler.apply(playerConnection, command)) {
                    return;
                }
            }

            packetForwarder.accept(playerConnection, packet);
            return;
        }

        packetForwarder.accept(playerConnection, packet);
    }

    @Override
    public void onDisconnect(MinecraftConnection connection) {
        log.info("Player disconnected: {}", playerConnection.getUsername());
        onDisconnectCallback.run();
    }

    @Override
    public void onException(MinecraftConnection connection, Throwable cause) {
        log.warn("Exception for {}: {}", playerConnection.getUsername(), cause.getMessage());
    }
}
