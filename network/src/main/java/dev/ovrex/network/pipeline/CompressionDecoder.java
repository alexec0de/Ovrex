package dev.ovrex.network.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.zip.Inflater;

@Slf4j
public class CompressionDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Setter
    private int threshold;
    private final Inflater inflater = new Inflater();

    public CompressionDecoder(int threshold) {
        this.threshold = threshold;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        if (msg.readableBytes() == 0) {
            return;
        }

        int dataLength = VarIntUtility.readVarInt(msg);

        if (dataLength == 0) {
            out.add(msg.readRetainedSlice(msg.readableBytes()));
            return;
        }

        byte[] compressed = new byte[msg.readableBytes()];
        msg.readBytes(compressed);

        inflater.reset();
        inflater.setInput(compressed);

        byte[] decompressed = new byte[dataLength];
        int resultLength = inflater.inflate(decompressed);

        if (resultLength != dataLength) {
            throw new RuntimeException("Decompressed length mismatch: expected " + dataLength + " got " + resultLength);
        }

        out.add(Unpooled.wrappedBuffer(decompressed));
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        inflater.end();
    }
}