package dev.ovrex.network.packet.impl.play;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DisconnectPacket implements Packet {

    private String reason;

    @Override
    public int getId() {
        return 0x1A;
    }

    @Override
    public void read(PacketBuffer buffer) {
        this.reason = buffer.readString(262144);
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeString(reason);
    }
}
