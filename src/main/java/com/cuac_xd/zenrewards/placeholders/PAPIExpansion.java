package com.cuac_xd.zenrewards.placeholders;

import com.cuac_xd.zenrewards.ZenRewards;
import com.cuac_xd.zenrewards.models.Reward;
import com.cuac_xd.zenrewards.utils.ChatUtils;
import com.cuac_xd.zenrewards.utils.TimeUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class PAPIExpansion extends PlaceholderExpansion {

    private final ZenRewards plugin;

    public PAPIExpansion(ZenRewards plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() { return "zenrewards"; }

    @Override
    public @NotNull String getAuthor() { return "cuac_xd"; }

    @Override
    public @NotNull String getVersion() { return "1.0"; }

    @Override
    public boolean persist() { return true; }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        // Obtenemos la configuración principal para acceder a los textos traducibles
        FileConfiguration config = plugin.getConfigManager().getMainConfig();

        // Placeholder: %zenrewards_streak_current%
        if (params.equalsIgnoreCase("streak_current")) {
            return String.valueOf(plugin.getPlayerDataManager().getPlayerData(player.getUniqueId()).getConnectionStreak());
        }

        // Placeholder: %zenrewards_claimable_count%
        if (params.equalsIgnoreCase("claimable_count")) {
            if (!player.isOnline()) return "0";
            long count = plugin.getRewardManager().getRewards().values().stream()
                    .filter(reward -> plugin.getRewardManager().isClaimable(player.getPlayer(), reward))
                    .count();
            return String.valueOf(count);
        }

        // Placeholders dinámicos
        if (params.startsWith("cooldown_")) {
            String rewardId = params.substring(9);
            Reward reward = plugin.getRewardManager().getReward(rewardId);
            if (reward == null) {
                // ANTES: "Invalid Reward" -> AHORA: Lee de la config
                return ChatUtils.colorize(config.getString("placeholder-outputs.invalid_reward", "&cInvalid Reward"));
            }

            long cooldownEnd = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId()).getCooldown(rewardId);
            if (plugin.getRewardManager().hasClaimedUnique(player.getPlayer(), rewardId)) {
                // ANTES: "Claimed" -> AHORA: Lee de la config
                return ChatUtils.colorize(config.getString("placeholder-outputs.claimed", "&cClaimed"));
            }
            if (cooldownEnd < System.currentTimeMillis()) {
                // ANTES: "Ready" -> AHORA: Lee de la config
                return ChatUtils.colorize(config.getString("placeholder-outputs.ready", "&aReady"));
            }

            return TimeUtils.formatDuration(cooldownEnd - System.currentTimeMillis());
        }

        if (params.startsWith("status_")) {
            if (!player.isOnline()) return "";
            String rewardId = params.substring(7);
            Reward reward = plugin.getRewardManager().getReward(rewardId);
            if (reward == null) {
                // ANTES: "Invalid Reward" -> AHORA: Lee de la config
                return ChatUtils.colorize(config.getString("placeholder-outputs.invalid_reward", "&cInvalid Reward"));
            }

            if (reward.getPermission() != null && !reward.getPermission().isEmpty() && !player.getPlayer().hasPermission(reward.getPermission())) {
                return ChatUtils.colorize(config.getString("reward-status.no_permission", "&cLocked"));
            }
            if (plugin.getRewardManager().hasClaimedUnique(player.getPlayer(), rewardId)) {
                return ChatUtils.colorize(config.getString("reward-status.claimed", "&aClaimed"));
            }
            if (plugin.getRewardManager().isOnCooldown(player.getPlayer(), reward)) {
                return ChatUtils.colorize(config.getString("reward-status.cooldown", "&eCooldown"));
            }

            return ChatUtils.colorize(config.getString("reward-status.available", "&aAvailable"));
        }

        return null;
    }
}