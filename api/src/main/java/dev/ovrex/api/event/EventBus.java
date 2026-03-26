package dev.ovrex.api.event;

import dev.ovrex.api.plugin.OvrexPlugin;

public interface EventBus {
    void register(OvrexPlugin plugin, Object listener);

    void unregister(Object listener);

    void unregisterAll(OvrexPlugin plugin);

    <T extends Event> T fire(T event);

    <T extends Event> void fireAsync(T event);
}
