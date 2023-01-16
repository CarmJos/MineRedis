package cc.carm.plugin.mineredis;

import cc.carm.plugin.mineredis.conf.PluginConfiguration;
import co.aikar.commands.BungeeCommandManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;
import org.bstats.charts.SimplePie;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Logger;

public class MineRedisBungee extends Plugin implements MineRedisPlatform {

    protected static MineRedisBungee instance;

    protected MineRedisCore core;
    protected BungeeCommandManager commandManager;

    @Override
    public void onLoad() {
        MineRedisBungee.instance = this;

        getLogger().info("加载基础核心...");
        this.core = new MineRedisCore(this);
    }

    @Override
    public void onEnable() {
        outputInfo();
        getLogger().info("初始化指令管理器...");
        this.commandManager = new BungeeCommandManager(this);

        getLogger().info("注册相关指令...");
        this.core.initializeCommands(this.commandManager);

        if (getConfiguration().METRICS.getNotNull()) {
            getLogger().info("启用统计数据...");
            Metrics metrics = new Metrics(this, 14076);
            metrics.addCustomChart(new SimplePie("update_check",
                    () -> getConfiguration().UPDATE_CHECKER.getNotNull() ? "ENABLED" : "DISABLED")
            );
        }

        if (getConfiguration().UPDATE_CHECKER.getNotNull()) {
            getLogger().info("开始检查更新，可能需要一小段时间...");
            getLogger().info("   如不希望检查更新，可在配置文件中关闭。");
            ProxyServer.getInstance().getScheduler().runAsync(
                    this, () -> this.core.checkUpdate(getDescription().getVersion())
            );
        } else {
            getLogger().info("已禁用检查更新，跳过。");
        }
    }

    @Override
    public void onDisable() {
        outputInfo();
        getLogger().info("终止Redis连接...");
        this.core.shutdown();
    }

    public static MineRedisBungee getInstance() {
        return instance;
    }

    @Override
    public @NotNull Logger getLogger() {
        return super.getLogger();
    }

    public @NotNull PluginConfiguration getConfiguration() {
        return this.core.getConfig();
    }

    @Override
    public @NotNull String getServerIdentifier() {
        return "bungee";
    }

    @Override
    public @NotNull File getPluginFolder() {
        return getDataFolder();
    }

    @SuppressWarnings("deprecation")
    public void outputInfo() {
        outputInfo(this.getResourceAsStream("PLUGIN_INFO"), s -> ProxyServer.getInstance().getConsole().sendMessage(s));
    }

}
