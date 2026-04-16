package dev.ovrex.network.packet.impl.configuration;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ClientboundKnownPacksPacket implements Packet {

    private byte[] rawData;

    @Override
    public int getId() {
        return 0x0E;
    }

    @Override
    public void read(PacketBuffer buffer) {
        this.rawData = buffer.readRemainingBytes();
    }

    @Override
    public void write(PacketBuffer buffer) {
        if (rawData != null) {
            buffer.writeBytes(rawData);
        }
    }
}
