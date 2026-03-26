package dev.ovrex.api.server;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Optional;

public interface ServerManager {
    void registerServer(String name, InetSocketAddress address, String type);

    void unregisterServer(String name);

    Optional<BackendServer> getServer(String name);

    Collection<BackendServer> getAllServers();

    Optional<BackendServer> getDefaultServer();
}
