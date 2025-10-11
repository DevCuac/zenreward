package com.cuac_xd.zenrewards.listeners;

import com.cuac_xd.zenrewards.ZenRewards;
import com.cuac_xd.zenrewards.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryCloseListener implements Listener {

    private final ZenRewards plugin;

    public InventoryCloseListener(ZenRewards plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            String menuTitle = ChatUtils.colorize(plugin.getConfigManager().getMenuConfig().getString("title"));

            // Si el inventario que se cierra es el nuestro, cancelamos la tarea.
            if (event.getView().getTitle().equals(menuTitle)) {
                plugin.getMenuManager().cancelUpdateTask(player);
            }
        }
    }
}