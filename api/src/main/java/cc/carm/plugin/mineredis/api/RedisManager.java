package cc.carm.plugin.mineredis.api;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;

public interface RedisManager extends RedisMessageManager {

    RedisClient getClient();

    /**
     * 准备以同步方式操作RedisCommands。
     *
     * @return {@link RedisCommands}
     */
    RedisCommands<String, String> sync();

    /**
     * 准备以异步方式操作RedisCommands。
     *
     * @return {@link RedisAsyncCommands}
     */
    RedisAsyncCommands<String, String> async();

}
