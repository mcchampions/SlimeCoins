package me.qscbm.plugins.slimecoins.command.sub;

import me.qscbm.plugins.slimecoins.command.SubCommand;
import me.qscbm.plugins.slimecoins.config.ConfigManager;
import me.qscbm.plugins.slimecoins.config.MessageConfig;
import me.qscbm.plugins.slimecoins.data.TransactionLog;
import me.qscbm.plugins.slimecoins.service.EconomyService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.List;

public class CheckCommand implements SubCommand {
    private final EconomyService economy;
    private final ConfigManager config;
    private final MessageConfig messages;

    public CheckCommand(EconomyService economy, ConfigManager config, MessageConfig messages) {
        this.economy = economy;
        this.config = config;
        this.messages = messages;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§c用法: /" + label + " check <玩家>");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(messages.get("player-not-found", "%player%", args[0]));
            return;
        }

        List<TransactionLog> logs = economy.getLogs(target.getUniqueId(), config.getMaxReturnRows());

        sender.sendMessage(messages.get("check-header", "%player%", target.getName()));

        if (logs.isEmpty()) {
            sender.sendMessage(messages.get("check-empty"));
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (TransactionLog log : logs) {
            sender.sendMessage(messages.get("check-entry")
                    .replace("%id%", String.valueOf(log.getId()))
                    .replace("%type%", log.getType())
                    .replace("%amount%", log.getAmount().setScale(2, RoundingMode.DOWN).toString())
                    .replace("%date%", sdf.format(log.getCreatedAt()))
                    .replace("%remark%", log.getRemark() != null ? log.getRemark() : "")
                    .replace("%symbol%", config.getCurrencySymbol()));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }

    @Override
    public String getPermission() {
        return "slimecoins.admin";
    }
}
