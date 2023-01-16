package cc.carm.plugin.mineredis.command;

import co.aikar.commands.*;

public class MineRedisHelpFormatter extends CommandHelpFormatter {

    public MineRedisHelpFormatter(CommandManager manager) {
        super(manager);
    }

    @Override
    public void printHelpHeader(CommandHelp help, CommandIssuer issuer) {
        issuer.sendMessage("§5§lMine§f§lRedis §7指令帮助");
    }

    @Override
    public void printHelpCommand(CommandHelp help, CommandIssuer issuer, HelpEntry entry) {
        issuer.sendMessage("§8# §f" + entry.getCommand() + " §r" + entry.getParameterSyntax());
        if (!entry.getDescription().isEmpty()) {
            issuer.sendMessage("§8- §7" + entry.getDescription());
        }
    }

    @Override
    public void printHelpFooter(CommandHelp help, CommandIssuer issuer) {
    }

    @Override
    public void printSearchHeader(CommandHelp help, CommandIssuer issuer) {
        issuer.sendMessage("§5§lMine§f§lRedis §7指令帮助查询");
    }

    @Override
    public void printSearchEntry(CommandHelp help, CommandIssuer issuer, HelpEntry entry) {
        printHelpCommand(help, issuer, entry);
    }

    @Override
    public void printSearchFooter(CommandHelp help, CommandIssuer issuer) {
    }

    @Override
    public void printDetailedHelpHeader(CommandHelp help, CommandIssuer issuer, HelpEntry entry) {
        issuer.sendMessage("§5§lMine§f§lRedis §7指令帮助 §8(§f" + entry.getCommand() + "§8)");
    }

    @Override
    public void printDetailedHelpCommand(CommandHelp help, CommandIssuer issuer, HelpEntry entry) {
        printHelpCommand(help, issuer, entry);
    }

    @Override
    public void printDetailedParameter(CommandHelp help, CommandIssuer issuer, HelpEntry entry, CommandParameter param) {
        if (param.getDescription() != null) {
            if (param.getSyntax() != null) {
                issuer.sendMessage("§8@§r" + param.getSyntax() + "§7 §f" + param.getDescription());
            } else {
                issuer.sendMessage("§8@§f" + param.getName() + "§7 §f" + param.getDescription());
            }
        }
    }

    @Override
    public void printDetailedHelpFooter(CommandHelp help, CommandIssuer issuer, HelpEntry entry) {
    }

}
