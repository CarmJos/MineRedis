package cc.carm.plugin.mineredis.handler;

import cc.carm.plugin.mineredis.MineRedisManager;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import io.lettuce.core.pubsub.RedisPubSubListener;

import java.io.ByteArrayInputStream;

public class RedisSubListener implements RedisPubSubListener<String, byte[]> {

    protected final MineRedisManager manager;

    public RedisSubListener(MineRedisManager manager) {
        this.manager = manager;
    }

    @Override
    public void message(String channel, byte[] message) {
        message(null, channel, message);
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void message(String pattern, String channel, byte[] message) {
        
        ByteArrayInputStream steam = new ByteArrayInputStream(message);
        ByteArrayDataInput data = ByteStreams.newDataInput(steam);
        String serverID = data.readUTF();
        long timestamp = data.readLong();

        byte[] remain = new byte[steam.available()];
        data.readFully(remain);

        manager.handleMessage(pattern, channel, serverID, timestamp, remain);
    }

    @Override
    public void subscribed(String channel, long count) {

    }

    @Override
    public void psubscribed(String pattern, long count) {

    }

    @Override
    public void unsubscribed(String channel, long count) {

    }

    @Override
    public void punsubscribed(String pattern, long count) {
    }
}
