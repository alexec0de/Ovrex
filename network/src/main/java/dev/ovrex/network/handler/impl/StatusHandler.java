package dev.ovrex.network.handler.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.ovrex.network.connection.MinecraftConnection;
import dev.ovrex.network.handler.PacketHandler;
import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.enums.ProtocolVersion;
import dev.ovrex.network.packet.impl.status.PingPacket;
import dev.ovrex.network.packet.impl.status.PongPacket;
import dev.ovrex.network.packet.impl.status.StatusRequestPacket;
import dev.ovrex.network.packet.impl.status.StatusResponsePacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public class StatusHandler implements PacketHandler {
    private final Supplier<Integer> playerCountSupplier;
    private final int maxPlayers;
    private final String motd;

    @Override
    public void handle(MinecraftConnection connection, Packet packet) {
        if (packet instanceof StatusRequestPacket) {
            connection.sendPacket(new StatusResponsePacket(buildStatusResponse().toString()));
        } else if (packet instanceof PingPacket ping) {
            connection.sendPacketAndClose(new PongPacket(ping.getPayload()));
        }
    }

    private JsonObject buildStatusResponse() {
        final JsonObject response = new JsonObject();

        final JsonObject version = new JsonObject();
        version.addProperty("name", "MinecraftProxy 1.20.4");
        version.addProperty("protocol", ProtocolVersion.getLatestProtocol());
        response.add("version", version);

        final JsonObject players = new JsonObject();
        players.addProperty("max", maxPlayers);
        players.addProperty("online", playerCountSupplier.get());
        players.add("sample", new JsonArray());
        response.add("players", players);

        final JsonObject description = new JsonObject();
        description.addProperty("text", motd);
        response.add("description", description);

        return response;
    }

    @Override
    public void onDisconnect(MinecraftConnection connection) {
    }
}
