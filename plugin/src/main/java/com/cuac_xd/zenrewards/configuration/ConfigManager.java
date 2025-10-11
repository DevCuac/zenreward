package com.cuac_xd.zenrewards.configuration;

import com.cryptomorin.xseries.XSound;
import com.cuac_xd.zenrewards.ZenRewards;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class ConfigManager {

    private final ZenRewards plugin;
    private FileConfiguration mainConfig;
    private FileConfiguration menuConfig;
    private FileConfiguration rewardsConfig;

    private File configFile;
    private File menuFile;
    private File rewardsFile;

    public ConfigManager(ZenRewards plugin) { this.plugin = plugin; }

    public void loadFiles() {
        // Guardar los archivos por defecto si no existen
        plugin.saveDefaultConfig();

        configFile = new File(plugin.getDataFolder(), "config.yml");
        menuFile = new File(plugin.getDataFolder(), "menu.yml");
        rewardsFile = new File(plugin.getDataFolder(), "rewards.yml");

        if (!menuFile.exists()) plugin.saveResource("menu.yml", false);
        if (!rewardsFile.exists()) plugin.saveResource("rewards.yml", false);

        // Cargar todos los archivos usando UTF-8
        mainConfig = loadYamlWithUTF8(configFile);
        menuConfig = loadYamlWithUTF8(menuFile);
        rewardsConfig = loadYamlWithUTF8(rewardsFile);
    }

    public void reloadFiles() {
        // Recargar todos los archivos usando UTF-8
        mainConfig = loadYamlWithUTF8(configFile);
        menuConfig = loadYamlWithUTF8(menuFile);
        rewardsConfig = loadYamlWithUTF8(rewardsFile);
        plugin.getRewardManager().loadRewards();
    }

    // MÉTODO AUXILIAR PARA CARGAR YAMLS CON ENCODING UTF-8
    public YamlConfiguration loadYamlWithUTF8(File file) {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            return YamlConfiguration.loadConfiguration(reader);
        } catch (IOException e) {
            plugin.getLogger().severe("Error loading file with UTF-8: " + file.getName());
            e.printStackTrace();
            return new YamlConfiguration(); // Devuelve una config vacía para evitar errores
        }
    }

    public void sendMessage(CommandSender sender, String key, String... replacements) {
        String message;
        if (key.equalsIgnoreCase("custom")) {
            message = replacements.length > 1 ? replacements[1] : "";
        } else {
            message = getMainConfig().getString("messages." + key, "&cMessage not found: " + key);
        }

        message = message.replace("%prefix%", getPrefix());

        for (int i = 0; i < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }

        if (plugin.isPapiEnabled() && sender instanceof Player) {
            message = PlaceholderAPI.setPlaceholders((Player) sender, message);
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public void playSound(Player player, String soundKey) {
        String soundConfig = mainConfig.getString("sounds." + soundKey);
        if (soundConfig == null || soundConfig.isEmpty()) return;

        String[] parts = soundConfig.split(":");
        Optional<XSound> xSound = XSound.matchXSound(parts[0]);

        if (xSound.isPresent()) {
            Sound sound = xSound.get().parseSound();
            if (sound != null) {
                float volume = 1.0F;
                float pitch = 1.0F;
                try {
                    if (parts.length > 1) volume = Float.parseFloat(parts[1]);
                    if (parts.length > 2) pitch = Float.parseFloat(parts[2]);
                } catch (NumberFormatException ignored) {}

                player.playSound(player.getLocation(), sound, volume, pitch);
            }
        }
    }

    // Getters
    public FileConfiguration getMenuConfig() { return menuConfig; }
    public FileConfiguration getRewardsConfig() { return rewardsConfig; }
    public FileConfiguration getMainConfig() { return mainConfig; }

    public String getPrefix() {
        return mainConfig.getString("prefix", "&e&lZenRewards &8» &r");
    }
}