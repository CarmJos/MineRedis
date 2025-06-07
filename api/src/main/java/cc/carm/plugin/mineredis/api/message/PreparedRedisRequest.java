package cc.carm.plugin.mineredis.api.message;

import cc.carm.plugin.mineredis.api.RedisManager;
import com.google.common.io.ByteArrayDataOutput;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

public class PreparedRedisRequest {

    protected final @NotNull RedisManager redis;
    protected final @NotNull String requestChannel;
    protected final @NotNull ByteArrayDataOutput requestData;

    protected Predicate<RedisMessage> filter;

    public PreparedRedisRequest(@NotNull RedisManager redis,
                                @NotNull String requestChannel, @NotNull ByteArrayDataOutput requestData) {
        this.redis = redis;
        this.requestChannel = requestChannel;
        this.requestData = requestData;
    }

    public PreparedRedisRequest filter(@NotNull Predicate<RedisMessage> filter) {
        this.filter = this.filter == null ? filter : this.filter.and(filter);
        return this;
    }

    public <R> CompletableFuture<R> handleResponse(@NotNull String channel,
                                                   @NotNull Function<RedisMessage, R> handler) {
        CompletableFuture<R> future = new CompletableFuture<>();
        RedisMessageListener listener = message -> {
            if (filter != null && !filter.test(message)) return;
            future.complete(handler.apply(message));
        };
        redis.registerChannelListener(listener, channel);
        redis.publish(requestChannel, requestData);
        return future.whenComplete((r, e) -> redis.unregisterListener(listener));
    }

}
