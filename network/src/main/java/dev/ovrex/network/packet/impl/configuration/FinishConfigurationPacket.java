package dev.ovrex.network.packet.impl.configuration;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;

public class FinishConfigurationPacket implements Packet {

    @Override
    public int getId() {
        return 0x03;
    }

    @Override
    public void read(PacketBuffer buffer) {
    }

    @Override
    public void write(PacketBuffer buffer) {
    }
}
