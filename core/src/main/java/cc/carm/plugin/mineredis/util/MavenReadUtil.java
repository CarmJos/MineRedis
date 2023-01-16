package cc.carm.plugin.mineredis.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.Properties;

public class MavenReadUtil {

    public static String getMavenPropertiesPath(@NotNull String groupID, @NotNull String artifactID) {
        return String.format("/META-INF/maven/%s/%s/pom.properties", groupID, artifactID);
    }

    public static synchronized @Nullable String getVersion(@NotNull Object provider,
                                                           @NotNull String groupID,
                                                           @NotNull String artifactID) {
        String path = getMavenPropertiesPath(groupID, artifactID);
        String version = null;
        // Using maven properties to get the version
        try (InputStream is = provider.getClass().getResourceAsStream(path)) {
            if (is != null) {
                Properties p = new Properties();
                p.load(is);
                version = p.getProperty("version", "");
            }
        } catch (Exception ignored) {
        }

        if (version != null) return version;

        // Fine, lets try Java API
        Package pkg = provider.getClass().getPackage();
        if (pkg != null) {
            version = pkg.getImplementationVersion();
            if (version == null) {
                version = pkg.getSpecificationVersion();
            }
        }

        return version;
    }


}
