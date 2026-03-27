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
public class LoginStartPacket implements Packet {
    private String username;
    private UUID playerUUID;

    @Override
    public int getId() {
        return 0x00;
    }

    @Override
    public void read(PacketBuffer buffer) {
        this.username = buffer.readString(16);
        if (buffer.readableBytes() >= 16) {
            this.playerUUID = buffer.readUUID();
        }
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeString(username);
        if (playerUUID != null) {
            buffer.writeUUID(playerUUID);
        }
    }
}
