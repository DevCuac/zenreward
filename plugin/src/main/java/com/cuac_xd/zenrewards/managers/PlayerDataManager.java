package com.cuac_xd.zenrewards.managers;

import com.cuac_xd.zenrewards.ZenRewards;
import com.cuac_xd.zenrewards.models.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap; // <-- IMPORT AÑADIDO
import java.util.logging.Level;

public class PlayerDataManager {

    private final ZenRewards plugin;
    // CAMBIO CLAVE AQUÍ: Usamos ConcurrentHashMap en lugar de HashMap
    private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();
    private final File dataFolder;

    public PlayerDataManager(ZenRewards plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        // Esta operación ahora es 100% segura gracias a ConcurrentHashMap
        return playerDataMap.computeIfAbsent(uuid, this::loadPlayerData);
    }

    public PlayerData loadPlayerData(UUID uuid) {
        File playerFile = new File(dataFolder, uuid.toString() + ".yml");
        PlayerData data = new PlayerData(uuid);

        if (playerFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            data.setLastStreakClaimedDay(config.getInt("data.last-streak-claimed", 0));
            data.setNotificationsEnabled(config.getBoolean("settings.notifications", true));
            data.setAutoClaimEnabled(config.getBoolean("settings.auto-claim", false));

            // Datos de racha
            data.setConnectionStreak(config.getInt("data.streak", 0));
            data.setLastLogin(config.getLong("data.last-login", 0));

            if (config.isConfigurationSection("cooldowns")) {
                config.getConfigurationSection("cooldowns").getKeys(false).forEach(rewardId -> {
                    long cooldownEnd = config.getLong("cooldowns." + rewardId);
                    if (cooldownEnd > System.currentTimeMillis() || cooldownEnd == -1L) {
                        data.setCooldown(rewardId, cooldownEnd);
                    }
                });
            }
        }
        // computeIfAbsent se encargará de ponerlo en el mapa de forma segura.
        return data;
    }

    public void savePlayerData(UUID uuid) {
        PlayerData data = playerDataMap.get(uuid);
        if (data == null) return;

        File playerFile = new File(dataFolder, uuid.toString() + ".yml");
        FileConfiguration config = new YamlConfiguration();

        config.set("settings.notifications", data.hasNotificationsEnabled());
        config.set("settings.auto-claim", data.hasAutoClaimEnabled());

        // Datos de racha
        config.set("data.last-streak-claimed", data.getLastStreakClaimedDay());
        config.set("data.streak", data.getConnectionStreak());
        config.set("data.last-login", data.getLastLogin());

        data.getRewardCooldowns().forEach((rewardId, cooldownEnd) -> {
            if (cooldownEnd > System.currentTimeMillis() || cooldownEnd == -1L) {
                config.set("cooldowns." + rewardId, cooldownEnd);
            }
        });

        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save player data for " + uuid, e);
        }
    }

    public void playerLeft(Player player) {
        savePlayerData(player.getUniqueId());
        playerDataMap.remove(player.getUniqueId());
    }

    public void saveAllPlayerData() {
        playerDataMap.keySet().forEach(this::savePlayerData);
    }
}