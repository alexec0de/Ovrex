package dev.ovrex.plugin;



import dev.ovrex.api.OvrexAPI;
import dev.ovrex.api.plugin.OvrexPlugin;
import dev.ovrex.api.plugin.PluginDescription;
import dev.ovrex.api.plugin.PluginManager;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
public class JavaPluginManager implements PluginManager {
    private final Map<String, OvrexPlugin> plugins = new ConcurrentHashMap<>();
    private final Map<String, PluginClassLoader> classLoaders = new ConcurrentHashMap<>();
    private final File pluginsDirectory;
    private final OvrexAPI api;

    public JavaPluginManager(File pluginsDirectory, OvrexAPI api) {
        this.pluginsDirectory = pluginsDirectory;
        this.api = api;

        if (!pluginsDirectory.exists()) {
            pluginsDirectory.mkdirs();
        }
    }

    @Override
    public void loadPlugins() {
        File[] files = pluginsDirectory.listFiles((dir, name) -> name.endsWith(".jar"));
        if (files == null || files.length == 0) {
            log.info("No plugins found in {}", pluginsDirectory.getAbsolutePath());
            return;
        }

        for (File file : files) {
            try {
                loadPlugin(file);
            } catch (Exception e) {
                log.error("Failed to load plugin from {}", file.getName(), e);
            }
        }

        log.info("Loaded {} plugin(s)", plugins.size());
    }

    private void loadPlugin(File file) throws Exception {
        JarFile jarFile = new JarFile(file);
        JarEntry pluginYml = jarFile.getJarEntry("plugin.yml");

        if (pluginYml == null) {
            jarFile.close();
            throw new IllegalArgumentException("Missing plugin.yml in " + file.getName());
        }

        PluginDescription description;
        try (InputStream is = jarFile.getInputStream(pluginYml)) {
            description = PluginDescriptionParser.parse(is);
        }
        jarFile.close();

        if (description.getName() == null || description.getMain() == null) {
            throw new IllegalArgumentException("Plugin name and main class are required");
        }

        PluginClassLoader classLoader = new PluginClassLoader(file, getClass().getClassLoader());
        Class<?> mainClass = classLoader.loadClass(description.getMain());

        if (!OvrexPlugin.class.isAssignableFrom(mainClass)) {
            classLoader.close();
            throw new IllegalArgumentException(description.getMain() + " does not extend Plugin");
        }

        OvrexPlugin plugin = (OvrexPlugin) mainClass.getDeclaredConstructor().newInstance();
        plugin.setApi(api);
        plugin.setDescription(description);
        plugin.setDataFolder(new File(pluginsDirectory, description.getName()));

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        plugins.put(description.getName(), plugin);
        classLoaders.put(description.getName(), classLoader);

        log.info("Loaded plugin: {} v{} by {}", description.getName(), description.getVersion(), description.getAuthor());
    }

    @Override
    public void enablePlugins() {
        plugins.values().forEach(plugin -> {
            try {
                plugin.onEnable();
                log.info("Enabled plugin: {}", plugin.getDescription().getName());
            } catch (Exception e) {
                log.error("Failed to enable plugin: {}", plugin.getDescription().getName(), e);
            }
        });
    }

    @Override
    public void disablePlugins() {
        plugins.values().forEach(plugin -> {
            try {
                plugin.onDisable();
                log.info("Disabled plugin: {}", plugin.getDescription().getName());
            } catch (Exception e) {
                log.error("Failed to disable plugin: {}", plugin.getDescription().getName(), e);
            }
        });

        classLoaders.values().forEach(cl -> {
            try {
                cl.close();
            } catch (Exception e) {
                log.warn("Failed to close class loader", e);
            }
        });

        plugins.clear();
        classLoaders.clear();
    }

    @Override
    public Optional<OvrexPlugin> getPlugin(String name) {
        return Optional.ofNullable(plugins.get(name));
    }

    @Override
    public Collection<OvrexPlugin> getPlugins() {
        return Collections.unmodifiableCollection(plugins.values());
    }
}
