package com.ssomar.score.utils.item;

import com.ssomar.score.menu.GUI;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UpdateItemInGUI {

    public static void updateItemInGUI(GUI gui, String editorName, String name, List<String> lore, Material material) {
        /* Bug item no update idk why */
        ItemStack clone = new ItemStack(material);
        GUI.setIdentifier(clone, editorName);
        int slot = gui.getSlotByIdentifier(editorName);
        if(slot == -1) return;
        gui.getInv().setItem(slot, null);
        ItemMeta meta2 = clone.getItemMeta();
        meta2.setDisplayName(name);
        meta2.setLore(lore);
        clone.setItemMeta(meta2);
        gui.getInv().setItem(slot, clone);
    }

    public static void updateItemInGUI(GUI gui, String editorName, @Nullable String name, @Nullable List<String> lore, ItemStack item) {
        /* Bug item no update idk why */
        ItemStack clone = item;
        GUI.setIdentifier(clone, editorName);
        int slot = gui.getSlotByIdentifier(editorName);
        if(slot == -1) return;
        gui.getInv().setItem(slot, null);
        ItemMeta meta2 = clone.getItemMeta();
        if(name != null) meta2.setDisplayName(name);
        if(lore != null) meta2.setLore(lore);
        clone.setItemMeta(meta2);
        gui.getInv().setItem(slot, clone);
    }
}
