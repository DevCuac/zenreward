package com.cuac_xd.zenrewards.listeners;

import com.cuac_xd.zenrewards.ZenRewards;
import com.cuac_xd.zenrewards.models.PlayerData;
import com.cuac_xd.zenrewards.models.StreakReward;
import com.cuac_xd.zenrewards.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class InventoryClickListener implements Listener {

    private final ZenRewards plugin;

    public InventoryClickListener(ZenRewards plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String clickedInventoryTitle = event.getView().getTitle();

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            if (event.getClickedInventory() == player.getOpenInventory().getTopInventory()) {
                event.setCancelled(true);
            }
            return;
        }

        String rewardsMenuTitle = ChatUtils.colorize(plugin.getConfigManager().getMenuConfig().getString("title"));
        if (clickedInventoryTitle.equals(rewardsMenuTitle)) {
            event.setCancelled(true);
            handleRewardsMenuClick(player, event.getSlot());
            return;
        }

        String streakMenuTitle = ChatUtils.colorize(plugin.getStreakMenuManager().getStreakMenuConfig().getString("title"));
        if (clickedInventoryTitle.equals(streakMenuTitle)) {
            event.setCancelled(true);
            handleStreakMenuClick(player, event.getSlot());
            return;
        }
    }

    private void handleRewardsMenuClick(Player player, int clickedSlot) {
        ConfigurationSection itemsSection = plugin.getConfigManager().getMenuConfig().getConfigurationSection("items");
        if (itemsSection == null) return;

        ConfigurationSection clickedItemSection = itemsSection.getKeys(false).stream()
                .map(itemsSection::getConfigurationSection)
                .filter(section -> section != null && section.getInt("slot") == clickedSlot)
                .findFirst().orElse(null);

        if (clickedItemSection == null) return;

        String rewardId = clickedItemSection.getString("reward_id");
        if (rewardId != null) {
            plugin.getRewardManager().claimReward(player, rewardId);
            refreshMenu(player, "rewards");
            return;
        }

        String permission = clickedItemSection.getString("permission");
        if (permission != null && !permission.isEmpty() && !player.hasPermission(permission)) {
            plugin.getConfigManager().sendMessage(player, "no-permission");
            plugin.getConfigManager().playSound(player, "fail");
            return; // ¡Acción detenida!
        }

        String type = clickedItemSection.getString("type");
        if (type != null && !type.isEmpty()) {
            PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            if (type.equalsIgnoreCase("OPTION_TOGGLE_NOTIFICATIONS")) {
                data.setNotificationsEnabled(!data.hasNotificationsEnabled());
                plugin.getConfigManager().sendMessage(player, data.hasNotificationsEnabled() ? "notifications-on" : "notifications-off");
            } else if (type.equalsIgnoreCase("OPTION_TOGGLE_AUTOCLAIM")) {
                data.setAutoClaimEnabled(!data.hasAutoClaimEnabled());
                plugin.getConfigManager().sendMessage(player, data.hasAutoClaimEnabled() ? "autoclaim-on" : "autoclaim-off");
            }
            plugin.getConfigManager().playSound(player, "success");
            refreshMenu(player, "rewards");
            return;
        }

        List<String> commands = clickedItemSection.getStringList("commands");
        if (!commands.isEmpty()) {
            commands.forEach(command -> executeCommand(player, command));
        }
    }

    private void handleStreakMenuClick(Player player, int clickedSlot) {
        List<Integer> rewardSlots = plugin.getStreakMenuManager().getStreakMenuConfig().getIntegerList("reward_slots");
        if (rewardSlots.contains(clickedSlot)) {
            int day = rewardSlots.indexOf(clickedSlot) + 1;
            PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

            if (day > data.getLastStreakClaimedDay() && day <= data.getConnectionStreak()) {
                StreakReward reward = plugin.getStreakManager().getRewardForDay(day);
                if (reward != null) {
                    reward.getCommands().forEach(cmd -> {
                        String processedCmd = cmd.replace("%player%", player.getName());
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCmd);
                    });

                    data.setLastStreakClaimedDay(day);

                    String claimMessage = plugin.getStreakManager().getStreaksConfig().getString("claim-message", "%prefix%&aRecompensa de racha reclamada!");
                    claimMessage = claimMessage.replace("{day}", String.valueOf(day));
                    plugin.getConfigManager().sendMessage(player, "custom", "{message}", claimMessage); // Enviamos como mensaje custom
                    plugin.getConfigManager().playSound(player, "success");
                    refreshMenu(player, "streak");
                }
            } else {
                plugin.getConfigManager().playSound(player, "fail");
            }
            return;
        }

        ConfigurationSection otherItemsSection = plugin.getStreakMenuManager().getStreakMenuConfig().getConfigurationSection("other_items");
        if (otherItemsSection != null) {
            for (String key : otherItemsSection.getKeys(false)) {
                if (otherItemsSection.getInt(key + ".slot") == clickedSlot) {
                    List<String> commands = otherItemsSection.getStringList(key + ".commands");
                    if (!commands.isEmpty()) {
                        commands.forEach(command -> executeCommand(player, command));
                    }
                    return;
                }
            }
        }
    }

    // Métodos de ayuda
    private void refreshMenu(Player player, String menuType) {
        player.closeInventory();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (menuType.equals("rewards")) {
                plugin.getMenuManager().openRewardsMenu(player);
            } else if (menuType.equals("streak")) {
                plugin.getStreakMenuManager().openStreakMenu(player);
            }
        }, 1L);
    }

    private void executeCommand(Player player, String command) {
        if (command.equalsIgnoreCase("[close]")) {
            player.closeInventory();
        } else if (command.toLowerCase().startsWith("[player]")) {
            player.performCommand(command.substring(8).trim());
        } else if (command.toLowerCase().startsWith("[console]")) {
            String consoleCmd = command.substring(9).trim().replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), consoleCmd);
        }
    }
}