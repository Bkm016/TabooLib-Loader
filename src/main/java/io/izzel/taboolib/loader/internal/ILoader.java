package io.izzel.taboolib.loader.internal;

import org.bukkit.Bukkit;
import sun.misc.Unsafe;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.URLClassLoader;

/**
 * @Author sky
 * @Since 2020-04-12 22:39
 */
public class ILoader extends URLClassLoader {

    static MethodHandles.Lookup lookup;
    static Unsafe UNSAFE;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            UNSAFE = (Unsafe) field.get(null);
            Field lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            Object lookupBase = UNSAFE.staticFieldBase(lookupField);
            long lookupOffset = UNSAFE.staticFieldOffset(lookupField);
            lookup = (MethodHandles.Lookup) UNSAFE.getObject(lookupBase, lookupOffset);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public ILoader(java.net.URL[] urls) {
        super(urls);
    }

    /**
     * 将文件读取至内存中
     * 读取后不会随着插件的卸载而卸载
     * 请在执行前判断是否已经被读取
     * 防止出现未知错误
     */
    public static void addPath(File file) {
        try {
            Field ucp = Bukkit.class.getClassLoader().getClass().getDeclaredField("ucp");
            long ucpOffset = UNSAFE.objectFieldOffset(ucp);
            Object urlClassPath = UNSAFE.getObject(Bukkit.class.getClassLoader(), ucpOffset);
            MethodHandle methodHandle = lookup.findVirtual(urlClassPath.getClass(), "addURL", MethodType.methodType(void.class, java.net.URL.class));
            methodHandle.invoke(urlClassPath, file.toURI().toURL());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static Class forName(String name, boolean initialize, ClassLoader loader) {
        try {
            return Class.forName(name, initialize, loader);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
