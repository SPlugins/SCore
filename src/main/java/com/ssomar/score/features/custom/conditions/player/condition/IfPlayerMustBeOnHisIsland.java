package com.ssomar.score.features.custom.conditions.player.condition;

import com.ssomar.score.SCore;
import com.ssomar.score.features.FeatureParentInterface;
import com.ssomar.score.features.FeatureSettingsSCore;
import com.ssomar.score.features.custom.conditions.player.PlayerConditionFeature;
import com.ssomar.score.features.custom.conditions.player.PlayerConditionRequest;
import com.ssomar.score.features.types.BooleanFeature;
import com.ssomar.score.usedapi.BentoBoxAPI;
import com.ssomar.score.usedapi.FabledSkyblockTool;
import com.ssomar.score.usedapi.IridiumSkyblockTool;
import com.ssomar.score.usedapi.SuperiorSkyblockTool;
import org.bukkit.entity.Player;

public class IfPlayerMustBeOnHisIsland extends PlayerConditionFeature<BooleanFeature, IfPlayerMustBeOnHisIsland> {

    public IfPlayerMustBeOnHisIsland(FeatureParentInterface parent) {
        super(parent, FeatureSettingsSCore.ifPlayerMustBeOnHisIsland);
    }

    @Override
    public boolean verifCondition(PlayerConditionRequest request) {
        Player player = request.getPlayer();
        if (SCore.hasIridiumSkyblock) {
            if (getCondition().getValue(request.getSp())) {
                if (!IridiumSkyblockTool.playerIsOnHisIsland(player)) {
                    runInvalidCondition(request);
                    return false;
                }

            }
        } else if (SCore.hasSuperiorSkyblock2) {
            if (hasCondition()) {
                if (!SuperiorSkyblockTool.playerIsOnHisIsland(player)) {
                    runInvalidCondition(request);
                    return false;
                }
            }
        } else if (SCore.hasBentoBox) {
            if (hasCondition()) {
                if (!BentoBoxAPI.playerIsOnHisIsland(player)) {
                    runInvalidCondition(request);
                    return false;
                }
            }
        } else if (SCore.hasFabledSkyblock) {
            if (hasCondition()) {
                if (!FabledSkyblockTool.playerIsOnHisIsland(player)) {
                    runInvalidCondition(request);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public IfPlayerMustBeOnHisIsland getValue() {
        return this;
    }

    @Override
    public void subReset() {
        setCondition(new BooleanFeature(getParent(),  false, FeatureSettingsSCore.ifPlayerMustBeOnHisIsland));
    }

    @Override
    public boolean hasCondition() {
        return getCondition().isConfigured();
    }

    @Override
    public IfPlayerMustBeOnHisIsland getNewInstance(FeatureParentInterface parent) {
        return new IfPlayerMustBeOnHisIsland(parent);
    }
}
