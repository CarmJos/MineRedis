import cc.carm.plugin.mineredis.MineRedis;

import java.util.UUID;

public class RedisRequest {

    public void demo() {
        UUID requestID = UUID.randomUUID();
        MineRedis.request("test.request", out -> {
                    out.writeUTF(requestID.toString());
                    out.writeUTF("test");
                })
                .filter(message -> message.dataCopy().readUTF().equals(requestID.toString())) // 限制条件
                .handleResponse("test.response", message -> { //
                    System.out.println("response: " + message.dataCopy().readUTF());
                    return message.dataCopy().readUTF();
                }) // 如有收到了符合条件的反馈，则读取结果
//                .orTimeout(2, TimeUnit.SECONDS) // 超时时间
                .thenAccept(System.out::println) // 使用结果
                .exceptionally(throwable -> {
                    throwable.printStackTrace();
                    return null;
                });
    }
}
