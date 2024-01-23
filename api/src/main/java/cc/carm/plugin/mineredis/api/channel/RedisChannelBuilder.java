package cc.carm.plugin.mineredis.api.channel;

import cc.carm.plugin.mineredis.api.message.RedisMessage;
import cc.carm.plugin.mineredis.api.message.PreparedRedisMessage;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class RedisChannelBuilder {

    protected final String channel;
    protected Predicate<RedisMessage> filter = m -> !m.isLocalMessage();

    public RedisChannelBuilder(String channel) {
        this.channel = channel;
    }


    public RedisChannelBuilder filter(Predicate<RedisMessage> filter) {
        return setFilter(this.filter == null ? filter : this.filter.and(filter));
    }

    public RedisChannelBuilder setFilter(Predicate<RedisMessage> filter) {
        this.filter = filter;
        return this;
    }

    public RedisChannel handle(Consumer<RedisMessage> handler) {
        return handle(m -> {
            handler.accept(m);
            return null;
        });
    }

    public RedisChannel handle(Function<RedisMessage, PreparedRedisMessage> handler) {
        return new RedisChannel(channel, filter, handler);
    }

}
