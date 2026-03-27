package dev.ovrex.network.packet.impl.play;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class UnknownPacket implements Packet {
    private int packetId;
    private byte[] data;

    public UnknownPacket(int packetId, byte[] data) {
        this.packetId = packetId;
        this.data = data;
    }

    @Override
    public int getId() {
        return packetId;
    }

    @Override
    public void read(PacketBuffer buffer) {
        this.data = buffer.readRemainingBytes();
    }

    @Override
    public void write(PacketBuffer buffer) {
        if (data != null) {
            buffer.writeBytes(data);
        }
    }
}
