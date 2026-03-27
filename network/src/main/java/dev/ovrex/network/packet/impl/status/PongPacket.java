package dev.ovrex.network.packet.impl.status;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PongPacket implements Packet {

    private long payload;

    @Override
    public int getId() {
        return 0x01;
    }

    @Override
    public void read(PacketBuffer buffer) {
        this.payload = buffer.readLong();
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeLong(payload);
    }
}
