package dev.ovrex.network.pipeline;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;
import dev.ovrex.network.packet.PacketRegistry;
import dev.ovrex.network.packet.enums.PacketDirection;
import dev.ovrex.network.packet.enums.ProtocolState;
import dev.ovrex.network.packet.impl.play.UnknownPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MinecraftDecoder extends MessageToMessageDecoder<ByteBuf> {
    private final PacketDirection direction;
    @Setter
    private volatile ProtocolState state;

    public MinecraftDecoder(PacketDirection direction, ProtocolState initialState) {
        this.direction = direction;
        this.state = initialState;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        if (!msg.isReadable()) {
            return;
        }

        final PacketBuffer buffer = new PacketBuffer(msg);
        final int packetId = buffer.readVarInt();

        Packet packet = PacketRegistry.createPacket(state, direction, packetId);

        if (packet != null) {
            try {
                packet.read(buffer);
            } catch (Exception e) {
                log.warn("Error decoding packet 0x{} in state {}", Integer.toHexString(packetId), state, e);
                return;
            }
        } else {
            byte[] remaining = buffer.readRemainingBytes();
            packet = new UnknownPacket(packetId, remaining);
        }

        out.add(packet);
    }
}
