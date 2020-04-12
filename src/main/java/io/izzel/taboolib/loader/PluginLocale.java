package io.izzel.taboolib.loader;

import io.izzel.taboolib.loader.internal.IO;
import org.bukkit.Bukkit;

/**
 * @Author sky
 * @Since 2020-04-12 22:28
 */
public enum PluginLocale {

    OFFLINE(
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### 错误 ####################",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  初始化 §4TabooLib §c失败!",
            "§4[TabooLib] §c  无法获取版本信息或下载时出现错误.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  请检查服务器的互联网连接是否有效.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### 错误 ####################",
            "§4[TabooLib] §c"
    ),

    UPDATE(
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### 警告 ####################",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  初始化 §4TabooLib §c失败!",
            "§4[TabooLib] §c  当前运行的版本低于插件所需版本.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  已下载最新版.",
            "§4[TabooLib] §c  服务端将在 5 秒后重新启动.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### 警告 ####################",
            "§4[TabooLib] §c"
    ),

    UPDATE_WAIT(
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### 警告 ####################",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  初始化 §4TabooLib §c失败!",
            "§4[TabooLib] §c  当前运行的版本低于插件所需版本.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  已下载最新版.",
            "§4[TabooLib] §c  将在下次启动服务端时启动插件.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### 警告 ####################",
            "§4[TabooLib] §c"
    ),

    IN_PLUGINS(
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### 警告 ####################",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  请勿将 §4TabooLib 5.0 §c放入插件文件夹中.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  已删除 §4{0}",
            "§4[TabooLib] §c  服务端将在 5 秒后重新启动.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### 警告 ####################",
            "§4[TabooLib] §c"
    ),

    DOWNLOAD_PLUGIN(
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### 警告 ####################",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  初始化 §4TabooLib §c失败!",
            "§4[TabooLib] §c  无法兼容 §4TabooLib {0} §c版本.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c  已更新 §4{1}",
            "§4[TabooLib] §c  服务端将在 5 秒后重新启动.",
            "§4[TabooLib] §c",
            "§4[TabooLib] §c#################### 警告 ####################",
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
