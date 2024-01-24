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

    public static RedisChannel of(@NotNull String channel) {
        return new RedisChannel(channel, null, null);
    }

    public static RedisChannel of(@NotNull String... channelPart) {
        return new RedisChannel(String.join(".", channelPart), null, null);
    }

    public static RedisChannel of(@NotNull String channel, @NotNull Consumer<RedisMessage> handler) {
        return builder(channel).handle(handler);
    }

    public static RedisChannel of(@NotNull String channel, @NotNull Function<RedisMessage, PreparedRedisMessage> handler) {
        return builder(channel).handle(handler);
    }

    public static RedisChannel of(@NotNull String channel, @NotNull Predicate<RedisMessage> predicate,
                                  @NotNull Function<RedisMessage, PreparedRedisMessage> handler) {
        return builder(channel).filter(predicate).handle(handler);
    }

    public static RedisChannelBuilder builder(String channel) {
        return new RedisChannelBuilder(channel);
    }

    public static RedisChannelBuilder builder(String... channelParts) {
        return builder(String.join(".", channelParts));
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

}
