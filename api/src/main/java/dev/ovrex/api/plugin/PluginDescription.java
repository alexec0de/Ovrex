package dev.ovrex.api.plugin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PluginDescription {

    private final String name;
    private final String version;
    private final String author;
    private final String main;
    private final String description;
}
