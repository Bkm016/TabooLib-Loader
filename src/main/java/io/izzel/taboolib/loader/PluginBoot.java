package io.izzel.taboolib.loader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.izzel.taboolib.PluginLoader;
import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.loader.util.ILoader;
import io.izzel.taboolib.loader.util.IO;
import io.izzel.taboolib.util.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipFile;

/**
 * @author 坏黑
 * @since 2019-07-05 9:03
 */
public class PluginBoot extends JavaPlugin {

    public static String URL;

    protected static Class<?> mainClass;

    protected static Plugin plugin;

    protected static PluginBoot pluginBoot;

    protected static PluginFile pluginFile;

    protected static final boolean forgeBase;

    protected static final File tabooLibFile;

    protected static Version tabooLibVersion;

    protected static Version tabooLibDependVersion;

    protected static boolean enableBoot = true;

    protected static final Map<String, PluginFile> outdatedPlugins;

    protected static final Map<PluginFile.Type, PluginFile> checkedPlugins;

    static {
        URL = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/version.json";
        forgeBase = ILoader.isExists("net.minecraftforge.classloading.FMLForgePlugin") || ILoader.isExists("net.minecraftforge.common.MinecraftForge");
        tabooLibFile = new File("libs/TabooLib.jar");
        checkedPlugins = new HashMap<>();
        outdatedPlugins = new HashMap<>();
        tabooLibDependVersion = new Version(Objects.requireNonNull(pluginFile.getFileConfiguration().getString("lib-version")));
        try (ZipFile zipFile = new ZipFile(tabooLibFile); InputStream inputStream = zipFile.getInputStream(zipFile.getEntry("plugin.yml"))) {
            FileConfiguration configuration = new YamlConfiguration();
            configuration.loadFromString(IO.readFully(inputStream));
            tabooLibVersion = new Version(Objects.requireNonNull(configuration.getString("version")));
        } catch (Throwable t) {
            tabooLibVersion = new Version("0.0.0");
            t.printStackTrace();
        }
        try {
            for (Class<?> clazz : IO.getClasses(PluginBoot.class)) {
                if (Plugin.class.isAssignableFrom(clazz) && !Plugin.class.equals(clazz)) {
                    mainClass = clazz;
                    break;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        try {
            initialize();
        } catch (Throwable t) {
            enableBoot = false;
            t.printStackTrace();
        }
    }

    @Override
    public final void onLoad() {
        if (!enableBoot) {
            return;
        }
        try {
            pluginBoot = this;
            plugin = (Plugin) Reflection.getValue(mainClass, mainClass, true, "INSTANCE");
        } catch (NoSuchFieldException ignored) {
            try {
                plugin = (Plugin) Reflection.instantiateObject(mainClass);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (plugin == null || (TabooLibAPI.getTPS()[0] > 0 && !plugin.allowHotswap())) {
            enableBoot = false;
            PluginLocale.LOAD_FAILED.warn(pluginBoot.getName());
            return;
        }
        PluginLoader.redefine(this, plugin);
        PluginLoader.addPlugin(this);
        PluginLoader.load(this);
        try {
            plugin.onLoad();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public final void onEnable() {
        if (!enableBoot) {
            return;
        }
        PluginLoader.start(this);
        try {
            plugin.onEnable();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        Bukkit.getScheduler().runTask(this, () -> {
            PluginLoader.active(PluginBoot.this);
            try {
                plugin.onActive();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    public final void onDisable() {
        if (!enableBoot) {
            return;
        }
        try {
            plugin.onDisable();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        PluginLoader.stop(this);
    }

    @NotNull
    @Override
    public final File getFile() {
        return super.getFile();
    }

    @Deprecated
    @Override
    public final FileConfiguration getConfig() {
        return super.getConfig();
    }

    @Nullable
    @Override
    public final ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, @Nullable String id) {
        if (plugin != null) {
            return plugin.getDefaultWorldGenerator(worldName, id);
        } else {
            return super.getDefaultWorldGenerator(worldName, id);
        }
    }

    static void initialize() throws URISyntaxException {
        // 检查插件
        File file = new File(PluginBoot.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        try (ZipFile zip = new ZipFile(file); InputStream stream = zip.getInputStream(zip.getEntry("plugin.yml"))) {
            String readFully = IO.readFully(stream);
            pluginFile = new PluginFile(file, new PluginDescriptionFile(new ByteArrayInputStream(readFully.getBytes(StandardCharsets.UTF_8))), readFully);
            checkedPlugins.put(PluginFile.Type.SELF, pluginFile);
        } catch (Throwable e) {
            enableBoot = false;
            e.printStackTrace();
            return;
        }
        // 插件名称
        String name = pluginFile.getName();
        // 文件不存在
        if (!tabooLibFile.exists()) {
            // 防呆模式
            File wrong = new File("TabooLib.jar");
            if (wrong.exists()) {
                IO.copy(wrong, IO.file(tabooLibFile));
                wrong.delete();
            }
            // 下载失败
            else if (!downloadTabooLib(null, false)) {
                enableBoot = false;
                return;
            }
        }
        // 版本检查
        Version version = getTabooLibVersion();
        // 版本无效 && 下载失败
        if (version == null && !downloadTabooLib(null, false)) {
            enableBoot = false;
            return;
        }
        // 低于 5.19 版本无法在 Kotlin 作为主类的条件下检查更新
        // 低于 5.34 版本无法在 CatServer 服务端下启动
        Version depend = new Version(Objects.requireNonNull(pluginFile.getFileConfiguration().getString("lib-version")));
        // 依赖版本高于当前运行版本
        if (depend.isAfter(version)) {
            enableBoot = false;
            // 获取版本信息
            Info information = getTabooLibInformation();
            if (information == null) {
                PluginLocale.LOAD_NO_INTERNET.warn(name);
                return;
            }
            // 检查依赖版本是否合理
            // 如果插件使用不合理的版本则跳过下载防止死循环
            // 并跳过插件加载
            if (depend.isAfter(information.getVersion())) {
                PluginLocale.LOAD_INVALID_VERSION.warn(name, depend, information.getVersion().getSource());
                return;
            }
            downloadTabooLib(information, true);
            return;
        } else {
            boolean developer = new File("plugins/TabooLib/iamdeveloper").exists();
            // 版本相同进行 HASH 检测
            if (depend.equals(version) && pluginFile.getFileConfiguration().getBoolean("lib-download", true) && !developer) {
                Info information = getTabooLibInformation();
                if (information == null) {
                    enableBoot = false;
                    PluginLocale.LOAD_NO_INTERNET.warn(name);
                    return;
                }
                if (!information.getHash().equals(IO.getFileHash(tabooLibFile, "sha-256"))) {
                    enableBoot = false;
                    downloadTabooLib(information, true);
                    return;
                }
            }
        }
        // 当 Forge 服务端
        if (forgeBase) {
            // 当 TabooLib 未被加载
            if (Bukkit.getPluginManager().getPlugin("TabooLib5") == null) {
                // 将 TabooLib 通过插件方式加载到服务端
                PluginLocale.LOAD_FORGE_MODE.warn();
                enableTabooLibAsPlugin();
            }
        }
        // 当 TabooLib 未被加载
        else if (!ILoader.isExists("io.izzel.taboolib.TabooLib")) {
            // 检查插件文件
            checkPlugins();
            // 如果 TabooLib 在插件文件夹内
            if (checkedPlugins.containsKey(PluginFile.Type.PLUGIN_MODE)) {
                enableBoot = false;
                PluginLocale.LOAD_IN_PLUGINS.warn();
                File wrong = checkedPlugins.get(PluginFile.Type.PLUGIN_MODE).getFile();
                // 如果 TabooLib 本体不存在则复制过去
                if (!tabooLibFile.exists()) {
                    IO.copy(wrong, tabooLibFile);
                }
                wrong.delete();
                Bukkit.shutdown();
                return;
            }
            // 当 TabooLib 4.X 存在插件文件夹时
            if (checkedPlugins.containsKey(PluginFile.Type.LEGACY_VERSION) && !new File("plugins/TabooLib/check").exists()) {
                PluginFile legacyFile = checkedPlugins.get(PluginFile.Type.LEGACY_VERSION);
                double legacyVersion = NumberConversions.toDouble(legacyFile.getPluginDescriptionFile().getVersion());
                // 进行版本检测
                // 保证 4.X 插件版本兼容 5.X 内置版本
                if (legacyVersion > 3.0 && legacyVersion < 4.92) {
                    enableBoot = false;
                    IO.file(new File("plugins/TabooLib/check"));
                    IO.downloadFile("https://skymc.oss-cn-shanghai.aliyuncs.com/plugins/TabooLib-4.92.jar", legacyFile.getFile());
                    PluginLocale.LOAD_COMPAT_MODE_UPDATE.warn();
                    Bukkit.shutdown();
                    return;
                } else {
                    PluginLocale.LOAD_COMPAT_MODE.warn(outdatedPlugins.keySet());
                }
            }
            // 将 TabooLib 通过 Bukkit.class 类加载器加载至内存中供其他插件使用
            // 并保证在热重载过程中不会被 Bukkit 卸载
            if (ILoader.addPath(tabooLibFile)) {
                // 初始化 TabooLib 主类
                if (ILoader.forName("io.izzel.taboolib.TabooLib", true, Bukkit.class.getClassLoader()) != null) {
                    PluginLocale.LOAD_SUCCESS.info(getTabooLibVersion().getSource(), name);
                } else {
                    enableBoot = false;
                    PluginLocale.LOAD_FAILED.warn(name);
                }
            } else {
                enableBoot = false;
                PluginLocale.LOAD_FAILED.warn(name);
            }
        }
    }

    /**
     * 检查插件文件夹
     * 记录所有与 TabooLib 有关的插件备用
     */
    public static void checkPlugins() {
        for (File file : new File("plugins").listFiles()) {
            if (!file.getName().endsWith(".jar") || file.length() == 0L) {
                continue;
            }
            PluginFile pluginFile = null;
            try (ZipFile zip = new ZipFile(file); InputStream stream = zip.getInputStream(zip.getEntry("plugin.yml"))) {
                String readFully = IO.readFully(stream);
                pluginFile = new PluginFile(file, new PluginDescriptionFile(new ByteArrayInputStream(readFully.getBytes(StandardCharsets.UTF_8))), readFully);
            } catch (Throwable ignored) {
            }
            if (pluginFile == null) {
                continue;
            }
            if (pluginFile.getName().equals("TabooLib5")) {
                checkedPlugins.put(PluginFile.Type.PLUGIN_MODE, pluginFile);
            } else if (pluginFile.getName().equals("TabooLib")) {
                checkedPlugins.put(PluginFile.Type.LEGACY_VERSION, pluginFile);
            }
            if (pluginFile.getPluginDescriptionFile().getDepend().contains("TabooLib")) {
                outdatedPlugins.put(pluginFile.getName(), pluginFile);
            }
        }
    }

    /**
     * 联网获取 TabooLib 版本信息
     */
    public static Info getTabooLibInformation() {
        try {
            JsonObject json = new JsonParser().parse(IO.readFromURL(URL, "{}")).getAsJsonObject();
            if (json.has("version")) {
                // 最新版本号
                Version newVersion = new Version(json.get("version").getAsString());
                // 最新版本数据
                JsonObject newestJson = json.get("history").getAsJsonObject().get(newVersion.getSource()).getAsJsonObject();
                // 最新版本信息
                Info newestInfo = new Info(
                        newVersion,
                        newestJson.get("url").getAsString(),
                        newestJson.get("sha-256").getAsString(),
                        newestJson.get("upload-time").getAsLong()
                );
                // 依赖版本数据
                JsonObject dependJson = json.get("history").getAsJsonObject().get(tabooLibDependVersion.getSource()).getAsJsonObject();
                return new Info(
                        tabooLibDependVersion,
                        dependJson.get("url").getAsString(),
                        dependJson.get("sha-256").getAsString(),
                        dependJson.get("upload-time").getAsLong(),
                        newestInfo
                );
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 尝试下载依赖文件
     * 若禁用下载模式则无法下载，返回值为是否下载成功
     */
    public static boolean downloadTabooLib(Info information, boolean restart) {
        if (pluginFile.getFileConfiguration().getBoolean("lib-download", true)) {
            PluginLocale.LOAD_DOWNLOAD.info();
            if (information == null) {
                information = getTabooLibInformation();
            }
            // 下载文件
            if (information != null && IO.downloadFile(information.getUrl(), IO.file(tabooLibFile))) {
                // 是否对服务器进行重启
                if (restart) {
                    if (Bukkit.getOnlinePlayers().isEmpty()) {
                        PluginLocale.LOAD_OUTDATED.warn();
                        Bukkit.shutdown();
                    } else {
                        PluginLocale.LOAD_OUTDATED_NO_RESTART.warn(pluginFile.getName());
                    }
                }
                return true;
            }
            PluginLocale.LOAD_NO_INTERNET.warn(pluginFile.getName());
            return false;
        }
        PluginLocale.LOAD_OFFLINE.warn(pluginFile.getName());
        return false;
    }

    /**
     * 以插件模式加载依赖
     * 该方法仅适用于含有 Forge 的服务端
     */
    public static void enableTabooLibAsPlugin() {
        try {
            org.bukkit.plugin.Plugin bukkitPlugin = Bukkit.getPluginManager().loadPlugin(tabooLibFile);
            if (bukkitPlugin != null) {
                bukkitPlugin.onLoad();
                Bukkit.getPluginManager().enablePlugin(bukkitPlugin);
            }
        } catch (Throwable t) {
            enableBoot = false;
            t.printStackTrace();
        }
    }

    public static void setEnableBoot(boolean enableBoot) {
        PluginBoot.enableBoot = enableBoot;
    }

    public static boolean isEnableBoot() {
        return enableBoot;
    }

    public static boolean isForgeBase() {
        return forgeBase;
    }

    public static Class<?> getMainClass() {
        return mainClass;
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static PluginBoot getPluginBoot() {
        return pluginBoot;
    }

    public static PluginFile getPluginFile() {
        return pluginFile;
    }

    public static File getTabooLibFile() {
        return tabooLibFile;
    }

    public static Version getTabooLibVersion() {
        return tabooLibVersion;
    }

    public static Version getTabooLibDependVersion() {
        return tabooLibDependVersion;
    }

    public static Map<String, PluginFile> getOutdatedPlugins() {
        return outdatedPlugins;
    }

    public static Map<PluginFile.Type, PluginFile> getCheckedPlugins() {
        return checkedPlugins;
    }
}
