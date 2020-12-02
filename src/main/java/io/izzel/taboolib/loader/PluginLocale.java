package io.izzel.taboolib.loader;

import io.izzel.taboolib.loader.internal.IO;
import org.bukkit.Bukkit;

/**
 * @author sky
 * @since 2020-04-12 22:28
 */
public enum PluginLocale {

    OFFLINE(
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### ERROR ####################",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  Failed to initialized §4TabooLib §c!",
            "§4[TabooLib] §c  Unable to obtain version info or an error occurred while downloading.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  Please check if the server’s internet connection is valid.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### ERROR ####################",
            "§4[TabooLib] §c"
    ),

    OFFLINE_FAILED(
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### ERROR ####################",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  Failed to initialized §4TabooLib §c!",
            "§4[TabooLib] §c  Please install it manually by following the instructions.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  Download §4TabooLib.jar§c and put it into §4libs§c folder.",
            "§4[TabooLib] §c  https://github.com/TabooLib/TabooLib/releases/latest/download/TabooLib.jar",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### ERROR ####################",
            "§4[TabooLib] §c"
    ),

    UPDATE(
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### WARNING ####################",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  Failed to initialized §4TabooLib §c!",
            "§4[TabooLib] §c  The current version is outdated to plugin required.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  The latest version has been downloaded.",
            "§4[TabooLib] §c  The server will restart in 5 seconds.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### WARNING ####################",
            "§4[TabooLib] §c"
    ),

    UPDATE_WAIT(
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### WARNING ####################",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  Failed to initialized §4TabooLib §c!",
            "§4[TabooLib] §c  The current version is outdated to plugin required.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  The latest version has been downloaded.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### WARNING ####################",
            "§4[TabooLib] §c"
    ),

    IN_PLUGINS(
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### WARNING ####################",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  Please do not put §4TabooLib 5.0§c into the plugins folder.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  Deleted §4{0}",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### WARNING ####################",
            "§4[TabooLib] §c"
    ),

    DOWNLOAD_PLUGIN(
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### WARNING ####################",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  Failed to initialized §4TabooLib §c!",
            "§4[TabooLib] §c  Not compatible with §4TabooLib {0} §c.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  Updated §4{1}",
            "§4[TabooLib] §c  The server will restart in 5 seconds.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### WARNING ####################",
            "§4[TabooLib] §c"
    );

    String[] message;

    PluginLocale(String... message) {
        this.message = message;
    }

    public void print(Object... args) {
        for (String it : message) {
            Bukkit.getConsoleSender().sendMessage(IO.replaceWithOrder(it, args));
        }
    }
}
