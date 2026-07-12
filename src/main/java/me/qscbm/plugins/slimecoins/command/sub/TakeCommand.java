package me.qscbm.plugins.slimecoins.command.sub;

import me.qscbm.plugins.slimecoins.api.EconomyResult;
import me.qscbm.plugins.slimecoins.command.SubCommand;
import me.qscbm.plugins.slimecoins.config.ConfigManager;
import me.qscbm.plugins.slimecoins.config.MessageConfig;
import me.qscbm.plugins.slimecoins.service.EconomyService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class TakeCommand implements SubCommand {
    private final EconomyService economy;
    private final ConfigManager config;
    private final MessageConfig messages;

    public TakeCommand(EconomyService economy, ConfigManager config, MessageConfig messages) {
        this.economy = economy;
        this.config = config;
        this.messages = messages;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§c用法: /" + label + " take <玩家> <金额> [备注]");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(messages.get("player-not-found", "%player%", args[0]));
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(args[1]);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sender.sendMessage(messages.get("invalid-amount"));
            return;
        }

        String remark = args.length >= 3 ? args[2] : "";
        String source = sender.getName();

        EconomyResult result = economy.withdraw(target.getUniqueId(), target.getName(), amount, source, remark);

        if (result.isSuccess()) {
            sender.sendMessage(messages.get("take-success")
                    .replace("%player%", target.getName())
                    .replace("%amount%", amount.setScale(2, RoundingMode.DOWN).toString())
                    .replace("%symbol%", config.getCurrencySymbol()));

            Player targetPlayer = target.getPlayer();
            if (targetPlayer != null) {
                targetPlayer.sendMessage(messages.get("take-notify")
                        .replace("%source%", source)
                        .replace("%amount%", amount.setScale(2, RoundingMode.DOWN).toString())
                        .replace("%symbol%", config.getCurrencySymbol()));
            }
        } else {
            sender.sendMessage("§c" + (result.getMessage() != null ? result.getMessage() : "操作失败"));
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
