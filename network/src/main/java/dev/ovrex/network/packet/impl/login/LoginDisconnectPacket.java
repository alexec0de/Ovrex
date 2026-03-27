package dev.ovrex.network.packet.impl.login;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginDisconnectPacket implements Packet {
    private String reason;

    @Override
    public int getId() {
        return 0x00;
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
