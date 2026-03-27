package dev.ovrex.tower.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TowerCredentials {
    private final String login;
    private final String password;
}
