package cc.carm.plugin.mineredis.api.message;

import cc.carm.plugin.mineredis.MineRedis;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class RedisMessage {

    protected final @NotNull String channel;
    protected final @NotNull String sourceServerID;
    protected final long timestamp;

    protected final byte[] rawData;
    protected @Nullable ByteArrayDataInput data;

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
        return sourceID();
    }

    public @NotNull String sourceID() {
        return sourceServerID;
    }

    public @NotNull String getChannel() {
        return channel();
    }

    public @NotNull String channel() {
        return channel;
    }

    public long getTimestamp() {
        return timestamp();
    }

    public long timestamp() {
        return timestamp;
    }

    public byte[] raw() {
        return rawData;
    }

    public ByteArrayDataInput data() {
        if (data == null) reset();
        return this.data;
    }

    public ByteArrayDataInput getData() {
        return data();
    }

    public void reset() {
        this.data = dataCopy();
    }

    public ByteArrayDataInput dataCopy() {
        return ByteStreams.newDataInput(raw());
    }

    public <T> T apply(@NotNull Function<ByteArrayDataInput, T> handler) {
        return handler.apply(dataCopy());
    }

}
