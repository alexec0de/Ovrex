package dev.ovrex.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class PluginClassLoader extends URLClassLoader {

    public PluginClassLoader(File jarFile, ClassLoader parent) throws MalformedURLException {
        super(new URL[]{jarFile.toURI().toURL()}, parent);
    }
}
