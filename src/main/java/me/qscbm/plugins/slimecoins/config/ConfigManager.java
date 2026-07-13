package me.qscbm.plugins.slimecoins.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private Map<Material, MiningReward> miningRewardsCache;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        miningRewardsCache = null;
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        miningRewardsCache = null;
    }

    public String getDatabaseType() {
        return config.getString("database.type", "SQLITE").toUpperCase();
    }

    public String getMysqlHost() { return config.getString("database.mysql.host", "localhost"); }
    public int getMysqlPort() { return config.getInt("database.mysql.port", 3306); }
    public String getMysqlDatabase() { return config.getString("database.mysql.database", "slimecoins"); }
    public String getMysqlUsername() { return config.getString("database.mysql.username", "root"); }
    public String getMysqlPassword() { return config.getString("database.mysql.password", ""); }
    public boolean getMysqlUseSSL() { return config.getBoolean("database.mysql.useSSL", false); }
    public String getH2File() { return config.getString("database.h2.file", "plugins/SlimeCoins/data/slimecoins"); }

    public BigDecimal getInitialBalance() {
        return new BigDecimal(config.getString("economy.initial-balance", "0.0"));
    }
    public String getCurrencySymbol() { return config.getString("economy.currency-symbol", "💰"); }
    public String getCurrencyNameSingular() { return config.getString("economy.currency-name-singular", "粘液币"); }
    public String getCurrencyNamePlural() { return config.getString("economy.currency-name-plural", "粘液币"); }
    public int getTopPageSize() { return config.getInt("economy.top-page-size", 10); }
    public BigDecimal getMinimumPayment() {
        return new BigDecimal(config.getString("economy.minimum-payment", "0.01"));
    }
    public BigDecimal getMaximumPayment() {
        return new BigDecimal(config.getString("economy.maximum-payment", "1000000.0"));
    }

    public boolean isLogEnabled() { return config.getBoolean("log.enabled", true); }
    public int getMaxReturnRows() { return config.getInt("log.max-return-rows", 50); }

    public boolean isMiningRewardsEnabled() { return config.getBoolean("mining-rewards.enabled", false); }

    public Map<Material, MiningReward> getMiningRewards() {
        if (miningRewardsCache != null) return miningRewardsCache;

        ConfigurationSection section = config.getConfigurationSection("mining-rewards.blocks");
        if (section == null) {
            miningRewardsCache = Collections.emptyMap();
            return miningRewardsCache;
        }

        Map<Material, MiningReward> rewards = new HashMap<>();
        for (String key : section.getKeys(false)) {
            Material material = Material.getMaterial(key);
            if (material == null) continue;

            double chance = section.getDouble(key + ".chance", 0.0);
            BigDecimal min = new BigDecimal(section.getString(key + ".min-amount", "1.0"));
            BigDecimal max = new BigDecimal(section.getString(key + ".max-amount", "10.0"));
            rewards.put(material, new MiningReward(chance, min, max));
        }
        miningRewardsCache = rewards;
        return miningRewardsCache;
    }
}
