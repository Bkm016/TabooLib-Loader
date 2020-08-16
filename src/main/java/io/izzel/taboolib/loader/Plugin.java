package io.izzel.taboolib.loader;

/**
 * @Author sky
 * @Since 2020-08-16 14:06
 */
public class Plugin {

    public double getTabooLibVersion() {
        return 5.34;
    }

    public void onLoad() {

    }

    public void onEnable() {

    }

    public void onDisable() {

    }

    public PluginBase getPlugin() {
        return PluginBoot.getPlugin();
    }
}