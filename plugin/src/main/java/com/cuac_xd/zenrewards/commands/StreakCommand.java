package com.cuac_xd.zenrewards.commands;

import com.cuac_xd.zenrewards.ZenRewards;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StreakCommand implements CommandExecutor {

    private final ZenRewards plugin;

    public StreakCommand(ZenRewards plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getConfigManager().sendMessage(sender, "player-only-command");
            return true;
        }

        Player player = (Player) sender;
        // Aquí llamaremos al nuevo manager que crearemos en el siguiente paso
        plugin.getStreakMenuManager().openStreakMenu(player);
        return true;
    }
}