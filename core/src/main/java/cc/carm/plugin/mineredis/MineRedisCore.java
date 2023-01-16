package cc.carm.plugin.mineredis;

import cc.carm.lib.configuration.EasyConfiguration;
import cc.carm.lib.configuration.yaml.YAMLConfigProvider;
import cc.carm.lib.githubreleases4j.GithubReleases4J;
import cc.carm.plugin.mineredis.api.RedisManager;
import cc.carm.plugin.mineredis.command.MineRedisCommand;
import cc.carm.plugin.mineredis.command.MineRedisHelpFormatter;
import cc.carm.plugin.mineredis.conf.PluginConfiguration;
import co.aikar.commands.CommandManager;
import co.aikar.commands.Locales;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisURI;
import io.lettuce.core.resource.ClientResources;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class MineRedisCore implements IMineRedis {

    protected static MineRedisCore instance;

    public static final String REPO_OWNER = "CarmJos";
    public static final String REPO_NAME = "MineRedis";

    protected final MineRedisPlatform platform;

    protected final YAMLConfigProvider configProvider;
    protected final PluginConfiguration config;


    protected @NotNull RedisManager manager;
    protected @Nullable Supplier<String> idProvider = null;

    public MineRedisCore(@NotNull MineRedisPlatform platform) {
        this.platform = platform;

        getLogger().info("加载配置文件...");
        this.configProvider = EasyConfiguration.from(new File(platform.getPluginFolder(), "config.yml"));
        this.config = new PluginConfiguration();
        this.configProvider.initialize(this.config);

        getLogger().info("初始化MineRedis API...");
        MineRedis.initializeAPI(this);

        getLogger().info("初始化管理器...");
        this.manager = create(config.CONNECTION.getNotNull());
    }

    @Override
    public @NotNull Logger getLogger() {
        return platform.getLogger();
    }

    @Override
    public @NotNull String getServerID() {
        if (idProvider != null) return idProvider.get();

        String conf = config.SERVER_ID.get();
        if (conf != null) return conf;

        return platform.getServerIdentifier();
    }

    public PluginConfiguration getConfig() {
        return config;
    }

    @Override
    public void supplyServerID(@Nullable Supplier<String> idProvider) {
        this.idProvider = idProvider;
    }

    @Override
    public @NotNull RedisManager getManager() {
        return manager;
    }

    @Override
    public @NotNull MineRedisManager create(@NotNull RedisURI url, @NotNull ClientResources resources, @NotNull ClientOptions options) {
        return new MineRedisManager(url, resources, options);
    }

    @Override
    public void shutdown(RedisManager manager) {
        manager.getClient().shutdown();
    }

    public void shutdown() {
        shutdown(this.manager);
    }

    @SuppressWarnings("deprecation")
    protected void initializeCommands(CommandManager<?, ?, ?, ?, ?, ?> commandManager) {
        commandManager.enableUnstableAPI("help");
        commandManager.setHelpFormatter(new MineRedisHelpFormatter(commandManager));
        commandManager.getLocales().setDefaultLocale(Locales.SIMPLIFIED_CHINESE);
        commandManager.registerCommand(new MineRedisCommand(this));
    }

    public void checkUpdate(String currentVersion) {
        Logger logger = getLogger();

        Integer behindVersions = GithubReleases4J.getVersionBehind(REPO_OWNER, REPO_NAME, currentVersion);
        String downloadURL = GithubReleases4J.getReleasesURL(REPO_OWNER, REPO_NAME);
        if (behindVersions == null) {
            logger.severe("检查更新失败，请您定期查看插件是否更新，避免安全问题。");
            logger.severe("下载地址 " + downloadURL);
        } else if (behindVersions < 0) {
            logger.severe("检查更新失败! 当前版本未知，请您使用原生版本以避免安全问题。");
            logger.severe("最新版下载地址 " + downloadURL);
        } else if (behindVersions > 0) {
            logger.warning("发现新版本! 目前已落后 " + behindVersions + " 个版本。");
            logger.warning("最新版下载地址 " + downloadURL);
        } else {
            logger.info("检查完成，当前已是最新版本。");
        }
    }

}
