package me.qscbm.plugins.slimecoins.command.sub;

import me.qscbm.plugins.slimecoins.command.SubCommand;
import me.qscbm.plugins.slimecoins.config.ConfigManager;
import me.qscbm.plugins.slimecoins.config.MessageConfig;
import me.qscbm.plugins.slimecoins.data.BalanceRecord;
import me.qscbm.plugins.slimecoins.service.EconomyService;
import org.bukkit.command.CommandSender;

import java.math.RoundingMode;
import java.util.List;

public class TopCommand implements SubCommand {
    private final EconomyService economy;
    private final ConfigManager config;
    private final MessageConfig messages;

    public TopCommand(EconomyService economy, ConfigManager config, MessageConfig messages) {
        this.economy = economy;
        this.config = config;
        this.messages = messages;
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        int page = 1;
        if (args.length >= 1) {
            try {
                page = Integer.parseInt(args[0]);
                if (page < 1) page = 1;
            } catch (NumberFormatException ignored) {
            }
        }

        int pageSize = config.getTopPageSize();
        int offset = (page - 1) * pageSize;

        List<BalanceRecord> top = economy.getTopBalances(pageSize, offset);

        if (top.isEmpty()) {
            sender.sendMessage(messages.get("top-empty"));
            return;
        }

        List<BalanceRecord> nextCheck = economy.getTopBalances(1, offset + pageSize);
        String totalPages = nextCheck.isEmpty() ? String.valueOf(page) : "?";
        sender.sendMessage(messages.get("top-header")
                .replace("%page%", String.valueOf(page))
                .replace("%total_pages%", totalPages));

        String symbol = config.getCurrencySymbol();
        int rank = offset + 1;
        for (BalanceRecord record : top) {
            sender.sendMessage(messages.get("top-entry")
                    .replace("%rank%", String.valueOf(rank++))
                    .replace("%player%", record.getPlayerName())
                    .replace("%balance%", record.getBalance().setScale(2, RoundingMode.DOWN).toString())
                    .replace("%symbol%", symbol));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }

    @Override
    public String getPermission() {
        return "slimecoins.top";
    }
}
