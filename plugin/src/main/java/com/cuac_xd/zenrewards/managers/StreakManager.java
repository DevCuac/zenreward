package com.cuac_xd.zenrewards.managers;

import com.cuac_xd.zenrewards.ZenRewards;
import com.cuac_xd.zenrewards.models.StreakReward;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StreakManager {

    private final ZenRewards plugin;
    private final Map<Integer, StreakReward> streakRewards = new HashMap<>();
    private FileConfiguration streaksConfig;

    public StreakManager(ZenRewards plugin) {
        this.plugin = plugin;
        loadStreaks();
    }

    public void loadStreaks() {
        File streaksFile = new File(plugin.getDataFolder(), "streaks.yml");
        if (!streaksFile.exists()) {
            plugin.saveResource("streaks.yml", false);
        }
        this.streaksConfig = plugin.getConfigManager().loadYamlWithUTF8(streaksFile);

        streakRewards.clear();

        ConfigurationSection rewardsSection = streaksConfig.getConfigurationSection("rewards");
        if (rewardsSection == null) {
            plugin.getLogger().warning("La sección 'rewards' no se encuentra en streaks.yml. El sistema de rachas no funcionará.");
            return;
        }

        for (String dayKey : rewardsSection.getKeys(false)) {
            try {
                int day = Integer.parseInt(dayKey);
                String path = "rewards." + dayKey;

                List<String> commands = streaksConfig.getStringList(path + ".commands");
                // ESTA ES LA LÍNEA CRÍTICA: Leemos la sección 'display'
                ConfigurationSection displayConfig = streaksConfig.getConfigurationSection(path + ".display");

                // Comprobación de seguridad: si una recompensa no tiene sección 'display', la ignoramos.
                if (displayConfig == null) {
                    plugin.getLogger().warning("La recompensa de racha para el día " + day + " no tiene una sección 'display' en streaks.yml y será ignorada.");
                    continue;
                }

                StreakReward reward = new StreakReward(day, commands, displayConfig);
                streakRewards.put(day, reward);

            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Se encontró un número de día inválido en streaks.yml: '" + dayKey + "'");
            }
        }
    }

    public StreakReward getRewardForDay(int day) {
        return streakRewards.get(day);
    }

    public FileConfiguration getStreaksConfig() {
        return streaksConfig;
    }
}