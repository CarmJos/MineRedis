package cc.carm.plugin.mineredis.command;

import cc.carm.plugin.mineredis.MineRedisCore;
import cc.carm.plugin.mineredis.util.VersionReader;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;


@SuppressWarnings("unused")
@CommandAlias("MineRedis")
@Description("MineRedis的主指令，用于开发者进行调试，只允许后台执行。")
public class MineRedisCommand extends BaseCommand {

    protected final MineRedisCore core;

    public MineRedisCommand(MineRedisCore core) {
        this.core = core;
    }

    @HelpCommand
    @Syntax("&d[页码或子指令名称]")
    @Description("查看指定数据源的统计信息与当前仍未关闭的查询。")
    public void help(CommandIssuer issuer, CommandHelp help) {
        if (issuer.isPlayer()) {
            issuer.sendMessage("§c只有后台执行才能使用此命令。");
            return;
        }
        help.showHelp();
    }

    @Subcommand("version")
    @Description("查看当前插件版本与核心库(lettuce-core)版本。")
    public void version(CommandIssuer issuer) {
        if (issuer.isPlayer()) {
            issuer.sendMessage("§c只有后台执行才能使用此命令。");
            return;
        }
        VersionReader reader = new VersionReader();
        String pluginVersion = reader.get("plugin", null);
        if (pluginVersion == null) {
            issuer.sendMessage("§c无法获取当前版本信息，请保证使用原生版本以避免安全问题。");
            return;
        }
        issuer.sendMessage("§r当前插件版本为 §d" + pluginVersion + "§r。 §7(基于 lettuce &3" + reader.get("api") + "&7)");
        issuer.sendMessage("§r正在检查插件更新，请稍候...");
        core.checkUpdate(pluginVersion);
    }

}
