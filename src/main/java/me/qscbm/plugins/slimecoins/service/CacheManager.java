package me.qscbm.plugins.slimecoins.service;

import me.qscbm.plugins.slimecoins.data.DataProvider;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CacheManager {
    private final ConcurrentHashMap<UUID, BigDecimal> cache = new ConcurrentHashMap<>();
    private final DataProvider dataProvider;

    public CacheManager(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    public BigDecimal getBalance(UUID uuid) {
        return cache.computeIfAbsent(uuid, id -> {
            BigDecimal balance = dataProvider.getBalance(id);
            return balance != null ? balance : BigDecimal.ZERO;
        });
    }

    public void updateBalance(UUID uuid, BigDecimal newBalance) {
        cache.put(uuid, newBalance);
    }

    public void invalidate(UUID uuid) {
        cache.remove(uuid);
    }

    public boolean isCached(UUID uuid) {
        return cache.containsKey(uuid);
    }

    public void loadPlayer(UUID uuid) {
        cache.computeIfAbsent(uuid, id -> {
            BigDecimal balance = dataProvider.getBalance(id);
            return balance != null ? balance : BigDecimal.ZERO;
        });
    }

    public void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }
}
