package com.ssomar.score.features.custom.conditions.player.condition;

import com.ssomar.score.SCore;
import com.ssomar.score.features.FeatureParentInterface;
import com.ssomar.score.features.FeatureSettingsSCore;
import com.ssomar.score.features.custom.conditions.player.PlayerConditionFeature;
import com.ssomar.score.features.custom.conditions.player.PlayerConditionRequest;
import com.ssomar.score.features.custom.ifhas.executableitems.group.HasExecutableItemGroupFeature;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class IfHasNotExecutableItems extends PlayerConditionFeature<HasExecutableItemGroupFeature, IfHasNotExecutableItems> {

    public IfHasNotExecutableItems(FeatureParentInterface parent) {
        super(parent, FeatureSettingsSCore.ifHasNotExecutableItems);
    }

    @Override
    public boolean verifCondition(PlayerConditionRequest request) {
        /* Checked first: the inventory contents were copied for every activator, condition set or not */
        if (!hasCondition()) return true;
        Player player = request.getPlayer();
        ItemStack[] items = player.getInventory().getContents();
        if(SCore.is1v11Less()){
            ItemStack [] armorContents = player.getInventory().getArmorContents();
            // Merge the two arrays
            ItemStack[] all = new ItemStack[items.length + armorContents.length];
            System.arraycopy(items, 0, all, 0, items.length);
            System.arraycopy(armorContents, 0, all, items.length, armorContents.length);
            items = all;
        }

        if (hasCondition() && !getCondition().verifHasNot(items, player.getInventory().getHeldItemSlot())) {
            runInvalidCondition(request);
            return false;
        }
        return true;
    }

    @Override
    public IfHasNotExecutableItems getValue() {
        return this;
    }

    @Override
    public void subReset() {
        setCondition(new HasExecutableItemGroupFeature(getParent(), FeatureSettingsSCore.ifHasNotExecutableItems, true));
    }

    @Override
    public boolean hasCondition() {
        return !getCondition().getHasExecutableItems().isEmpty();
    }

    @Override
    public IfHasNotExecutableItems getNewInstance(FeatureParentInterface parent) {
        return new IfHasNotExecutableItems(parent);
    }
}
