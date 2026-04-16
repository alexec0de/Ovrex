package dev.ovrex.network.packet;

import dev.ovrex.network.packet.enums.PacketDirection;
import dev.ovrex.network.packet.enums.ProtocolState;
import dev.ovrex.network.packet.impl.configuration.*;
import dev.ovrex.network.packet.impl.handshake.HandshakePacket;
import dev.ovrex.network.packet.impl.login.*;
import dev.ovrex.network.packet.impl.play.*;
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

        register(ProtocolState.LOGIN, PacketDirection.CLIENTBOUND, 0x00, LoginDisconnectPacket::new);
        register(ProtocolState.LOGIN, PacketDirection.CLIENTBOUND, 0x02, LoginSuccessPacket::new);
        register(ProtocolState.LOGIN, PacketDirection.CLIENTBOUND, 0x03, SetCompressionPacket::new);

        register(ProtocolState.LOGIN, PacketDirection.SERVERBOUND, 0x00, LoginStartPacket::new);
        register(ProtocolState.LOGIN, PacketDirection.SERVERBOUND, 0x03, LoginAcknowledgedPacket::new);

        register(ProtocolState.CONFIGURATION, PacketDirection.CLIENTBOUND, 0x03, FinishConfigurationPacket::new);
        register(ProtocolState.CONFIGURATION, PacketDirection.CLIENTBOUND, 0x0E, ClientboundKnownPacksPacket::new);

        register(ProtocolState.CONFIGURATION, PacketDirection.SERVERBOUND, 0x03, FinishConfigurationAckPacket::new);

        register(ProtocolState.PLAY, PacketDirection.CLIENTBOUND, 0x1D, DisconnectPacket::new);
        register(ProtocolState.PLAY, PacketDirection.CLIENTBOUND, 0x27, KeepAliveClientboundPacket::new);
        register(ProtocolState.PLAY, PacketDirection.CLIENTBOUND, 0x2B, JoinGamePacket::new);
        register(ProtocolState.PLAY, PacketDirection.CLIENTBOUND, 0x47, RespawnPacket::new);
        register(ProtocolState.PLAY, PacketDirection.CLIENTBOUND, 0x70, StartConfigurationPacket::new);

        register(ProtocolState.PLAY, PacketDirection.SERVERBOUND, 0x05, ServerboundChatCommandPacket::new);
        register(ProtocolState.PLAY, PacketDirection.SERVERBOUND, 0x07, ServerboundChatPacket::new);
        register(ProtocolState.PLAY, PacketDirection.SERVERBOUND, 0x1A, KeepAliveServerboundPacket::new);
        register(ProtocolState.PLAY, PacketDirection.SERVERBOUND, 0x0E, AcknowledgeConfigurationPacket::new);
    }

    private static void register(ProtocolState state, PacketDirection direction, int id,
                                 Supplier<? extends Packet> supplier) {
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
