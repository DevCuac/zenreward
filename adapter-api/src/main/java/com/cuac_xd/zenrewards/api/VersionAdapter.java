package com.cuac_xd.zenrewards.api;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public interface VersionAdapter {

    /**
     * Crea un ItemStack manejando la compatibilidad de versiones para materiales generales.
     *
     * @param material El nombre del material (puede ser moderno como "RED_WOOL" o legacy como "WOOL").
     * @param data El valor de data para versiones legacy (ej: 14 para el color rojo). Ignorado en versiones modernas.
     * @return El ItemStack creado.
     */
    ItemStack createItemStack(String material, byte data);

    /**
     * Aplica una textura a un ItemMeta existente. Implementado solo para Modern.
     *
     * @param meta El ItemMeta (debe ser SkullMeta).
     * @param base64Texture La textura en formato Base64.
     */
    void applyTexture(ItemMeta meta, String base64Texture);



}
