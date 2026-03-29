package dev.ovrex.core.config;

import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class ConfigLoader {
    private static final String CONFIG_FILE = "config.yml";

    public static OvrexConfig load() {
        final Path configPath = Path.of(CONFIG_FILE);

        if (!Files.exists(configPath)) {
            final OvrexConfig defaultConfig = new OvrexConfig();
            save(defaultConfig);
            log.info("Created default configuration file");
            return defaultConfig;
        }

        try (final InputStream is = Files.newInputStream(configPath)) {
            final Representer representer = new Representer(new DumperOptions());
            representer.getPropertyUtils().setSkipMissingProperties(true);
            final Yaml yaml = new Yaml(new Constructor(OvrexConfig.class, new org.yaml.snakeyaml.LoaderOptions()), representer);
            final OvrexConfig config = yaml.load(is);
            log.info("Configuration loaded");
            return config != null ? config : new OvrexConfig();
        } catch (Exception e) {
            log.error("Failed to load configuration, using defaults", e);
            return new OvrexConfig();
        }
    }

    public static void save(OvrexConfig config) {
        final DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);

        final Representer representer = new Representer(options);
        representer.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        representer.addClassTag(OvrexConfig.class, Tag.MAP);
        representer.addClassTag(OvrexConfig.TowerAuth.class, Tag.MAP);
        representer.addClassTag(OvrexConfig.ServerEntry.class, Tag.MAP);

        final Yaml yaml = new Yaml(representer, options);

        try (final Writer writer = new FileWriter(CONFIG_FILE)) {
            yaml.dump(config, writer);
        } catch (IOException e) {
            log.error("Failed to save configuration", e);
        }
    }
}
