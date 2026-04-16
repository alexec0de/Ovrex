package dev.ovrex.network.packet.impl.play;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class StartConfigurationPacket implements Packet {



    @Override
    public int getId() {
        return 0x70;
    }

    @Override
    public void read(PacketBuffer buffer) {}

    @Override
    public void write(PacketBuffer buffer) {}
}