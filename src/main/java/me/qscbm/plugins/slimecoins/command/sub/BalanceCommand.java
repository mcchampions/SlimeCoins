package me.qscbm.plugins.slimecoins.command.sub;

import me.qscbm.plugins.slimecoins.command.SubCommand;
import me.qscbm.plugins.slimecoins.config.ConfigManager;
import me.qscbm.plugins.slimecoins.config.MessageConfig;
import me.qscbm.plugins.slimecoins.service.EconomyService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.RoundingMode;
import java.util.List;

public class BalanceCommand implements SubCommand {
    private final EconomyService economy;
    private final ConfigManager config;
    private final MessageConfig messages;

    public BalanceCommand(EconomyService economy, ConfigManager config, MessageConfig messages) {
        this.economy = economy;
        this.config = config;
        this.messages = messages;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        OfflinePlayer target;
        if (args.length >= 1) {
            target = Bukkit.getOfflinePlayer(args[0]);
            if (!target.hasPlayedBefore() && !target.isOnline()) {
                sender.sendMessage(messages.get("player-not-found", "%player%", args[0]));
                return;
            }
        } else {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(messages.get("player-only"));
                return;
            }
            target = player;
        }

        java.math.BigDecimal balance = economy.getBalance(target.getUniqueId());
        String msg;
        if (sender instanceof Player player && player.getUniqueId().equals(target.getUniqueId())) {
            msg = messages.get("balance-self");
        } else {
            msg = messages.get("balance-other", "%player%", target.getName());
        }
        sender.sendMessage(msg
                .replace("%balance%", balance.setScale(2, RoundingMode.DOWN).toString())
                .replace("%symbol%", config.getCurrencySymbol())
        );
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
        return "slimecoins.bal";
    }
}
