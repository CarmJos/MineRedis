package cc.carm.plugin.mineredis.conf;

import cc.carm.lib.configuration.core.ConfigurationRoot;
import cc.carm.lib.configuration.core.annotation.HeaderComment;
import cc.carm.lib.configuration.core.value.ConfigValue;
import cc.carm.lib.configuration.core.value.type.ConfiguredValue;
import io.lettuce.core.RedisURI;

import java.util.LinkedHashMap;
import java.util.Map;

public class PluginConfiguration extends ConfigurationRoot {

    @HeaderComment("排错模式，一般留给开发者检查问题，平常使用无需开启。")
    public final ConfigValue<Boolean> DEBUG = ConfiguredValue.of(Boolean.class, false);

    @HeaderComment({"",
            "统计数据设定",
            "该选项用于帮助开发者统计插件版本与使用情况，且绝不会影响性能与使用体验。",
            "当然，您也可以选择在这里关闭，或在plugins/bStats下的配置文件中关闭所有插件的统计信息。"
    })
    public final ConfigValue<Boolean> METRICS = ConfiguredValue.of(Boolean.class, true);

    @HeaderComment({"",
            "检查更新设定",
            "该选项用于插件判断是否要检查更新，若您不希望插件检查更新并提示您，可以选择关闭。",
            "检查更新为异步操作，绝不会影响性能与使用体验。"
    })
    public final ConfigValue<Boolean> UPDATE_CHECKER = ConfiguredValue.of(Boolean.class, true);

    public final ConfigValue<String> SERVER_ID = ConfiguredValue.of(String.class, "server-name");

    @SuppressWarnings("deprecation")
    public final ConfigValue<RedisURI> CONNECTION = ConfigValue.builder()
            .asValue(RedisURI.class).fromSection()
            .defaults(RedisURI.Builder
                    .redis("127.0.0.1", RedisURI.DEFAULT_REDIS_PORT)
                    .withPassword("password".toCharArray())
                    .build()
            ).parseValue((section, d) -> {
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
            }).serializeValue((url) -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("host", url.getHost());
                map.put("port", url.getPort());
                map.put("database", url.getDatabase());
                if (url.getUsername() != null) map.put("username", url.getUsername());
                if (url.getPassword() != null) map.put("password", new String(url.getPassword()));
                return map;
            }).build();


}
