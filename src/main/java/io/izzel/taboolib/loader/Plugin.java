package io.izzel.taboolib.loader;

import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author sky
 * @since 2020-08-16 14:06
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

    public PluginBoot getPlugin() {
        return PluginBoot.getPluginBase();
    }

    public static Plugin getInstance() {
        return PluginBoot.getPluginInstance();
    }
}