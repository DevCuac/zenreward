package com.cuac_xd.zenrewards.utils;

import com.cuac_xd.zenrewards.ZenRewards;
import com.cuac_xd.zenrewards.api.VersionAdapter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.stream.Collectors;

public class ItemBuilder {

    private final ConfigurationSection section;
    private String namePlaceholder = null;
    private String nameReplacement = null;
    private String lorePlaceholder = null;
    private String loreReplacement = null;

    public ItemBuilder(ConfigurationSection section) { this.section = section; }
    public ItemBuilder replaceInName(String placeholder, String replacement) { this.namePlaceholder = placeholder; this.nameReplacement = replacement; return this; }
    public ItemBuilder replaceInLore(String placeholder, String replacement) { this.lorePlaceholder = placeholder; this.loreReplacement = replacement; return this; }

    public ItemStack build() {
        String materialName = section.getString("material", "STONE");
        byte data = (byte) section.getInt("data", 0);

        VersionAdapter versionAdapter = ZenRewards.getInstance().getVersionAdapter();

        // 1. Crear siempre el item base (en blanco si es una cabeza)
        ItemStack item = versionAdapter.createItemStack(materialName, data);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item; // No se puede modificar
        }

        // 2. Aplicar nombre y lore
        String name = section.getString("name");
        if (name != null) {
            if (namePlaceholder != null) name = name.replace(namePlaceholder, nameReplacement);
            meta.setDisplayName(ChatUtils.colorize(name));
        }

        List<String> lore = section.getStringList("lore");
        if (!lore.isEmpty()) {
            meta.setLore(lore.stream()
                    .map(line -> {
                        if (lorePlaceholder != null) line = line.replace(lorePlaceholder, loreReplacement);
                        return ChatUtils.colorize(line);
                    })
                    .collect(Collectors.toList()));
        }

        // 3. Intentar aplicar la textura (el adaptador decidirá si es posible)
        String texture = section.getString("head-texture-value");
        versionAdapter.applyTexture(meta, texture);

        // 4. Aplicar la metadata final al item
        item.setItemMeta(meta);
        return item;
    }
}
