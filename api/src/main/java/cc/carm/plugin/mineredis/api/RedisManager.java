package cc.carm.plugin.mineredis.api;

import cc.carm.plugin.mineredis.api.message.RedisMessageListener;
import com.google.common.io.ByteArrayDataOutput;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface RedisManager {

    RedisClient getClient();

    RedisCommands<String, String> sync();

    RedisAsyncCommands<String, String> async();

    void subscribe(@NotNull String channel, @NotNull String... moreChannels);

    void unsubscribe(@NotNull String channel, @NotNull String... moreChannels);

    void subscribePattern(@NotNull String channelPattern, @NotNull String... morePatterns);

    void unsubscribePattern(@NotNull String channelPattern, @NotNull String... morePatterns);

    long publish(@NotNull String channel, @NotNull ByteArrayDataOutput byteOutput);

    default long publish(@NotNull String channel, @NotNull String content) {
        return publish(channel, s -> s.writeUTF(content));
    }

    long publish(@NotNull String channel, @Nullable Consumer<ByteArrayDataOutput> byteOutput);

    void registerGlobalListener(@NotNull RedisMessageListener listener);

    void registerChannelListener(@NotNull RedisMessageListener handler,
                                 @NotNull String channel, @NotNull String... moreChannels);

    void registerPatternListener(@NotNull RedisMessageListener handler,
                                 @NotNull String channelPattern, @NotNull String... morePatterns);

    void unregisterListener(@NotNull RedisMessageListener handler);

}
