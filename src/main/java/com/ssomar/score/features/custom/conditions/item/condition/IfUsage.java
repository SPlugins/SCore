package com.ssomar.score.features.custom.conditions.item.condition;

import com.ssomar.executableitems.executableitems.ExecutableItemObject;
import com.ssomar.score.features.FeatureParentInterface;
import com.ssomar.score.features.FeatureSettingsSCore;
import com.ssomar.score.features.custom.conditions.item.ItemConditionFeature;
import com.ssomar.score.features.custom.conditions.item.ItemConditionRequest;
import com.ssomar.score.features.types.NumberConditionFeature;
import com.ssomar.score.utils.strings.StringCalculation;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class IfUsage extends ItemConditionFeature<NumberConditionFeature, IfUsage> {


    public IfUsage(FeatureParentInterface parent) {
        super(parent, FeatureSettingsSCore.ifUsage);
    }

    @Override
    public boolean verifCondition(ItemConditionRequest request) {

        /* Checked first: reading the ExecutableItem of the stack copies its meta, for every activator of every item */
        if (!hasCondition()) return true;

        ItemStack itemStack = request.getItemStack();
        Optional<Player> playerOpt = request.getPlayerOpt();
        ExecutableItemObject executableItem = new ExecutableItemObject(itemStack);
        if (executableItem.isValid()) {
            executableItem.loadExecutableItemInfos();
            if (!StringCalculation.calculation(getCondition().getValue(playerOpt, request.getSp()).get(), executableItem.getUsage())) {
                runInvalidCondition(request);
                return false;
            }
        }

        return true;
    }

    @Override
    public IfUsage getValue() {
        return this;
    }

    @Override
    public void subReset() {
        setCondition(new NumberConditionFeature(this, FeatureSettingsSCore.ifUsage));
    }

    @Override
    public boolean hasCondition() {
        return getCondition().getValue().isPresent();
    }

    @Override
    public IfUsage getNewInstance(FeatureParentInterface parent) {
        return new IfUsage(parent);
    }
}
