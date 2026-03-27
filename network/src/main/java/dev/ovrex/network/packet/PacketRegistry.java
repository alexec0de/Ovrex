package dev.ovrex.network.packet;

import dev.ovrex.network.packet.enums.PacketDirection;
import dev.ovrex.network.packet.enums.ProtocolState;
import dev.ovrex.network.packet.impl.handshake.HandshakePacket;
import dev.ovrex.network.packet.impl.login.LoginDisconnectPacket;
import dev.ovrex.network.packet.impl.login.LoginStartPacket;
import dev.ovrex.network.packet.impl.login.LoginSuccessPacket;
import dev.ovrex.network.packet.impl.login.SetCompressionPacket;
import dev.ovrex.network.packet.impl.play.DisconnectPacket;
import dev.ovrex.network.packet.impl.play.JoinGamePacket;
import dev.ovrex.network.packet.impl.play.KeepAlivePacket;
import dev.ovrex.network.packet.impl.play.RespawnPacket;
import dev.ovrex.network.packet.impl.status.PingPacket;
import dev.ovrex.network.packet.impl.status.PongPacket;
import dev.ovrex.network.packet.impl.status.StatusRequestPacket;
import dev.ovrex.network.packet.impl.status.StatusResponsePacket;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PacketRegistry {

    private static final Map<ProtocolState, Map<PacketDirection, Map<Integer, Supplier<? extends Packet>>>> REGISTRY =
            new EnumMap<>(ProtocolState.class);

    static {
        register(ProtocolState.HANDSHAKE, PacketDirection.SERVERBOUND, 0x00, HandshakePacket::new);

        register(ProtocolState.STATUS, PacketDirection.SERVERBOUND, 0x00, StatusRequestPacket::new);
        register(ProtocolState.STATUS, PacketDirection.SERVERBOUND, 0x01, PingPacket::new);
        register(ProtocolState.STATUS, PacketDirection.CLIENTBOUND, 0x00, StatusResponsePacket::new);
        register(ProtocolState.STATUS, PacketDirection.CLIENTBOUND, 0x01, PongPacket::new);

        register(ProtocolState.LOGIN, PacketDirection.SERVERBOUND, 0x00, LoginStartPacket::new);
        register(ProtocolState.LOGIN, PacketDirection.CLIENTBOUND, 0x00, LoginDisconnectPacket::new);
        register(ProtocolState.LOGIN, PacketDirection.CLIENTBOUND, 0x02, LoginSuccessPacket::new);
        register(ProtocolState.LOGIN, PacketDirection.CLIENTBOUND, 0x03, SetCompressionPacket::new);

        register(ProtocolState.PLAY, PacketDirection.CLIENTBOUND, 0x1A, DisconnectPacket::new);
        register(ProtocolState.PLAY, PacketDirection.CLIENTBOUND, 0x24, KeepAlivePacket::new);
        register(ProtocolState.PLAY, PacketDirection.SERVERBOUND, 0x12, KeepAlivePacket::new);
        register(ProtocolState.PLAY, PacketDirection.CLIENTBOUND, 0x28, JoinGamePacket::new);
        register(ProtocolState.PLAY, PacketDirection.CLIENTBOUND, 0x41, RespawnPacket::new);
    }

    private static void register(ProtocolState state, PacketDirection direction, int id, Supplier<? extends Packet> supplier) {
        REGISTRY.computeIfAbsent(state, k -> new EnumMap<>(PacketDirection.class))
                .computeIfAbsent(direction, k -> new HashMap<>())
                .put(id, supplier);
    }

    public static Packet createPacket(ProtocolState state, PacketDirection direction, int id) {
        Map<PacketDirection, Map<Integer, Supplier<? extends Packet>>> directionMap = REGISTRY.get(state);
        if (directionMap == null) return null;

        Map<Integer, Supplier<? extends Packet>> packetMap = directionMap.get(direction);
        if (packetMap == null) return null;

        Supplier<? extends Packet> supplier = packetMap.get(id);
        return supplier != null ? supplier.get() : null;
    }

}
