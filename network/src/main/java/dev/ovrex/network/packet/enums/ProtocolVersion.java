package dev.ovrex.network.packet.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProtocolVersion {
    MC_1_20_4(765, "1.20.4"),
    MC_1_20_5(766, "1.20.5"),
    MC_1_21(767, "1.21"),
    MC_1_21_2(768, "1.21.2"),
    MC_1_21_3(768, "1.21.3"),
    MC_1_21_4(769, "1.21.4");

    private final int protocol;
    private final String name;

    public static ProtocolVersion fromProtocol(int protocol) {
        for (ProtocolVersion version : values()) {
            if (version.protocol == protocol) {
                return version;
            }
        }
        return MC_1_21_4;
    }

    public static int getLatestProtocol() {
        return MC_1_21_4.protocol;
    }

    public static String getLatestName() {
        return MC_1_21_4.name;
    }
}

