package me.qscbm.plugins.slimecoins.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.math.BigDecimal;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
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
        return BigDecimal.valueOf(config.getDouble("economy.initial-balance", 0.0));
    }
    public String getCurrencySymbol() { return config.getString("economy.currency-symbol", "💰"); }
    public String getCurrencyNameSingular() { return config.getString("economy.currency-name-singular", "粘液币"); }
    public String getCurrencyNamePlural() { return config.getString("economy.currency-name-plural", "粘液币"); }
    public int getTopPageSize() { return config.getInt("economy.top-page-size", 10); }
    public BigDecimal getMinimumPayment() {
        return BigDecimal.valueOf(config.getDouble("economy.minimum-payment", 0.01));
    }
    public BigDecimal getMaximumPayment() {
        return BigDecimal.valueOf(config.getDouble("economy.maximum-payment", 1000000.0));
    }

    public boolean isLogEnabled() { return config.getBoolean("log.enabled", true); }
    public int getMaxReturnRows() { return config.getInt("log.max-return-rows", 50); }
}
