package io.izzel.taboolib.loader;

import io.izzel.taboolib.PluginLoader;
import io.izzel.taboolib.util.Ref;
import io.izzel.taboolib.util.Reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Author sky
 * @Since 2020-03-08 12:48
 */
public class PluginBoot extends PluginBase {

    @Override
    public void preLoad() {
        if (main != null) {
            PluginLoader.redefine(this, main);
        }
    }

    @Override
    public void onLoading() {
        if (main != null) {
            main.onLoad();
        }
    }

    @Override
    public void onStarting() {
        if (main != null) {
            main.onEnable();
        }
    }

    @Override
    public void onStopping() {
        if (main != null) {
            main.onDisable();
        }
    }
}
