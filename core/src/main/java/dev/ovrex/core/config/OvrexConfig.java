package dev.ovrex.core.config;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class OvrexConfig {
    private String bindAddress = "0.0.0.0";
    private int bindPort = 25577;
    private boolean onlineMode = false;
    private int maxPlayers = 500;
    private String motd = "§bOvrex server";
    private String defaultServer = "lobby";

    private String towerBindAddress = "0.0.0.0";
    private int towerPort = 25580;

    private Map<String, TowerAuth> towerAuth = new HashMap<>();
    private Map<String, ServerEntry> servers = new HashMap<>();

    private String forwardingMode = "none"; // none, legacy, modern

    @Getter
    @Setter
    public static class TowerAuth {
        private String password;
    }

    @Getter
    @Setter
    public static class ServerEntry {
        private String address;
        private int port;
        private String type = "default";
    }
}
