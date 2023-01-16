package cc.carm.plugin.mineredis;

import cc.carm.plugin.mineredis.api.RedisManager;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisURI;
import io.lettuce.core.resource.ClientResources;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import java.util.logging.Logger;

public class MineRedis {

    private static IMineRedis instance;

    protected static void initializeAPI(IMineRedis api) {
        MineRedis.instance = api;
    }

    public static @NotNull Logger getLogger() {
        return instance.getLogger();
    }

    public static @NotNull String getServerID() {
        return instance.getServerID();
    }

    public static void supplyServerID(@Nullable Supplier<String> idProvider) {
        instance.supplyServerID(idProvider);
    }

    public static @NotNull RedisManager getManager() {
        return instance.getManager();
    }

    public static @NotNull RedisManager create(@NotNull RedisURI url,
                                               @NotNull ClientResources resources,
                                               @NotNull ClientOptions options) {
        return instance.create(url, resources, options);
    }

    public static @NotNull RedisManager create(@NotNull RedisURI url, @NotNull ClientOptions options) {
        return instance.create(url, options);
    }

    public static @NotNull RedisManager create(@NotNull RedisURI url) {
        return instance.create(url);
    }

    /**
     * 终止并关闭一个 RedisManager 实例。
     *
     * @param manager RedisManager实例
     */
    public static void shutdown(RedisManager manager) {
        instance.shutdown(manager);
    }


}
