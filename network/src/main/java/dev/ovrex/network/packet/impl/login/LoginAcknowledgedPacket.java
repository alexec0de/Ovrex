package dev.ovrex.network.packet.impl.login;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;

public class LoginAcknowledgedPacket implements Packet {

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
