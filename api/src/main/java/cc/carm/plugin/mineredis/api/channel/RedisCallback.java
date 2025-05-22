package cc.carm.plugin.mineredis.api.channel;

import cc.carm.plugin.mineredis.MineRedis;
import cc.carm.plugin.mineredis.api.message.RedisMessage;
import cc.carm.plugin.mineredis.api.message.RedisMessageListener;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class RedisCallback<REQUEST, RESPONSE> implements RedisMessageListener {

    public static <T, R> Builder<T, R> create() {
        return new Builder<>();
    }

    public static Builder<ByteArrayDataOutput, ByteArrayDataOutput> original(@NotNull String requestChannel,
                                                                             @NotNull String responseChannel) {
        return RedisCallback.<ByteArrayDataOutput, ByteArrayDataOutput>create()
                .requester(requestChannel, (b, r) -> b.write(r.toByteArray()))
                .responser(responseChannel, (b, r) -> b.write(r.toByteArray()));
    }

    protected final @NotNull String requestChannel;
    protected final @NotNull String responseChannel;

    protected final @Nullable Predicate<RedisMessage> requestFilter;
    protected final @Nullable Predicate<RedisMessage> responseFilter;

    protected final @NotNull BiConsumer<ByteArrayDataOutput, REQUEST> requestSerializer;
    protected final @NotNull BiConsumer<ByteArrayDataOutput, RESPONSE> responseSerializer;

    protected final @NotNull Function<RedisMessage, RESPONSE> handler;

    public RedisCallback(@NotNull String requestChannel, @NotNull String responseChannel,
                         @Nullable Predicate<RedisMessage> requestFilter,
                         @Nullable Predicate<RedisMessage> responseFilter,
                         @NotNull BiConsumer<ByteArrayDataOutput, REQUEST> requestSerializer,
                         @NotNull BiConsumer<ByteArrayDataOutput, RESPONSE> responseSerializer,
                         @NotNull Function<RedisMessage, RESPONSE> handler) {
        this.requestChannel = requestChannel;
        this.responseChannel = responseChannel;
        this.requestFilter = requestFilter;
        this.responseFilter = responseFilter;
        this.requestSerializer = requestSerializer;
        this.responseSerializer = responseSerializer;
        this.handler = handler;
    }

    public @NotNull String getRequestChannel() {
        return this.requestChannel;
    }

    @Override
    public void handle(RedisMessage message) {
        if (!requestChannel.equals(message.getChannel())) return;
        if (requestFilter != null && !requestFilter.test(message)) return;

        RESPONSE response = handler.apply(message);
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        responseSerializer.accept(out, response);
        MineRedis.publish(requestChannel, out);
    }

    public CompletableFuture<RESPONSE> call(@NotNull REQUEST request) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        requestSerializer.accept(out, request);

        CompletableFuture<RESPONSE> future = new CompletableFuture<>();
        RedisMessageListener listener = message -> {
            if (requestFilter != null && !requestFilter.test(message)) return;
            future.complete(handler.apply(message));
        };
        MineRedis.registerChannelListener(listener, requestChannel);
        return MineRedis.publishAsync(requestChannel, out)
                .thenCompose(l -> future)
                .whenComplete((r, e) -> MineRedis.unregisterListener(listener))
                .toCompletableFuture();
    }

    public static class Builder<REQUEST, RESPONSE> {

        protected String requestChannel;
        protected String responseChannel;

        protected @Nullable Predicate<RedisMessage> requestFilter;
        protected @Nullable Predicate<RedisMessage> responseFilter;

        protected BiConsumer<ByteArrayDataOutput, REQUEST> requestSerializer;
        protected BiConsumer<ByteArrayDataOutput, RESPONSE> responseSerializer;

        protected Function<RedisMessage, RESPONSE> handler;

        public Builder() {
        }

        public Builder<REQUEST, RESPONSE> requester(@NotNull String channel,
                                                    @NotNull BiConsumer<ByteArrayDataOutput, REQUEST> serializer) {
            return requester(channel, null, serializer);
        }

        public Builder<REQUEST, RESPONSE> requester(@NotNull String channel,
                                                    @Nullable Predicate<RedisMessage> filter,
                                                    @NotNull BiConsumer<ByteArrayDataOutput, REQUEST> serializer) {
            this.requestChannel = channel;
            this.requestFilter = filter;
            this.requestSerializer = serializer;
            return this;
        }

        public Builder<REQUEST, RESPONSE> responser(@NotNull String channel,
                                                    @NotNull BiConsumer<ByteArrayDataOutput, RESPONSE> serializer) {
            return responser(channel, null, serializer);
        }

        public Builder<REQUEST, RESPONSE> responser(@NotNull String channel,
                                                    @Nullable Predicate<RedisMessage> filter,
                                                    @NotNull BiConsumer<ByteArrayDataOutput, RESPONSE> serializer) {
            this.responseChannel = channel;
            this.responseFilter = filter;
            this.responseSerializer = serializer;
            return this;
        }

        public RedisCallback<REQUEST, RESPONSE> handle(Function<RedisMessage, RESPONSE> handler) {
            return new RedisCallback<>(
                    Objects.requireNonNull(this.requestChannel),
                    Objects.requireNonNull(this.responseChannel),
                    this.requestFilter, this.responseFilter,
                    Objects.requireNonNull(this.requestSerializer),
                    Objects.requireNonNull(this.responseSerializer),
                    Objects.requireNonNull(handler)
            );
        }
    }


}
