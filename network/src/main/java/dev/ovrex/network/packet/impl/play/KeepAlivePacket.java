package dev.ovrex.network.packet.impl.play;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class KeepAlivePacket implements Packet {
    private long keepAliveId;
    @Setter
    private int packetId = 0x27;

    public KeepAlivePacket(long keepAliveId) {
        this.keepAliveId = keepAliveId;
    }

    @Override
    public int getId() {
        return 0x24;
    }

    @Override
    public void read(PacketBuffer buffer) {
        this.keepAliveId = buffer.readLong();
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeLong(keepAliveId);
    }
}
