package io.izzel.taboolib.loader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.izzel.taboolib.loader.internal.ILoader;
import io.izzel.taboolib.loader.internal.IO;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.NumberConversions;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

/**
 * @Author sky
 * @Since 2020-04-12 22:43
 */
public class PluginHandle {

    private static File pluginModeFile;
    private static File pluginOriginFile;
    private static PluginDescriptionFile pluginModeDescriptionFile;
    private static PluginDescriptionFile pluginOriginDescriptionFile;

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
        Bukkit.getConsoleSender().sendMessage("§f[TabooLib] §7正在下载资源文件...");
        String[] newVersion = getCurrentVersion();
        return newVersion != null && IO.downloadFile(newVersion[2], IO.file(Plugin.getTabooLibFile()));
    }

    public static boolean isLoaded() {
        return ILoader.forName("io.izzel.taboolib.TabooLib", false, Bukkit.class.getClassLoader()) != null;
    }

    public static double getVersion() {
        try (ZipFile zipFile = new ZipFile(Plugin.getTabooLibFile())) {
            return NumberConversions.toDouble(IO.readFully(zipFile.getInputStream(zipFile.getEntry("__resources__/version")), StandardCharsets.UTF_8));
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return -1;
    }

    public static String[] getCurrentVersion() {
        for (String[] url : URL) {
            String read = IO.readFromURL(url[0]);
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

    public static YamlConfiguration getPluginDescription() {
        File file = IO.file(new File("plugins/TabooLib/temp/" + UUID.randomUUID()));
        try (ZipFile zipFile = new ZipFile(IO.toFile(Plugin.class.getProtectionDomain().getCodeSource().getLocation().openStream(), file))) {
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

    public static YamlConfiguration getPluginDescription(File file) {
        YamlConfiguration conf = new YamlConfiguration();
        try (JarFile jar = new JarFile(file)) {
            JarEntry entry = jar.getJarEntry("plugin.yml");
            if (entry == null) {
                return conf;
            }
            try (InputStream stream = jar.getInputStream(entry)) {
                conf.loadFromString(IO.readFully(stream, StandardCharsets.UTF_8));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return conf;
    }

    public static PluginDescriptionFile getPluginDescriptionFile(File file) {
        PluginDescriptionFile descriptionFile = null;
        try (JarFile jar = new JarFile(file)) {
            JarEntry entry = jar.getJarEntry("plugin.yml");
            if (entry == null) {
                return descriptionFile;
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
            org.bukkit.plugin.Plugin plugin = Bukkit.getPluginManager().loadPlugin(Plugin.getTabooLibFile());
            plugin.onLoad();
            Bukkit.getPluginManager().enablePlugin(plugin);
        } catch (Throwable t) {
            Plugin.disabled = true;
            Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c主运行库未完成初始化, 插件停止加载.");
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
        }
    }

    public static Class<?> getMainClass(String node) {
        try {
            return Class.forName(getPluginDescription().getString(node));
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
}
