package dev.ovrex.network.packet.impl.play;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class RespawnPacket implements Packet {

    private byte[] rawData;

    @Override
    public int getId() {
        return 0x41;
    }

    @Override
    public void read(PacketBuffer buffer) {
        this.rawData = buffer.readRemainingBytes();
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeBytes(rawData);
    }
}
