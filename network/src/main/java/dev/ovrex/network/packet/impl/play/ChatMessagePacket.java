package dev.ovrex.network.packet.impl.play;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ChatMessagePacket implements Packet {
    private String message;

    @Override
    public int getId() {
        return 0x67;
    }

    @Override
    public void read(PacketBuffer buffer) {
        this.message = buffer.readString(256);
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeString(message);
    }
}
