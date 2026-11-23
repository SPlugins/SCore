package com.ssomar.score.features.custom.restrictions;

import com.ssomar.score.features.FeatureSettingsInterface;
import com.ssomar.score.features.FeatureSettingsSCore;
import lombok.Getter;

public enum RestrictionEnum {
    CANCEL_DROP(FeatureSettingsSCore.cancelItemDrop),
    CANCEL_PLACE(FeatureSettingsSCore.cancelItemPlace),

    CANCEL_TOOL_INTERACTIONS(FeatureSettingsSCore.cancelToolInteractions),
    CANCEL_CONSUMPTION(FeatureSettingsSCore.cancelConsumption),

    CANCEL_HORN(FeatureSettingsSCore.cancelHorn),

    CANCEL_CRAFT(FeatureSettingsSCore.cancelCraft),
    CANCEL_ALL_CRAFT(FeatureSettingsSCore.cancelAllCraft),

    CANCEL_DEPOSIT_IN_CHEST(FeatureSettingsSCore.cancelDepositInChest),
    CANCEL_DEPOSIT_IN_FURNACE(FeatureSettingsSCore.cancelDepositInFurnace),
    CANCEL_STONE_CUTTER(FeatureSettingsSCore.cancelStoneCutter),
    CANCEL_ENCHANT_TABLE(FeatureSettingsSCore.cancelEnchant),
    CANCEL_ANVIL(FeatureSettingsSCore.cancelAnvil),
    CANCEL_ACTION_RENAME_IN_ANVIL(FeatureSettingsSCore.cancelActionRenameInAnvil),
    CANCEL_ACTION_ENCHANT_IN_ANVIL(FeatureSettingsSCore.cancelActionEnchantInAnvil),

    CANCEL_ARMORSTAND(FeatureSettingsSCore.cancelArmorStand),
    CANCEL_GRIND_STONE(FeatureSettingsSCore.cancelGrindStone),
    CANCEL_ITEM_FRAME(FeatureSettingsSCore.cancelItemFrame),
    CANCEL_SMITHING_TABLE(FeatureSettingsSCore.cancelSmithingTable),
    CANCEL_BREWING(FeatureSettingsSCore.cancelBrewing),
    CANCEL_BEACON(FeatureSettingsSCore.cancelBeacon),
    CANCEL_CARTOGRAPHY(FeatureSettingsSCore.cancelCartography),
    CANCEL_COMPOSTER(FeatureSettingsSCore.cancelComposter),
    CANCEL_DISPENSER(FeatureSettingsSCore.cancelDispenser),
    CANCEL_DROPPER(FeatureSettingsSCore.cancelDropper),
    CANCEL_HOPPER(FeatureSettingsSCore.cancelHopper),
    CANCEL_LECTERN(FeatureSettingsSCore.cancelLectern),
    CANCEL_LOOM(FeatureSettingsSCore.cancelLoom),
    CANCEL_DECORATED_POT(FeatureSettingsSCore.cancelDecoratedPot),
    CANCEL_SHELF(FeatureSettingsSCore.cancelShelf),
    CANCEL_CRAFTER(FeatureSettingsSCore.cancelCrafter),
    CANCEL_PLACE_IN_BUNDLE(FeatureSettingsSCore.cancelPlaceInBundle),

    CANCEL_MERCHANT(FeatureSettingsSCore.cancelMerchant),
    CANCEL_HORSE(FeatureSettingsSCore.cancelHorse),

    CANCEL_ITEM_BURN(FeatureSettingsSCore.cancelItemBurn),
    CANCEL_ITEM_DELETE_BY_CACTUS(FeatureSettingsSCore.cancelItemDeleteByCactus),
    CANCEL_ITEM_DELETE_BY_LIGHTNING(FeatureSettingsSCore.cancelItemDeleteByLightning),

    CANCEL_SWAPHAND(FeatureSettingsSCore.cancelSwapHand),
    LOCKED_INVENTORY(FeatureSettingsSCore.lockedInventory),
    CANCEL_SPAWNER(FeatureSettingsSCore.cancelSpawner);

    @Getter
    public final FeatureSettingsInterface featureSetting;

    public final boolean deprecated = false;

    RestrictionEnum(FeatureSettingsInterface featureSetting) {
        this.featureSetting = featureSetting;
    }

    RestrictionEnum(FeatureSettingsInterface featureSetting, boolean deprecated) {
        this.featureSetting = featureSetting;
        deprecated = deprecated;
    }

}
