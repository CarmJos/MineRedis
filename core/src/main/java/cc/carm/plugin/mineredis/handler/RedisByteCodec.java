package cc.carm.plugin.mineredis.handler;

import io.lettuce.core.codec.RedisCodec;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class RedisByteCodec implements RedisCodec<String, byte[]> {

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    @Override
    public String decodeKey(ByteBuffer bytes) {
        return CHARSET.decode(bytes).toString();
    }

    @Override
    public byte[] decodeValue(ByteBuffer bytes) {
        int remaining = bytes.remaining();
        if (remaining == 0) return new byte[0];

        byte[] b = new byte[remaining];
        bytes.get(b);
        return b;
    }

    @Override
    public ByteBuffer encodeKey(String key) {
        return CHARSET.encode(key);
    }

    @Override
    public ByteBuffer encodeValue(byte[] value) {
        if (value == null) return ByteBuffer.wrap(new byte[0]);
        return ByteBuffer.wrap(value);
    }

}
