package cc.carm.plugin.mineredis;

import cc.carm.plugin.mineredis.api.RedisManager;
import cc.carm.plugin.mineredis.api.message.RedisMessageListener;
import cc.carm.plugin.mineredis.api.message.RedisMessage;
import cc.carm.plugin.mineredis.handler.RedisByteCodec;
import cc.carm.plugin.mineredis.handler.RedisSubListener;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.resource.ClientResources;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MineRedisManager implements RedisManager {

    protected final @NotNull RedisClient client;

    protected final @NotNull StatefulRedisConnection<String, String> conn;
    protected final @NotNull StatefulRedisPubSubConnection<String, byte[]> subConn;
    protected final @NotNull StatefulRedisPubSubConnection<String, byte[]> pubConn;

    protected final @NotNull List<RedisMessageListener> globalListeners = new ArrayList<>();
    protected final @NotNull Map<String, RedisMessageListener> channelListeners = new ConcurrentHashMap<>();
    protected final @NotNull Map<String, RedisMessageListener> patternListeners = new ConcurrentHashMap<>();

    public MineRedisManager(@NotNull RedisURI url, @NotNull ClientResources resources, @NotNull ClientOptions options) {

        this.client = RedisClient.create(resources, url);
        this.client.setOptions(options);

        // 用于Redis操作的连接
        this.conn = client.connect();

        // 用于订阅/发布的连接
        this.subConn = client.connectPubSub(new RedisByteCodec());
        this.pubConn = client.connectPubSub(new RedisByteCodec());

        this.subConn.addListener(new RedisSubListener(this));
    }

    @SuppressWarnings("UnstableApiUsage")
    public void handleMessage(String pattern, String channel, String source, long timestamp, byte[] data) {
        List<RedisMessageListener> listeners = new ArrayList<>(globalListeners);

        channelListeners.entrySet().stream()
                .filter(e -> e.getKey().equals(channel))
                .map(Map.Entry::getValue)
                .forEach(listeners::add);

        if (pattern != null) {
            patternListeners.entrySet().stream()
                    .filter(e -> e.getKey().equals(pattern))
                    .map(Map.Entry::getValue)
                    .forEach(listeners::add);
        }

        listeners.forEach(listener -> listener.handle(new RedisMessage(channel, source, timestamp, ByteStreams.newDataInput(data))));
    }

    @Override
    public @NotNull RedisClient getClient() {
        return client;
    }

    @Override
    public RedisCommands<String, String> sync() {
        return this.conn.sync();
    }

    @Override
    public RedisAsyncCommands<String, String> async() {
        return this.conn.async();
    }

    @Override
    public void subscribe(@NotNull String channel, @NotNull String... moreChannels) {
        subConn.sync().subscribe(channel);
        if (moreChannels.length > 0) subConn.sync().subscribe(moreChannels);
    }

    @Override
    public void unsubscribe(@NotNull String channel, @NotNull String... moreChannels) {
        subConn.sync().unsubscribe(channel);
        if (moreChannels.length > 0) subConn.sync().unsubscribe(moreChannels);
        channelListeners.remove(channel);
    }

    @Override
    public void subscribePattern(@NotNull String channelPattern, @NotNull String... morePatterns) {
        subConn.sync().psubscribe();
        if (morePatterns.length > 0) subConn.sync().psubscribe(morePatterns);
    }

    @Override
    public void unsubscribePattern(@NotNull String channelPattern, @NotNull String... morePatterns) {
        subConn.sync().punsubscribe(channelPattern);
        if (morePatterns.length > 0) subConn.sync().punsubscribe(morePatterns);
        patternListeners.remove(channelPattern);
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public long publish(@NotNull String channel, @NotNull ByteArrayDataOutput byteOutput) {
        ByteArrayDataOutput stream = ByteStreams.newDataOutput();
        stream.writeUTF(MineRedis.getServerID()); // 在头部写入本节点的ID
        stream.writeLong(System.currentTimeMillis()); // 在头部写入发送时间
        stream.write(byteOutput.toByteArray()); // 写入用户传入的内容
        return pubConn.sync().publish(channel, stream.toByteArray());
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public long publish(@NotNull String channel, @Nullable Consumer<ByteArrayDataOutput> byteOutput) {
        ByteArrayDataOutput stream = ByteStreams.newDataOutput();
        if (byteOutput != null) byteOutput.accept(stream);
        return publish(channel, stream);
    }

    @Override
    public void registerGlobalListener(@NotNull RedisMessageListener listener) {
        globalListeners.add(listener);
    }

    @Override
    public void registerChannelListener(@NotNull RedisMessageListener handler, @NotNull String channel, @NotNull String... moreChannels) {
        subscribe(channel, moreChannels);
        channelListeners.put(channel, handler);
        Arrays.stream(moreChannels).forEach(c -> channelListeners.put(c, handler));
    }

    @Override
    public void registerPatternListener(@NotNull RedisMessageListener handler, @NotNull String channelPattern, @NotNull String... morePatterns) {
        subscribePattern(channelPattern, morePatterns);
        patternListeners.put(channelPattern, handler);
        Arrays.stream(morePatterns).forEach(c -> patternListeners.put(c, handler));
    }

    @Override
    public void unregisterListener(@NotNull RedisMessageListener handler) {
        globalListeners.remove(handler);
        channelListeners.entrySet().removeIf(e -> e.getValue().equals(handler));
        patternListeners.entrySet().removeIf(e -> e.getValue().equals(handler));
    }

}
