package io.izzel.taboolib.loader;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;

/**
 * TabooLibLoader
 * io.izzel.taboolib.loader.PluginFile
 *
 * @author sky
 * @since 2021/5/19 10:56 下午
 */
public class PluginFile {

    enum Type {

        SELF, PLUGIN_MODE, LEGACY_VERSION
    }

    private final File file;
    private final FileConfiguration fileConfiguration;
    private final PluginDescriptionFile pluginDescriptionFile;

    public PluginFile(File file, PluginDescriptionFile pluginDescriptionFile, String source) {
        this.file = file;
        this.fileConfiguration = new YamlConfiguration();
        this.pluginDescriptionFile = pluginDescriptionFile;
        try {
            this.fileConfiguration.loadFromString(source);
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return pluginDescriptionFile.getName();
    }

    public File getFile() {
        return file;
    }

    public FileConfiguration getFileConfiguration() {
        return fileConfiguration;
    }

    public PluginDescriptionFile getPluginDescriptionFile() {
        return pluginDescriptionFile;
    }
}
