package dev.ovrex.network.packet.impl.login;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SetCompressionPacket implements Packet {
    private int threshold;

    @Override
    public int getId() {
        return 0x03;
    }

    @Override
    public void read(PacketBuffer buffer) {
        this.threshold = buffer.readVarInt();
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeVarInt(threshold);
    }
}
