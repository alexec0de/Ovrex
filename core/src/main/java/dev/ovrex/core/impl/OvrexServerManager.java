package dev.ovrex.core.impl;

import dev.ovrex.api.server.BackendServer;
import dev.ovrex.api.server.ServerManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class OvrexServerManager implements ServerManager {
    private final Map<String, OvrexBackendServer> servers = new ConcurrentHashMap<>();
    @Getter
    @Setter
    private volatile String defaultServerName;

    @Override
    public void registerServer(String name, InetSocketAddress address, String type) {
        OvrexBackendServer server = new OvrexBackendServer(name, address, type);
        servers.put(name, server);
        log.info("Server registered: {} -> {}:{} [{}]", name, address.getHostString(), address.getPort(), type);
    }

    @Override
    public void unregisterServer(String name) {
        final OvrexBackendServer removed = servers.remove(name);
        if (removed != null) {
            log.info("Server unregistered: {}", name);
        }
    }

    @Override
    public Optional<BackendServer> getServer(String name) {
        return Optional.ofNullable(servers.get(name));
    }

    @Override
    public Collection<BackendServer> getAllServers() {
        return Collections.unmodifiableCollection(servers.values());
    }

    @Override
    public Optional<BackendServer> getDefaultServer() {
        if (defaultServerName == null) return Optional.empty();
        return getServer(defaultServerName);
    }

}
