package dev.ovrex.api.plugin;

import com.sun.source.util.Plugin;

import java.util.Collection;
import java.util.Optional;

public interface PluginManager {

    void loadPlugins();

    void enablePlugins();

    void disablePlugins();

    Optional<Plugin> getPlugin(String name);

    Collection<Plugin> getPlugins();

}
