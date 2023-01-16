package cc.carm.plugin.mineredis;

import cc.carm.lib.easyplugin.EasyPlugin;
import cc.carm.plugin.mineredis.conf.PluginConfiguration;
import co.aikar.commands.PaperCommandManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class MineRedisBukkit extends EasyPlugin implements MineRedisPlatform {

    protected static MineRedisBukkit instance;

    protected MineRedisCore core;
    protected PaperCommandManager commandManager;

    @Override
    protected void load() {
        MineRedisBukkit.instance = this;

        log("加载基础核心...");
        this.core = new MineRedisCore(this);
    }

    @Override
    protected boolean initialize() {
        log("初始化指令管理器...");
        this.commandManager = new PaperCommandManager(this);

        log("注册相关指令...");
        this.core.initializeCommands(this.commandManager);

        if (getConfiguration().METRICS.getNotNull()) {
            log("启用统计数据...");
            Metrics metrics = new Metrics(this, 14075);
            metrics.addCustomChart(new SimplePie("update_check",
                    () -> getConfiguration().UPDATE_CHECKER.getNotNull() ? "ENABLED" : "DISABLED")
            );
        }

        if (getConfiguration().UPDATE_CHECKER.getNotNull()) {
            log("开始检查更新，可能需要一小段时间...");
            log("   如不希望检查更新，可在配置文件中关闭。");
            getScheduler().runAsync(() -> this.core.checkUpdate(getDescription().getVersion()));
        } else {
            log("已禁用检查更新，跳过。");
        }

        return true;
    }

    @Override
    protected void shutdown() {
        log("终止Redis连接...");
        this.core.shutdown();
    }

    @Override
    public boolean isDebugging() {
        return getConfiguration().DEBUG.getNotNull();
    }

    public @NotNull PluginConfiguration getConfiguration() {
        return this.core.getConfig();
    }

    @Override
    public @NotNull String getServerIdentifier() {
        String serverName = Bukkit.getServer().getName();
        if (!serverName.isEmpty()) return serverName;
        return "bukkit-" + Bukkit.getServer().getPort();
    }

    @Override
    public @NotNull File getPluginFolder() {
        return getDataFolder();
    }

}