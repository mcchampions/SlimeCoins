package me.qscbm.plugins.slimecoins.service;

import me.qscbm.plugins.slimecoins.api.EconomyResult;
import me.qscbm.plugins.slimecoins.data.BalanceRecord;
import me.qscbm.plugins.slimecoins.data.DataProvider;
import me.qscbm.plugins.slimecoins.data.TransactionLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class EconomyServiceTest {
    private EconomyService economyService;
    private Map<UUID, BigDecimal> store;
    private UUID uuid;
    private UUID uuid2;

    @BeforeEach
    void setUp() {
        store = new ConcurrentHashMap<>();
        uuid = UUID.randomUUID();
        uuid2 = UUID.randomUUID();
        store.put(uuid, new BigDecimal("500.00"));
        store.put(uuid2, new BigDecimal("100.00"));

        DataProvider mockProvider = new DataProvider() {
            public void init() {}
            public void close() {}
            public BigDecimal getBalance(UUID id) { return store.get(id); }
            public void updateBalance(UUID id, String name, BigDecimal bal) { store.put(id, bal); }
            public boolean accountExists(UUID id) { return store.containsKey(id); }
            public void createAccount(UUID id, String name) { store.put(id, BigDecimal.ZERO); }
            public void logTransaction(UUID pu, String pn, BigDecimal a, String t, String s, BigDecimal ba, String r) {}
            public List<TransactionLog> getLogs(UUID id, int limit) { return List.of(); }
            public List<BalanceRecord> getTopBalances(int limit, int offset) { return List.of(); }
        };

        CacheManager cacheManager = new CacheManager(mockProvider);
        LogService logService = new LogService(mockProvider, true);
        economyService = new EconomyService(mockProvider, cacheManager, logService,
                new BigDecimal("0.01"), new BigDecimal("1000000.00"));
    }

    @Test
    void getBalance_ReturnsCorrectBalance() {
        assertEquals(new BigDecimal("500.00"), economyService.getBalance(uuid));
    }

    @Test
    void hasBalance_ReturnsTrueWhenEnough() {
        assertTrue(economyService.hasBalance(uuid, new BigDecimal("500.00")));
        assertFalse(economyService.hasBalance(uuid, new BigDecimal("501.00")));
    }

    @Test
    void deposit_IncreasesBalance() {
        EconomyResult result = economyService.deposit(uuid, "Test", new BigDecimal("100.00"), "admin", "");
        assertTrue(result.isSuccess());
        assertEquals(new BigDecimal("600.00"), economyService.getBalance(uuid));
    }

    @Test
    void withdraw_DecreasesBalance() {
        EconomyResult result = economyService.withdraw(uuid, "Test", new BigDecimal("100.00"), "admin", "");
        assertTrue(result.isSuccess());
        assertEquals(new BigDecimal("400.00"), economyService.getBalance(uuid));
    }

    @Test
    void withdraw_FailsWhenInsufficient() {
        EconomyResult result = economyService.withdraw(uuid, "Test", new BigDecimal("999.00"), "admin", "");
        assertFalse(result.isSuccess());
        assertEquals(new BigDecimal("500.00"), economyService.getBalance(uuid));
    }

    @Test
    void setBalance_SetsExactBalance() {
        EconomyResult result = economyService.setBalance(uuid, "Test", new BigDecimal("999.99"), "admin", "");
        assertTrue(result.isSuccess());
        assertEquals(new BigDecimal("999.99"), economyService.getBalance(uuid));
    }

    @Test
    void pay_TransfersBetweenPlayers() {
        EconomyResult result = economyService.pay(uuid, "A", uuid2, "B", new BigDecimal("50.00"));
        assertTrue(result.isSuccess());
        assertEquals(new BigDecimal("450.00"), economyService.getBalance(uuid));
        assertEquals(new BigDecimal("150.00"), economyService.getBalance(uuid2));
    }

    @Test
    void pay_FailsWhenInsufficient() {
        EconomyResult result = economyService.pay(uuid, "A", uuid2, "B", new BigDecimal("999.00"));
        assertFalse(result.isSuccess());
        assertEquals(new BigDecimal("500.00"), economyService.getBalance(uuid));
        assertEquals(new BigDecimal("100.00"), economyService.getBalance(uuid2));
    }

    @Test
    void pay_FailsWhenPayingSelf() {
        EconomyResult result = economyService.pay(uuid, "A", uuid, "A", new BigDecimal("10.00"));
        assertFalse(result.isSuccess());
    }
}
