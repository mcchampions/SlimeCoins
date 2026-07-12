package me.qscbm.plugins.slimecoins.data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface DataProvider {
    void init() throws Exception;
    void close() throws Exception;
    BigDecimal getBalance(UUID uuid);
    void updateBalance(UUID uuid, String playerName, BigDecimal balance);
    boolean accountExists(UUID uuid);
    void createAccount(UUID uuid, String playerName);
    void logTransaction(UUID playerUuid, String playerName, BigDecimal amount,
                        String type, String source, BigDecimal balanceAfter, String remark);
    List<TransactionLog> getLogs(UUID uuid, int limit);
    List<BalanceRecord> getTopBalances(int limit, int offset);
}
