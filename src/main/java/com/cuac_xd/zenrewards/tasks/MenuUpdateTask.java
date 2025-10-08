package com.cuac_xd.zenrewards.tasks;

import com.cuac_xd.zenrewards.ZenRewards;
import com.cuac_xd.zenrewards.models.Reward;
import com.cuac_xd.zenrewards.utils.ChatUtils;
import com.cuac_xd.zenrewards.utils.TimeUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.stream.Collectors;

public class MenuUpdateTask extends BukkitRunnable {

    private final ZenRewards plugin;
    private final Player player;
    private final String menuTitle;

    public MenuUpdateTask(ZenRewards plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.menuTitle = ChatUtils.colorize(plugin.getConfigManager().getMenuConfig().getString("title"));
    }

    @Override
    public void run() {
        // Si el jugador ya no es válido o no tiene un inventario abierto, cancelamos.
        if (player == null || !player.isOnline() || player.getOpenInventory() == null) {
            this.cancel();
            return;
        }

        Inventory openInventory = player.getOpenInventory().getTopInventory();

        // Si el inventario que tiene abierto ya no es el de recompensas, cancelamos.
        if (!player.getOpenInventory().getTitle().equals(menuTitle)) {
            this.cancel();
            plugin.getMenuManager().cancelUpdateTask(player); // Notificamos al manager
            return;
        }

        ConfigurationSection itemsSection = plugin.getConfigManager().getMenuConfig().getConfigurationSection("items");
        if (itemsSection == null) return;

        // Recorremos los ítems configurados en el menú
        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemConfig = itemsSection.getConfigurationSection(key);
            String rewardId = itemConfig.getString("reward_id");

            // Solo nos interesan los ítems que son recompensas
            if (rewardId == null) continue;

            Reward reward = plugin.getRewardManager().getReward(rewardId);
            if (reward == null) continue;

            // Si la recompensa está en cooldown, actualizamos el lore
            if (plugin.getRewardManager().isOnCooldown(player, reward)) {
                int slot = itemConfig.getInt("slot");
                ItemStack item = openInventory.getItem(slot);

                if (item != null) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null && meta.hasLore()) {
                        long cooldownEnd = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId()).getCooldown(rewardId);
                        String newFormattedTime = TimeUtils.formatDuration(cooldownEnd - System.currentTimeMillis());

                        // Recreamos el lore con el tiempo actualizado
                        List<String> newLore = meta.getLore().stream()
                                .map(line -> line.replaceAll(TimeUtils.formatDuration(cooldownEnd - System.currentTimeMillis() + 1000), newFormattedTime))
                                .collect(Collectors.toList());

                        // Una forma más simple si el formato no cambia drásticamente
                        ConfigurationSection cooldownItemConfig = itemConfig.getConfigurationSection("cooldown-item");
                        if (cooldownItemConfig != null) {
                            List<String> originalLore = cooldownItemConfig.getStringList("lore");
                            newLore = originalLore.stream()
                                    .map(line -> ChatUtils.colorize(line.replace("%cooldown_status%", newFormattedTime)))
                                    .collect(Collectors.toList());
                        }

                        meta.setLore(newLore);
                        item.setItemMeta(meta);
                    }
                }
            }
        }
    }
}