package me.qscbm.plugins.slimecoins.command;

import me.qscbm.plugins.slimecoins.config.ConfigManager;
import me.qscbm.plugins.slimecoins.config.MessageConfig;
import me.qscbm.plugins.slimecoins.service.EconomyService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

public class SlimeCoinsCommand implements CommandExecutor, TabCompleter {
    private final Map<String, SubCommand> subCommands = new LinkedHashMap<>();
    private final MessageConfig messages;

    public SlimeCoinsCommand(EconomyService economyService, ConfigManager config, MessageConfig messages) {
        this.messages = messages;
    }

    public void register(String name, SubCommand command) {
        subCommands.put(name.toLowerCase(), command);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }

        String sub = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(sub);

        if (subCommand == null) {
            sendUsage(sender, label);
            return true;
        }

        if (!sender.hasPermission(subCommand.getPermission())) {
            sender.sendMessage(messages.get("no-permission"));
            return true;
        }

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        subCommand.execute(sender, label, subArgs);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            for (Map.Entry<String, SubCommand> entry : subCommands.entrySet()) {
                if (sender.hasPermission(entry.getValue().getPermission())) {
                    completions.add(entry.getKey());
                }
            }
            return filterStartsWith(completions, args[0]);
        }

        String sub = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(sub);
        if (subCommand != null && sender.hasPermission(subCommand.getPermission())) {
            String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
            return subCommand.tabComplete(sender, subArgs);
        }

        return List.of();
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage("§e===== SlimeCoins 粘液币 =====");
        sender.sendMessage("§e/" + label + " bal [玩家] §7- 查看余额");
        sender.sendMessage("§e/" + label + " pay <玩家> <金额> §7- 向玩家支付");
        sender.sendMessage("§e/" + label + " top [页数] §7- 查看排行榜");
        if (sender.hasPermission("slimecoins.admin")) {
            sender.sendMessage("§e/" + label + " give <玩家> <金额> §7- 给予货币");
            sender.sendMessage("§e/" + label + " take <玩家> <金额> §7- 扣除货币");
            sender.sendMessage("§e/" + label + " set <玩家> <金额> §7- 设置余额");
            sender.sendMessage("§e/" + label + " check <玩家> §7- 查看流水");
        }
    }

    private List<String> filterStartsWith(List<String> list, String prefix) {
        List<String> result = new ArrayList<>();
        String lower = prefix.toLowerCase();
        for (String s : list) {
            if (s.toLowerCase().startsWith(lower)) {
                result.add(s);
            }
        }
        return result;
    }
}
