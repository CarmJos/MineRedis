package demo;

import cc.carm.plugin.mineredis.api.channel.RedisCallback;
import cc.carm.plugin.mineredis.api.channel.RedisChannel;
import cc.carm.plugin.mineredis.api.message.PreparedRedisMessage;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import demo.callback.RegisterRequest;
import demo.callback.RegisterResponse;

public interface RedisChannels {


    RedisChannel TEST_REQUEST = RedisChannel.builder("test.request").handle(m -> {
        String id = m.data().readUTF();
        String content = m.data().readUTF();
        return PreparedRedisMessage.of("test.response", id, true);
    });

    RedisCallback<ByteArrayDataOutput, ByteArrayDataOutput> CALLBACK = RedisCallback.original(
            "something.request", "something.response"
    ).handle(m -> {
        ByteArrayDataOutput resp = ByteStreams.newDataOutput();
        resp.writeUTF(m.data().readUTF());
        return resp;
    });

    RedisCallback<RegisterRequest, RegisterResponse> REGISTER = RedisCallback
            .<RegisterRequest, RegisterResponse>create()
            .requester("register.request", (data, req) -> {
                data.writeUTF(req.getName());
            })
            .responser("register,response", (data, resp) -> {
                data.writeUTF(resp.getName());
                data.writeBoolean(resp.isSuccess());
            })
            .handle(m -> new RegisterResponse(m.data().readUTF(), true));


    static void demo() {
        REGISTER.call(new RegisterRequest("test")).thenAccept(resp -> {
            System.out.println("Register result: " + resp.isSuccess());
        });
    }


    interface PURCHASE {

        RedisChannel PAID = RedisChannel.builder("purchase.paid").handle(m -> {
            String id = m.data().readUTF();
            // Handle paid actions
        });

    }


}
