package me.qscbm.plugins.slimecoins.data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

public class TransactionLog {
    private final long id;
    private final UUID playerUuid;
    private final String playerName;
    private final BigDecimal amount;
    private final String type;
    private final String source;
    private final BigDecimal balanceAfter;
    private final String remark;
    private final Timestamp createdAt;

    public TransactionLog(long id, UUID playerUuid, String playerName, BigDecimal amount,
                          String type, String source, BigDecimal balanceAfter,
                          String remark, Timestamp createdAt) {
        this.id = id;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.amount = amount;
        this.type = type;
        this.source = source;
        this.balanceAfter = balanceAfter;
        this.remark = remark;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public UUID getPlayerUuid() { return playerUuid; }
    public String getPlayerName() { return playerName; }
    public BigDecimal getAmount() { return amount; }
    public String getType() { return type; }
    public String getSource() { return source; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public String getRemark() { return remark; }
    public Timestamp getCreatedAt() { return createdAt; }
}
