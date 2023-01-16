package cc.carm.plugin.mineredis.api.message;

import cc.carm.plugin.mineredis.MineRedis;
import com.google.common.io.ByteArrayDataInput;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class RedisReceivedMessage {

    protected final @NotNull String channel;
    protected final @NotNull String sourceServerID;
    protected final long timestamp;

    protected final ByteArrayDataInput data;

    public RedisReceivedMessage(@NotNull String channel, @NotNull String sourceServerID,
                                long timestamp, ByteArrayDataInput data) {
        this.channel = channel;
        this.sourceServerID = sourceServerID;
        this.timestamp = timestamp;
        this.data = data;
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

    public ByteArrayDataInput getData() {
        return data;
    }

    public <T> T apply(@NotNull Function<ByteArrayDataInput, T> handler) {
        return handler.apply(getData());
    }

}
