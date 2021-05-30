package io.izzel.taboolib.loader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * TabooLibLoader
 * io.izzel.taboolib.loader.Information
 *
 * @author sky
 * @since 2021/5/19 10:57 下午
 */
public class Info {

    private final Version version;
    private final String url;
    private final String hash;
    private final long uploadTime;
    private final Info newestInfo;

    public Info(Version version, String url, String hash, long uploadTime) {
        this(version, url, hash, uploadTime, null);
    }

    public Info(Version version, String url, String hash, long uploadTime, Info newestInfo) {
        this.version = version;
        this.url = url;
        this.hash = hash;
        this.uploadTime = uploadTime;
        this.newestInfo = newestInfo;
    }

    @NotNull
    public Version getVersion() {
        return version;
    }

    @NotNull
    public String getUrl() {
        return url;
    }

    @NotNull
    public String getHash() {
        return hash;
    }

    public long getUploadTime() {
        return uploadTime;
    }

    @Nullable
    public Info getNewestInfo() {
        return newestInfo;
    }

    @Override
    public String toString() {
        return "Info{" +
                "version=" + version +
                ", url='" + url + '\'' +
                ", hash='" + hash + '\'' +
                ", uploadTime=" + uploadTime +
                ", newestInfo=" + newestInfo +
                '}';
    }
}