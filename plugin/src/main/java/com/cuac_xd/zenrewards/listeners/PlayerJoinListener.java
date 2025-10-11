package com.cuac_xd.zenrewards.listeners;

import com.cuac_xd.zenrewards.ZenRewards;
import com.cuac_xd.zenrewards.models.PlayerData;
import com.cuac_xd.zenrewards.models.Reward;
import com.cuac_xd.zenrewards.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.LocalDate;
import java.time.ZoneId;

public class PlayerJoinListener implements Listener {

    private final ZenRewards plugin;

    public PlayerJoinListener(ZenRewards plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        updateStreak(player);
        plugin.getPlayerDataManager().loadPlayerData(player.getUniqueId());

        // Retrasar para que otros plugins carguen
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            checkAndNotify(player);
        }, 40L); // 2 segundos
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getMenuManager().cancelUpdateTask(event.getPlayer());
        plugin.getPlayerDataManager().playerLeft(event.getPlayer());
    }

    private void updateStreak(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate lastLoginDate = LocalDate.ofEpochDay(0);
        if (data.getLastLogin() > 0) {
            lastLoginDate = LocalDate.ofEpochDay(data.getLastLogin() / (24 * 60 * 60 * 1000));
        }

        if (lastLoginDate.isEqual(today)) {
            // Ya se conectó hoy, no hacer nada.
            return;
        }

        if (lastLoginDate.isEqual(today.minusDays(1))) {
            // Se conectó ayer, aumenta la racha.
            data.setConnectionStreak(data.getConnectionStreak() + 1);
        } else {
            // Faltó un día, reinicia la racha a 1.
            data.setConnectionStreak(1);
        }

        data.setLastLogin(System.currentTimeMillis());

        // Aquí podrías añadir un mensaje o un pop-up de "¡Tu racha es de X días!"
    }

    private void checkAndNotify(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        boolean hasClaimable = false;

        for (String rewardId : plugin.getRewardManager().getRewards().keySet()) {
            Reward reward = plugin.getRewardManager().getReward(rewardId);

            boolean canClaim = !plugin.getRewardManager().isOnCooldown(player, reward) &&
                    (reward.getPermission() == null || reward.getPermission().isEmpty() || player.hasPermission(reward.getPermission()));
            if(reward.getType().equalsIgnoreCase("UNIQUE") && plugin.getRewardManager().hasClaimedUnique(player, rewardId)){
                canClaim = false;
            }

            if (canClaim) {
                if (data.hasAutoClaimEnabled()) {
                    plugin.getRewardManager().claimReward(player, rewardId);
                } else {
                    hasClaimable = true;
                }
            }
        }

        if (hasClaimable && data.hasNotificationsEnabled()) {
            plugin.getConfigManager().sendMessage(player, "join-notification");
            plugin.getConfigManager().playSound(player, "notify");
        }
    }
}