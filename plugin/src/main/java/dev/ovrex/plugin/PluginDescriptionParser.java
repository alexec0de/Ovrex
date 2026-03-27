package dev.ovrex.plugin;

import dev.ovrex.api.plugin.PluginDescription;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class PluginDescriptionParser {
    private static final Yaml YAML = new Yaml();

    public static PluginDescription parse(InputStream inputStream) {
        Map<String, Object> data = YAML.load(inputStream);

        return PluginDescription.builder()
                .name((String) data.get("name"))
                .version((String) data.getOrDefault("version", "1.0.0"))
                .author((String) data.getOrDefault("author", "Unknown"))
                .main((String) data.get("main"))
                .description((String) data.getOrDefault("description", ""))
                .build();
    }
}
