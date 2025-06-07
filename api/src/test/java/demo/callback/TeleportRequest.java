package demo.callback;

import cc.carm.plugin.mineredis.api.channel.RedisSerializableMessage;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;

public class TeleportRequest extends RedisSerializableMessage {

    protected final @NotNull UUID player;
    protected final @NotNull String target;

    public TeleportRequest(@NotNull UUID player, @NotNull String target) {
        this.player = player;
        this.target = target;
    }

    public TeleportRequest(@NotNull ByteArrayDataInput data) {
        this(
                UUID.fromString(data.readUTF()),
                data.readUTF()
        );
    }

    @Override
    public void writeData(@NotNull ByteArrayDataOutput out) throws IOException {
        out.writeUTF(player.toString());
        out.writeUTF(target);
    }

}
