package cc.carm.plugin.mineredis;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Logger;

public interface MineRedisPlatform {

    @NotNull String getServerIdentifier();

    @NotNull File getPluginFolder();

    @NotNull Logger getLogger();

}
