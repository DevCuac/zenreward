package com.cuac_xd.zenrewards.commands;

import com.cuac_xd.zenrewards.ZenRewards;
import com.cuac_xd.zenrewards.models.PlayerData;
import com.cuac_xd.zenrewards.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ZenRewardsAdminCommand implements CommandExecutor {

    private final ZenRewards plugin;
    public ZenRewardsAdminCommand(ZenRewards plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("zenrewards.admin")) {
            plugin.getConfigManager().sendMessage(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            plugin.getConfigManager().sendMessage(sender, "admin-help-header");
            plugin.getConfigManager().sendMessage(sender, "admin-help-reload");
            plugin.getConfigManager().sendMessage(sender, "admin-help-reset");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.getConfigManager().reloadFiles();
            plugin.getConfigManager().sendMessage(sender, "reload-success");
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            if (args.length < 3) {
                plugin.getConfigManager().sendMessage(sender, "admin-usage-reset");
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            if (!target.hasPlayedBefore()) {
                plugin.getConfigManager().sendMessage(sender, "player-not-found");
                return true;
            }

            PlayerData data = plugin.getPlayerDataManager().getPlayerData(target.getUniqueId());
            String rewardId = args[2];

            if (rewardId.equalsIgnoreCase("all")) {
                data.getRewardCooldowns().clear();
                plugin.getConfigManager().sendMessage(sender, "all-rewards-reset", "{player}", target.getName());
            } else {
                if (plugin.getRewardManager().getReward(rewardId) == null) {
                    plugin.getConfigManager().sendMessage(sender, "reward-not-found", "{reward}", rewardId);
                    return true;
                }
                data.clearCooldown(rewardId);
                plugin.getConfigManager().sendMessage(sender, "reward-reset", "{reward}", rewardId, "{player}", target.getName());
            }
            if (!target.isOnline()) {
                plugin.getPlayerDataManager().savePlayerData(target.getUniqueId());
            }
            return true;
        }
        return true;
    }
}