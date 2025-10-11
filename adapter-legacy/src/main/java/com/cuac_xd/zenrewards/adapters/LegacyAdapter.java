package com.cuac_xd.zenrewards.adapters;

import com.cryptomorin.xseries.XMaterial;
import com.cuac_xd.zenrewards.api.VersionAdapter;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

public class LegacyAdapter implements VersionAdapter {

    @Override
    public ItemStack createItemStack(String material, byte data) {
        Optional<XMaterial> xMaterialOptional = XMaterial.matchXMaterial(material);

        if (!material.contains(":") && data != 0) {
            xMaterialOptional = XMaterial.matchXMaterial(material + ":" + data);
        }

        if (xMaterialOptional.isPresent()) {
            ItemStack item = xMaterialOptional.get().parseItem();
            if (item != null) {
                return item;
            }
        }

        return new ItemStack(Material.getMaterial("STONE"));
    }

    public void applyTexture(ItemMeta meta, String base64Texture) {
        if (!(meta instanceof SkullMeta) || base64Texture == null) return;
        SkullMeta skullMeta = (SkullMeta) meta;
        try {
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", base64Texture));
            Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullMeta, profile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}