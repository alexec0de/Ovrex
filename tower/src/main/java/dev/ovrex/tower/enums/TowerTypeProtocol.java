package dev.ovrex.tower.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public enum TowerTypeProtocol {

    AUTH("auth"),
    REGISTER("register"),
    HEARTBEAT("heartbeat"),
    DISCONNECT("disconnect"),
    RESPONSE("response");

    private final String name;
}
