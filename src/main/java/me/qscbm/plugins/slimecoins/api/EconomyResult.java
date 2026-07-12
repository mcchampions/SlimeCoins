package me.qscbm.plugins.slimecoins.api;

import java.math.BigDecimal;

public class EconomyResult {
    private final boolean success;
    private final String message;
    private final BigDecimal balanceBefore;
    private final BigDecimal balanceAfter;

    public EconomyResult(boolean success, String message, BigDecimal balanceBefore, BigDecimal balanceAfter) {
        this.success = success;
        this.message = message;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public BigDecimal getBalanceBefore() { return balanceBefore; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }

    public static EconomyResult success(BigDecimal before, BigDecimal after) {
        return new EconomyResult(true, null, before, after);
    }

    public static EconomyResult failure(String message, BigDecimal before) {
        return new EconomyResult(false, message, before, before);
    }
}
