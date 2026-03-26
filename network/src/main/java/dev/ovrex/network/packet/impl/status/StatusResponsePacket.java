package dev.ovrex.network.packet.impl.status;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StatusResponsePacket implements Packet {

    private String jsonResponse;

    @Override
    public int getId() {
        return 0x00;
    }

    /**
     * 2<sup>15</sup>-1
    **/
    @Override
    public void read(PacketBuffer buffer) {
        this.jsonResponse = buffer.readString(32767);
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeString(jsonResponse);
    }
}
