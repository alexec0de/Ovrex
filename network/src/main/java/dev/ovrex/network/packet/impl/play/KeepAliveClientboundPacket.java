package dev.ovrex.network.packet.impl.play;


import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KeepAliveClientboundPacket implements Packet {

    private long keepAliveId;

    @Override
    public int getId() {
        return 0x27;
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