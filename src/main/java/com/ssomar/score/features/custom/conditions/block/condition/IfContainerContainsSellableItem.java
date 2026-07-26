package com.ssomar.score.features.custom.conditions.block.condition;

import com.ssomar.score.SCore;
import com.ssomar.score.features.FeatureParentInterface;
import com.ssomar.score.features.FeatureSettingsSCore;
import com.ssomar.score.features.custom.conditions.block.BlockConditionFeature;
import com.ssomar.score.features.custom.conditions.block.BlockConditionRequest;
import com.ssomar.score.features.types.BooleanFeature;
import com.ssomar.score.usedapi.ShopGUIPlusTool;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class IfContainerContainsSellableItem extends BlockConditionFeature<BooleanFeature, IfContainerContainsSellableItem> {

    public IfContainerContainsSellableItem(FeatureParentInterface parent) {
        super(parent, FeatureSettingsSCore.ifContainerContainsSellableItems);
    }

    @Override
    public boolean verifCondition(BlockConditionRequest request) {

        if (hasCondition()) {
            Optional<Player> playerOpt = request.getPlayerOpt();
            Block b = request.getBlock();
            if(playerOpt.isPresent()) {
                /* Single getState() call: it snapshots the whole container inventory NBT. */
                BlockState state = b.getState();
                if (state instanceof Container) {
                    Container container = (Container) state;
                    Inventory inv = container.getInventory();
                    for (int i = 0; i < inv.getSize(); i++) {
                        ItemStack item = inv.getItem(i);
                        if (item != null && SCore.hasShopGUIPlus) {
                            double check = ShopGUIPlusTool.sellItem(playerOpt.get(), item);
                            if (check > 0) {
                                return true;
                            }
                        }
                    }
                }
            }

            runInvalidCondition(request);
            return false;
        }
        return true;
    }

    @Override
    public boolean hasCondition() {
        return getCondition().getValue();
    }

    @Override
    public IfContainerContainsSellableItem getValue() {
        return this;
    }

    @Override
    public void subReset() {
        setCondition(new BooleanFeature(getParent(),  false, FeatureSettingsSCore.ifContainerContainsSellableItems));
    }

    @Override
    public IfContainerContainsSellableItem getNewInstance(FeatureParentInterface parent) {
        return new IfContainerContainsSellableItem(parent);
    }
}
