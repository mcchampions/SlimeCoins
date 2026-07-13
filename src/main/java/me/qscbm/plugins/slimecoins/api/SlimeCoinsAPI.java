package me.qscbm.plugins.slimecoins.api;

import me.qscbm.plugins.slimecoins.config.ConfigManager;
import me.qscbm.plugins.slimecoins.data.BalanceRecord;
import me.qscbm.plugins.slimecoins.data.TransactionLog;
import me.qscbm.plugins.slimecoins.service.EconomyService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class SlimeCoinsAPI {
    private static volatile SlimeCoinsAPI instance;
    private final EconomyService economyService;
    private final ConfigManager configManager;

    public SlimeCoinsAPI(EconomyService economyService, ConfigManager configManager) {
        this.economyService = economyService;
        this.configManager = configManager;
        instance = this;
    }

    public static SlimeCoinsAPI getInstance() {
        return instance;
    }

    public BigDecimal getBalance(UUID uuid) {
        return economyService.getBalance(uuid);
    }

    public BigDecimal getBalance(OfflinePlayer player) {
        return getBalance(player.getUniqueId());
    }

    public boolean hasBalance(UUID uuid, BigDecimal amount) {
        return economyService.hasBalance(uuid, amount);
    }

    public boolean hasBalance(OfflinePlayer player, BigDecimal amount) {
        return hasBalance(player.getUniqueId(), amount);
    }

    public EconomyResult withdraw(UUID uuid, BigDecimal amount, String reason) {
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        if (name == null) name = uuid.toString();
        return economyService.withdraw(uuid, name, amount, "API", reason);
    }

    public EconomyResult deposit(UUID uuid, BigDecimal amount, String reason) {
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        if (name == null) name = uuid.toString();
        return economyService.deposit(uuid, name, amount, "API", reason);
    }

    public EconomyResult setBalance(UUID uuid, BigDecimal amount, String reason) {
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        if (name == null) name = uuid.toString();
        return economyService.setBalance(uuid, name, amount, "API", reason);
    }

    public EconomyResult pay(UUID from, UUID to, BigDecimal amount) {
        String fromName = Bukkit.getOfflinePlayer(from).getName();
        String toName = Bukkit.getOfflinePlayer(to).getName();
        if (fromName == null) fromName = from.toString();
        if (toName == null) toName = to.toString();
        return economyService.pay(from, fromName, to, toName, amount);
    }

    public List<BalanceRecord> getTopBalances(int limit, int offset) {
        return economyService.getTopBalances(limit, offset);
    }

    public List<TransactionLog> getLogs(UUID uuid, int limit) {
        return economyService.getLogs(uuid, limit);
    }

    public String getCurrencySymbol() {
        return configManager.getCurrencySymbol();
    }

    public String getCurrencyNameSingular() {
        return configManager.getCurrencyNameSingular();
    }

    public String getCurrencyNamePlural() {
        return configManager.getCurrencyNamePlural();
    }
}
