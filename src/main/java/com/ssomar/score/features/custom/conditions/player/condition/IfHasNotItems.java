package com.ssomar.score.features.custom.conditions.player.condition;

import com.ssomar.score.SCore;
import com.ssomar.score.features.FeatureParentInterface;
import com.ssomar.score.features.FeatureSettingsSCore;
import com.ssomar.score.features.custom.conditions.player.PlayerConditionFeature;
import com.ssomar.score.features.custom.conditions.player.PlayerConditionRequest;
import com.ssomar.score.features.custom.ifhas.items.group.HasItemGroupFeature;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class IfHasNotItems extends PlayerConditionFeature<HasItemGroupFeature, IfHasNotItems> {

    public IfHasNotItems(FeatureParentInterface parent) {
        super(parent, FeatureSettingsSCore.ifHasNotItems);
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
        boolean verif = (!getCondition().verifHasNot(items, player.getInventory().getHeldItemSlot()));
        //SsomarDev.testMsg("ifHasNotItems >>"+hasCondition()+" >> "+verif, true);
        if (hasCondition() && verif) {
            //SsomarDev.testMsg("ifHasNotItems >> PASSE FALSE ", true);
            runInvalidCondition(request);
            return false;
        }
        //SsomarDev.testMsg("ifHasNotItems >>>>>>>>>>>>>>  "+verif, true);
        return true;
    }

    @Override
    public IfHasNotItems getValue() {
        return this;
    }

    @Override
    public void subReset() {
        setCondition(new HasItemGroupFeature(getParent(), FeatureSettingsSCore.ifHasNotItems, true));
    }

    @Override
    public boolean hasCondition() {
        return !getCondition().getHasItems().isEmpty();
    }

    @Override
    public IfHasNotItems getNewInstance(FeatureParentInterface parent) {
        return new IfHasNotItems(parent);
    }
}
