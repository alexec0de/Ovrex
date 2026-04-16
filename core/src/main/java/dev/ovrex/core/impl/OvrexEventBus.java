package dev.ovrex.core.impl;

import com.sun.source.util.Plugin;
import dev.ovrex.api.event.*;
import dev.ovrex.api.plugin.OvrexPlugin;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class OvrexEventBus implements EventBus {
    private final Map<Class<? extends Event>, List<RegisteredListener>> listeners = new ConcurrentHashMap<>();
    private final Map<OvrexPlugin, List<Object>> pluginListeners = new ConcurrentHashMap<>();
    private final ExecutorService asyncExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "EventBus-Async");
        t.setDaemon(true);
        return t;
    });

    @Override
    public void register(OvrexPlugin plugin, Listener listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            EventHandler annotation = method.getAnnotation(EventHandler.class);
            if (annotation == null) continue;

            if (method.getParameterCount() != 1) {
                log.warn("Event handler method {} must have exactly one parameter", method.getName());
                continue;
            }

            Class<?> paramType = method.getParameterTypes()[0];
            if (!Event.class.isAssignableFrom(paramType)) {
                log.warn("Event handler parameter must extend Event: {}", method.getName());
                continue;
            }

            @SuppressWarnings("unchecked")
            Class<? extends Event> eventType = (Class<? extends Event>) paramType;

            RegisteredListener registeredListener = new RegisteredListener(
                    plugin, listener, method, annotation.priority());

            listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                    .add(registeredListener);

            pluginListeners.computeIfAbsent(plugin, k -> new CopyOnWriteArrayList<>())
                    .add(listener);

            // Sort by priority
            listeners.get(eventType).sort(Comparator.comparingInt(l -> l.priority.ordinal()));
        }
    }

    @Override
    public void unregister(Listener listener) {
        listeners.values().forEach(list ->
                list.removeIf(rl -> rl.listener == listener));
    }

    @Override
    public void unregisterAll(OvrexPlugin plugin) {
        listeners.values().forEach(list ->
                list.removeIf(rl -> rl.plugin == plugin));
        pluginListeners.remove(plugin);
    }

    @Override
    public <T extends Event> T fire(T event) {
        List<RegisteredListener> registered = listeners.get(event.getClass());
        if (registered == null || registered.isEmpty()) {
            return event;
        }

        for (RegisteredListener rl : registered) {
            try {
                rl.method.setAccessible(true);
                rl.method.invoke(rl.listener, event);
            } catch (Exception e) {
                log.error("Error dispatching event {} to plugin {}",
                        event.getClass().getSimpleName(),
                        rl.plugin.getDescription().getName(), e);
            }
        }

        return event;
    }

    @Override
    public <T extends Event> void fireAsync(T event) {
        asyncExecutor.submit(() -> fire(event));
    }

    public void shutdown() {
        asyncExecutor.shutdown();
    }

    private record RegisteredListener(OvrexPlugin plugin, Object listener, Method method, EventPriority priority) {
    }
}
