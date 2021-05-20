package io.izzel.taboolib.loader.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarFile;

/**
 * @author sky
 * @since 2020-04-12 22:36
 */
public class IO {

    public static String readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            stream.write(buf, 0, len);
        }
        return stream.toString("UTF-8");
    }

    public static String readFromURL(String in, String def) {
        try (InputStream inputStream = new URL(in).openStream(); BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            return readFully(bufferedInputStream);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return def;
    }

    public static boolean downloadFile(String in, File file) {
        try (InputStream inputStream = new URL(in).openStream(); BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            try (FileOutputStream fos = new FileOutputStream(file); BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    bos.write(buf, 0, len);
                }
                bos.flush();
            } catch (Exception ignored) {
            }
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }

    public static void copy(File file1, File file2) {
        try (FileInputStream fileIn = new FileInputStream(file1);
             FileOutputStream fileOut = new FileOutputStream(file2);
             FileChannel channelIn = fileIn.getChannel();
             FileChannel channelOut = fileOut.getChannel()) {
            channelIn.transferTo(0, channelIn.size(), channelOut);
        } catch (IOException t) {
            t.printStackTrace();
        }
    }

    public static File file(File file) {
        if (!file.exists()) {
            try {
                folder(file).createNewFile();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return file;
    }

    public static File folder(File file) {
        if (!file.exists()) {
            String filePath = file.getPath();
            int index = filePath.lastIndexOf(File.separator);
            String folderPath;
            File folder;
            if ((index >= 0) && (!(folder = new File(filePath.substring(0, index))).exists())) {
                folder.mkdirs();
            }
        }
        return file;
    }

    @Nullable
    public static String getFileHash(File file, @NotNull String algorithm) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fileInputStream.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, length);
            }
            byte[] md5Bytes = digest.digest();
            return new BigInteger(1, md5Bytes).toString(16);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public static String replaceWithOrder(String template, Object... args) {
        if (args.length == 0 || template.length() == 0) {
            return template;
        }
        char[] arr = template.toCharArray();
        StringBuilder stringBuilder = new StringBuilder(template.length());
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == '{' && Character.isDigit(arr[Math.min(i + 1, arr.length - 1)])
                    && arr[Math.min(i + 1, arr.length - 1)] - '0' < args.length
                    && arr[Math.min(i + 2, arr.length - 1)] == '}') {
                stringBuilder.append(args[arr[i + 1] - '0']);
                i += 2;
            } else {
                stringBuilder.append(arr[i]);
            }
        }
        return stringBuilder.toString();
    }

    public static List<Class<?>> getClasses(Class<?> plugin) {
        return getClasses(plugin, new String[0]);
    }

    public static List<Class<?>> getClasses(Class<?> plugin, String[] ignore) {
        List<Class<?>> classes = new CopyOnWriteArrayList<>();
        URL url = plugin.getProtectionDomain().getCodeSource().getLocation();
        try {
            File src;
            try {
                src = new File(url.toURI());
            } catch (URISyntaxException e) {
                src = new File(url.getPath());
            }
            new JarFile(src).stream().filter(entry -> entry.getName().endsWith(".class")).forEach(entry -> {
                String className = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);
                try {
                    if (Arrays.stream(ignore).noneMatch(className::startsWith)) {
                        classes.add(Class.forName(className, false, plugin.getClassLoader()));
                    }
                } catch (Throwable ignored) {
                }
            });
        } catch (Throwable ignored) {
        }
        return classes;
    }
}
