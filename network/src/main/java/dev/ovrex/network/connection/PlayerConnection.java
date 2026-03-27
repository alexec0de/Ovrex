package dev.ovrex.network.connection;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter @Getter
public class PlayerConnection {
    private final MinecraftConnection clientConnection;
    private String username;
    private UUID uuid;
    private int protocolVersion;
    private volatile BackendConnection backendConnection;
    private volatile String currentServerName;

    public PlayerConnection(MinecraftConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    public boolean hasBackend() {
        return backendConnection != null && backendConnection.isActive();
    }

    public void disconnectFromBackend() {
        if (backendConnection != null) {
            backendConnection.close();
            backendConnection = null;
        }
    }
}
