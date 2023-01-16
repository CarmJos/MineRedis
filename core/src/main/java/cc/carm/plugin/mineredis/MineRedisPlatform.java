package cc.carm.plugin.mineredis;

import cc.carm.lib.easyplugin.utils.ColorParser;
import cc.carm.lib.easyplugin.utils.JarResourceUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Logger;

public interface MineRedisPlatform {

    @NotNull String getServerIdentifier();

    @NotNull File getPluginFolder();

    @NotNull Logger getLogger();

    default void outputInfo(InputStream fileStream, Consumer<String> messageConsumer) {
        Optional.ofNullable(JarResourceUtils.readResource(fileStream))
                .map(v -> ColorParser.parse(Arrays.asList(v)))
                .ifPresent(list -> list.forEach(messageConsumer));
    }
}
