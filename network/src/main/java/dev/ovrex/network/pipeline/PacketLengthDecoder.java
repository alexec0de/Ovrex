package dev.ovrex.network.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class PacketLengthDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (!in.isReadable()) {
            return;
        }

        in.markReaderIndex();

        int length;
        try {
            length = readVarIntOrReset(in);
        } catch (Exception e) {
            return;
        }

        if (length < 0) {
            return;
        }

        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }

        out.add(in.readRetainedSlice(length));
    }

    private int readVarIntOrReset(ByteBuf buf) {
        int value = 0;
        int position = 0;

        while (buf.isReadable()) {
            byte currentByte = buf.readByte();
            value |= (currentByte & 0x7F) << position;
            position += 7;

            if (position >= 32) {
                throw new RuntimeException("VarInt too big");
            }

            if ((currentByte & 0x80) == 0) {
                return value;
            }
        }

        buf.resetReaderIndex();
        return -1;
    }
}
