package io.izzel.taboolib.loader;

import io.izzel.taboolib.PluginLoader;
import io.izzel.taboolib.loader.internal.ILoader;
import io.izzel.taboolib.loader.internal.IO;
import io.izzel.taboolib.util.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.NumberConversions;

import java.io.*;

/**
 * @Author 坏黑
 * @Since 2019-07-05 9:03
 */
public abstract class PluginBase extends JavaPlugin {

    protected static PluginBase plugin;
    protected static File libFile = new File("libs/TabooLib.jar");

    /**
     * 插件在初始化过程中出现错误
     * 将在 onLoad 方法下关闭插件
     */
    protected static boolean disabled;
    protected static boolean forge = ILoader.forName("net.minecraftforge.classloading.FMLForgePlugin", false, PluginBase.class.getClassLoader()) != null || ILoader.forName("net.minecraftforge.common.MinecraftForge", false, PluginBase.class.getClassLoader()) != null;

    protected static Plugin main;

    @Override
    public final void onLoad() {
        if (disabled) {
            setEnabled(false);
            return;
        }
        plugin = this;
        try {
            preLoad();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        PluginLoader.addPlugin(this);
        PluginLoader.load(this);
        try {
            onLoading();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public final void onEnable() {
        if (disabled) {
            return;
        }
        PluginLoader.start(this);
        try {
            onStarting();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        Bukkit.getScheduler().runTask(this, () -> PluginLoader.active(this));
    }

    @Override
    public final void onDisable() {
        if (disabled) {
            return;
        }
        try {
            onStopping();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        PluginLoader.stop(this);
    }

    @Deprecated
    @Override
    public final FileConfiguration getConfig() {
        return super.getConfig();
    }

    @Override
    public File getFile() {
        return super.getFile();
    }

    public void preLoad() {
    }

    public void onLoading() {
    }

    public void onStarting() {
    }

    public void onStopping() {
    }

    public static boolean isDisabled() {
        return disabled;
    }

    public static boolean isForge() {
        return forge;
    }

    public static PluginBase getPlugin() {
        return plugin;
    }

    public static File getTabooLibFile() {
        return libFile;
    }

    public static void setDisabled(boolean disabled) {
        PluginBase.disabled = disabled;
    }

    static void init() {
        // 依赖无效 && 下载失败
        if (!libFile.exists() && !PluginHandle.downloadFile()) {
            disabled = true;
            PluginLocale.OFFLINE.print();
            PluginHandle.sleep(5000L);
            return;
        }
        // 版本检查
        else {
            double version = PluginHandle.getVersion();
            // 版本无效 && 下载失败
            if (version == -1 && !PluginHandle.downloadFile()) {
                disabled = true;
                PluginLocale.OFFLINE.print();
                PluginHandle.sleep(5000L);
                return;
            }
            // 低于 5.19 版本无法在 Kotlin 作为主类的条件下检查更新
            // 低于 5.34 版本无法在 CatServer 服务端下启动
            double requireVersion = main.getTabooLibVersion();
            // 依赖版本高于当前运行版本
            if (requireVersion > version) {
                disabled = true;
                // 获取版本信息
                String[] newVersion = PluginHandle.getCurrentVersion();
                if (newVersion == null) {
                    PluginLocale.OFFLINE.print();
                    PluginHandle.sleep(5000L);
                    return;
                }
                // 检查依赖版本是否合理
                // 如果插件使用不合理的版本则跳过下载防止死循环
                // 并跳过插件加载
                if (requireVersion > NumberConversions.toDouble(newVersion[0])) {
                    Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c无效的依赖版本... " + requireVersion + " > " + NumberConversions.toDouble(newVersion[0]));
                    return;
                }
                Bukkit.getConsoleSender().sendMessage("§f[TabooLib] §7正在下载资源文件...");
                if (IO.downloadFile(newVersion[2], IO.file(libFile))) {
                    if (Bukkit.getOnlinePlayers().isEmpty()) {
                        PluginLocale.UPDATE.print();
                        PluginHandle.sleep(5000L);
                        Bukkit.shutdown();
                    } else {
                        PluginLocale.UPDATE_WAIT.print();
                    }
                }
                return;
            }
        }
        // 当 Forge 服务端
        if (forge) {
            // 当 TabooLib 未被加载
            if (Bukkit.getPluginManager().getPlugin("TabooLib5") == null) {
                Bukkit.getConsoleSender().sendMessage("§f[TabooLib] §7检测到当前为 Forge 服务端, 主运行库将以插件模式启动.");
                // 将 TabooLib 通过插件方式加载到服务端
                PluginHandle.LoadPluginMode();
            }
        }
        // 当 TabooLib 未被加载
        else if (!PluginHandle.isLoaded()) {
            // 检查插件文件
            PluginHandle.checkPlugins();
            // 当 TabooLib 存在插件文件夹时
            if (PluginHandle.getPluginModeFile() != null) {
                disabled = true;
                PluginLocale.IN_PLUGINS.print(PluginHandle.getPluginModeFile().getName());
                PluginHandle.getPluginModeFile().delete();
                PluginHandle.sleep(5000L);
                Bukkit.shutdown();
                return;
            }
            // 当 TabooLib 4.X 存在插件文件夹时
            if (PluginHandle.getPluginOriginFile() != null && !new File("plugins/TabooLib/check").exists()) {
                double version = NumberConversions.toDouble(PluginHandle.getPluginOriginDescriptionFile().getVersion());
                // 进行版本检测
                // 保证 4.X 插件版本兼容 5.X 内置版本
                if (version > 3.0 && version < 4.92) {
                    disabled = true;
                    IO.file(new File("plugins/TabooLib/check"));
                    IO.downloadFile("https://skymc.oss-cn-shanghai.aliyuncs.com/plugins/TabooLib-4.92.jar", PluginHandle.getPluginOriginFile());
                    PluginLocale.DOWNLOAD_PLUGIN.print(version, PluginHandle.getPluginOriginFile().getName());
                    PluginHandle.sleep(5000L);
                    Bukkit.shutdown();
                    return;
                }
            }
            YamlConfiguration description = PluginHandle.getPluginDescription();
            if (description != null) {
                Bukkit.getConsoleSender().sendMessage("§f[TabooLib] §7当前由 " + description.getString("name") + " 引导主运行库 (" + PluginHandle.getVersion() + ") 启动.");
            }
            // 将 TabooLib 通过 Bukkit.class 类加载器加载至内存中供其他插件使用
            // 并保证在热重载过程中不会被 Bukkit 卸载
            ILoader.addPath(libFile);
            // 初始化 TabooLib 主类
            if (ILoader.forName("io.izzel.taboolib.TabooLib", true, Bukkit.class.getClassLoader()) == null) {
                disabled = true;
                Bukkit.getConsoleSender().sendMessage("§4[TabooLib] §c主运行库未完成初始化, 插件停止加载.");
            }
        }
        // 清理临时文件
        IO.deepDelete(new File("plugins/TabooLib/temp"));
    }

    static {
        try {
            for (Class<?> c : IO.getClasses(PluginBase.class)) {
                if (c.isAssignableFrom(Plugin.class) && !c.equals(Plugin.class)) {
                    try {
                        try {
                            main = (Plugin) Reflection.getValue(null, c, true, "INSTANCE");
                        } catch (Throwable ignored) {
                            main = (Plugin) Reflection.instantiateObject(c);
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
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
