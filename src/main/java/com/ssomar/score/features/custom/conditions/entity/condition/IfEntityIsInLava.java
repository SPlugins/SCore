package com.ssomar.score.features.custom.conditions.entity.condition;

import com.ssomar.score.features.FeatureParentInterface;
import com.ssomar.score.features.FeatureSettingsSCore;
import com.ssomar.score.features.custom.conditions.entity.EntityConditionFeature;
import com.ssomar.score.features.custom.conditions.entity.EntityConditionRequest;
import com.ssomar.score.features.types.BooleanFeature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.Material;

public class IfEntityIsInLava extends EntityConditionFeature<BooleanFeature, IfEntityIsInLava> {

    public IfEntityIsInLava(FeatureParentInterface parent) {
        super(parent, FeatureSettingsSCore.ifEntityIsInLava);
    }

    @Override
    public boolean verifCondition(EntityConditionRequest request) {
        Entity entity = request.getEntity();
        if (getCondition().getValue(request.getSp()) && !isInLava(entity)) {
            runInvalidCondition(request);
            return false;
        }
        return true;
    }

    private static boolean isLava(Material m) {
        return m == Material.LAVA || m.name().equals("STATIONARY_LAVA");
    }

    /** Entity#isInLava() is Paper only: on Spigot, the block at the entity's feet or eyes */
    private static boolean isInLava(Entity entity) {
        try {
            return entity.isInLava();
        } catch (NoSuchMethodError e) {
            if (isLava(entity.getLocation().getBlock().getType())) return true;
            return entity instanceof LivingEntity && isLava(((LivingEntity) entity).getEyeLocation().getBlock().getType());
        }
    }

    @Override
    public IfEntityIsInLava getValue() {
        return this;
    }

    @Override
    public void subReset() {
        setCondition(new BooleanFeature(getParent(), false, FeatureSettingsSCore.ifEntityIsInLava));
    }

    @Override
    public boolean hasCondition() {
        return getCondition().isConfigured();
    }

    @Override
    public IfEntityIsInLava getNewInstance(FeatureParentInterface parent) {
        return new IfEntityIsInLava(parent);
    }
}
