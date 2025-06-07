package cc.carm.plugin.mineredis.api.channel;

import cc.carm.plugin.mineredis.MineRedis;
import cc.carm.plugin.mineredis.api.message.RedisMessage;
import cc.carm.plugin.mineredis.api.message.RedisMessageListener;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class RedisCallback<KEY, REQUEST, RESPONSE> implements RedisMessageListener {

    /**
     * 创建一个新的 RedisCallback.Builder 实例，用于构建 RedisCallback。
     *
     * @param requestClazz  请求消息的类类型，用于序列化和反序列化。
     * @param responseClazz 响应消息的类类型，用于解析响应。
     * @param <KEY>         请求和响应的键类型，用于标识请求和响应的唯一性。
     * @param <REQUEST>     请求消息的类型参数，通常是一个具体的类。
     * @param <RESPONSE>    响应消息的类型参数，通常是一个具体的类。
     * @return 一个新的 RedisCallback.Builder 实例，用于构建 RedisCallback。
     */
    public static <KEY, REQUEST, RESPONSE>
    Builder<KEY, REQUEST, RESPONSE> create(
            @NotNull Class<KEY> keyClass,
            @NotNull Class<REQUEST> requestClazz,
            @NotNull Class<RESPONSE> responseClazz) {
        return new Builder<>(requestClazz, responseClazz);
    }

    protected final @NotNull String requestChannel;
    protected final @NotNull String responseChannel;

    protected final @NotNull BiConsumer<ByteArrayDataOutput, REQUEST> requestSerializer;

    protected final @NotNull Function<REQUEST, KEY> requestKey;
    protected final @NotNull Function<RedisMessage, KEY> responseKey;

    protected final @NotNull Function<RedisMessage, RESPONSE> responseParser;
    protected final @NotNull Map<KEY, CompletableFuture<RESPONSE>> pendingRequests = new ConcurrentHashMap<>();

    public RedisCallback(@NotNull String requestChannel, @NotNull String responseChannel,
                         @NotNull Function<REQUEST, KEY> requestKey, @NotNull Function<RedisMessage, KEY> responseKey,
                         @NotNull BiConsumer<ByteArrayDataOutput, REQUEST> requestSerializer,
                         @NotNull Function<RedisMessage, RESPONSE> responseParser) {
        this.requestChannel = requestChannel;
        this.responseChannel = responseChannel;
        this.requestSerializer = requestSerializer;
        this.requestKey = requestKey;
        this.responseKey = responseKey;
        this.responseParser = responseParser;
    }

    public @NotNull String requestChannel() {
        return this.requestChannel;
    }

    public @NotNull String responseChannel() {
        return this.responseChannel;
    }

    public @NotNull Map<KEY, CompletableFuture<RESPONSE>> pendingRequests() {
        return pendingRequests;
    }

    public @Nullable CompletableFuture<RESPONSE> get(@NotNull KEY key) {
        return pendingRequests.get(key);
    }

    public void cancel(@NotNull KEY key) {
        CompletableFuture<RESPONSE> future = pendingRequests.remove(key);
        if (future != null) {
            future.completeExceptionally(new RuntimeException("Request cancelled."));
        }
    }

    /**
     * 取消所有挂起的请求，并清理相关资源。
     * 这通常在关闭连接或应用程序退出时调用。
     */
    public void shutdown() {
        for (CompletableFuture<RESPONSE> future : pendingRequests.values()) {
            future.completeExceptionally(new RuntimeException("Shutting down. All requests cancelled."));
        }
        pendingRequests.clear();
    }

    @Override
    public void handle(RedisMessage message) {
        if (!responseChannel.equals(message.getChannel())) return;

        KEY key = keyOf(message);
        CompletableFuture<RESPONSE> future = pendingRequests.remove(key);
        if (future == null) return; // 无对应请求

        try {
            future.complete(responseParser.apply(message));
        } catch (Exception ex) {
            future.completeExceptionally(new RuntimeException("Failed to handle response", ex));
        }
    }

    public CompletableFuture<RESPONSE> call(@NotNull Supplier<REQUEST> request) {
        return call(request.get());
    }

    public CompletableFuture<RESPONSE> call(@NotNull REQUEST request) {
        KEY key = keyOf(request);
        CompletableFuture<RESPONSE> exists = get(key);
        if (exists != null) return exists;

        CompletableFuture<RESPONSE> future = new CompletableFuture<>();
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        try {
            requestSerializer.accept(out, request);
        } catch (Exception ex) {
            future.completeExceptionally(new RuntimeException("Failed to serialize request", ex));
            return future;
        }

        this.pendingRequests.put(key, future);

        return MineRedis.publishAsync(requestChannel, out)
                .thenCompose(l -> future)
                .whenComplete((r, e) -> this.pendingRequests.remove(key))
                .toCompletableFuture();
    }

    public @NotNull KEY keyOf(@NotNull REQUEST request) {
        return Objects.requireNonNull(requestKey.apply(request), "Request key cannot be null.");
    }

    public @NotNull KEY keyOf(@NotNull RedisMessage response) {
        return Objects.requireNonNull(responseKey.apply(response), "Response key cannot be null.");
    }

    public static class Builder<KEY, REQUEST, RESPONSE> {

        protected String requestChannel;
        protected String responseChannel;

        protected BiConsumer<ByteArrayDataOutput, REQUEST> requestSerializer;

        protected Function<REQUEST, KEY> requestKey;
        protected Function<RedisMessage, KEY> responseKey;

        protected Function<RedisMessage, RESPONSE> responseParser;

        public Builder(@NotNull Class<REQUEST> requestClazz, @NotNull Class<RESPONSE> responseClazz) {
            this.requestSerializer = RedisSerializableMessage.serializerOf(requestClazz);
            this.responseParser = RedisSerializableMessage.deserializerOf(responseClazz);
        }

        public Builder<KEY, REQUEST, RESPONSE> at(@NotNull String channel) {
            return requestAt(channel + RedisChannel.CHANNEL_DELIMITER + "request")
                    .responseAt(channel + RedisChannel.CHANNEL_DELIMITER + "response");
        }

        public Builder<KEY, REQUEST, RESPONSE> requestAt(@NotNull String channel) {
            this.requestChannel = channel;
            return this;
        }

        public Builder<KEY, REQUEST, RESPONSE> responseAt(@NotNull String channel) {
            this.responseChannel = channel;
            return this;
        }

        public Builder<KEY, REQUEST, RESPONSE> keys(@NotNull Function<REQUEST, KEY> requestKey,
                                                    @NotNull Function<RedisMessage, KEY> responseKey) {
            return requestKey(requestKey).responseKey(responseKey);
        }

        public Builder<KEY, REQUEST, RESPONSE> requestKey(@NotNull Function<REQUEST, KEY> keyFunction) {
            this.requestKey = keyFunction;
            return this;
        }

        public Builder<KEY, REQUEST, RESPONSE> responseKey(@NotNull Function<RedisMessage, KEY> keyFunction) {
            this.responseKey = keyFunction;
            return this;
        }

        public Builder<KEY, REQUEST, RESPONSE> request(@NotNull BiConsumer<ByteArrayDataOutput, REQUEST> serializer) {
            this.requestSerializer = serializer;
            return this;
        }

        public Builder<KEY, REQUEST, RESPONSE> response(@NotNull Function<RedisMessage, RESPONSE> parser) {
            this.responseParser = parser;
            return this;
        }

        public RedisCallback<KEY, REQUEST, RESPONSE> build() {
            Objects.requireNonNull(requestChannel, "Request channel must be set.");
            Objects.requireNonNull(responseChannel, "Response channel must be set.");
            Objects.requireNonNull(requestSerializer, "Request serializer must be set.");
            Objects.requireNonNull(requestKey, "Request key function must be set.");
            Objects.requireNonNull(responseKey, "Response key function must be set.");
            Objects.requireNonNull(responseParser, "Response parser must be set.");

            return new RedisCallback<>(
                    requestChannel, responseChannel,
                    requestKey, responseKey,
                    requestSerializer, responseParser
            );
        }


    }


}
