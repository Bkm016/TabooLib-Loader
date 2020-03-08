package me.skymc.example

import io.izzel.taboolib.loader.Plugin
import io.izzel.taboolib.loader.PluginRedefine

/**
 * @Author sky
 * @Since 2020-03-08 12:51
 */
@Plugin.Version(5.18)
class ExamplePlugin : PluginRedefine() {

    override fun onStarting() {
        println("${getPlugin().name} Enabled!")
    }

    override fun onStopping() {
        println("${getPlugin().name} Disabled!")
    }
}