package cc.carm.plugin.mineredis.api.channel;

import cc.carm.plugin.mineredis.api.message.RedisMessage;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class RedisSerializableMessage {


    /**
     * 将当前消息内容写入到指定的输出流中。
     *
     * @param out 输出流，用于写入消息数据
     * @throws IOException 如果写入过程中发生错误
     */
    public abstract void writeData(@NotNull ByteArrayDataOutput out) throws IOException;

    protected static <T> @Nullable BiConsumer<ByteArrayDataOutput, T> serializerOf(@NotNull Class<T> clazz) {
        if (!RedisSerializableMessage.class.isAssignableFrom(clazz)) return null;
        return (b, r) -> {
            try {
                RedisSerializableMessage message = (RedisSerializableMessage) r;
                message.writeData(b);
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize RedisChannelMessage(" + clazz.getName() + ")", e);
            }
        };
    }

    protected static <T> @Nullable Function<RedisMessage, T> deserializerOf(@NotNull Class<T> clazz) {
        Function<RedisMessage, T> parser = getDeserializerConstructor(clazz, RedisMessage.class, r -> r);
        if (parser != null) return parser;

        parser = getDeserializerConstructor(clazz, ByteArrayDataInput.class, RedisMessage::data);
        if (parser != null) return parser;

        parser = getDeserializerMethod(clazz, "deserialize", RedisMessage.class, r -> r);
        if (parser != null) return parser;

        parser = getDeserializerMethod(clazz, "valueOf", RedisMessage.class, r -> r);
        if (parser != null) return parser;

        parser = getDeserializerMethod(clazz, "deserialize", ByteArrayDataInput.class, RedisMessage::data);
        if (parser != null) return parser;

        parser = getDeserializerMethod(clazz, "valueOf", ByteArrayDataInput.class, RedisMessage::data);
        return parser;
    }

    private static <T, V> Function<RedisMessage, T> getDeserializerConstructor(
            @NotNull Class<T> clazz, @NotNull Class<V> paramType, Function<RedisMessage, V> extractor
    ) {
        try {
            Constructor<T> c = clazz.getConstructor(paramType);
            return (message -> {
                try {
                    return c.newInstance(extractor.apply(message));
                } catch (Exception e) {
                    throw new RuntimeException("Failed to deserialize RedisChannelMessage(" + clazz.getName() + ")", e);
                }
            });
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static <T, V> Function<RedisMessage, T> getDeserializerMethod(
            @NotNull Class<T> clazz, @NotNull String name,
            @NotNull Class<V> paramType, Function<RedisMessage, V> extractor
    ) {
        try {
            Method method = clazz.getDeclaredMethod(name, paramType);
            method.setAccessible(true);
            if (clazz.isAssignableFrom(method.getReturnType()) && Modifier.isStatic(method.getModifiers())) {
                return (message -> {
                    try {
                        return (T) method.invoke(null, extractor.apply(message));
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to deserialize RedisChannelMessage(" + clazz.getName() + ")", e);
                    }
                });
            }
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

}
