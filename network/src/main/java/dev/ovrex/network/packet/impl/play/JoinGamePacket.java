package dev.ovrex.network.packet.impl.play;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class JoinGamePacket implements Packet {
    private int entityId;
    private byte[] rawData;

    @Override
    public int getId() {
        return 0x28;
    }

    @Override
    public void read(PacketBuffer buffer) {
        this.entityId = buffer.readInt();
        this.rawData = buffer.readRemainingBytes();
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeInt(entityId);
        buffer.writeBytes(rawData);
    }
}
