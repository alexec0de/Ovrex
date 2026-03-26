package dev.ovrex.network.packet.impl.status;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;

public class StatusRequestPacket implements Packet {
    @Override
    public int getId() {
        return 0x00;
    }

    @Override
    public void read(PacketBuffer buffer) {

    }

    @Override
    public void write(PacketBuffer buffer) {

    }
}
