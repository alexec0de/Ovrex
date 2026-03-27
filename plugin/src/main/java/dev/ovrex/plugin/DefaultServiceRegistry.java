package dev.ovrex.plugin;

import dev.ovrex.api.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DefaultServiceRegistry implements ServiceRegistry {
    private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> void register(Class<T> serviceClass, T implementation) {
        services.put(serviceClass, implementation);
        log.debug("Service registered: {}", serviceClass.getSimpleName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(Class<T> serviceClass) {
        return Optional.ofNullable((T) services.get(serviceClass));
    }

    @Override
    public <T> void unregister(Class<T> serviceClass) {
        services.remove(serviceClass);
    }
}
