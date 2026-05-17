package com.ssomar.score.features.custom.itemcheckers;

import com.ssomar.score.SCore;
import com.ssomar.score.features.FeatureSettingsInterface;
import com.ssomar.score.features.FeatureSettingsSCore;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;

public enum ItemCheckerEnum {

    AMOUNT(FeatureSettingsSCore.checkAmount),
    DISPLAY_NAME(FeatureSettingsSCore.checkDisplayName),
    MATERIAL(FeatureSettingsSCore.checkMaterial),

    CUSTOM_MODEL_DATA(FeatureSettingsSCore.checkCustomModelData),
    LORE(FeatureSettingsSCore.checkLore),

    DURABILITY(FeatureSettingsSCore.checkDurability),

    EXECUTABLEITEM_ID(FeatureSettingsSCore.checkExecutableItemID),
    EXECUTABLEITEM_USAGE(FeatureSettingsSCore.checkExecutableItemUsage),

    EXECUTABLEITEM_VARIABLES(FeatureSettingsSCore.checkExecutableItemVariables);

    @Getter
    public final FeatureSettingsInterface featureSetting;

    public boolean deprecated = false;

    ItemCheckerEnum(FeatureSettingsInterface featureSetting) {
        this.featureSetting = featureSetting;
    }

    ItemCheckerEnum(FeatureSettingsInterface featureSetting, boolean deprecated) {
        this.featureSetting = featureSetting;
        this.deprecated = deprecated;
    }

    public boolean check(ItemStack item1, ItemStack item2) {

        boolean item1HasMeta = item1.hasItemMeta();
        boolean item2HasMeta = item2.hasItemMeta();
        boolean oneOfThemNaNoMeta = !item1HasMeta || !item2HasMeta;
        boolean bothNaNoMeta = !item1HasMeta && !item2HasMeta;
        ItemMeta item1Meta = item1HasMeta ? item1.getItemMeta() : null;
        ItemMeta item2Meta = item2HasMeta ? item2.getItemMeta() : null;

        switch (this) {
            case AMOUNT:
                return item1.getAmount() == item2.getAmount();
            case DISPLAY_NAME:
                if(oneOfThemNaNoMeta) return bothNaNoMeta;

                boolean item1HasDisplayName = item1Meta.hasDisplayName();
                boolean item2HasDisplayName = item2Meta.hasDisplayName();
                boolean oneOfThemNaNoDisplayName = !item1HasDisplayName || !item2HasDisplayName;
                boolean bothNaNoDisplayName = !item1HasDisplayName && !item2HasDisplayName;
                if(oneOfThemNaNoDisplayName) return bothNaNoDisplayName;

                return item1Meta.getDisplayName().equals(item2Meta.getDisplayName());
            case MATERIAL:
                return item1.getType() == item2.getType();
            case CUSTOM_MODEL_DATA:
                if(oneOfThemNaNoMeta) return bothNaNoMeta;

                boolean item1HasCustomModelData = item1Meta.hasCustomModelData();
                boolean item2HasCustomModelData = item2Meta.hasCustomModelData();
                boolean oneOfThemNaNoCustomModelData = !item1HasCustomModelData || !item2HasCustomModelData;
                boolean bothNaNoCustomModelData = !item1HasCustomModelData && !item2HasCustomModelData;
                if(oneOfThemNaNoCustomModelData) return bothNaNoCustomModelData;

                return item1Meta.getCustomModelData() == item2Meta.getCustomModelData();
            case LORE:
                if(oneOfThemNaNoMeta) return bothNaNoMeta;

                boolean item1HasLore = item1Meta.hasLore();
                boolean item2HasLore = item2Meta.hasLore();
                boolean oneOfThemNaNoLore = !item1HasLore || !item2HasLore;
                boolean bothNaNoLore = !item1HasLore && !item2HasLore;
                if(oneOfThemNaNoLore) return bothNaNoLore;

                return item1Meta.getLore().equals(item2Meta.getLore());
            case DURABILITY:
                return item1.getDurability() == item2.getDurability();
            case EXECUTABLEITEM_ID:
                if(oneOfThemNaNoMeta) return bothNaNoMeta;
                String eiID1 = item1Meta.getPersistentDataContainer().get(new NamespacedKey("executableitems", "ei-id"), PersistentDataType.STRING);
                String eiID2 = item2Meta.getPersistentDataContainer().get(new NamespacedKey("executableitems", "ei-id"), PersistentDataType.STRING);
                if (eiID1 == null || eiID2 == null) return false;
                return eiID1.equals(eiID2);
            case EXECUTABLEITEM_USAGE:
                if(oneOfThemNaNoMeta) return bothNaNoMeta;
                Integer eiUsage1 = item1Meta.getPersistentDataContainer().get(new NamespacedKey("score", "usage"), PersistentDataType.INTEGER);
                Integer eiUsage2 = item2Meta.getPersistentDataContainer().get(new NamespacedKey("score", "usage"), PersistentDataType.INTEGER);
                if (eiUsage1 == null || eiUsage2 == null) return false;
                return eiUsage1.equals(eiUsage2);
            case EXECUTABLEITEM_VARIABLES:
                if(oneOfThemNaNoMeta) return bothNaNoMeta;
                // So far, ItemStack variables in PDC are written as: score:score-<var>
                // And there's no formal way to specifically get those. I also don't wanna try using ExecutableItemObject because
                // right now, it seems stupid to enforce the rule to loaded/valid ids only
                try {
                    return extractScoreVariables(item1Meta.getPersistentDataContainer())
                            .equals(extractScoreVariables(item2Meta.getPersistentDataContainer()));
                } catch (NullPointerException e) {
                    return false;
                }
                
        }
        return false;
    }

    /**
     * This method is for comparing the contents of 2 ItemStacks. Since EXECUTABLEITEM_VARIABLES only really care about
     * item variables made from SCore, filter the pdc nbts accordingly.
     * @param pdc
     * @return
     */
    private HashMap<NamespacedKey, Object> extractScoreVariables(PersistentDataContainer pdc) {
        HashMap<NamespacedKey, Object> variables = new HashMap<>();
        for (NamespacedKey key : pdc.getKeys()) {
            if (key.toString().startsWith("score:score-")) {
                variables.put(key, tryToGetObjectFromPDC(pdc, key));
            }
        }
        return variables;
    }

    /**
     * You can get the keys of the PDC but you can't know the datatype of the value pairs
     * so the best option is to make attempts in guessing the datatype.
     * <br/><br/>
     * ItemStack variables only come in STRING, DOUBLE and STRING_ARRAY and nothing more
     * @param pdc
     * @return
     */
    private Object tryToGetObjectFromPDC(PersistentDataContainer pdc, NamespacedKey key) {
        if (pdc.has(key, PersistentDataType.STRING))
            return pdc.get(key, PersistentDataType.STRING);
        if (pdc.has(key, PersistentDataType.DOUBLE))
            return pdc.get(key, PersistentDataType.DOUBLE);
        if (pdc.has(key, PersistentDataType.LIST.strings()))
            return pdc.get(key, PersistentDataType.LIST.strings());

        // Will never be reached but just in case
        SCore.plugin.getLogger().warning("[!] A line in ItemCheckerEnum for SCore has been reached but not meant to. Contact Special70 about this.");
        SCore.plugin.getLogger().warning("[!] NamespacedKey => "+key.toString());
        return null;
    }

}
