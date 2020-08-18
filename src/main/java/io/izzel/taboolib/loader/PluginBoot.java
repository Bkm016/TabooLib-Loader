package io.izzel.taboolib.loader;

import io.izzel.taboolib.PluginLoader;
import io.izzel.taboolib.util.Ref;
import io.izzel.taboolib.util.Reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Author sky
 * @Since 2020-03-08 12:48
 */
public class PluginBoot extends PluginBase {

    private Plugin instance;

    @Override
    public void preLoad() {
        if (main != null) {
            try {
                Field obj = main.getDeclaredField("INSTANCE");
                if (obj != null) {
                    obj.setAccessible(true);
                    instance = (Plugin) obj.get(main);
                } else {
                    instance = (Plugin) main.newInstance();
                }
            } catch (Throwable ignored) {
            }
            PluginLoader.redefine(this, instance);
        }
    }

    @Override
    public void onLoading() {
        if (instance != null) {
            instance.onLoad();
        }
    }

    @Override
    public void onStarting() {
        if (instance != null) {
            instance.onEnable();
        }
    }

    @Override
    public void onStopping() {
        if (instance != null) {
            instance.onDisable();
        }
    }
}
