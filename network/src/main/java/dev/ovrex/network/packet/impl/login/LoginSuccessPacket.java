package dev.ovrex.network.packet.impl.login;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginSuccessPacket implements Packet {

    private UUID uuid;
    private String username;
    private List<Property> properties;

    public LoginSuccessPacket(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.properties = new ArrayList<>();
    }

    @Override
    public int getId() {
        return 0x02;
    }

    @Override
    public void read(PacketBuffer buffer) {
        this.uuid = buffer.readUUID();
        this.username = buffer.readString(16);

        int propertyCount = buffer.readVarInt();
        this.properties = new ArrayList<>();

        for (int i = 0; i < propertyCount; i++) {
            String name = buffer.readString(32767);
            String value = buffer.readString(32767);
            boolean isSigned = buffer.readBoolean();
            String signature = null;
            if (isSigned) {
                signature = buffer.readString(32767);
            }
            properties.add(new Property(name, value, signature));
        }
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeUUID(uuid);
        buffer.writeString(username);

        if (properties == null) {
            buffer.writeVarInt(0);
        } else {
            buffer.writeVarInt(properties.size());
            for (Property property : properties) {
                buffer.writeString(property.name);
                buffer.writeString(property.value);
                if (property.signature != null) {
                    buffer.writeBoolean(true);
                    buffer.writeString(property.signature);
                } else {
                    buffer.writeBoolean(false);
                }
            }
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Property {
        private final String name;
        private final String value;
        private final String signature;
    }

}
