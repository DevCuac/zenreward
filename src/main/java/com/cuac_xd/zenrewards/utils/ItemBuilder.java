package com.cuac_xd.zenrewards.utils;

import com.cryptomorin.xseries.XMaterial;
import com.saicone.rtag.RtagItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
        ItemStack item = XMaterial.matchXMaterial(materialName)
                .orElse(XMaterial.STONE)
                .parseItem();

        ItemMeta meta = item.getItemMeta();

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
        item.setItemMeta(meta);

        // --- INICIO DE LA IMPLEMENTACIÓN FINAL Y CORRECTA CON RTAG ---
        if (item.getType() == XMaterial.PLAYER_HEAD.parseMaterial() && section.contains("head-texture-value")) {
            RtagItem rtagItem = new RtagItem(item);

            // --- LÍNEA CORREGIDA SEGÚN LA DOCUMENTACIÓN OFICIAL ---
            // Usamos el método .setSkull() directamente sobre el objeto RtagItem.
            rtagItem.setSkull(section.getString("head-texture-value"));

            // Devolvemos el ítem ya modificado por la API.
            return rtagItem.getItem();
        }
        // --- FIN DE LA IMPLEMENTACIÓN ---

        return item;
    }
}