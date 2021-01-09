package io.izzel.taboolib.loader.internal;

import io.izzel.taboolib.loader.PluginBase;
import org.bukkit.Bukkit;
import sun.misc.Unsafe;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author sky
 * @since 2020-04-12 22:39
 */
public class ILoader extends URLClassLoader {

    static MethodHandles.Lookup lookup;
    static Unsafe unsafe;
    static Method addUrlMethod;

    static {
        if (PluginBase.isForge()) {
            try {
                addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                addUrlMethod.setAccessible(true);
            } catch (Throwable ignore) {
            }
        }
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
            Field lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            Object lookupBase = unsafe.staticFieldBase(lookupField);
            long lookupOffset = unsafe.staticFieldOffset(lookupField);
            lookup = (MethodHandles.Lookup) unsafe.getObject(lookupBase, lookupOffset);
        } catch (Throwable ignore) {
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
            ClassLoader loader = Bukkit.class.getClassLoader();
            if (PluginBase.isForge()) {
                addUrlMethod.invoke(loader, file.toURI().toURL());
            } else if (loader.getClass().getSimpleName().equals("LaunchClassLoader")) {
                MethodHandle methodHandle = lookup.findVirtual(loader.getClass(), "addURL", MethodType.methodType(void.class, java.net.URL.class));
                methodHandle.invoke(loader, file.toURI().toURL());
            } else {
                Field ucpField = loader.getClass().getDeclaredField("ucp");
                long ucpOffset = unsafe.objectFieldOffset(ucpField);
                Object ucp = unsafe.getObject(loader, ucpOffset);
                MethodHandle methodHandle = lookup.findVirtual(ucp.getClass(), "addURL", MethodType.methodType(void.class, java.net.URL.class));
                methodHandle.invoke(ucp, file.toURI().toURL());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static Class<?> forName(String name, boolean initialize, ClassLoader loader) {
        try {
            return Class.forName(name, initialize, loader);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
