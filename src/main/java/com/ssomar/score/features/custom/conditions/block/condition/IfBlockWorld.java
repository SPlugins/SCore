package com.ssomar.score.features.custom.conditions.block.condition;

import com.ssomar.score.features.FeatureParentInterface;
import com.ssomar.score.features.FeatureSettingsSCore;
import com.ssomar.score.features.custom.conditions.block.BlockConditionFeature;
import com.ssomar.score.features.custom.conditions.block.BlockConditionRequest;
import com.ssomar.score.features.types.list.ListWorldFeature;

import java.util.ArrayList;

public class IfBlockWorld extends BlockConditionFeature<ListWorldFeature, IfBlockWorld> {

    public IfBlockWorld(FeatureParentInterface parent) {
        super(parent, FeatureSettingsSCore.ifBlockWorld);
    }

    @Override
    public boolean verifCondition(BlockConditionRequest request) {
        if (hasCondition() && !getCondition().getValue().contains(request.getBlock().getWorld().getName())) {
            runInvalidCondition(request);
            return false;
        }
        return true;
    }

    @Override
    public void subReset() {
        setCondition(new ListWorldFeature(getParent(),  new ArrayList<>(), FeatureSettingsSCore.ifBlockWorld));
    }

    @Override
    public boolean hasCondition() {
        return getCondition().getValue().size() > 0;
    }

    @Override
    public IfBlockWorld getNewInstance(FeatureParentInterface newParent) {
        return new IfBlockWorld(newParent);
    }

    @Override
    public IfBlockWorld getValue() {
        return this;
    }
}
