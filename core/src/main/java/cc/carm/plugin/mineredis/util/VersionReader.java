package cc.carm.plugin.mineredis.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.Properties;

public class VersionReader {

    String versionsFileName;


    public VersionReader() {
        this("versions.properties");
    }

    public VersionReader(String versionsFileName) {
        this.versionsFileName = versionsFileName;
    }

    public synchronized @NotNull String get(@NotNull String artifactID) {
        return get(artifactID, "unknown");
    }

    @Contract("_,!null->!null")
    public synchronized @Nullable String get(@NotNull String artifactID,
                                             @Nullable String defaultValue) {
        try (InputStream is = this.getClass().getResourceAsStream("/" + versionsFileName)) {
            Properties p = new Properties();
            p.load(is);
            return p.getProperty(artifactID, defaultValue);
        } catch (Exception ignore) {
        }
        return defaultValue;
    }

}
