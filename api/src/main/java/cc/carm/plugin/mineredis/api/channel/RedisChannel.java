package cc.carm.plugin.mineredis.api.channel;

import cc.carm.plugin.mineredis.MineRedis;
import cc.carm.plugin.mineredis.api.message.PreparedRedisMessage;
import cc.carm.plugin.mineredis.api.message.RedisMessage;
import cc.carm.plugin.mineredis.api.message.RedisMessageListener;
import com.google.common.io.ByteArrayDataOutput;
import io.lettuce.core.RedisFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class RedisChannel implements RedisMessageListener {

    public static final String CHANNEL_DELIMITER = ".";

    /**
     * 创建一个新的 RedisChannel 实例，用于监听指定的频道。
     *
     * @param channel 频道名称，使用 {@link #CHANNEL_DELIMITER} 作为分隔符。
     * @return 一个新的 RedisChannelBuilder 实例，用于构建 RedisChannel。
     */
    public static RedisChannelBuilder at(String channel) {
        return new RedisChannelBuilder(channel);
    }

    /**
     * 创建一个新的 RedisChannel 实例，用于监听指定的频道。
     *
     * @param channelParts 频道的各个部分，将使用 {@link #CHANNEL_DELIMITER} 连接。
     * @return 一个新的 RedisChannelBuilder 实例，用于构建 RedisChannel。
     */
    public static RedisChannelBuilder at(String... channelParts) {
        return at(String.join(CHANNEL_DELIMITER, channelParts));
    }

    protected final @NotNull String channel;
    protected final @Nullable Predicate<RedisMessage> filter;
    protected final @Nullable Function<RedisMessage, PreparedRedisMessage> handler;

    public RedisChannel(@NotNull String channel,
                        @Nullable Predicate<RedisMessage> filter,
                        @Nullable Function<RedisMessage, PreparedRedisMessage> handler) {
        this.channel = channel;
        this.filter = filter;
        this.handler = handler;
    }

    public @NotNull String getChannel() {
        return this.channel;
    }

    public boolean shouldRegister() {
        return this.handler != null;
    }

    @Override
    public void handle(RedisMessage message) {
        if (handler == null) return;
        if (!channel.equals(message.getChannel())) return;
        if (filter != null && !filter.test(message)) return;
        PreparedRedisMessage response = handler.apply(message);
        if (response != null) response.publish();
    }

    public RedisFuture<Long> publishAsync(@NotNull ByteArrayDataOutput data) {
        return MineRedis.publishAsync(channel, data);
    }


    public RedisFuture<Long> publishAsync(@NotNull Consumer<ByteArrayDataOutput> data) {
        return MineRedis.publishAsync(channel, data);
    }
    
    public RedisFuture<Long> publishAsync(Object... values) {
        return MineRedis.publishAsync(channel, values);
    }

    public long publish(@NotNull Object... values) {
        return MineRedis.publish(channel, values);
    }

    public long publish(@NotNull ByteArrayDataOutput data) {
        return MineRedis.publish(channel, data);
    }

    public long publish(@NotNull Consumer<ByteArrayDataOutput> data) {
        return MineRedis.publish(channel, data);
    }

    public static class RedisChannelBuilder {

        protected final String channel;
        protected Predicate<RedisMessage> filter = m -> !m.isLocalMessage();

        public RedisChannelBuilder(String channel) {
            this.channel = channel;
        }

        public RedisChannelBuilder filter(Predicate<RedisMessage> filter) {
            this.filter = this.filter == null ? filter : this.filter.and(filter);
            return this;
        }

        public RedisChannel handle(Consumer<RedisMessage> handler) {
            return handle(m -> {
                handler.accept(m);
                return null;
            });
        }

        public RedisChannel handle(Function<RedisMessage, PreparedRedisMessage> handler) {
            return new RedisChannel(channel, filter, handler);
        }

    }

}
