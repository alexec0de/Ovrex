package dev.ovrex.api;

import dev.ovrex.api.command.CommandManager;
import dev.ovrex.api.event.EventBus;
import dev.ovrex.api.player.PlayerManager;
import dev.ovrex.api.plugin.PluginManager;
import dev.ovrex.api.registry.ServiceRegistry;
import dev.ovrex.api.scheduler.Scheduler;
import dev.ovrex.api.server.ServerManager;

public interface OvrexAPI {
    PlayerManager getPlayerManager();

    ServerManager getServerManager();

    EventBus getEventBus();

    CommandManager getCommandManager();

    PluginManager getPluginManager();

    Scheduler getScheduler();

    ServiceRegistry getServiceRegistry();

    String getVersion();

    void shutdown();
}
