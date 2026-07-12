package me.qscbm.plugins.slimecoins.expansion;

import me.qscbm.plugins.slimecoins.data.BalanceRecord;
import me.qscbm.plugins.slimecoins.service.EconomyService;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

public class SlimeCoinsExpansion extends PlaceholderExpansion {
    private final EconomyService economy;
    private final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

    public SlimeCoinsExpansion(EconomyService economy) {
        this.economy = economy;
    }

    @Override
    public String getIdentifier() {
        return "slimecoins";
    }

    @Override
    public String getAuthor() {
        return "qscbm187531";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) return "";

        if (params.equals("balance")) {
            return economy.getBalance(player.getUniqueId())
                    .setScale(2, RoundingMode.DOWN).toString();
        }

        if (params.equals("balance_formatted")) {
            return decimalFormat.format(economy.getBalance(player.getUniqueId()));
        }

        if (params.startsWith("top_player_")) {
            try {
                int rank = Integer.parseInt(params.substring("top_player_".length()));
                if (rank < 1 || rank > 10) return "";
                List<BalanceRecord> top = economy.getTopBalances(rank, 0);
                if (top.size() < rank) return "";
                return top.get(rank - 1).getPlayerName();
            } catch (NumberFormatException e) {
                return "";
            }
        }

        if (params.startsWith("top_balance_")) {
            try {
                int rank = Integer.parseInt(params.substring("top_balance_".length()));
                if (rank < 1 || rank > 10) return "";
                List<BalanceRecord> top = economy.getTopBalances(rank, 0);
                if (top.size() < rank) return "";
                return top.get(rank - 1).getBalance()
                        .setScale(2, RoundingMode.DOWN).toString();
            } catch (NumberFormatException e) {
                return "";
            }
        }

        return null;
    }
}
