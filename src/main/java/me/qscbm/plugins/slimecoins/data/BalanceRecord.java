package me.qscbm.plugins.slimecoins.data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

public class BalanceRecord {
    private final UUID uuid;
    private final String playerName;
    private final BigDecimal balance;
    private final Timestamp updatedAt;

    public BalanceRecord(UUID uuid, String playerName, BigDecimal balance, Timestamp updatedAt) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.balance = balance;
        this.updatedAt = updatedAt;
    }

    public UUID getUuid() { return uuid; }
    public String getPlayerName() { return playerName; }
    public BigDecimal getBalance() { return balance; }
    public Timestamp getUpdatedAt() { return updatedAt; }
}
