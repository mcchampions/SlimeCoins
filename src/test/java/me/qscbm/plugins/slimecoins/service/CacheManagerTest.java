package me.qscbm.plugins.slimecoins.service;

import me.qscbm.plugins.slimecoins.data.DataProvider;
import me.qscbm.plugins.slimecoins.data.BalanceRecord;
import me.qscbm.plugins.slimecoins.data.TransactionLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CacheManagerTest {
    private CacheManager cacheManager;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        DataProvider mockProvider = new DataProvider() {
            public void init() {}
            public void close() {}
            public BigDecimal getBalance(UUID uuid) {
                return testUuid.equals(uuid) ? new BigDecimal("100.00") : null;
            }
            public void updateBalance(UUID uuid, String name, BigDecimal bal) {}
            public boolean accountExists(UUID uuid) { return true; }
            public void createAccount(UUID uuid, String name) {}
            public void logTransaction(UUID pu, String pn, BigDecimal a, String t, String s, BigDecimal ba, String r) {}
            public List<TransactionLog> getLogs(UUID uuid, int limit) { return List.of(); }
            public List<BalanceRecord> getTopBalances(int limit, int offset) { return List.of(); }
        };
        cacheManager = new CacheManager(mockProvider);
        testUuid = UUID.randomUUID();
    }

    @Test
    void getBalance_LoadsFromProviderAndCaches() {
        BigDecimal first = cacheManager.getBalance(testUuid);
        assertEquals(new BigDecimal("100.00"), first);
        assertTrue(cacheManager.isCached(testUuid));
    }

    @Test
    void updateBalance_UpdatesCache() {
        cacheManager.updateBalance(testUuid, new BigDecimal("200.00"));
        assertEquals(new BigDecimal("200.00"), cacheManager.getBalance(testUuid));
    }

    @Test
    void invalidate_RemovesFromCache() {
        cacheManager.getBalance(testUuid);
        cacheManager.invalidate(testUuid);
        assertFalse(cacheManager.isCached(testUuid));
    }

    @Test
    void getBalance_ReturnsZeroForNewPlayer() {
        UUID newUuid = UUID.randomUUID();
        assertEquals(BigDecimal.ZERO, cacheManager.getBalance(newUuid));
    }
}
