package dev.ovrex.network.connection;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.enums.ProtocolState;
import dev.ovrex.network.pipeline.MinecraftDecoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;

@Getter
public class MinecraftConnection {
    private final Channel channel;
    @Setter
    private volatile ProtocolState protocolState;
    @Setter
    private volatile ConnectionState connectionState;

    public MinecraftConnection(Channel channel) {
        this.channel = channel;
        this.protocolState = ProtocolState.HANDSHAKE;
        this.connectionState = ConnectionState.HANDSHAKE;
    }

    public void sendPacket(Packet packet) {
        if (channel.isActive()) {
            channel.writeAndFlush(packet, channel.voidPromise());
        }
    }

    public void sendPacketAndClose(Packet packet) {
        if (channel.isActive()) {
            channel.writeAndFlush(packet).addListener(ChannelFutureListener.CLOSE);
        }
    }

    public void close() {
        if (channel.isActive()) {
            channel.close();
        }
    }

    public void setDecoderState(ProtocolState state) {
        this.protocolState = state;
        final MinecraftDecoder decoder = channel.pipeline().get(MinecraftDecoder.class);
        if (decoder != null) {
            decoder.setState(state);
        }
    }

    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) channel.remoteAddress();
    }

    public boolean isActive() {
        return channel.isActive();
    }
}
