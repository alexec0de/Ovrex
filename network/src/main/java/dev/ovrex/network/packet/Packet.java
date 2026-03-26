package dev.ovrex.network.packet;

public interface Packet {
    int getId();

    void read(PacketBuffer buffer);

    void write(PacketBuffer buffer);

    default int expectedMaxLength() {
        return 2097152;
    }
}
