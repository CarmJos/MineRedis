package cc.carm.plugin.mineredis.api.message;

import cc.carm.plugin.mineredis.MineRedis;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class RedisMessage {

    protected final @NotNull String channel;
    protected final @NotNull String sourceServerID;
    protected final long timestamp;

    protected final byte[] rawData;

    public RedisMessage(@NotNull String channel, @NotNull String sourceServerID,
                        long timestamp, byte[] raw) {
        this.channel = channel;
        this.sourceServerID = sourceServerID;
        this.timestamp = timestamp;
        this.rawData = raw;
    }

    /**
     * 判断当前消息是否为本地(当前服务器)发出的。
     *
     * @return 是否为本地发出的消息
     */
    public boolean isLocalMessage() {
        return MineRedis.getServerID().equals(sourceServerID);
    }

    public @NotNull String getSourceServerID() {
        return sourceServerID;
    }

    public @NotNull String getChannel() {
        return channel;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte[] getRawData() {
        return rawData;
    }

    @SuppressWarnings("UnstableApiUsage")
    public ByteArrayDataInput getData() {
        return ByteStreams.newDataInput(rawData);
    }

    public <T> T apply(@NotNull Function<ByteArrayDataInput, T> handler) {
        return handler.apply(getData());
    }

}
