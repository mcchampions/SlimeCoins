package me.qscbm.plugins.slimecoins.service;

import me.qscbm.plugins.slimecoins.api.EconomyResult;
import me.qscbm.plugins.slimecoins.api.event.BalanceChangeEvent;
import me.qscbm.plugins.slimecoins.api.event.PaymentEvent;
import me.qscbm.plugins.slimecoins.api.event.TransactionType;
import me.qscbm.plugins.slimecoins.data.BalanceRecord;
import me.qscbm.plugins.slimecoins.data.DataProvider;
import me.qscbm.plugins.slimecoins.data.TransactionLog;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.Event;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class EconomyService {
    private final DataProvider dataProvider;
    private final CacheManager cacheManager;
    private final LogService logService;
    private final BigDecimal minimumPayment;
    private final BigDecimal maximumPayment;

    public EconomyService(DataProvider dataProvider, CacheManager cacheManager,
                          LogService logService, BigDecimal minimumPayment, BigDecimal maximumPayment) {
        this.dataProvider = dataProvider;
        this.cacheManager = cacheManager;
        this.logService = logService;
        this.minimumPayment = minimumPayment;
        this.maximumPayment = maximumPayment;
    }

    private void callEvent(Event event) {
        try {
            Server server = Bukkit.getServer();
            if (server != null) {
                server.getPluginManager().callEvent(event);
            }
        } catch (Exception ignored) {
            // Server not available (unit test environment)
        }
    }

    private void ensureAccount(UUID uuid, String playerName) {
        if (!dataProvider.accountExists(uuid)) {
            dataProvider.createAccount(uuid, playerName);
        }
    }

    public BigDecimal getBalance(UUID uuid) {
        return cacheManager.getBalance(uuid);
    }

    public boolean hasBalance(UUID uuid, BigDecimal amount) {
        return getBalance(uuid).compareTo(amount) >= 0;
    }

    public EconomyResult deposit(UUID uuid, String playerName, BigDecimal amount,
                                  String source, String remark) {
        ensureAccount(uuid, playerName);
        BigDecimal before = getBalance(uuid);
        BigDecimal after = before.add(amount);

        BalanceChangeEvent event = new BalanceChangeEvent(uuid, amount, before, after,
                TransactionType.GIVE, source);
        callEvent(event);
        if (event.isCancelled()) {
            return EconomyResult.failure("Event cancelled", before);
        }

        dataProvider.updateBalance(uuid, playerName, after);
        cacheManager.updateBalance(uuid, after);
        logService.logGive(uuid, playerName, amount, source, after, remark);
        return EconomyResult.success(before, after);
    }

    public EconomyResult withdraw(UUID uuid, String playerName, BigDecimal amount,
                                   String source, String remark) {
        ensureAccount(uuid, playerName);
        BigDecimal before = getBalance(uuid);

        if (before.compareTo(amount) < 0) {
            return EconomyResult.failure("Insufficient funds", before);
        }

        BigDecimal after = before.subtract(amount);

        BalanceChangeEvent event = new BalanceChangeEvent(uuid, amount.negate(), before, after,
                TransactionType.TAKE, source);
        callEvent(event);
        if (event.isCancelled()) {
            return EconomyResult.failure("Event cancelled", before);
        }

        dataProvider.updateBalance(uuid, playerName, after);
        cacheManager.updateBalance(uuid, after);
        logService.logTake(uuid, playerName, amount, source, after, remark);
        return EconomyResult.success(before, after);
    }

    public EconomyResult setBalance(UUID uuid, String playerName, BigDecimal amount,
                                     String source, String remark) {
        ensureAccount(uuid, playerName);
        BigDecimal before = getBalance(uuid);

        BalanceChangeEvent event = new BalanceChangeEvent(uuid, amount.subtract(before), before, amount,
                TransactionType.SET, source);
        callEvent(event);
        if (event.isCancelled()) {
            return EconomyResult.failure("Event cancelled", before);
        }

        dataProvider.updateBalance(uuid, playerName, amount);
        cacheManager.updateBalance(uuid, amount);
        logService.logSet(uuid, playerName, amount, source, amount, remark);
        return EconomyResult.success(before, amount);
    }

    public EconomyResult pay(UUID from, String fromName, UUID to, String toName, BigDecimal amount) {
        if (from.equals(to)) {
            return EconomyResult.failure("Cannot pay yourself", getBalance(from));
        }

        if (amount.compareTo(minimumPayment) < 0 || amount.compareTo(maximumPayment) > 0) {
            return EconomyResult.failure("Amount out of range", getBalance(from));
        }

        PaymentEvent paymentEvent = new PaymentEvent(from, to, amount);
        callEvent(paymentEvent);
        if (paymentEvent.isCancelled()) {
            return EconomyResult.failure("Payment cancelled", getBalance(from));
        }

        BigDecimal fromBefore = getBalance(from);
        if (fromBefore.compareTo(amount) < 0) {
            return EconomyResult.failure("Insufficient funds", fromBefore);
        }

        ensureAccount(from, fromName);
        ensureAccount(to, toName);

        BigDecimal fromAfter = fromBefore.subtract(amount);
        BigDecimal toBefore = getBalance(to);
        BigDecimal toAfter = toBefore.add(amount);

        dataProvider.updateBalance(from, fromName, fromAfter);
        cacheManager.updateBalance(from, fromAfter);
        logService.logPay(from, fromName, amount, to.toString(), fromAfter);

        dataProvider.updateBalance(to, toName, toAfter);
        cacheManager.updateBalance(to, toAfter);
        logService.logPayReceive(to, toName, amount, from.toString(), toAfter);

        BalanceChangeEvent fromEvent = new BalanceChangeEvent(from, amount.negate(), fromBefore, fromAfter,
                TransactionType.PAY, to.toString());
        callEvent(fromEvent);

        BalanceChangeEvent toEvent = new BalanceChangeEvent(to, amount, toBefore, toAfter,
                TransactionType.PAY_RECEIVE, from.toString());
        callEvent(toEvent);

        return EconomyResult.success(fromBefore, fromAfter);
    }

    public List<BalanceRecord> getTopBalances(int limit, int offset) {
        return dataProvider.getTopBalances(limit, offset);
    }

    public List<TransactionLog> getLogs(UUID uuid, int limit) {
        return logService.getLogs(uuid, limit);
    }
}
