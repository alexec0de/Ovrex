package dev.ovrex.network.packet.impl.play;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ServerboundChatCommandPacket implements Packet {

    private String command;

    @Override
    public int getId() {
        return 0x05;
    }

    @Override
    public void read(PacketBuffer buffer) {
        this.command = buffer.readString(256);
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeString(command);
    }
}
