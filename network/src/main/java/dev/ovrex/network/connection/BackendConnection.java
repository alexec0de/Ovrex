package dev.ovrex.network.connection;

import dev.ovrex.network.backend.BackendHandler;
import dev.ovrex.network.packet.Packet;
import io.netty.channel.Channel;
import lombok.Getter;

@Getter
public class BackendConnection {
    private final MinecraftConnection connection;
    private final String serverName;
    private final Channel channel;

    public BackendConnection(Channel channel, String serverName) {
        this.connection = new MinecraftConnection(channel);
        this.serverName = serverName;
        this.channel = channel;
    }

    public void pause() {
        BackendHandler handler = channel.pipeline().get(BackendHandler.class);
        if (handler != null) {
            handler.pause();
        }
    }

    public void sendPacket(Packet packet) {
        connection.sendPacket(packet);
    }

    public boolean isActive() {
        return connection.isActive();
    }

    public void close() {
        connection.close();
    }
}
