package com.cuac_xd.zenrewards.managers;

import com.cuac_xd.zenrewards.ZenRewards;
import com.cuac_xd.zenrewards.models.Reward;
import com.cuac_xd.zenrewards.tasks.MenuUpdateTask;
import com.cuac_xd.zenrewards.utils.ChatUtils;
import com.cuac_xd.zenrewards.utils.ItemBuilder;
import com.cuac_xd.zenrewards.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MenuManager {

    private final ZenRewards plugin;
    private final Map<UUID, BukkitTask> updateTasks = new HashMap<>();

    public MenuManager(ZenRewards plugin) {
        this.plugin = plugin;
    }

    public void openRewardsMenu(Player player) {
        ConfigurationSection menuConfig = plugin.getConfigManager().getMenuConfig().getConfigurationSection("items");
        String title = ChatUtils.colorize(plugin.getConfigManager().getMenuConfig().getString("title", "&8Reward Menu"));
        int rows = plugin.getConfigManager().getMenuConfig().getInt("rows", 6);
        Inventory inv = Bukkit.createInventory(null, rows * 9, title);

        FileConfiguration mainConfig = plugin.getConfigManager().getMainConfig();
        String enabledText = ChatUtils.colorize(mainConfig.getString("menu-variables.status-enabled", "&aActivado"));
        String disabledText = ChatUtils.colorize(mainConfig.getString("menu-variables.status-disabled", "&cDesactivado"));

        if (plugin.getConfigManager().getMenuConfig().isSet("filler_item")) {
            ItemStack filler = new ItemBuilder(plugin.getConfigManager().getMenuConfig().getConfigurationSection("filler_item")).build();
            for (int i = 0; i < inv.getSize(); i++) {
                inv.setItem(i, filler);
            }
        }

        if (menuConfig != null) {
            for (String key : menuConfig.getKeys(false)) {
                ConfigurationSection itemSection = menuConfig.getConfigurationSection(key);
                if (itemSection == null) continue;

                int slot = itemSection.getInt("slot");
                ItemStack itemStack = null;
                String rewardId = itemSection.getString("reward_id");

                if (rewardId != null) {
                    Reward reward = plugin.getRewardManager().getReward(rewardId);
                    if (reward == null) continue;

                    String status;
                    ConfigurationSection displaySection;

                    if (reward.getPermission() != null && !reward.getPermission().isEmpty() && !player.hasPermission(reward.getPermission())) {
                        status = mainConfig.getString("reward-status.no_permission", "&cLocked");
                        displaySection = itemSection.getConfigurationSection("no-permission-item");
                    } else if (plugin.getRewardManager().isOnCooldown(player, reward)) {
                        status = mainConfig.getString("reward-status.cooldown", "&eCooldown");
                        displaySection = itemSection.getConfigurationSection("cooldown-item");
                    } else if (reward.getType().equalsIgnoreCase("UNIQUE") && plugin.getRewardManager().hasClaimedUnique(player, rewardId)) {
                        status = mainConfig.getString("reward-status.claimed", "&aClaimed");
                        displaySection = itemSection.getConfigurationSection("unique-claimed-item");
                    } else {
                        status = mainConfig.getString("reward-status.available", "&aAvailable");
                        displaySection = itemSection.getConfigurationSection("claimable-item");
                    }

                    if (displaySection != null) {
                        long cooldownEnd = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId()).getCooldown(rewardId);
                        String cooldownFormatted = TimeUtils.formatDuration(cooldownEnd - System.currentTimeMillis());
                        itemStack = new ItemBuilder(displaySection)
                                .replaceInName("%status%", status)
                                .replaceInLore("%status%", status)
                                .replaceInLore("%cooldown_status%", cooldownFormatted)
                                .build();
                    }
                } else {
                    String permission = itemSection.getString("permission");
                    boolean hasPermission = (permission == null || permission.isEmpty() || player.hasPermission(permission));

                    ConfigurationSection displaySection;

                    if (hasPermission) {
                        displaySection = itemSection.getConfigurationSection("has-permission-item");
                        if (displaySection == null) displaySection = itemSection;
                    } else {
                        displaySection = itemSection.getConfigurationSection("no-permission-item");
                    }

                    if (displaySection == null) continue;
                    ItemBuilder builder = new ItemBuilder(displaySection);

                    String type = itemSection.getString("type", "");
                    if (type.equalsIgnoreCase("OPTION_TOGGLE_NOTIFICATIONS")) {
                        boolean notify = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId()).hasNotificationsEnabled();
                        builder.replaceInLore("%player_notify_status%", notify ? enabledText : disabledText);
                    } else if (type.equalsIgnoreCase("OPTION_TOGGLE_AUTOCLAIM")) {
                        boolean autoClaim = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId()).hasAutoClaimEnabled();
                        builder.replaceInLore("%player_autoclaim_status%", autoClaim ? enabledText : disabledText);
                    }

                    itemStack = builder.build();
                }

                if (itemStack != null) {
                    inv.setItem(slot, itemStack);
                }
            }
        }

        player.openInventory(inv);
        plugin.getConfigManager().playSound(player, "open");

        cancelUpdateTask(player);
        BukkitTask task = new MenuUpdateTask(plugin, player).runTaskTimer(plugin, 20L, 20L);
        updateTasks.put(player.getUniqueId(), task);
    }

    public void cancelUpdateTask(Player player) {
        if (updateTasks.containsKey(player.getUniqueId())) {
            updateTasks.get(player.getUniqueId()).cancel();
            updateTasks.remove(player.getUniqueId());
        }
    }

    public void cancelAllTasks() {
        updateTasks.values().forEach(BukkitTask::cancel);
        updateTasks.clear();
    }
}