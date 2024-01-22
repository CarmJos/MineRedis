import cc.carm.plugin.mineredis.MineRedis;

import java.util.UUID;

public class RedisCallback {

    public void demo() {
        UUID requestID = UUID.randomUUID();
        MineRedis.request("test.request", out -> {
                    out.writeUTF(requestID.toString());
                    out.writeUTF("test");
                })
                .filter(message -> message.getData().readUTF().equals(requestID.toString())) // 限制条件
                .response("test.response", message -> { //
                    System.out.println("response: " + message.getData().readUTF());
                    return message.getData().readUTF();
                }) // 如有收到了符合条件的反馈，则读取结果
                .thenAccept(System.out::println) // 使用结果
                .exceptionally(throwable -> {
                    throwable.printStackTrace();
                    return null;
                });
    }
}
