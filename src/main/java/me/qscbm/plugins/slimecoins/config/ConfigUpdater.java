package me.qscbm.plugins.slimecoins.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigUpdater {

    public static void update(JavaPlugin plugin, String resource, File targetFile) {
        FileConfiguration jarConfig = loadJarConfig(plugin, resource);
        if (jarConfig == null) return;

        int jarVersion = jarConfig.getInt("config-version", 1);

        if (!targetFile.exists()) {
            plugin.saveResource(resource, false);
            return;
        }

        FileConfiguration diskConfig = YamlConfiguration.loadConfiguration(targetFile);
        int diskVersion = diskConfig.getInt("config-version", 0);

        if (jarVersion <= diskVersion) return;

        mergeMissing(jarConfig, diskConfig, "");
        diskConfig.set("config-version", jarVersion);

        try {
            diskConfig.save(targetFile);
            plugin.getLogger().info("Updated " + resource + " from version " + diskVersion + " to " + jarVersion);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save updated " + resource + ": " + e.getMessage());
        }
    }

    private static void mergeMissing(ConfigurationSection source, ConfigurationSection target, String path) {
        for (String key : source.getKeys(false)) {
            String fullKey = path.isEmpty() ? key : path + "." + key;

            if (!target.contains(fullKey, true)) {
                Object value = source.get(key);
                if (value instanceof ConfigurationSection sourceSection) {
                    target.createSection(fullKey);
                    ConfigurationSection targetSection = target.getConfigurationSection(fullKey);
                    if (targetSection != null) {
                        mergeMissing(sourceSection, targetSection, fullKey);
                    }
                } else {
                    target.set(fullKey, value);
                }
            } else {
                Object sourceObj = source.get(key);
                Object targetObj = target.get(key);
                if (sourceObj instanceof ConfigurationSection sourceSection
                        && targetObj instanceof ConfigurationSection targetSection) {
                    mergeMissing(sourceSection, targetSection, fullKey);
                }
            }
        }
    }

    private static FileConfiguration loadJarConfig(JavaPlugin plugin, String resource) {
        var stream = plugin.getResource(resource);
        if (stream == null) return null;
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return YamlConfiguration.loadConfiguration(reader);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load jar " + resource + ": " + e.getMessage());
            return null;
        }
    }
}
