package dev.ovrex.api.plugin;



import java.util.Collection;
import java.util.Optional;

public interface PluginManager {

    void loadPlugins();

    void enablePlugins();

    void disablePlugins();

    Optional<OvrexPlugin> getPlugin(String name);

    Collection<OvrexPlugin> getPlugins();

}
