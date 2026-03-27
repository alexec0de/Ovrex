package dev.ovrex.network.packet.impl.play;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SystemChatMessagePacket implements Packet {
    private String jsonMessage;
    private boolean overlay;

    @Override
    public int getId() {
        return 0x67;
    }

    @Override
    public void read(PacketBuffer buffer) {
        this.jsonMessage = buffer.readString(262144);
        this.overlay = buffer.readBoolean();
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeString(jsonMessage);
        buffer.writeBoolean(overlay);
    }
}
