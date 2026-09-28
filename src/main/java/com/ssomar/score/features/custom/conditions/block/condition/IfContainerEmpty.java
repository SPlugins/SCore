package com.ssomar.score.features.custom.conditions.block.condition;

import com.ssomar.score.features.FeatureParentInterface;
import com.ssomar.score.features.FeatureSettingsSCore;
import com.ssomar.score.features.custom.conditions.block.BlockConditionFeature;
import com.ssomar.score.features.custom.conditions.block.BlockConditionRequest;
import com.ssomar.score.features.types.BooleanFeature;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;

public class IfContainerEmpty extends BlockConditionFeature<BooleanFeature, IfContainerEmpty> {

    public IfContainerEmpty(FeatureParentInterface parent) {
        super(parent, FeatureSettingsSCore.ifContainerEmpty);
    }

    @Override
    public boolean verifCondition(BlockConditionRequest request) {
        /* getState() snapshots the whole container inventory NBT (expensive on shulkers with
         * heavy items): skip it when the condition is off, and call it exactly once. */
        if (!getCondition().getValue(request.getSp())) return true;

        Block b = request.getBlock();
        BlockState state = b.getState();
        if (state instanceof Container) {
            Inventory inv = ((Container) state).getInventory();
            if (!inv.isEmpty()) {
                runInvalidCondition(request);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasCondition() {
        return getCondition().isConfigured();
    }

    @Override
    public IfContainerEmpty getValue() {
        return this;
    }


    @Override
    public void subReset() {
        setCondition(new BooleanFeature(getParent(), false, FeatureSettingsSCore.ifContainerEmpty));
    }

    @Override
    public IfContainerEmpty getNewInstance(FeatureParentInterface parent) {
        return new IfContainerEmpty(parent);
    }
}
