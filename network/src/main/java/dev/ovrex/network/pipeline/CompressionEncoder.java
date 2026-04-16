package dev.ovrex.network.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.zip.Deflater;

@Slf4j
public class CompressionEncoder extends MessageToByteEncoder<ByteBuf> {

    @Setter
    private int threshold;
    private final Deflater deflater = new Deflater();

    public CompressionEncoder(int threshold) {
        this.threshold = threshold;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        int uncompressedLength = msg.readableBytes();

        if (uncompressedLength < threshold) {
            VarIntUtility.writeVarInt(out, 0);
            out.writeBytes(msg);
            return;
        }

        byte[] input = new byte[uncompressedLength];
        msg.readBytes(input);

        deflater.reset();
        deflater.setInput(input);
        deflater.finish();

        byte[] compressed = new byte[uncompressedLength];
        int compressedLength = deflater.deflate(compressed);

        VarIntUtility.writeVarInt(out, uncompressedLength);
        out.writeBytes(compressed, 0, compressedLength);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        deflater.end();
    }
}
