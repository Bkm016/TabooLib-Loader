package io.izzel.taboolib.loader;

import io.izzel.taboolib.loader.util.IO;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStream;
import java.util.Objects;

/**
 * @author sky
 * @since 2020-04-12 22:28
 */
public enum PluginLocale {

    LOAD_NO_INTERNAL,

    LOAD_OFFLINE,

    LOAD_OUTDATED,

    LOAD_OUTDATED_NO_RESTART,

    LOAD_IN_PLUGINS,

    LOAD_COMPAT_MODE,

    LOAD_COMPAT_MODE_UPDATE,

    LOAD_FORGE_MODE,

    LOAD_FAILED,

    LOAD_HOTSWAP_DISABLE,

    LOAD_INVALID_VERSION,

    LOAD_DOWNLOAD,

    LOAD_SUCCESS;

    static FileConfiguration locale;

    static {
        try (InputStream inputStream = PluginLocale.class.getClassLoader().getResourceAsStream("taboolib-loader.yml")) {
            locale = new YamlConfiguration();
            locale.loadFromString(IO.readFully(Objects.requireNonNull(inputStream)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void info(Object... args) {
        for (String message : locale.getStringList(name().toLowerCase().replace("_", "-"))) {
            Bukkit.getLogger().info(IO.replaceWithOrder(message, args));
        }
    }

    public void warn(Object... args) {
        for (String message : locale.getStringList(name().toLowerCase().replace("_", "-"))) {
            Bukkit.getLogger().warning(IO.replaceWithOrder(message, args));
        }
    }
}
