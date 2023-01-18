package cc.carm.plugin.mineredis.api;

import cc.carm.plugin.mineredis.api.message.RedisMessageListener;
import com.google.common.io.ByteArrayDataOutput;
import io.lettuce.core.RedisFuture;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * 发布与订阅(Pub/Sub)管理器。
 *
 * @since 1.1.0
 */
public interface RedisMessageManager {

    /**
     * 订阅某些指定频道。
     * <br>订阅后，本端将接收该频道中的消息，并触发对应的Handler。
     *
     * @param channel      频道名
     * @param moreChannels 频道名
     */
    void subscribe(@NotNull String channel, @NotNull String... moreChannels);

    /**
     * 取消订阅某些指定频道。
     * <br>取消订阅后，本端将不再接收该频道中的消息。
     *
     * @param channel      频道名
     * @param moreChannels 频道名
     */
    void unsubscribe(@NotNull String channel, @NotNull String... moreChannels);

    /**
     * 以模式匹配的方式订阅频道，支持使用“*”“?”等符号。
     * <br>订阅后，本端将接收相关频道中的消息，并触发对应的Handler。
     *
     * @param channelPattern 频道条件
     * @param morePatterns   频道条件
     */
    void subscribePattern(@NotNull String channelPattern, @NotNull String... morePatterns);

    /**
     * 取消以模式匹配的方式订阅频道，支持使用“*”“?”等符号。
     * <br>取消订阅后，本端将不再接收相关频道中的消息。
     *
     * @param channelPattern 频道条件
     * @param morePatterns   频道条件
     */
    void unsubscribePattern(@NotNull String channelPattern, @NotNull String... morePatterns);

    long publish(@NotNull String channel, @NotNull ByteArrayDataOutput byteOutput);

    long publish(@NotNull String channel, @NotNull Consumer<ByteArrayDataOutput> byteOutput);

    default long publish(@NotNull String channel, @NotNull String content) {
        return publish(channel, s -> s.writeUTF(content));
    }

    RedisFuture<Long> publishAsync(@NotNull String channel, @NotNull ByteArrayDataOutput byteOutput);

    RedisFuture<Long> publishAsync(@NotNull String channel, @NotNull Consumer<ByteArrayDataOutput> byteOutput);

    default RedisFuture<Long> publishAsync(@NotNull String channel, @NotNull String content) {
        return publishAsync(channel, s -> s.writeUTF(content));
    }

    void registerGlobalListener(@NotNull RedisMessageListener listener, @NotNull RedisMessageListener... moreListeners);

    void registerChannelListener(@NotNull RedisMessageListener listener,
                                 @NotNull String channel, @NotNull String... moreChannels);

    void registerPatternListener(@NotNull RedisMessageListener listener,
                                 @NotNull String channelPattern, @NotNull String... morePatterns);

    void unregisterListener(@NotNull RedisMessageListener listener);
}
