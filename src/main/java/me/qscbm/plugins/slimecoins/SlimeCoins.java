package me.qscbm.plugins.slimecoins;

import me.qscbm.plugins.slimecoins.api.SlimeCoinsAPI;
import me.qscbm.plugins.slimecoins.command.SlimeCoinsCommand;
import me.qscbm.plugins.slimecoins.command.sub.*;
import me.qscbm.plugins.slimecoins.config.ConfigManager;
import me.qscbm.plugins.slimecoins.config.MessageConfig;
import me.qscbm.plugins.slimecoins.data.DataProvider;
import me.qscbm.plugins.slimecoins.data.impl.H2Provider;
import me.qscbm.plugins.slimecoins.data.impl.MySQLProvider;
import me.qscbm.plugins.slimecoins.data.impl.SQLiteProvider;
import me.qscbm.plugins.slimecoins.expansion.SlimeCoinsExpansion;
import me.qscbm.plugins.slimecoins.service.CacheManager;
import me.qscbm.plugins.slimecoins.service.EconomyService;
import me.qscbm.plugins.slimecoins.service.LogService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class SlimeCoins extends JavaPlugin {
    private DataProvider dataProvider;
    private CacheManager cacheManager;
    private LogService logService;
    private EconomyService economyService;
    private ConfigManager configManager;
    private MessageConfig messageConfig;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.load();

        messageConfig = new MessageConfig(this);
        messageConfig.load();

        if (!initDatabase()) {
            getLogger().severe("Failed to initialize database! Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        cacheManager = new CacheManager(dataProvider);
        logService = new LogService(dataProvider, configManager.isLogEnabled());

        economyService = new EconomyService(
                dataProvider,
                cacheManager,
                logService,
                configManager.getMinimumPayment(),
                configManager.getMaximumPayment()
        );

        new SlimeCoinsAPI(economyService);

        registerCommands();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new SlimeCoinsExpansion(economyService).register();
            getLogger().info("PlaceholderAPI expansion registered!");
        }

        getLogger().info("SlimeCoins enabled! Database: " + configManager.getDatabaseType());
    }

    @Override
    public void onDisable() {
        if (cacheManager != null) {
            cacheManager.clear();
        }
        if (dataProvider != null) {
            try {
                dataProvider.close();
            } catch (Exception e) {
                getLogger().warning("Failed to close database connection: " + e.getMessage());
            }
        }
        getLogger().info("SlimeCoins disabled!");
    }

    private boolean initDatabase() {
        String type = configManager.getDatabaseType();
        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        try {
            switch (type) {
                case "MYSQL":
                    dataProvider = new MySQLProvider(
                            configManager.getMysqlHost(),
                            configManager.getMysqlPort(),
                            configManager.getMysqlDatabase(),
                            configManager.getMysqlUsername(),
                            configManager.getMysqlPassword(),
                            configManager.getMysqlUseSSL()
                    );
                    break;
                case "H2":
                    dataProvider = new H2Provider(configManager.getH2File());
                    break;
                default:
                    dataProvider = new SQLiteProvider(
                            new File(dataFolder, "slimecoins.db").getAbsolutePath()
                    );
                    break;
            }
            dataProvider.init();
            return true;
        } catch (Exception e) {
            getLogger().severe("Database init error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void registerCommands() {
        SlimeCoinsCommand command = new SlimeCoinsCommand(economyService, configManager, messageConfig);
        command.register("bal", new BalanceCommand(economyService, configManager, messageConfig));
        command.register("pay", new PayCommand(economyService, configManager, messageConfig));
        command.register("top", new TopCommand(economyService, configManager, messageConfig));
        command.register("give", new GiveCommand(economyService, configManager, messageConfig));
        command.register("take", new TakeCommand(economyService, configManager, messageConfig));
        command.register("set", new SetCommand(economyService, configManager, messageConfig));
        command.register("check", new CheckCommand(economyService, configManager, messageConfig));

        var cmd = getCommand("slimecoins");
        if (cmd != null) {
            cmd.setExecutor(command);
            cmd.setTabCompleter(command);
        }
    }
}
