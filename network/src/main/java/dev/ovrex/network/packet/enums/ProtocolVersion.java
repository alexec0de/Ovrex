package dev.ovrex.network.packet.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProtocolVersion {
    MC_1_19_4(762, "1.19.4"),
    MC_1_20(763, "1.20"),
    MC_1_20_1(763, "1.20.1"),
    MC_1_20_2(764, "1.20.2"),
    MC_1_20_3(765, "1.20.3"),
    MC_1_20_4(765, "1.20.4"),
    MC_1_21(767, "1.21");

    private final int protocol;
    private final String name;

    public static ProtocolVersion fromProtocol(int protocol) {
        for (ProtocolVersion version : values()) {
            if (version.protocol == protocol) {
                return version;
            }
        }
        return MC_1_20_4;
    }

    public static int getLatestProtocol() {
        return MC_1_21.protocol;
    }
}

