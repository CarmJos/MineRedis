package cc.carm.plugin.mineredis.api.message;

@FunctionalInterface
public interface RedisMessageListener {

    void handle(RedisMessage message);

}
