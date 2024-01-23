package cc.carm.plugin.mineredis.api.message;

import cc.carm.plugin.mineredis.MineRedis;
import cc.carm.plugin.mineredis.api.RedisMessageManager;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Consumer;

public class PreparedRedisMessage {

    public static PreparedRedisMessage of(@NotNull String channel, @NotNull ByteArrayDataOutput data) {
        return new PreparedRedisMessage(channel, data);
    }

    public static PreparedRedisMessage of(@NotNull String channel, @NotNull Consumer<ByteArrayDataOutput> data) {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        data.accept(output);
        return of(channel, output);
    }

    public static PreparedRedisMessage of(@NotNull String channel, @NotNull Object... values) {
        return of(channel, o -> RedisMessageManager.writeParams(o, Arrays.asList(values)));
    }

    protected final @NotNull String channel;
    protected final @NotNull ByteArrayDataOutput data;

    public PreparedRedisMessage(@NotNull String channel, @NotNull ByteArrayDataOutput data) {
        this.channel = channel;
        this.data = data;
    }

    public String channel() {
        return channel;
    }

    public ByteArrayDataOutput data() {
        return data;
    }

    public void publish() {
        MineRedis.publish(channel, data);
    }

    public void publishAsync() {
        MineRedis.publishAsync(channel, data);
    }

}
