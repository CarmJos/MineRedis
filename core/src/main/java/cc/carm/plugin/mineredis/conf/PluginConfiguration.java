package cc.carm.plugin.mineredis.conf;

import cc.carm.lib.configuration.Configuration;
import cc.carm.lib.configuration.annotation.ConfigPath;
import cc.carm.lib.configuration.annotation.HeaderComments;
import cc.carm.lib.configuration.value.standard.ConfiguredValue;
import io.lettuce.core.RedisURI;

@ConfigPath(root = true)
public class PluginConfiguration implements Configuration {

    @HeaderComments("排错模式，一般留给开发者检查问题，平常使用无需开启。")
    public final ConfiguredValue<Boolean> DEBUG = ConfiguredValue.of(false);

    @HeaderComments({"",
            "统计数据设定",
            "该选项用于帮助开发者统计插件版本与使用情况，且绝不会影响性能与使用体验。",
            "当然，您也可以选择在这里关闭，或在plugins/bStats下的配置文件中关闭所有插件的统计信息。"
    })
    public final ConfiguredValue<Boolean> METRICS = ConfiguredValue.of(true);

    @HeaderComments({"",
            "检查更新设定",
            "该选项用于插件判断是否要检查更新，若您不希望插件检查更新并提示您，可以选择关闭。",
            "检查更新为异步操作，绝不会影响性能与使用体验。"
    })
    public final ConfiguredValue<Boolean> UPDATE_CHECKER = ConfiguredValue.of(true);

    @HeaderComments("当前服务器的ID，用于消息通讯时辨别消息来源。")
    public final ConfiguredValue<String> SERVER_ID = ConfiguredValue.of("server-name");

    public final ConfiguredValue<RedisURI> CONNECTION = ConfiguredValue.builderOf(RedisURI.class)
            .fromSection()
            .parse((holder, section) -> {
                RedisURI.Builder builder = RedisURI.Builder.redis(section.getString("host", "127.0.0.1"));
                builder.withPort(section.getInt("port", RedisURI.DEFAULT_REDIS_PORT));
                builder.withDatabase(section.getInt("database", 0));

                String username = section.getString("username");
                String password = section.getString("password");

                if (username != null && password != null) {
                    builder.withAuthentication(username, password);
                } else if (password != null) {
                    builder.withPassword(password);
                }

                return builder.build();
            }).build();

}
