package dev.ovrex.network.packet.impl.login;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginSuccessPacket implements Packet {

    private UUID uuid;
    private String username;

    @Override
    public int getId() {
        return 0x02;
    }

    @Override
    public void read(PacketBuffer buffer) {
        this.uuid = buffer.readUUID();
        this.username = buffer.readString(16);
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeUUID(uuid);
        buffer.writeString(username);
    }

}
