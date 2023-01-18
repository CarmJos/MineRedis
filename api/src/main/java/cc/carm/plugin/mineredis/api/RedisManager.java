package cc.carm.plugin.mineredis.api;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;

public interface RedisManager extends RedisMessageManager {

    RedisClient getClient();

    RedisCommands<String, String> sync();

    RedisAsyncCommands<String, String> async();
    
}
