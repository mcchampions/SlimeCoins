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

public class PayCommand implements SubCommand {
    private final EconomyService economy;
    private final ConfigManager config;
    private final MessageConfig messages;

    public PayCommand(EconomyService economy, ConfigManager config, MessageConfig messages) {
        this.economy = economy;
        this.config = config;
        this.messages = messages;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("player-only"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§c用法: /" + label + " pay <玩家> <金额>");
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
        } catch (NumberFormatException e) {
            sender.sendMessage(messages.get("invalid-amount"));
            return;
        }

        if (amount.compareTo(config.getMinimumPayment()) < 0 || amount.compareTo(config.getMaximumPayment()) > 0) {
            sender.sendMessage(messages.get("amount-out-of-range")
                    .replace("%min%", config.getMinimumPayment().toString())
                    .replace("%max%", config.getMaximumPayment().toString()));
            return;
        }

        if (player.getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage(messages.get("cannot-pay-self"));
            return;
        }

        EconomyResult result = economy.pay(
                player.getUniqueId(), player.getName(),
                target.getUniqueId(), target.getName(),
                amount);

        if (!result.isSuccess()) {
            if (result.getMessage() != null && result.getMessage().contains("Insufficient")) {
                sender.sendMessage(messages.get("insufficient-funds")
                        .replace("%balance%", result.getBalanceBefore().setScale(2, RoundingMode.DOWN).toString())
                        .replace("%symbol%", config.getCurrencySymbol()));
            } else {
                sender.sendMessage("§c" + (result.getMessage() != null ? result.getMessage() : "支付失败"));
            }
            return;
        }

        sender.sendMessage(messages.get("pay-sender")
                .replace("%player%", target.getName())
                .replace("%amount%", amount.setScale(2, RoundingMode.DOWN).toString())
                .replace("%symbol%", config.getCurrencySymbol()));

        Player targetPlayer = target.getPlayer();
        if (targetPlayer != null) {
            targetPlayer.sendMessage(messages.get("pay-receiver")
                    .replace("%player%", player.getName())
                    .replace("%amount%", amount.setScale(2, RoundingMode.DOWN).toString())
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
        return "slimecoins.pay";
    }
}
