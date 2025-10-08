package com.cuac_xd.zenrewards.managers;

import com.cuac_xd.zenrewards.ZenRewards;
import com.cuac_xd.zenrewards.models.PlayerData;
import com.cuac_xd.zenrewards.models.StreakReward;
import com.cuac_xd.zenrewards.utils.ChatUtils;
import com.cuac_xd.zenrewards.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StreakMenuManager {

    private final ZenRewards plugin;
    private FileConfiguration streakMenuConfig;

    public StreakMenuManager(ZenRewards plugin) {
        this.plugin = plugin;
        loadMenuConfig();
    }

    private void loadMenuConfig() {
        File menuFile = new File(plugin.getDataFolder(), "streak-menu.yml");
        if (!menuFile.exists()) {
            plugin.saveResource("streak-menu.yml", false);
        }
        this.streakMenuConfig = plugin.getConfigManager().loadYamlWithUTF8(menuFile);
    }

    public void openStreakMenu(Player player) {
        String title = ChatUtils.colorize(streakMenuConfig.getString("title"));
        int rows = streakMenuConfig.getInt("rows");
        Inventory inv = Bukkit.createInventory(null, rows * 9, title);

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        // 1. Rellenar el fondo del inventario
        if (streakMenuConfig.isConfigurationSection("filler_item")) {
            // CORREGIDO: Usaba un nombre de variable incorrecto
            ItemStack filler = new ItemBuilder(streakMenuConfig.getConfigurationSection("filler_item")).build();
            for (int i = 0; i < inv.getSize(); i++) {
                inv.setItem(i, filler);
            }
        }

        // 2. Colocar las recompensas de racha
        List<Integer> rewardSlots = streakMenuConfig.getIntegerList("reward_slots");
        for (int i = 0; i < rewardSlots.size(); i++) {
            int day = i + 1;
            int slot = rewardSlots.get(i);

            StreakReward reward = plugin.getStreakManager().getRewardForDay(day);
            ConfigurationSection itemAppearanceConfig;

            if (reward == null) {
                itemAppearanceConfig = streakMenuConfig.getConfigurationSection("reward_item.unconfigured");
            } else if (day <= data.getLastStreakClaimedDay()) {
                itemAppearanceConfig = streakMenuConfig.getConfigurationSection("reward_item.claimed");
            } else if (day <= data.getConnectionStreak()) {
                itemAppearanceConfig = streakMenuConfig.getConfigurationSection("reward_item.claimable");
            } else {
                itemAppearanceConfig = streakMenuConfig.getConfigurationSection("reward_item.locked");
            }

            if (itemAppearanceConfig == null) continue;

            ItemStack item = new ItemBuilder(itemAppearanceConfig).build();
            ItemMeta meta = item.getItemMeta();

            if (meta.hasDisplayName()) {
                meta.setDisplayName(meta.getDisplayName().replace("{day}", String.valueOf(day)));
            }
            if (meta.hasLore()) {
                List<String> finalLore = new ArrayList<>();
                for (String line : meta.getLore()) {
                    if (line.contains("{reward_lore}") && reward != null) {
                        // CORREGIDO: Llamamos a getDisplayConfig() en lugar de getDisplay()
                        List<String> rewardLore = reward.getDisplayConfig().getStringList("lore");
                        for (String rewardLine : rewardLore) {
                            finalLore.add(ChatUtils.colorize(rewardLine));
                        }
                    } else {
                        finalLore.add(line.replace("{day}", String.valueOf(day)));
                    }
                }
                meta.setLore(finalLore);
            }

            item.setItemMeta(meta);
            inv.setItem(slot, item);
        }

        // 3. Colocar otros ítems (decorativos, funcionales)
        ConfigurationSection otherItemsSection = streakMenuConfig.getConfigurationSection("other_items");
        if (otherItemsSection != null) {
            for (String key : otherItemsSection.getKeys(false)) {
                ConfigurationSection itemConfig = otherItemsSection.getConfigurationSection(key);
                int slot = itemConfig.getInt("slot");
                ItemStack item = new ItemBuilder(itemConfig).build();
                inv.setItem(slot, item);
            }
        }

        player.openInventory(inv);
    }

    public FileConfiguration getStreakMenuConfig() {
        return streakMenuConfig;
    }
}