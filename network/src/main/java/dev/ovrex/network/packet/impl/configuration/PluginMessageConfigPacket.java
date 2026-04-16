package dev.ovrex.network.packet.impl.configuration;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PluginMessageConfigPacket implements Packet {

    private String channel;
    private byte[] data;
    private int packetId;

    public PluginMessageConfigPacket(int packetId) {
        this.packetId = packetId;
    }

    @Override
    public int getId() {
        return packetId;
    }

    @Override
    public void read(PacketBuffer buffer) {
        this.channel = buffer.readString(32767);
        this.data = buffer.readRemainingBytes();
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeString(channel);
        if (data != null) {
            buffer.writeBytes(data);
        }
    }
}