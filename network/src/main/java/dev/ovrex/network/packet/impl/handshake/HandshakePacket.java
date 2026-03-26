package dev.ovrex.network.packet.impl.handshake;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HandshakePacket implements Packet {

    private int protocolVersion;
    private String serverAddress;
    private int serverPort;
    private int nextState;

    @Override
    public int getId() {
        return 0x00;
    }

    @Override
    public void read(PacketBuffer buffer) {
        this.protocolVersion = buffer.readVarInt();
        this.serverAddress = buffer.readString(255);
        this.serverPort = buffer.readUnsignedShort();
        this.nextState = buffer.readVarInt();
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeVarInt(protocolVersion);
        buffer.writeString(serverAddress);
        buffer.writeShort(serverPort);
        buffer.writeVarInt(nextState);
    }
}
