package io.izzel.taboolib.loader;

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

    public Info(Version version, String url, String hash, long uploadTime) {
        this.version = version;
        this.url = url;
        this.hash = hash;
        this.uploadTime = uploadTime;
    }

    public Version getVersion() {
        return version;
    }

    public String getUrl() {
        return url;
    }

    public String getHash() {
        return hash;
    }

    public long getUploadTime() {
        return uploadTime;
    }

    @Override
    public String toString() {
        return "Info{" +
                "version=" + version +
                ", url='" + url + '\'' +
                ", hash='" + hash + '\'' +
                ", uploadTime=" + uploadTime +
                '}';
    }
}