package me.qscbm.plugins.slimecoins.service;

import me.qscbm.plugins.slimecoins.data.DataProvider;
import me.qscbm.plugins.slimecoins.data.TransactionLog;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class LogService {
    private final DataProvider dataProvider;
    private final boolean enabled;

    public LogService(DataProvider dataProvider, boolean enabled) {
        this.dataProvider = dataProvider;
        this.enabled = enabled;
    }

    public void logGive(UUID playerUuid, String playerName, BigDecimal amount,
                        String source, BigDecimal balanceAfter, String remark) {
        if (!enabled) return;
        dataProvider.logTransaction(playerUuid, playerName, amount, "GIVE", source, balanceAfter, remark);
    }

    public void logTake(UUID playerUuid, String playerName, BigDecimal amount,
                        String source, BigDecimal balanceAfter, String remark) {
        if (!enabled) return;
        dataProvider.logTransaction(playerUuid, playerName, amount.negate(), "TAKE", source, balanceAfter, remark);
    }

    public void logSet(UUID playerUuid, String playerName, BigDecimal amount,
                       String source, BigDecimal balanceAfter, String remark) {
        if (!enabled) return;
        dataProvider.logTransaction(playerUuid, playerName, amount, "SET", source, balanceAfter, remark);
    }

    public void logPay(UUID playerUuid, String playerName, BigDecimal amount,
                       String source, BigDecimal balanceAfter) {
        if (!enabled) return;
        dataProvider.logTransaction(playerUuid, playerName, amount.negate(), "PAY", source, balanceAfter, "");
    }

    public void logPayReceive(UUID playerUuid, String playerName, BigDecimal amount,
                              String source, BigDecimal balanceAfter) {
        if (!enabled) return;
        dataProvider.logTransaction(playerUuid, playerName, amount, "PAY_RECEIVE", source, balanceAfter, "");
    }

    public List<TransactionLog> getLogs(UUID uuid, int limit) {
        return dataProvider.getLogs(uuid, limit);
    }
}
