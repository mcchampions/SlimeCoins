package me.qscbm.plugins.slimecoins.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

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

        mergeMissing(jarConfig, diskConfig);
        diskConfig.set("config-version", jarVersion);

        try {
            File tmpFile = new File(targetFile.getParent(), "." + targetFile.getName() + ".tmp");
            diskConfig.save(tmpFile);
            Files.move(tmpFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            plugin.getLogger().info("Updated " + resource + " from version " + diskVersion + " to " + jarVersion);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save updated " + resource + ": " + e.getMessage());
        }
    }

    private static void mergeMissing(ConfigurationSection source, ConfigurationSection target) {
        for (String key : source.getKeys(false)) {
            if (!target.contains(key, true)) {
                Object value = source.get(key);
                if (value instanceof ConfigurationSection sourceSection) {
                    target.createSection(key);
                    ConfigurationSection targetSection = target.getConfigurationSection(key);
                    if (targetSection != null) {
                        mergeMissing(sourceSection, targetSection);
                    }
                } else {
                    target.set(key, value);
                }
            } else {
                Object sourceObj = source.get(key);
                Object targetObj = target.get(key);
                if (sourceObj instanceof ConfigurationSection sourceSection
                        && targetObj instanceof ConfigurationSection targetSection) {
                    mergeMissing(sourceSection, targetSection);
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
