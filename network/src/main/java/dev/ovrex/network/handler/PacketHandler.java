package dev.ovrex.network.handler;

import dev.ovrex.network.connection.MinecraftConnection;
import dev.ovrex.network.packet.Packet;

public interface PacketHandler {
    void handle(MinecraftConnection connection, Packet packet);

    void onDisconnect(MinecraftConnection connection);

    default void onException(MinecraftConnection connection, Throwable cause) {
    }
}
