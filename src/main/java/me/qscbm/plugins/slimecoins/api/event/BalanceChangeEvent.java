package me.qscbm.plugins.slimecoins.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.math.BigDecimal;
import java.util.UUID;

public class BalanceChangeEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID player;
    private final BigDecimal amount;
    private final BigDecimal balanceBefore;
    private final BigDecimal balanceAfter;
    private final TransactionType type;
    private final String source;
    private boolean cancelled;

    public BalanceChangeEvent(UUID player, BigDecimal amount, BigDecimal balanceBefore,
                              BigDecimal balanceAfter, TransactionType type, String source) {
        this.player = player;
        this.amount = amount;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.type = type;
        this.source = source;
    }

    public UUID getPlayer() { return player; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getBalanceBefore() { return balanceBefore; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public TransactionType getTransactionType() { return type; }
    public String getSource() { return source; }

    @Override
    public boolean isCancelled() { return cancelled; }

    @Override
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }
}
