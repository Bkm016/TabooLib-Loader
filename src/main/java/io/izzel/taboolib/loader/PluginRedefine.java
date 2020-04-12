package io.izzel.taboolib.loader;

/**
 * @Author sky
 * @Since 2020-03-08 14:00
 */
public abstract class PluginRedefine {

    public void preLoad() {
    }

    public void onLoading() {
    }

    public void onStarting() {
    }

    public void onStopping() {
    }

    public void onActivated() {
    }

    public Plugin getPlugin() {
        return Plugin.getPlugin();
    }
}