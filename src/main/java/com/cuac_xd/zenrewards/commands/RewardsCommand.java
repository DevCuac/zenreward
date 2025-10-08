package com.cuac_xd.zenrewards.commands;

import com.cuac_xd.zenrewards.ZenRewards;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RewardsCommand implements CommandExecutor {

    private final ZenRewards plugin;

    public RewardsCommand(ZenRewards plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getConfigManager().sendMessage(sender, "player-only-command");
            return true;
        }

        Player player = (Player) sender;
        plugin.getMenuManager().openRewardsMenu(player);
        return true;
    }
}