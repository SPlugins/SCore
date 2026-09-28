package com.ssomar.score.sobject.menu;

import com.ssomar.score.SCore;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Preview of an object shown on its icon in the list editors (/ei editor, /eb editor...): the
 * editor overwrites the name and the lore of the icon and hides its enchantments / attributes, so
 * they are read from the built item first and written back as a few bounded lines.
 * Every section is capped: an icon can't go over 256 lore lines and long lores make the menu unreadable.
 */
public final class SObjectIconPreview {

    public static final int MAX_LORE_LINES = 8;
    public static final int MAX_ENCHANTS = 5;
    public static final int MAX_ATTRIBUTES = 4;
    public static final int MAX_VISIBLE_LENGTH = 40;

    private SObjectIconPreview() {
    }

    /**
     * @param meta the meta of the built item, BEFORE the editor overwrites it
     * @return the preview lines, empty if the item has nothing to show
     */
    public static List<String> of(ItemMeta meta) {
        List<String> preview = new ArrayList<>();
        if (meta == null) return preview;

        if (meta.hasDisplayName()) preview.add("§7Name: §f" + truncate(meta.getDisplayName()));

        if (meta.hasLore() && meta.getLore() != null) {
            List<String> lore = meta.getLore();
            preview.add("§7Lore:");
            for (int i = 0; i < Math.min(lore.size(), MAX_LORE_LINES); i++) {
                preview.add(" §5§o" + truncate(lore.get(i)));
            }
            if (lore.size() > MAX_LORE_LINES) preview.add(" §8... +" + (lore.size() - MAX_LORE_LINES) + " lines");
        }

        if (meta.hasEnchants()) {
            preview.add("§7Enchantments:");
            int i = 0;
            for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                if (i++ >= MAX_ENCHANTS) {
                    preview.add(" §8... +" + (meta.getEnchants().size() - MAX_ENCHANTS));
                    break;
                }
                preview.add(" §b" + enchantName(entry.getKey()) + " §f" + entry.getValue());
            }
        }

        try {
            if (!SCore.is1v13Less() && meta.hasAttributeModifiers() && meta.getAttributeModifiers() != null) {
                Collection<? extends Map.Entry<?, AttributeModifier>> entries = meta.getAttributeModifiers().entries();
                if (!entries.isEmpty()) preview.add("§7Attributes:");
                int i = 0;
                for (Map.Entry<?, AttributeModifier> entry : entries) {
                    if (i++ >= MAX_ATTRIBUTES) {
                        preview.add(" §8... +" + (entries.size() - MAX_ATTRIBUTES));
                        break;
                    }
                    AttributeModifier modifier = entry.getValue();
                    preview.add(" §a" + attributeName(entry.getKey()) + " §f" + amount(modifier));
                }
            }
        } catch (Throwable ignored) {
            // The attributes API moved a lot between versions: the preview must never break the editor.
        }

        return preview;
    }

    /** Cuts a colored string on its VISIBLE length: a hex gradient is 14 raw chars per visible char. */
    public static String truncate(String colored) {
        if (colored == null) return "";
        StringBuilder sb = new StringBuilder();
        int visible = 0;
        for (int i = 0; i < colored.length(); i++) {
            char c = colored.charAt(i);
            if (c == '§' && i + 1 < colored.length()) {
                sb.append(c).append(colored.charAt(++i));
                continue;
            }
            if (visible == MAX_VISIBLE_LENGTH - 1) return sb.append("§7...").toString();
            sb.append(c);
            visible++;
        }
        return sb.toString();
    }

    private static String enchantName(Enchantment enchantment) {
        try {
            if (!SCore.is1v13Less()) return enchantment.getKey().getKey();
        } catch (Throwable ignored) {
        }
        return String.valueOf(enchantment.getName()).toLowerCase();
    }

    private static String attributeName(Object attribute) {
        try {
            Object key = attribute.getClass().getMethod("getKey").invoke(attribute);
            return (String) key.getClass().getMethod("getKey").invoke(key);
        } catch (Throwable ignored) {
            return String.valueOf(attribute).toLowerCase();
        }
    }

    private static String amount(AttributeModifier modifier) {
        double amount = modifier.getAmount();
        String number = amount == Math.rint(amount) ? String.valueOf((long) amount) : String.valueOf(amount);
        String sign = amount >= 0 ? "+" : "";
        switch (modifier.getOperation()) {
            case ADD_SCALAR:
                return sign + number + " (x base)";
            case MULTIPLY_SCALAR_1:
                return sign + number + " (x total)";
            default:
                return sign + number;
        }
    }
}
