package io.izzel.taboolib.loader;

import io.izzel.taboolib.PluginLoader;
import io.izzel.taboolib.TabooLibAPI;
import io.izzel.taboolib.loader.util.ILoader;
import io.izzel.taboolib.loader.util.IO;
import io.izzel.taboolib.util.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author 坏黑
 * @since 2019-07-05 9:03
 */
public class PluginBoot extends JavaPlugin {

    protected static File tabooLibFile = new File("libs/TabooLib.jar");

    protected static Class<?> mainClass;

    protected static PluginBoot pluginBoot;
    protected static Plugin pluginInstance;

    protected static boolean isDisabled;
    protected static boolean isForge = ILoader.forName("net.minecraftforge.classloading.FMLForgePlugin", false, PluginBoot.class.getClassLoader()) != null
            || ILoader.forName("net.minecraftforge.common.MinecraftForge", false, PluginBoot.class.getClassLoader()) != null;

    @Override
    public final void onLoad() {
        if (isDisabled) {
            setEnabled(false);
            return;
        }
        pluginBoot = this;
        try {
            pluginInstance = (Plugin) Reflection.getValue(mainClass, mainClass, true, "INSTANCE");
        } catch (NoSuchFieldException ignored) {
            try {
                pluginInstance = (Plugin) Reflection.instantiateObject(mainClass);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (pluginInstance != null) {
            // 热重载检测
            if (TabooLibAPI.getTPS()[0] > 0 && !pluginInstance.allowHotswap()) {
                setEnabled(false);
                return;
            }
            PluginLoader.redefine(this, pluginInstance);
        }
        PluginLoader.addPlugin(this);
        PluginLoader.load(this);
        try {
            if (pluginInstance != null) {
                pluginInstance.onLoad();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public final void onEnable() {
        if (isDisabled) {
            return;
        }
        PluginLoader.start(this);
        try {
            if (pluginInstance != null) {
                pluginInstance.onEnable();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        Bukkit.getScheduler().runTask(this, () -> PluginLoader.active(this));
    }

    @Override
    public final void onDisable() {
        if (isDisabled) {
            return;
        }
        try {
            if (pluginInstance != null) {
                pluginInstance.onDisable();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        PluginLoader.stop(this);
    }

    @Nullable
    @Override
    public final ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, @Nullable String id) {
        if (pluginInstance != null) {
            return pluginInstance.getDefaultWorldGenerator(worldName, id);
        }
        return super.getDefaultWorldGenerator(worldName, id);
    }

    @Deprecated
    @Override
    public final FileConfiguration getConfig() {
        return super.getConfig();
    }

    @NotNull
    @Override
    public final File getFile() {
        return super.getFile();
    }

    public static File getTabooLibFile() {
        return tabooLibFile;
    }

    public static PluginBoot getPluginBase() {
        return pluginBoot;
    }

    public static Plugin getPluginInstance() {
        return pluginInstance;
    }

    public static boolean isForge() {
        return isForge;
    }

    public static boolean isDisabled() {
        return isDisabled;
    }

    public static void setDisabled(boolean disabled) {
        PluginBoot.isDisabled = disabled;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void init() {
        FileConfiguration description = PluginHandle.getPluginDescription();
        // 文件有效性
        if (description == null) {
            File file = new File(PluginBoot.class.getProtectionDomain().getCodeSource().getLocation().getFile());
            PluginLocale.LOAD_FAILED.warn(file.getName() + " (JVM)");
            return;
        }
        String name = description.getString("name");

        // 文件不存在
        if (!tabooLibFile.exists()) {
            // 防呆模式
            File wrong = new File("TabooLib.jar");
            if (wrong.exists()) {
                IO.copy(wrong, IO.file(tabooLibFile));
                wrong.delete();
            } else {
                // 离线模式
                if (!description.getBoolean("lib-download", true)) {
                    isDisabled = true;
                    PluginLocale.LOAD_OFFLINE.warn(name);
                    return;
                }
                // 下载文件失败
                if (!PluginHandle.downloadFile()) {
                    isDisabled = true;
                    PluginLocale.LOAD_NO_INTERNAL.warn(name);
                    return;
                }
            }
        }

        // 版本检查
        double version = PluginHandle.getVersion();
        // 版本无效 && 下载失败
        if (version == -1 && !PluginHandle.downloadFile()) {
            isDisabled = true;
            PluginLocale.LOAD_NO_INTERNAL.warn(name);
            return;
        }

        // 低于 5.19 版本无法在 Kotlin 作为主类的条件下检查更新
        // 低于 5.34 版本无法在 CatServer 服务端下启动
        double requireVersion = description.getDouble("lib-version", 5.35);
        // 依赖版本高于当前运行版本
        if (requireVersion > version) {
            isDisabled = true;
            // 获取版本信息
            String[] newVersion = PluginHandle.getCurrentVersion();
            if (newVersion == null) {
                PluginLocale.LOAD_NO_INTERNAL.warn(name);
                return;
            }
            // 检查依赖版本是否合理
            // 如果插件使用不合理的版本则跳过下载防止死循环
            // 并跳过插件加载
            if (requireVersion > NumberConversions.toDouble(newVersion[0])) {
                PluginLocale.LOAD_INVALID_VERSION.warn(name, requireVersion, newVersion[0]);
                return;
            }
            // 下载更新
            PluginLocale.LOAD_DOWNLOAD.info();
            if (IO.downloadFile(newVersion[2], IO.file(tabooLibFile))) {
                // 服务器服务器没人则重启
                if (Bukkit.getOnlinePlayers().isEmpty()) {
                    PluginLocale.LOAD_OUTDATED.warn();
                    PluginHandle.sleep(5000L);
                    Bukkit.shutdown();
                } else {
                    PluginLocale.LOAD_OUTDATED_NO_RESTART.warn(name);
                }
            }
        }

        // 当 Forge 服务端
        if (isForge) {
            // 当 TabooLib 未被加载
            if (Bukkit.getPluginManager().getPlugin("TabooLib5") == null) {
                // 将 TabooLib 通过插件方式加载到服务端
                PluginLocale.LOAD_FORGE_MODE.warn();
                PluginHandle.LoadPluginMode();
            }
        }
        // 当 TabooLib 未被加载
        else if (!PluginHandle.isLoaded()) {
            // 在线模式
            if (!description.getBoolean("lib-download", true)) {
                // 检查插件文件
                PluginHandle.checkPlugins();
                // 当 TabooLib 存在插件文件夹时
                if (PluginHandle.getPluginModeFile() != null) {
                    isDisabled = true;
                    PluginLocale.LOAD_IN_PLUGINS.warn(PluginHandle.getPluginModeFile().getName());
                    PluginHandle.getPluginModeFile().delete();
                    PluginHandle.sleep(5000L);
                    Bukkit.shutdown();
                    return;
                }
                // 当 TabooLib 4.X 存在插件文件夹时
                if (PluginHandle.getPluginOriginFile() != null && !new File("plugins/TabooLib/check").exists()) {
                    double legacyVersion = NumberConversions.toDouble(PluginHandle.getPluginOriginDescriptionFile().getVersion());
                    // 进行版本检测
                    // 保证 4.X 插件版本兼容 5.X 内置版本
                    if (legacyVersion > 3.0 && legacyVersion < 4.92) {
                        isDisabled = true;
                        IO.file(new File("plugins/TabooLib/check"));
                        IO.downloadFile("https://skymc.oss-cn-shanghai.aliyuncs.com/plugins/TabooLib-4.92.jar", PluginHandle.getPluginOriginFile());
                        PluginLocale.LOAD_COMPAT_MODE_UPDATE.warn(PluginHandle.getPluginOriginFile().getName());
                        PluginHandle.sleep(5000L);
                        Bukkit.shutdown();
                        return;
                    } else {
                        PluginLocale.LOAD_COMPAT_MODE.warn(PluginHandle.getPluginOriginFile().getName(), PluginHandle.getLegacy());
                    }
                }
            }
            // 将 TabooLib 通过 Bukkit.class 类加载器加载至内存中供其他插件使用
            // 并保证在热重载过程中不会被 Bukkit 卸载
            ILoader.addPath(tabooLibFile);
            // 初始化 TabooLib 主类
            if (ILoader.forName("io.izzel.taboolib.TabooLib", true, Bukkit.class.getClassLoader()) != null) {
                PluginLocale.LOAD_SUCCESS.info(PluginHandle.getVersion(), name);
            } else {
                isDisabled = true;
                PluginLocale.LOAD_FAILED.warn(name);
            }
        }
        // 清理临时文件
        IO.deepDelete(new File("plugins/TabooLib/temp"));
    }

    static {
        try {
            for (Class<?> c : IO.getClasses(PluginBoot.class)) {
                if (Plugin.class.isAssignableFrom(c) && !Plugin.class.equals(c)) {
                    mainClass = c;
                    break;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        try {
            init();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
