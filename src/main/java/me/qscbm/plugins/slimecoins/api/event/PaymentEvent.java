package me.qscbm.plugins.slimecoins.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID from;
    private final UUID to;
    private final BigDecimal amount;
    private boolean cancelled;

    public PaymentEvent(UUID from, UUID to, BigDecimal amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    public UUID getFrom() { return from; }
    public UUID getTo() { return to; }
    public BigDecimal getAmount() { return amount; }

    @Override
    public boolean isCancelled() { return cancelled; }

    @Override
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }
}
