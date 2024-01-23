package cc.carm.plugin.mineredis.api.channel;

import cc.carm.plugin.mineredis.MineRedis;
import cc.carm.plugin.mineredis.api.message.RedisMessage;
import cc.carm.plugin.mineredis.api.message.RedisMessageListener;
import cc.carm.plugin.mineredis.api.message.PreparedRedisMessage;
import com.google.common.io.ByteArrayDataOutput;
import io.lettuce.core.RedisFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class RedisChannel implements RedisMessageListener {

    public static RedisChannelBuilder builder(String channel) {
        return new RedisChannelBuilder(channel);
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
