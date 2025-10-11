package com.cuac_xd.zenrewards.adapters;

import com.cryptomorin.xseries.XMaterial;
import com.cuac_xd.zenrewards.api.VersionAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

public class ModernAdapter implements VersionAdapter {

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

        return new ItemStack(Material.STONE);
    }

    @Override
    public void applyTexture(ItemMeta meta, String textureValue) {
        if (!(meta instanceof SkullMeta) || textureValue == null) return;
        SkullMeta skullMeta = (SkullMeta) meta;

        try {
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), null);
            PlayerTextures textures = profile.getTextures();

            String decodedJson = new String(Base64.getDecoder().decode(textureValue), StandardCharsets.UTF_8);
            String textureUrl = extractTextureUrl(decodedJson);

            if (textureUrl != null && !textureUrl.isEmpty()) {
                textures.setSkin(new URL(textureUrl));
                profile.setTextures(textures);
                skullMeta.setOwnerProfile(profile);
            } else {
                System.err.println("No se pudo extraer la URL de la textura desde el JSON.");
            }
        } catch (Exception e) {
            System.err.println("Error al aplicar la textura de la cabeza: " + textureValue);
            e.printStackTrace();
        }
    }

    private String extractTextureUrl(String jsonString) {
        try {
            String searchFor = "\"url\":\"";
            int startIndex = jsonString.indexOf(searchFor);
            if (startIndex != -1) {
                startIndex += searchFor.length();
                int endIndex = jsonString.indexOf("\"", startIndex);
                if (endIndex != -1) {
                    return jsonString.substring(startIndex, endIndex);
                }
            }
        } catch (Exception e) {
            //fallo
        }
        return null;
    }
}