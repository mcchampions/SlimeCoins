package me.qscbm.plugins.slimecoins.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MessageConfig {
    private final JavaPlugin plugin;
    private FileConfiguration messages;

    public MessageConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public String get(String key) {
        String msg = messages.getString("messages." + key, "");
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public String get(String key, String placeholder, String value) {
        return get(key).replace(placeholder, value);
    }
}
