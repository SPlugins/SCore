package com.ssomar.score.features.custom.conditions.player.condition;

import com.ssomar.score.features.FeatureParentInterface;
import com.ssomar.score.features.FeatureSettingsSCore;
import com.ssomar.score.features.custom.conditions.player.PlayerConditionFeature;
import com.ssomar.score.features.custom.conditions.player.PlayerConditionRequest;
import com.ssomar.score.features.types.BooleanFeature;
import com.ssomar.score.utils.safebreak.SafeBreak;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

public class IfCanBreakTargetedBlock extends PlayerConditionFeature<BooleanFeature, IfCanBreakTargetedBlock> {

    public IfCanBreakTargetedBlock(FeatureParentInterface parent) {
        super(parent, FeatureSettingsSCore.ifCanBreakTargetedBlock);
    }

    @Override
    public boolean verifCondition(PlayerConditionRequest request) {
        if (getCondition().getValue(request.getSp())) {
            Player player = request.getPlayer();
            Block block = blockOfEvent(request.getEvent());
            // Activators without a block (sneak, write command, death…) used to test whatever the player
            // happened to look at: the block of the event is preferred when there is one.
            if (block == null) block = player.getTargetBlock(null, 5);
            if (!SafeBreak.verifSafeBreak(player.getUniqueId(), block)) {
                runInvalidCondition(request);
                return false;
            }
        }
        return true;
    }

    @Override
    public IfCanBreakTargetedBlock getValue() {
        return this;
    }

    @Nullable
    static Block blockOfEvent(@Nullable Event event) {
        if (event instanceof BlockEvent) return ((BlockEvent) event).getBlock();
        if (event instanceof PlayerInteractEvent) return ((PlayerInteractEvent) event).getClickedBlock();
        return null;
    }

    @Override
    public void subReset() {
        setCondition(new BooleanFeature(getParent(),  false, FeatureSettingsSCore.ifCanBreakTargetedBlock));
    }

    @Override
    public boolean hasCondition() {
        return getCondition().isConfigured();
    }

    @Override
    public IfCanBreakTargetedBlock getNewInstance(FeatureParentInterface parent) {
        return new IfCanBreakTargetedBlock(parent);
    }
}
