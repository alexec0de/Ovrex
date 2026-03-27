package dev.ovrex.network.packet.impl.login;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class EncryptionRequestPacket implements Packet {
    private String serverId;
    private byte[] publicKey;
    private byte[] verifyToken;

    @Override
    public int getId() {
        return 0x01;
    }

    @Override
    public void read(PacketBuffer buffer) {
        this.serverId = buffer.readString(20);
        int pkLen = buffer.readVarInt();
        this.publicKey = new byte[pkLen];
        buffer.getHandle().readBytes(publicKey);
        int vtLen = buffer.readVarInt();
        this.verifyToken = new byte[vtLen];
        buffer.getHandle().readBytes(verifyToken);
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeString(serverId);
        buffer.writeVarInt(publicKey.length);
        buffer.writeBytes(publicKey);
        buffer.writeVarInt(verifyToken.length);
        buffer.writeBytes(verifyToken);
    }
}
