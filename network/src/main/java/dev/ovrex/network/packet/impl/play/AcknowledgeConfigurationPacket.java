package dev.ovrex.network.packet.impl.play;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AcknowledgeConfigurationPacket implements Packet {

    private static final int ID = 0x0E;

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public void read(PacketBuffer buffer) {}

    @Override
    public void write(PacketBuffer buffer) {}
}