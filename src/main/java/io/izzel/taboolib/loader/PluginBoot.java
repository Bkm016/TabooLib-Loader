package io.izzel.taboolib.loader;

import io.izzel.taboolib.PluginLoader;
import io.izzel.taboolib.util.Ref;
import io.izzel.taboolib.util.Reflection;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author sky
 * @since 2020-03-08 12:48
 */
public class PluginBoot extends PluginBase {

    private Plugin instance;

    @Override
    public void preLoad() {
        if (main != null) {
            try {
                Field obj = main.getDeclaredField("INSTANCE");
                obj.setAccessible(true);
                instance = (Plugin) obj.get(main);
            } catch (NoSuchFieldException ignored) {
                try {
                    instance = (Plugin) main.getDeclaredConstructor().newInstance();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            if (instance != null) {
                PluginLoader.redefine(this, instance);
            }
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

    @Nullable
    @Override
    public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, @Nullable String id) {
        if (instance != null) {
            return instance.getDefaultWorldGenerator(worldName, id);
        }
        return super.getDefaultWorldGenerator(worldName, id);
    }
}
