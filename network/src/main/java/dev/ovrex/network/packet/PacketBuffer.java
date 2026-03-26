package dev.ovrex.network.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class PacketBuffer {
    @Getter
    private final ByteBuf handle;

    public PacketBuffer(ByteBuf handle) {
        this.handle = handle;
    }

    public static PacketBuffer alloc() {
        return new PacketBuffer(ByteBufAllocator.DEFAULT.buffer());
    }

    public int readVarInt() {
        int value = 0;
        int position = 0;
        byte currentByte;

        do {
            currentByte = handle.readByte();
            value |= (currentByte & 0x7F) << position;
            position += 7;

            if (position >= 32) {
                throw new RuntimeException("VarInt is too big");
            }
        } while ((currentByte & 0x80) != 0);

        return value;
    }

    public void writeVarInt(int value) {
        while ((value & ~0x7F) != 0) {
            handle.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        handle.writeByte(value);
    }

    public String readString() {
        return readString(32767);
    }

    public String readString(int maxLength) {
        int length = readVarInt();
        if (length > maxLength * 4) {
            throw new RuntimeException("String too long: " + length + " > " + maxLength * 4);
        }
        byte[] bytes = new byte[length];
        handle.readBytes(bytes);
        String str = new String(bytes, StandardCharsets.UTF_8);
        if (str.length() > maxLength) {
            throw new RuntimeException("String too long: " + str.length() + " > " + maxLength);
        }
        return str;
    }

    public void writeString(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(bytes.length);
        handle.writeBytes(bytes);
    }

    public UUID readUUID() {
        return new UUID(handle.readLong(), handle.readLong());
    }

    public void writeUUID(UUID uuid) {
        handle.writeLong(uuid.getMostSignificantBits());
        handle.writeLong(uuid.getLeastSignificantBits());
    }

    public int readUnsignedShort() {
        return handle.readUnsignedShort();
    }

    public void writeShort(int value) {
        handle.writeShort(value);
    }

    public long readLong() {
        return handle.readLong();
    }

    public void writeLong(long value) {
        handle.writeLong(value);
    }

    public byte readByte() {
        return handle.readByte();
    }

    public void writeByte(int value) {
        handle.writeByte(value);
    }

    public int readInt() {
        return handle.readInt();
    }

    public void writeInt(int value) {
        handle.writeInt(value);
    }

    public boolean readBoolean() {
        return handle.readBoolean();
    }

    public void writeBoolean(boolean value) {
        handle.writeBoolean(value);
    }

    public float readFloat() {
        return handle.readFloat();
    }

    public void writeFloat(float value) {
        handle.writeFloat(value);
    }

    public double readDouble() {
        return handle.readDouble();
    }

    public void writeDouble(double value) {
        handle.writeDouble(value);
    }

    public byte[] readRemainingBytes() {
        byte[] bytes = new byte[handle.readableBytes()];
        handle.readBytes(bytes);
        return bytes;
    }

    public void writeBytes(byte[] bytes) {
        handle.writeBytes(bytes);
    }

    public int readableBytes() {
        return handle.readableBytes();
    }

    public void release() {
        handle.release();
    }
}
