package io.izzel.taboolib.loader;

import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @Author sky
 * @Since 2020-08-16 14:06
 */
public class Plugin {

    public void onLoad() {

    }

    public void onEnable() {

    }

    public void onDisable() {

    }

    @Nullable
    public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, @Nullable String id) {
        return null;
    }

    public PluginBase getPlugin() {
        return PluginBoot.getPlugin();
    }
}