package dev.ovrex.network.packet.impl.play;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ServerboundChatPacket implements Packet {

    private String message;
    private byte[] remaining;

    @Override
    public int getId() {
        return 0x07;
    }

    @Override
    public void read(PacketBuffer buffer) {
        this.message = buffer.readString(256);
        this.remaining = buffer.readRemainingBytes();
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeString(message);
        if (remaining != null) {
            buffer.writeBytes(remaining);
        }
    }
}