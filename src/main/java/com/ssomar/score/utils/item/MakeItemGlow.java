package com.ssomar.score.utils.item;

import com.ssomar.score.SCore;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MakeItemGlow {

    public static ItemStack makeGlow(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta = makeGlow(meta);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemMeta makeGlow(ItemMeta meta) {
        if(SCore.is1v20v5Plus()) {
            meta.setEnchantmentGlintOverride(true);
        }
        else {
            meta.addEnchant(Enchantment.getByName("PROTECTION_FALL"), 6, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        return meta;
    }

    public static ItemStack makeUnGlow(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta = makeUnGlow(meta);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemMeta makeUnGlow(ItemMeta meta) {
        if(SCore.is1v20v5Plus()) {
            meta.setEnchantmentGlintOverride(false);
        }
        else {
            if (meta.hasEnchants()) {
                for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                    meta.removeEnchant(entry.getKey());
                }
            }
        }
        return meta;
    }
}
