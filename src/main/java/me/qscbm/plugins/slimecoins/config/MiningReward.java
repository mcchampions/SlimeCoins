package me.qscbm.plugins.slimecoins.config;

import java.math.BigDecimal;

public record MiningReward(double chance, BigDecimal minAmount, BigDecimal maxAmount) {}
