package io.izzel.taboolib.loader;

import io.izzel.taboolib.PluginLoader;
import io.izzel.taboolib.util.Reflection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.lang.reflect.Field;

/**
 * @Author sky
 * @Since 2020-03-08 12:48
 */
public class PluginTransfer extends Plugin {

    private static PluginRedefine redefine;

    @Override
    public void preLoad() {
        YamlConfiguration conf = PluginHandle.getPluginDescription(getFile());
        try {
            Class<?> clazz = Class.forName(conf.getString("main-transfer"), false, PluginTransfer.class.getClassLoader());
            try {
                redefine = (PluginRedefine) Reflection.getValue(null, clazz, true, "INSTANCE");
            } catch (Throwable ignored) {
                redefine = (PluginRedefine) clazz.newInstance();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        PluginLoader.redefine(this, redefine);
        if (redefine != null) {
            redefine.preLoad();
        }
    }

    @Override
    public void onLoading() {
        if (redefine != null) {
            redefine.onLoading();
        }
    }

    @Override
    public void onStarting() {
        if (redefine != null) {
            redefine.onStarting();
        }
    }

    @Override
    public void onStopping() {
        if (redefine != null) {
            redefine.onStopping();
        }
    }

    @Override
    public void onActivated() {
        if (redefine != null) {
            redefine.onActivated();
        }
    }

    public static PluginRedefine getRedefine() {
        return redefine;
    }
}
