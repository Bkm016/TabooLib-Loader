package io.izzel.taboolib.loader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.izzel.taboolib.loader.util.ILoader;
import io.izzel.taboolib.loader.util.IO;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.NumberConversions;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

/**
 * @author sky
 * @since 2020-04-12 22:43
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class PluginHandle {

    private static File pluginModeFile;
    private static File pluginOriginFile;
    private static PluginDescriptionFile pluginModeDescriptionFile;
    private static PluginDescriptionFile pluginOriginDescriptionFile;

    private static final List<String> legacy = new ArrayList<>();

    private static double tabooLibVersion = -1;

    /**
     * 版本信息获取地址
     * 优先采用国内地址
     * 防止部分机器封禁海外访问
     */
    public static final String[][] URL = {
            {
                    "https://skymc.oss-cn-shanghai.aliyuncs.com/plugins/latest.json",
                    "https://skymc.oss-cn-shanghai.aliyuncs.com/plugins/TabooLib.jar"
            },
            {
                    "https://api.github.com/repos/TabooLib/TabooLib/releases/latest",
                    "https://github.com/TabooLib/TabooLib/releases/latest/download/TabooLib.jar",
            },
    };

    public static boolean downloadFile() {
        tabooLibVersion = -1;
        PluginLocale.LOAD_DOWNLOAD.info();
        String[] newVersion = getCurrentVersion();
        return newVersion != null && IO.downloadFile(newVersion[2], IO.file(PluginBoot.getTabooLibFile()));
    }

    public static boolean isLoaded() {
        return ILoader.forName("io.izzel.taboolib.TabooLib", false, Bukkit.class.getClassLoader()) != null;
    }

    public static double getVersion() {
        if (tabooLibVersion != -1) {
            return tabooLibVersion;
        }
        try (ZipFile zipFile = new ZipFile(PluginBoot.getTabooLibFile())) {
            tabooLibVersion = NumberConversions.toDouble(IO.readFully(zipFile.getInputStream(zipFile.getEntry("__resources__/version"))));
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return tabooLibVersion;
    }

    public static String[] getCurrentVersion() {
        for (String[] url : URL) {
            String read = IO.readFromURL(url[0], "{}");
            try {
                JsonObject jsonObject = new JsonParser().parse(read).getAsJsonObject();
                if (jsonObject.has("tag_name")) {
                    return new String[] {jsonObject.get("tag_name").getAsString(), url[0], url[1]};
                }
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    public static FileConfiguration getPluginDescription() {
        File file = IO.file(new File("plugins/TabooLib/temp/" + UUID.randomUUID()));
        try (ZipFile zipFile = new ZipFile(IO.toFile(PluginBoot.class.getProtectionDomain().getCodeSource().getLocation().openStream(), file))) {
            try (InputStream inputStream = zipFile.getInputStream(zipFile.getEntry("plugin.yml"))) {
                return YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public static PluginDescriptionFile getPluginDescriptionFile(File file) {
        PluginDescriptionFile descriptionFile = null;
        try (JarFile jar = new JarFile(file)) {
            JarEntry entry = jar.getJarEntry("plugin.yml");
            if (entry == null) {
                return null;
            }
            try (InputStream stream = jar.getInputStream(entry)) {
                descriptionFile = new PluginDescriptionFile(stream);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return descriptionFile;
    }

    public static void LoadPluginMode() {
        try {
            org.bukkit.plugin.Plugin plugin = Bukkit.getPluginManager().loadPlugin(PluginBoot.getTabooLibFile());
            if (plugin != null) {
                plugin.onLoad();
                Bukkit.getPluginManager().enablePlugin(plugin);
            }
        } catch (Throwable t) {
            PluginBoot.setDisabled(true);
            t.printStackTrace();
        }
    }

    public static void checkPlugins() {
        for (File file : new File("plugins").listFiles()) {
            if (!file.getName().endsWith(".jar")) {
                continue;
            }
            PluginDescriptionFile desc = getPluginDescriptionFile(file);
            if (desc == null) {
                continue;
            }
            switch (desc.getName()) {
                case "TabooLib":
                    pluginOriginFile = file;
                    pluginOriginDescriptionFile = desc;
                    break;
                case "TabooLib5":
                    pluginModeFile = file;
                    pluginModeDescriptionFile = desc;
                    break;
            }
            if (desc.getDepend().contains("TabooLib")) {
                legacy.add(desc.getName());
            }
        }
    }

    public static Class<?> getMainClass(String node) {
        try {
            return Class.forName(Objects.requireNonNull(getPluginDescription()).getString(node));
        } catch (NoClassDefFoundError ignored) {
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public static void sleep(long time) {
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            try {
                Thread.sleep(time);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public static File getPluginModeFile() {
        return pluginModeFile;
    }

    public static File getPluginOriginFile() {
        return pluginOriginFile;
    }

    public static PluginDescriptionFile getPluginModeDescriptionFile() {
        return pluginModeDescriptionFile;
    }

    public static PluginDescriptionFile getPluginOriginDescriptionFile() {
        return pluginOriginDescriptionFile;
    }

    public static List<String> getLegacy() {
        return legacy;
    }
}
