package dev.ovrex.api.plugin;

import dev.ovrex.api.OvrexAPI;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

public abstract class OvrexPlugin {

    @Getter @Setter
    private OvrexAPI api;

    @Getter @Setter
    private PluginDescription description;

    @Getter @Setter
    private File dataFolder;

    public abstract void onEnable();
    public abstract void onDisable();
}
