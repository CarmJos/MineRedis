package cc.carm.plugin.mineredis;

import cc.carm.plugin.mineredis.api.RedisManager;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisURI;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import java.util.logging.Logger;

public interface IMineRedis {

    @NotNull Logger getLogger();

    @NotNull String getServerID();

    void supplyServerID(@Nullable Supplier<String> idProvider);

    @NotNull RedisManager getManager();

    @NotNull RedisManager create(@NotNull RedisURI url,
                                 @NotNull ClientResources resources,
                                 @NotNull ClientOptions options);

    default @NotNull RedisManager create(@NotNull RedisURI url, @NotNull ClientOptions options) {
        return create(url, DefaultClientResources.builder().ioThreadPoolSize(4).computationThreadPoolSize(4).build(), options);
    }

    default @NotNull RedisManager create(@NotNull RedisURI url) {
        return create(url, ClientOptions.builder().autoReconnect(true).build());
    }

    /**
     * 终止并关闭一个 RedisManager 实例。
     *
     * @param manager RedisManager实例
     */
    void shutdown(RedisManager manager);

}
