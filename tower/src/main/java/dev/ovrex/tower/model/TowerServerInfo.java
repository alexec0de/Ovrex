package dev.ovrex.tower.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class TowerServerInfo {
    private final String name;
    private final String host;
    private final int port;
    private final String type;
    private final int maxPlayers;
}
