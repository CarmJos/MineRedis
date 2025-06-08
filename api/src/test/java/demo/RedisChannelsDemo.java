package demo;

import cc.carm.plugin.mineredis.api.channel.RedisCallback;
import cc.carm.plugin.mineredis.api.channel.RedisChannel;
import cc.carm.plugin.mineredis.api.channel.RedisRequest;
import cc.carm.plugin.mineredis.api.message.PreparedRedisMessage;
import demo.callback.RegisterRequest;
import demo.callback.RegisterResponse;
import demo.callback.TeleportRequest;

public interface RedisChannelsDemo {

    RedisChannel TEST_REQUEST = RedisChannel.at("test.request").handle(m -> {
        String id = m.data().readUTF();
        String content = m.data().readUTF();
        return PreparedRedisMessage.of("test.response", id, true);
    });

    RedisRequest<TeleportRequest> TELEPORTING = RedisRequest.at("teleport.request", TeleportRequest.class).build();

    RedisCallback<String, RegisterRequest, RegisterResponse> REGISTER = RedisCallback
            .create(String.class, RegisterRequest.class, RegisterResponse.class)
            .at("register") // 频道名称，等同于下面两行
//            .requestAt("register.request")
//            .responseAt("register.response")
            .keys(
                    RegisterRequest::getName,
                    m -> m.dataCopy().readUTF()
            ) // 回应键解析函数，等同于下面两行
//            .requestKey(RegisterRequest::getName)
//            .responseKey(m -> m.dataCopy().readUTF())
            .request((o, r) -> o.writeUTF(r.getName()))
            .response(m -> new RegisterResponse(m.data().readUTF(), m.data().readBoolean()))
            .build();


    static void demo() {
        //noinspection Since15
        REGISTER.call(new RegisterRequest("Handsome_Carm"))
//                .orTimeout(2, TimeUnit.SECONDS) // 可设置超时时间，建议设置。
                .thenAccept(resp -> System.out.println("Register result: " + resp.isSuccess()));
    }


    interface PURCHASE {

        RedisChannel PAID = RedisChannel.at("purchase.paid").handle(m -> {
            String id = m.data().readUTF();
            // Handle paid actions
        });

    }


}
