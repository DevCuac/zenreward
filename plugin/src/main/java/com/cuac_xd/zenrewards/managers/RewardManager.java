package com.cuac_xd.zenrewards.managers;

import com.cuac_xd.zenrewards.ZenRewards;
import com.cuac_xd.zenrewards.models.PlayerData;
import com.cuac_xd.zenrewards.models.Reward;
import com.cuac_xd.zenrewards.utils.ChatUtils;
import com.cuac_xd.zenrewards.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardManager {

    private final ZenRewards plugin;
    private final Map<String, Reward> rewards = new HashMap<>();

    public RewardManager(ZenRewards plugin) {
        this.plugin = plugin;
        loadRewards();
    }

    public void loadRewards() {
        rewards.clear();
        plugin.getConfigManager().getRewardsConfig().getConfigurationSection("rewards").getKeys(false).forEach(rewardId -> {
            String path = "rewards." + rewardId;
            String type = plugin.getConfigManager().getRewardsConfig().getString(path + ".type");
            long cooldown = TimeUtils.parseTime(plugin.getConfigManager().getRewardsConfig().getString(path + ".cooldown", "0"));
            String permission = plugin.getConfigManager().getRewardsConfig().getString(path + ".permission", "");
            List<String> commands = plugin.getConfigManager().getRewardsConfig().getStringList(path + ".commands");
            List<String> messages = plugin.getConfigManager().getRewardsConfig().getStringList(path + ".messages");
            rewards.put(rewardId, new Reward(rewardId, type, cooldown, permission, commands, messages));
        });
        plugin.getLogger().info("Loaded " + rewards.size() + " rewards.");
    }

    public Reward getReward(String id) {
        return rewards.get(id);
    }


    public void claimReward(Player player, String rewardId) {
        Reward reward = getReward(rewardId);
        if (reward == null) return;

        if (reward.getPermission() != null && !reward.getPermission().isEmpty() && !player.hasPermission(reward.getPermission())) {
            plugin.getConfigManager().playSound(player, "fail");
            plugin.getConfigManager().sendMessage(player, "reward-no-permission");
            return;
        }

        if (reward.getType().equalsIgnoreCase("UNIQUE") && hasClaimedUnique(player, reward.getId())) {
            plugin.getConfigManager().playSound(player, "fail");
            plugin.getConfigManager().sendMessage(player, "reward-unique-claimed");
            return;
        }

        if (isOnCooldown(player, reward)) {
            plugin.getConfigManager().playSound(player, "fail");
            plugin.getConfigManager().sendMessage(player, "reward-on-cooldown");
            return;
        }

        reward.getCommands().forEach(command -> {
            String processedCmd = command.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCmd);
        });

        reward.getMessages().forEach(message -> {
            String processedMsg = message.replace("%prefix%", plugin.getConfigManager().getPrefix()).replace("%player%", player.getName());
            player.sendMessage(ChatUtils.colorize(processedMsg));
        });

        if (reward.getType().equalsIgnoreCase("REPEATABLE")) {
            if (reward.getCooldown() > 0) {
                plugin.getPlayerDataManager().getPlayerData(player.getUniqueId()).setCooldown(reward.getId(), System.currentTimeMillis() + reward.getCooldown());
            }
        } else {
            plugin.getPlayerDataManager().getPlayerData(player.getUniqueId()).setCooldown(reward.getId(), -1L);
        }

        plugin.getConfigManager().playSound(player, "success");
    }

    public boolean isClaimable(Player player, Reward reward) {
        if (player == null || reward == null) return false;
        if (reward.getPermission() != null && !reward.getPermission().isEmpty() && !player.hasPermission(reward.getPermission())) return false;
        if (reward.getType().equalsIgnoreCase("UNIQUE") && hasClaimedUnique(player, reward.getId())) return false;
        return !isOnCooldown(player, reward);
    }

    public boolean isOnCooldown(Player player, Reward reward) {
        long cooldownEnd = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId()).getCooldown(reward.getId());
        return System.currentTimeMillis() < cooldownEnd;
    }

    public boolean hasClaimedUnique(Player player, String rewardId) {
        return plugin.getPlayerDataManager().getPlayerData(player.getUniqueId()).getCooldown(rewardId) == -1L;
    }

    public Map<String, Reward> getRewards() {
        return Collections.unmodifiableMap(rewards);
    }
}