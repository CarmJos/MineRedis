package cc.carm.plugin.mineredis;

import cc.carm.plugin.mineredis.api.RedisManager;
import cc.carm.plugin.mineredis.api.channel.RedisChannel;
import cc.carm.plugin.mineredis.api.message.RedisMessageListener;
import cc.carm.plugin.mineredis.api.request.RedisRequestBuilder;
import com.google.common.io.ByteArrayDataOutput;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.resource.ClientResources;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class MineRedis {

    private static IMineRedis instance;

    protected static void initializeAPI(IMineRedis api) {
        MineRedis.instance = api;
    }

    public static @NotNull Logger getLogger() {
        return instance.getLogger();
    }

    public static @NotNull String getServerID() {
        return instance.getServerID();
    }

    public static void supplyServerID(@Nullable Supplier<String> idProvider) {
        instance.supplyServerID(idProvider);
    }

    public static @NotNull RedisManager getManager() {
        return instance.getManager();
    }

    public static @NotNull RedisManager create(@NotNull RedisURI url,
                                               @NotNull ClientResources resources,
                                               @NotNull ClientOptions options) {
        return instance.create(url, resources, options);
    }

    public static @NotNull RedisManager create(@NotNull RedisURI url, @NotNull ClientOptions options) {
        return instance.create(url, options);
    }

    public static @NotNull RedisManager create(@NotNull RedisURI url) {
        return instance.create(url);
    }

    /**
     * 终止并关闭一个 RedisManager 实例。
     *
     * @param manager RedisManager实例
     */
    public static void shutdown(RedisManager manager) {
        instance.shutdown(manager);
    }

    /**
     * 准备以同步方式操作RedisCommands。
     *
     * @return {@link RedisCommands}
     */
    public static RedisCommands<String, String> sync() {
        return getManager().sync();
    }

    /**
     * 准备以异步方式操作RedisCommands。
     *
     * @return {@link RedisAsyncCommands}
     */
    public static RedisAsyncCommands<String, String> async() {
        return getManager().async();
    }

    public static void subscribe(@NotNull String channel, @NotNull String... moreChannels) {
        getManager().subscribe(channel, moreChannels);
    }

    public static void unsubscribe(@NotNull String channel, @NotNull String... moreChannels) {
        getManager().unsubscribe(channel, moreChannels);
    }

    public static void subscribePattern(@NotNull String channelPattern, @NotNull String... morePatterns) {
        getManager().subscribePattern(channelPattern, morePatterns);
    }

    public static void unsubscribePattern(@NotNull String channelPattern, @NotNull String... morePatterns) {
        getManager().unsubscribePattern(channelPattern, morePatterns);
    }

    public static long publish(@NotNull String channel, @NotNull ByteArrayDataOutput byteOutput) {
        return getManager().publish(channel, byteOutput);
    }

    public static long publish(@NotNull String channel, @NotNull Consumer<ByteArrayDataOutput> byteOutput) {
        return getManager().publish(channel, byteOutput);
    }

    public static long publish(@NotNull String channel, @NotNull Object... values) {
        return getManager().publish(channel, values);
    }

    public static RedisFuture<Long> publishAsync(@NotNull String channel, @NotNull ByteArrayDataOutput byteOutput) {
        return getManager().publishAsync(channel, byteOutput);
    }

    public static RedisFuture<Long> publishAsync(@NotNull String channel, @NotNull Consumer<ByteArrayDataOutput> byteOutput) {
        return getManager().publishAsync(channel, byteOutput);
    }

    public static RedisFuture<Long> publishAsync(@NotNull String channel, @NotNull Object... values) {
        return getManager().publishAsync(channel, values);
    }

    public static RedisRequestBuilder request(@NotNull String channel, @NotNull ByteArrayDataOutput byteOutput) {
        return getManager().callback(channel, byteOutput);
    }

    public static RedisRequestBuilder request(@NotNull String channel, @NotNull Consumer<ByteArrayDataOutput> byteOutput) {
        return getManager().callback(channel, byteOutput);
    }

    public static RedisRequestBuilder request(@NotNull String channel, @NotNull Object... values) {
        return getManager().callback(channel, values);
    }

    public static void registerGlobalListener(@NotNull RedisMessageListener listener, @NotNull RedisMessageListener... moreListeners) {
        getManager().registerGlobalListener(listener, moreListeners);
    }

    public static void registerChannelListener(@NotNull RedisMessageListener listener,
                                               @NotNull String channel, @NotNull String... moreChannels) {
        getManager().registerChannelListener(listener, channel, moreChannels);
    }

    public static void registerPatternListener(@NotNull RedisMessageListener listener,
                                               @NotNull String channelPattern, @NotNull String... morePatterns) {
        getManager().registerPatternListener(listener, channelPattern, morePatterns);
    }

    public static void registerChannels(@NotNull Class<?> channelClazz) {
        getManager().registerChannels(channelClazz);
    }

    public static void unregisterChannels(@NotNull Class<?> channelClazz) {
        getManager().unregisterChannels(channelClazz);
    }

    public static void registerChannel(@NotNull RedisChannel channel) {
        getManager().registerChannel(channel);
    }

    public static void unregisterListener(@NotNull RedisMessageListener listener) {
        getManager().unregisterListener(listener);
    }

}
