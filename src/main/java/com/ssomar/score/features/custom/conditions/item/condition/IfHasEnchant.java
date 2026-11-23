package com.ssomar.score.features.custom.conditions.item.condition;

import com.ssomar.score.features.FeatureParentInterface;
import com.ssomar.score.features.FeatureSettingsSCore;
import com.ssomar.score.features.custom.conditions.item.ItemConditionFeature;
import com.ssomar.score.features.custom.conditions.item.ItemConditionRequest;
import com.ssomar.score.features.types.list.ListEnchantAndLevelFeature;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

public class IfHasEnchant extends ItemConditionFeature<ListEnchantAndLevelFeature, IfHasEnchant> {

    public IfHasEnchant(FeatureParentInterface parent) {
        super(parent, FeatureSettingsSCore.ifHasEnchant);
    }

    @Override
    public boolean verifCondition(ItemConditionRequest request) {

        if (hasCondition()) {
            ItemStack itemStack = request.getItemStack();
            ItemMeta itemMeta = null;
            boolean hasItemMeta = itemStack.hasItemMeta();
            if (hasItemMeta) itemMeta = itemStack.getItemMeta();

            if (!hasItemMeta) {
                runInvalidCondition(request);
                return false;
            }
            /* Each enchantment at one of the levels written for it, at any level if written without level */
            if (!ListEnchantAndLevelFeature.hasAll(itemMeta.getEnchants(), getCondition().getLevels())) {
                runInvalidCondition(request);
                return false;
            }
        }

        return true;
    }

    @Override
    public IfHasEnchant getValue() {
        return this;
    }

    @Override
    public void subReset() {
        setCondition(new ListEnchantAndLevelFeature(this,  new HashMap<>(), FeatureSettingsSCore.ifHasEnchant, true));
    }

    @Override
    public boolean hasCondition() {
        return !getCondition().getLevels().isEmpty();
    }

    @Override
    public IfHasEnchant getNewInstance(FeatureParentInterface parent) {
        return new IfHasEnchant(parent);
    }
}
