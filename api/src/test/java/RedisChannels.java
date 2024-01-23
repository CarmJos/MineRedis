import cc.carm.plugin.mineredis.api.channel.RedisChannel;
import cc.carm.plugin.mineredis.api.message.PreparedRedisMessage;

public interface RedisChannels {


    RedisChannel TEST_REQUEST = RedisChannel.builder("test.request").handle(m -> {
        String id = m.data().readUTF();
        String content = m.data().readUTF();
        return PreparedRedisMessage.of("test.response", id, true);
    });

    interface PURCHASE {

        RedisChannel PAID = RedisChannel.builder("purchase.paid").handle(m -> {
            String id = m.data().readUTF();
            // Handle paid actions
        });

    }


}
