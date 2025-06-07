package cc.carm.plugin.mineredis.api.channel;

import cc.carm.plugin.mineredis.MineRedis;
import cc.carm.plugin.mineredis.api.message.PreparedRedisMessage;
import cc.carm.plugin.mineredis.api.message.RedisMessage;
import cc.carm.plugin.mineredis.api.message.RedisMessageListener;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.lettuce.core.RedisFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.*;

public class RedisRequest<MSG> implements RedisMessageListener {

    /**
     * 创建一个新的 RedisRequest.Builder 实例，用于构建 RedisRequest。
     *
     * @param channel      频道名称，通常是一个字符串标识符。
     * @param requestClazz 请求消息的类类型，用于序列化和反序列化。
     * @param <T>          请求消息的类型参数，通常是一个具体的类。
     * @return 一个新的 RedisRequest.Builder 实例，用于构建 RedisRequest。
     */
    public static <T> Builder<T> at(String channel, Class<T> requestClazz) {
        return new Builder<>(channel, requestClazz);
    }

    protected final @NotNull String channel;

    protected @NotNull BiConsumer<ByteArrayDataOutput, MSG> serializer;

    protected @Nullable Predicate<RedisMessage> filter;
    protected @Nullable Function<RedisMessage, PreparedRedisMessage> handler;

    public RedisRequest(@NotNull String channel,
                        @Nullable Predicate<RedisMessage> filter,
                        @NotNull BiConsumer<ByteArrayDataOutput, MSG> serializer,
                        @Nullable Function<RedisMessage, PreparedRedisMessage> handler) {
        this.channel = channel;
        this.filter = filter;
        this.serializer = serializer;
        this.handler = handler;
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

    public @NotNull String getChannel() {
        return this.channel;
    }

    public long publish(@NotNull Supplier<MSG> message) {
        return publish(message.get());
    }

    public long publish(@NotNull MSG message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        serializer.accept(out, message);
        return MineRedis.publish(channel, out);
    }

    public @NotNull RedisFuture<Long> publishAsync(@NotNull Supplier<MSG> message) {
        return publishAsync(message.get());
    }

    public @NotNull RedisFuture<Long> publishAsync(@NotNull MSG message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        serializer.accept(out, message);
        return MineRedis.publishAsync(channel, out);
    }

    public static class Builder<REQUEST> {

        protected @NotNull Class<REQUEST> requestClazz;
        protected @NotNull String requestChannel;

        protected @Nullable Predicate<RedisMessage> filter;
        protected BiConsumer<ByteArrayDataOutput, REQUEST> serializer;
        protected Function<RedisMessage, REQUEST> deserializer;

        public Builder(@NotNull String channel, @NotNull Class<REQUEST> requestClazz) {
            this.requestChannel = channel;
            this.requestClazz = requestClazz;
            if (RedisSerializableMessage.class.isAssignableFrom(requestClazz)) {
                // 如果 requestClazz 是 RedisChannelMessage 的子类，则默认使用类提供的序列化方式
                this.serializer = RedisSerializableMessage.serializerOf(requestClazz);
                this.deserializer = RedisSerializableMessage.deserializerOf(requestClazz);
            }
        }

        public Builder<REQUEST> filter(@NotNull Predicate<RedisMessage> filter) {
            this.filter = this.filter == null ? null : this.filter.and(filter);
            return this;
        }

        public Builder<REQUEST> serializer(@NotNull BiConsumer<ByteArrayDataOutput, REQUEST> serializer) {
            this.serializer = Objects.requireNonNull(serializer);
            return this;
        }

        public Builder<REQUEST> deserializer(@NotNull Function<RedisMessage, REQUEST> deserializer) {
            this.deserializer = Objects.requireNonNull(deserializer);
            return this;
        }

        public RedisRequest<REQUEST> handle(@Nullable Function<RedisMessage, PreparedRedisMessage> handler) {
            return new RedisRequest<>(this.requestChannel, this.filter, this.serializer, handler);
        }

        public RedisRequest<REQUEST> handle(@Nullable BiFunction<RedisMessage, REQUEST, PreparedRedisMessage> handler) {
            if (handler == null) return build();
            if (this.deserializer == null) {
                throw new IllegalStateException("Deserializer must be set before using REQUEST handler");
            }
            return new RedisRequest<>(
                    this.requestChannel, this.filter,
                    this.serializer, message -> {
                REQUEST request = deserializer.apply(message);
                return handler.apply(message, request);
            });
        }

        public RedisRequest<REQUEST> build() {
            return handle((BiFunction<RedisMessage, REQUEST, PreparedRedisMessage>) null);
        }
    }
}
