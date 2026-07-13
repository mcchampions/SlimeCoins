package me.qscbm.plugins.slimecoins.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class MessageConfig {
    private final JavaPlugin plugin;
    private FileConfiguration messages;

    public MessageConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        messages = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages.yml"));
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
