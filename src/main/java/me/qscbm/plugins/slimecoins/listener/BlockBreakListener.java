package me.qscbm.plugins.slimecoins.listener;

import me.qscbm.plugins.slimecoins.api.EconomyResult;
import me.qscbm.plugins.slimecoins.api.SlimeCoinsAPI;
import me.qscbm.plugins.slimecoins.config.ConfigManager;
import me.qscbm.plugins.slimecoins.config.MiningReward;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Random;

public class BlockBreakListener implements Listener {
    private final ConfigManager configManager;
    private final SlimeCoinsAPI api;
    private final Random random = new Random();

    public BlockBreakListener(ConfigManager configManager, SlimeCoinsAPI api) {
        this.configManager = configManager;
        this.api = api;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!configManager.isMiningRewardsEnabled()) return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;

        Material block = event.getBlock().getType();
        Map<Material, MiningReward> rewards = configManager.getMiningRewards();

        MiningReward reward = rewards.get(block);
        if (reward == null) return;

        if (random.nextDouble() > reward.chance()) return;

        BigDecimal min = reward.minAmount();
        BigDecimal max = reward.maxAmount();
        BigDecimal range = max.subtract(min);
        BigDecimal amount = min.add(range.multiply(BigDecimal.valueOf(random.nextDouble())))
                .setScale(2, RoundingMode.DOWN);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) return;

        Player player = event.getPlayer();
        EconomyResult result = api.deposit(player.getUniqueId(), amount, "mining");
        if (result.isSuccess()) {
            player.sendMessage("§a+ " + amount.toPlainString() + " " + api.getCurrencySymbol());
        }
    }
}
