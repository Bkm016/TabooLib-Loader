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

    private Object instance;
    private Method loading;
    private Method starting;
    private Method stopping;

    @Override
    public void preLoad() {
        try {
            try {
                instance = Reflection.getValue(null, main, true, "INSTANCE");
            } catch (Throwable ignored) {
                instance = Reflection.instantiateObject(main);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (instance != null) {
            Ref.getDeclaredMethods(main).forEach(method -> {
                if (method.isAnnotationPresent(Plugin.Loading.class)) {
                    loading = method;
                    loading.setAccessible(true);
                } else if (method.isAnnotationPresent(Plugin.Starting.class)) {
                    starting = method;
                    loading.setAccessible(true);
                } else if (method.isAnnotationPresent(Plugin.Stopping.class)) {
                    stopping = method;
                    loading.setAccessible(true);
                }
            });
            PluginLoader.redefine(this, instance);
        }
    }

    @Override
    public void onLoading() {
        if (loading != null) {
            try {
                loading.invoke(instance);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStarting() {
        if (starting != null) {
            try {
                starting.invoke(instance);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStopping() {
        if (starting != null) {
            try {
                starting.invoke(instance);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static PluginBase getPlugin() {
        return PluginBase.getPlugin();
    }
}
