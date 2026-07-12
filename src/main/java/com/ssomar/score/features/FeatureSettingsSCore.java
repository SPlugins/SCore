package com.ssomar.score.features;

import com.ssomar.score.SCore;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.config.Locale;
import com.ssomar.score.features.lang.*;
import com.ssomar.score.utils.logging.Utils;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public enum FeatureSettingsSCore implements FeatureSettingsInterface {

    HARDNESS(getFeatureSettings("HARDNESS")),
    SPROJECTILE(getFeatureSettings("SPROJECTILE")),
    VARIABLE(getFeatureSettings("VARIABLE")),
    aboveValue(getFeatureSettings("aboveValue")),
    activator(getFeatureSettings("activator")),
    activators(getFeatureSettings("activators")),
    activeTitle(getFeatureSettings("activeTitle", true)),
    playerCanSit(getFeatureSettings("playerCanSit")),
    amount(getFeatureSettings("amount")),
    amplifier(getFeatureSettings("amplifier")),
    armorTrim(getFeatureSettings("armorTrim")),
    aroundBlock(getFeatureSettings("aroundBlock", "AroundBlock")),
    aroundBlockCdts(getFeatureSettings("aroundBlockCdts", "blockAroundCdts")),
    attribute(getFeatureSettings("attribute")),
    attributes(getFeatureSettings("attributes")),
    bannerSettings(getFeatureSettings("bannerSettings")),
    blockType(getFeatureSettings("blockType")),
    blockTypeMustBe(getFeatureSettings("blockTypeMustBe")),
    blocks(getFeatureSettings("blocks")),
    bounce(getFeatureSettings("bounce")),
    cancelActionEnchantInAnvil(getFeatureSettings("cancelActionEnchantInAnvil", "cancel-enchant-anvil", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelActionRenameInAnvil(getFeatureSettings("cancelActionRenameInAnvil", "cancel-rename-anvil", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelAllCraft(getFeatureSettings("cancelAllCraft", "cancel-item-craft", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelAnvil(getFeatureSettings("cancelAnvil", "cancel-anvil", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelArmorStand(getFeatureSettings("cancelArmorStand", "cancel-armorstand", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelBeacon(getFeatureSettings("cancelBeacon", "cancel-beacon", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelBrewing(getFeatureSettings("cancelBrewing", "cancel-brewing", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelCartography(getFeatureSettings("cancelCartography", "cancel-cartography", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelComposter(getFeatureSettings("cancelComposter", "cancel-composter", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelConsumption(getFeatureSettings("cancelConsumption", "cancel-consumption", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelCraft(getFeatureSettings("cancelCraft", "cancel-item-craft-no-custom", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelDepositInChest(getFeatureSettings("cancelDepositInChest", "cancel-deposit-in-chest", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelDepositInFurnace(getFeatureSettings("cancelDepositInFurnace", "cancel-deposit-in-furnace", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelDispenser(getFeatureSettings("cancelDispenser", "cancel-dispenser", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelDropper(getFeatureSettings("cancelDropper", "cancel-dropper", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelEnchant(getFeatureSettings("cancelEnchant", "cancel-enchant", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelEventIfError(getFeatureSettings("cancelEventIfError", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelEventIfInCooldown(getFeatureSettings("cancelEventIfInCooldown", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelEventIfMaxReached(getFeatureSettings("cancelEventIfMaxReached")),
    cancelEventIfNoPermission(getFeatureSettings("cancelEventIfNoPermission")),
    cancelEventIfNotValid(getFeatureSettings("cancelEventIfNotValid")),
    cancelEvents(getFeatureSettings("cancelEvents")),
    cancelGrindStone(getFeatureSettings("cancelGrindStone", "cancel-grind-stone", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelHopper(getFeatureSettings("cancelHopper", "cancel-hopper", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelHorn(getFeatureSettings("cancelHorn", "cancel-horn", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelHorse(getFeatureSettings("cancelHorse", "cancel-horse", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelItemBurn(getFeatureSettings("cancelItemBurn", "cancel-item-burn", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelItemDeleteByCactus(getFeatureSettings("cancelItemDeleteByCactus", "cancel-item-delete-by-cactus", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelItemDeleteByLightning(getFeatureSettings("cancelItemDeleteByLightning", "cancel-item-delete-by-lightning", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelItemDrop(getFeatureSettings("cancelItemDrop", "cancel-item-drop", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelItemFrame(getFeatureSettings("cancelItemFrame", "cancel-item-frame", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelItemPlace(getFeatureSettings("cancelItemPlace", "cancel-item-place", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelLectern(getFeatureSettings("cancelLectern", "cancel-lectern", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelLoom(getFeatureSettings("cancelLoom", "cancel-loom", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelDecoratedPot(getFeatureSettings("cancelDecoratedPot", "cancel-decorated-pot", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelCrafter(getFeatureSettings("cancelCrafter", "cancel-crafter", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelPlaceInBundle(getFeatureSettings("cancelPlaceInBundle", "cancel-place-in-bundle", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelMerchant(getFeatureSettings("cancelMerchant", "cancel-merchant", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelSmithingTable(getFeatureSettings("cancelSmithingTable", "cancel-smithing-table", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelStoneCutter(getFeatureSettings("cancelStoneCutter", "cancel-stone-cutter", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelSwapHand(getFeatureSettings("cancelSwapHand", "cancel-swap-hand", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelToolInteractions(getFeatureSettings("cancelToolInteractions", "cancel-tool-interactions", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    cancelSpawner(getFeatureSettings("cancelSpawner", "cancel-spawner", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    charged(getFeatureSettings("charged")),
    color(getFeatureSettings("color")),
    containerContent(getFeatureSettings("containerContent")),
    colors(getFeatureSettings("colors")),
    disableBlockingTime(getFeatureSettings("disableBlockingTime")),
    comparator(getFeatureSettings("comparator")),
    cooldown(getFeatureSettings("cooldown")),
    cooldownMsg(getFeatureSettings("cooldownMsg", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    critical(getFeatureSettings("critical")),
    customModelData(getFeatureSettings("customModelData")),
    customName(getFeatureSettings("customName")),
    customNameVisible(getFeatureSettings("customNameVisible")),
    damage(getFeatureSettings("damage")),
    default_double(getFeatureSettings("default_double", "default")),
    default_list(getFeatureSettings("default_list", "default")),
    default_string(getFeatureSettings("default_string", "default", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    delay(getFeatureSettings("delay")),
    delayInTick(getFeatureSettings("delayInTick")),
    despawnDelay(getFeatureSettings("despawnDelay")),
    damagePerAttack(getFeatureSettings("damagePerAttack")),
    detailedBlocks(getFeatureSettings("detailedBlocks")),
    weaponFeatures(getFeatureSettings("weaponFeatures")),
    dyeFeatures(getFeatureSettings("dyeFeatures")),
    dyeColor(getFeatureSettings("dyeColor")),
    sulfurCubeContentFeatures(getFeatureSettings("sulfurCubeContentFeatures")),
    absorbedItem(getFeatureSettings("absorbedItem")),
    detailedEffects(getFeatureSettings("detailedEffects")),
    detailedItems(getFeatureSettings("detailedItems")),
    detailedTargetItems(getFeatureSettings("detailedTargetItems")),
    detailedSlots(getFeatureSettings("detailedSlots")),
    displayCooldownMessage(getFeatureSettings("displayCooldownMessage", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    displayNameDrop(getFeatureSettings("displayNameDrop")),
    dropFeatures(getFeatureSettings("dropFeatures")),
    duration(getFeatureSettings("duration")),
    eastValue(getFeatureSettings("eastValue")),
    effects(getFeatureSettings("effects")),
    enableArmorTrim(getFeatureSettings("enableArmorTrim")),
    enchantment(getFeatureSettings("enchantment")),
    enchantmentWithLevel(getFeatureSettings("enchantmentWithLevel")),
    enchantments(getFeatureSettings("enchantments")),
    entityType(getFeatureSettings("entityType")),
    errorMessage(getFeatureSettings("errorMessage", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    errorMsg(getFeatureSettings("errorMsg")),
    executableItem(getFeatureSettings("executableItem")),
    itemsAdder(getFeatureSettings("itemsAdder")),
    fadeColors(getFeatureSettings("fadeColors")),
    fireworkFeatures(getFeatureSettings("fireworkFeatures")),
    fireworkExplosion(getFeatureSettings("fireworkExplosion")),
    fireworkExplosions(getFeatureSettings("fireworkExplosions")),
    hasTrail(getFeatureSettings("hasTrail")),
    hasTwinkle(getFeatureSettings("hasTwinkle")),
    for_(getFeatureSettings("for_", "for")),
    giveFirstJoin(getFeatureSettings("giveFirstJoin")),
    giveFirstJoinAmount(getFeatureSettings("giveFirstJoinAmount")),
    giveFirstJoinFeatures(getFeatureSettings("giveFirstJoinFeatures")),
    giveFirstJoinSlot(getFeatureSettings("giveFirstJoinSlot")),
    glowDrop(getFeatureSettings("glowDrop")),
    glowDropColor(getFeatureSettings("glowDropColor", true)),
    glowing(getFeatureSettings("glowing")),
    gravity(getFeatureSettings("gravity")),
    hasExecutableItem(getFeatureSettings("hasExecutableItem")),
    hasIcon(getFeatureSettings("hasIcon")),
    hasItem(getFeatureSettings("hasItem")),
    hasParticles(getFeatureSettings("hasParticles")),
    headDBID(getFeatureSettings("headDBID")),
    headFeatures(getFeatureSettings("headFeatures")),
    headValue(getFeatureSettings("headValue")),
    hideArmorTrim(getFeatureSettings("hideArmorTrim")),
    hideAttributes(getFeatureSettings("hideAttributes")),
    hideDestroys(getFeatureSettings("hideDestroys")),
    hideDye(getFeatureSettings("hideDye")),
    hideEnchantments(getFeatureSettings("hideEnchantments")),
    hidePlacedOn(getFeatureSettings("hidePlacedOn")),
    hideAdditionalTooltip(getFeatureSettings("hideAdditionalTooltip")),
    //TODO find a way to fix this typo and accept both
    hideTooltip(getFeatureSettings("hideTooltip", "hideToolTip")),
    hidePotionEffects(getFeatureSettings("hidePotionEffects")),
    hideUnbreakable(getFeatureSettings("hideUnbreakable")),
    hideUsage(getFeatureSettings("hideUsage")),
    hiders(getFeatureSettings("hiders")),
    icon(getFeatureSettings("icon")),
    ifAdult(getFeatureSettings("ifAdult", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifBaby(getFeatureSettings("ifBaby", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifBlockAge(getFeatureSettings("ifBlockAge", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifBlockLocationX(getFeatureSettings("ifBlockLocationX", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifBlockLocationY(getFeatureSettings("ifBlockLocationY", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifBlockLocationZ(getFeatureSettings("ifBlockLocationZ", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifBlocking(getFeatureSettings("ifBlocking", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifCanBreakTargetedBlock(getFeatureSettings("ifCanBreakTargetedBlock", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifContainerContains(getFeatureSettings("ifContainerContains", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifContainerContainsEI(getFeatureSettings("ifContainerContainsEI", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifContainerContainsSellableItems(getFeatureSettings("ifContainerContainsSellableItems", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifContainerEmpty(getFeatureSettings("ifContainerEmpty", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifContainerNotEmpty(getFeatureSettings("ifContainerNotEmpty", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifCrossbowMustBeCharged(getFeatureSettings("ifCrossbowMustBeCharged", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifCrossbowMustNotBeCharged(getFeatureSettings("ifCrossbowMustNotBeCharged", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifCursorDistance(getFeatureSettings("ifCursorDistance", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifDurability(getFeatureSettings("ifDurability", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifUseCooldown(getFeatureSettings("ifUseCooldown", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifEntityHealth(getFeatureSettings("ifEntityHealth", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifEntityInRegion(getFeatureSettings("ifEntityInRegion",SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifEntityIsInWater(getFeatureSettings("ifEntityIsInWater", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifEntityIsInLava(getFeatureSettings("ifEntityIsInLava", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifEntityAge(getFeatureSettings("ifEntityAge", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifEntityLastDamageCause(getFeatureSettings("ifEntityLastDamageCause", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifEntityVelocity(getFeatureSettings("ifEntityVelocity", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifFlying(getFeatureSettings("ifFlying", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifFromSpawner(getFeatureSettings("ifFromSpawner", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifFrozen(getFeatureSettings("ifFrozen", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifGliding(getFeatureSettings("ifGliding", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifGlowing(getFeatureSettings("ifGlowing", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifHasAI(getFeatureSettings("ifHasAI", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifHasEnchant(getFeatureSettings("ifHasEnchant", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifHasExecutableItems(getFeatureSettings("ifHasExecutableItems", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifHasItems(getFeatureSettings("ifHasItems", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifHasNotEnchant(getFeatureSettings("ifHasNotEnchant", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifHasNotExecutableItems(getFeatureSettings("ifHasNotExecutableItems", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifHasNotItems(getFeatureSettings("ifHasNotItems", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifHasPermission(getFeatureSettings("ifHasPermission", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifHasSaddle(getFeatureSettings("ifHasSaddle", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifHasTag(getFeatureSettings("ifHasTag", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifInBiome(getFeatureSettings("ifInBiome", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifInRegion(getFeatureSettings("ifInRegion", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifInWorld(getFeatureSettings("ifInWorld", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifInvulnerable(getFeatureSettings("ifInvulnerable", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifIsInTheAir(getFeatureSettings("ifIsInTheAir", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifIsInTheBlock(getFeatureSettings("ifIsInTheBlock", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifIsNotInTheAir(getFeatureSettings("ifIsNotInTheAir", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifIsNotInTheBlock(getFeatureSettings("ifIsNotInTheBlock", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifIsNotOnFire(getFeatureSettings("ifIsNotOnFire", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifIsNotOnTheBlock(getFeatureSettings("ifIsNotOnTheBlock", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifIsOnFire(getFeatureSettings("ifIsOnFire", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifIsOnTheBlock(getFeatureSettings("ifIsOnTheBlock", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifIsPowered(getFeatureSettings("ifIsPowered", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifLightLevel(getFeatureSettings("ifLightLevel", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifMustBeNatural(getFeatureSettings("ifMustBeNatural", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifMustBeNotNatural(getFeatureSettings("ifMustBeNotNatural", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifMustBeNotPowered(getFeatureSettings("ifMustBeNotPowered", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifMustBeEB(getFeatureSettings("ifMustBeEB", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifName(getFeatureSettings("ifName", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifNamed(getFeatureSettings("ifNamed", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifNeedPlayerConfirmation(getFeatureSettings("ifNeedPlayerConfirmation", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifNoPlayerMustBeOnTheBlock(getFeatureSettings("ifNoPlayerMustBeOnTheBlock", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifNotBlocking(getFeatureSettings("ifNotBlocking", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifNotEntityType(getFeatureSettings("ifNotEntityType", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifNotFlying(getFeatureSettings("ifNotFlying", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifNotFromSpawner(getFeatureSettings("ifNotFromSpawner", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifNotGliding(getFeatureSettings("ifNotGliding", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifNotHasAI(getFeatureSettings("ifNotHasAI", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifNotHasPermission(getFeatureSettings("ifNotHasPermission", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifNotHasSaddle(getFeatureSettings("ifNotHasSaddle", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifNotHasTag(getFeatureSettings("ifNotHasTag", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifNotInBiome(getFeatureSettings("ifNotInBiome", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifNotInRegion(getFeatureSettings("ifNotInRegion", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifNotInWorld(getFeatureSettings("ifNotInWorld", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifNotNamed(getFeatureSettings("ifNotNamed", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifNotOwnerOfTheEI(getFeatureSettings("ifNotOwnerOfTheEI", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifNotSneaking(getFeatureSettings("ifNotSneaking", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifNotSprinting(getFeatureSettings("ifNotSprinting", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifNotStunned(getFeatureSettings("ifNotStunned", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifNotSwimming(getFeatureSettings("ifNotSwimming", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifNotTamed(getFeatureSettings("ifNotTamed", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifNotTargetBlock(getFeatureSettings("ifNotTargetBlock", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifNotEntityInRegion(getFeatureSettings("ifNotEntityInRegion",SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    input1(getFeatureSettings("input1")),
    input2(getFeatureSettings("input2")),
    input3(getFeatureSettings("input3")),
    input4(getFeatureSettings("input4")),
    input5(getFeatureSettings("input5")),
    input6(getFeatureSettings("input6")),
    input7(getFeatureSettings("input7")),
    input8(getFeatureSettings("input8")),
    input9(getFeatureSettings("input9")),
    ifOnFire(getFeatureSettings("ifOnFire", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifOwnerOfTheEI(getFeatureSettings("ifOwnerOfTheEI", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPlantFullyGrown(getFeatureSettings("ifPlantFullyGrown", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPlantNotFullyGrown(getFeatureSettings("ifPlantNotFullyGrown", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPlayerEXP(getFeatureSettings("ifPlayerEXP", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPlayerFoodLevel(getFeatureSettings("ifPlayerFoodLevel", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPlayerHasEffect(getFeatureSettings("ifPlayerHasEffect", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPlayerHasEffectEquals(getFeatureSettings("ifPlayerHasEffectEquals", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPlayerHealth(getFeatureSettings("ifPlayerHealth", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPlayerLevel(getFeatureSettings("ifPlayerLevel", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPlayerMounts(getFeatureSettings("ifPlayerMounts", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPlayerMustBeInHisTown(getFeatureSettings("ifPlayerMustBeInHisTown", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPlayerMustBeOnHisClaim(getFeatureSettings("ifPlayerMustBeOnHisClaim", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPlayerMustBeOnHisClaimOrWilderness(getFeatureSettings("ifPlayerMustBeOnHisClaimOrWilderness", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPlayerMustBeOnHisIsland(getFeatureSettings("ifPlayerMustBeOnHisIsland", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPlayerMustBeOnHisPlot(getFeatureSettings("ifPlayerMustBeOnHisPlot", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPlayerMustBeOnTheBlock(getFeatureSettings("ifPlayerMustBeOnTheBlock", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPlayerNotHasEffect(getFeatureSettings("ifPlayerNotHasEffect", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPlayerNotMounts(getFeatureSettings("ifPlayerNotMounts", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPlayerIsRiding(getFeatureSettings("ifPlayerIsRiding", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPlayerLastDamage(getFeatureSettings("ifPlayerLastDamage", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPlayerOxygen(getFeatureSettings("ifPlayerOxygen", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPlayerBedSpawnLocationX(getFeatureSettings("ifPlayerBedSpawnLocationX", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPlayerBedSpawnLocationY(getFeatureSettings("ifPlayerBedSpawnLocationY", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPlayerBedSpawnLocationZ(getFeatureSettings("ifPlayerBedSpawnLocationZ", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPosX(getFeatureSettings("ifPosX", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPosY(getFeatureSettings("ifPosY", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPosZ(getFeatureSettings("ifPosZ", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPowered(getFeatureSettings("ifPowered", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifSheepColor(getFeatureSettings("ifSheepColor", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifSneaking(getFeatureSettings("ifSneaking", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifSprinting(getFeatureSettings("ifSprinting", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifStunned(getFeatureSettings("ifStunned", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifSwimming(getFeatureSettings("ifSwimming", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifTamed(getFeatureSettings("ifTamed", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifTargetBlock(getFeatureSettings("ifTargetBlock", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifUsage(getFeatureSettings("ifUsage", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifWeather(getFeatureSettings("ifWeather", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifWorldTime(getFeatureSettings("ifWorldTime", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    incendiary(getFeatureSettings("incendiary")),
    invisible(getFeatureSettings("invisible")),
    isAmbient(getFeatureSettings("isAmbient")),
    isCooldownInTicks(getFeatureSettings("isCooldownInTicks", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    itemCheckerType(getFeatureSettings("itemCheckerType")),
    itemCheckers(getFeatureSettings("itemCheckers")),
    input1ItemCheckers(getFeatureSettings("input1ItemCheckers")),
    input2ItemCheckers(getFeatureSettings("input2ItemCheckers")),
    input3ItemCheckers(getFeatureSettings("input3ItemCheckers")),
    input4ItemCheckers(getFeatureSettings("input4ItemCheckers")),
    input5ItemCheckers(getFeatureSettings("input5ItemCheckers")),
    input6ItemCheckers(getFeatureSettings("input6ItemCheckers")),
    input7ItemCheckers(getFeatureSettings("input7ItemCheckers")),
    input8ItemCheckers(getFeatureSettings("input8ItemCheckers")),
    input9ItemCheckers(getFeatureSettings("input9ItemCheckers")),

    items(getFeatureSettings("items")),
    knockbackStrength(getFeatureSettings("knockbackStrength")),
    level(getFeatureSettings("level")),
    lifeTime(getFeatureSettings("lifeTime")),
    lockedInventory(getFeatureSettings("lockedInventory", "locked-in-inventory")),
    loop(getFeatureSettings("loop")),
    magicID(getFeatureSettings("magicID")),
    material(getFeatureSettings("material")),
    materialAndTags(getFeatureSettings("materialAndTags")),
    maxUsePerDay(getFeatureSettings("maxUsePerDay", true)),
    messageIfMaxReached(getFeatureSettings("messageIfMaxReached")),
    messageIfNotValid(getFeatureSettings("messageIfNotValid")),
    messageIfNotValidForTarget(getFeatureSettings("messageIfNotValidForTarget")),
    modification_double(getFeatureSettings("modification_double", "modification")),
    modification_string(getFeatureSettings("modification_string", "modification")),
    multiChoices(getFeatureSettings("multiChoices", "multi-choices")),
    name(getFeatureSettings("name")),
    northValue(getFeatureSettings("northValue")),
    notExecutableItem(getFeatureSettings("notExecutableItem")),
    object(getFeatureSettings("object")),
    operation(getFeatureSettings("operation")),
    part1(getFeatureSettings("part1")),
    part2(getFeatureSettings("part2")),
    particle(getFeatureSettings("particle")),
    particles(getFeatureSettings("particles")),
    particlesAmount(getFeatureSettings("particlesAmount")),
    particlesDelay(getFeatureSettings("particlesDelay")),
    particlesDensity(getFeatureSettings("particlesDensity")),
    particlesOffSet(getFeatureSettings("particlesOffSet")),
    particlesSpeed(getFeatureSettings("particlesSpeed")),
    particlesType(getFeatureSettings("particlesType")),
    pattern(getFeatureSettings("pattern")),
    patterns(getFeatureSettings("patterns")),
    pausePlaceholdersConditions(getFeatureSettings("pausePlaceholdersConditions")),
    pauseWhenOffline(getFeatureSettings("pauseWhenOffline", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    enableVisualCooldown(getFeatureSettings("enableVisualCooldown", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    period(getFeatureSettings("period")),
    periodInTicks(getFeatureSettings("periodInTicks")),
    pickupStatus(getFeatureSettings("pickupStatus")),
    pierceLevel(getFeatureSettings("pierceLevel")),
    placeholderCondition(getFeatureSettings("placeholderCondition")),
    placeholderConditionCmds(getFeatureSettings("placeholderConditionCmds")),
    placeholdersConditions(getFeatureSettings("placeholdersConditions")),
    potionColor(getFeatureSettings("potionColor")),
    potionEffect(getFeatureSettings("potionEffect")),
    potionEffectType(getFeatureSettings("potionEffectType")),
    potionEffects(getFeatureSettings("potionEffects")),
    potionExtended(getFeatureSettings("potionExtended")),
    potionFeatures(getFeatureSettings("potionFeatures")),
    potionType(getFeatureSettings("potionType")),
    potionUpgraded(getFeatureSettings("potionUpgraded")),
    radius(getFeatureSettings("radius")),
    redstoneColor(getFeatureSettings("redstoneColor")),
    removeWhenHitBlock(getFeatureSettings("removeWhenHitBlock")),
    requiredExecutableItem(getFeatureSettings("requiredExecutableItem", true)),
    requiredExecutableItems(getFeatureSettings("requiredExecutableItems", true)),
    requiredExperience(getFeatureSettings("requiredExperience", true)),
    requiredGroups(getFeatureSettings("requiredGroups")),
    requiredItem(getFeatureSettings("requiredItem", false)),
    requiredItems(getFeatureSettings("requiredItems", true)),
    requiredLevel(getFeatureSettings("requiredLevel", true)),
    requiredMagic(getFeatureSettings("requiredMagic", true)),
    requiredMagics(getFeatureSettings("requiredMagics", true)),
    requiredMana(getFeatureSettings("requiredMana", true)),
    requiredMoney(getFeatureSettings("requiredMoney", true)),
    restrictions(getFeatureSettings("restrictions")),
    silent(getFeatureSettings("silent")),
    slot(getFeatureSettings("slot")),
    southValue(getFeatureSettings("southValue")),
    stopCheckingOtherConditionsIfNotValid(getFeatureSettings("stopCheckingOtherConditionsIfNotValid")),
    string(getFeatureSettings("string")),
    subPattern(getFeatureSettings("subPattern")),
    subPatterns(getFeatureSettings("subPatterns")),
    tags(getFeatureSettings("tags")),
    title(getFeatureSettings("title")),
    titleAdjustment(getFeatureSettings("titleAdjustment")),
    titleFeatures(getFeatureSettings("titleFeatures", true)),
    sitFeatures(getFeatureSettings("sitFeatures")),
    storageFeatures(getFeatureSettings("storageFeatures")),
    trimMaterial(getFeatureSettings("trimMaterial")),
    type(getFeatureSettings("type")),
    underValue(getFeatureSettings("underValue")),
    usageConditions(getFeatureSettings("usageConditions")),
    usePerDay(getFeatureSettings("usePerDay", true)),
    uuid(getFeatureSettings("uuid")),
    variable(getFeatureSettings("variable")),
    variableName(getFeatureSettings("variableName")),
    variableUpdate(getFeatureSettings("variableUpdate")),
    variables(getFeatureSettings("variables")),
    variablesModification(getFeatureSettings("variablesModification")),
    velocity(getFeatureSettings("velocity")),
    visualFire(getFeatureSettings("visualFire")),
    visualItem(getFeatureSettings("visualItem")),
    westValue(getFeatureSettings("westValue")),
    bookFeatures(getFeatureSettings("bookFeatures")),
    usageFeatures(getFeatureSettings("usageFeatures")),
    myFurnitureFeatures(getFeatureSettings("myFurnitureFeatures")),
    pages(getFeatureSettings("pages")),
    author(getFeatureSettings("author")),
    equippableFeatures(getFeatureSettings("equippableFeatures")),
    enableSound(getFeatureSettings("enableSound")),
    sound(getFeatureSettings("sound")),
    equipModel(getFeatureSettings("equipModel")),
    cameraOverlay(getFeatureSettings("cameraOverlay")),
    checkAmount(getFeatureSettings("checkAmount")),
    checkDisplayName(getFeatureSettings("checkDisplayName")),
    checkMaterial(getFeatureSettings("checkMaterial")),
    checkCustomModelData(getFeatureSettings("checkCustomModelData", true)),
    checkLore(getFeatureSettings("checkLore", true)),
    checkDurability(getFeatureSettings("checkDurability", true)),
    checkExecutableItemID(getFeatureSettings("checkExecutableItemID")),
    checkExecutableItemUsage(getFeatureSettings("checkExecutableItemUsage", true)),
    checkExecutableItemVariables(getFeatureSettings("checkExecutableItemVariables", true)),
    anvilMergeType(getFeatureSettings("anvilMergeType")),
    itemCommands(getFeatureSettings("itemCommands")),
    damageableOnHurt(getFeatureSettings("damageableOnHurt")),
    dispensable(getFeatureSettings("dispensable")),
    swappable(getFeatureSettings("swappable")),
    allowedEntities(getFeatureSettings("allowedEntities")),
    repairableFeatures(getFeatureSettings("repairableFeatures")),
    repairCost(getFeatureSettings("repairCost")),
    repairableItems(getFeatureSettings("repairableItems")),
    glider(getFeatureSettings("glider")),
    itemModel(getFeatureSettings("itemModel")),
    tooltipModel(getFeatureSettings("tooltipModel")),
    toolRules(getFeatureSettings("toolRules")),
    toolRule(getFeatureSettings("toolRule")),
    miningSpeed(getFeatureSettings("miningSpeed")),
    enable(getFeatureSettings("enable")),
    defaultMiningSpeed(getFeatureSettings("defaultMiningSpeed")),
    correctForDrops(getFeatureSettings("correctForDrops")),
    materials(getFeatureSettings("blocks")),
    damagePerBlock(getFeatureSettings("damagePerBlock")),
    instrumentFeatures(getFeatureSettings("instrumentFeatures")),
    instrument(getFeatureSettings("instrument")),
    chargedProjectiles(getFeatureSettings("chargedProjectiles")),
    hitSound(getFeatureSettings("hitSound")),
    useCooldownFeatures(getFeatureSettings("useCooldownFeatures")),
    cooldownGroup(getFeatureSettings("cooldownGroup")),
    vanillaUseCooldown(getFeatureSettings("vanillaUseCooldown")),
    typeTarget(getFeatureSettings("typeTarget")),
    detailedClick(getFeatureSettings("detailedClick")),
    detailedInput(getFeatureSettings("detailedInput")),
    usageModification(getFeatureSettings("usageModification", true)),
    cancelEvent(getFeatureSettings("cancelEvent")),
    noActivatorRunIfTheEventIsCancelled(getFeatureSettings("noActivatorRunIfTheEventIsCancelled")),
    silenceOutput(getFeatureSettings("silenceOutput", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    mustBeAProjectileLaunchWithTheSameEI(getFeatureSettings("mustBeAProjectileLaunchWithTheSameEI", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    desactiveDrops(getFeatureSettings("desactiveDrops", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    option(getFeatureSettings("option")),
    cooldownFeatures(getFeatureSettings("cooldownFeatures")),
    globalCooldownFeatures(getFeatureSettings("globalCooldownFeatures")),
    detailedEntities(getFeatureSettings("detailedEntities")),
    detailedTargetEntities(getFeatureSettings("detailedTargetEntities")),
    detailedTargetBlocks(getFeatureSettings("detailedTargetBlocks")),
    detailedDamageCauses(getFeatureSettings("detailedDamageCauses")),
    detailedCommands(getFeatureSettings("detailedCommands")),
    detailedMessagesContains(getFeatureSettings("detailedMessagesContains")),
    detailedMessagesEquals(getFeatureSettings("detailedMessagesEquals")),
    detailedInventories(getFeatureSettings("detailedInventories")),
    mustBeItsOwnInventory(getFeatureSettings("mustBeItsOwnInventory")),
    targetCommands(getFeatureSettings("targetCommands")),
    entityCommands(getFeatureSettings("entityCommands")),
    blockCommands(getFeatureSettings("blockCommands")),
    playerCommands(getFeatureSettings("playerCommands")),
    playerSettings(getFeatureSettings("playerSettings")),
    ownerCommands(getFeatureSettings("ownerCommands")),
    consoleCommands(getFeatureSettings("consoleCommands")),
    targetPlayerCommands(getFeatureSettings("targetPlayerCommands")),
    targetEntityCommands(getFeatureSettings("targetEntityCommands")),
    targetBlockCommands(getFeatureSettings("targetBlockCommands")),
    targetItemCommands(getFeatureSettings("targetItemCommands")),
    displayCommands(getFeatureSettings("displayCommands")),
    playerConditions(getFeatureSettings("playerConditions")),
    targetConditions(getFeatureSettings("targetConditions")),
    entityConditions(getFeatureSettings("entityConditions")),
    blockConditions(getFeatureSettings("blockConditions")),
    worldConditions(getFeatureSettings("worldConditions")),
    itemConditions(getFeatureSettings("itemConditions")),
    customConditions(getFeatureSettings("customConditions")),
    ownerConditions(getFeatureSettings("ownerConditions")),
    targetBlockConditions(getFeatureSettings("targetBlockConditions")),
    targetPlayerConditions(getFeatureSettings("targetPlayerConditions")),
    targetEntityConditions(getFeatureSettings("targetEntityConditions")),
    displayConditions_conditions(getFeatureSettings("displayConditions_conditions", "displayConditions", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    containsMineInCube(getFeatureSettings("containsMineInCube")),
    playerCooldownFeatures(getFeatureSettings("playerCooldownFeatures")),
    entityCooldownFeatures(getFeatureSettings("entityCooldownFeatures")),
    isRefreshableClean(getFeatureSettings("isRefreshableClean")),
    parsePlaceholders(getFeatureSettings("parsePlaceholders", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    refreshTag(getFeatureSettings("refreshTag", "refreshTagDoNotEdit")),
    otherEICooldowns(getFeatureSettings("otherEICooldowns")),
    cooldown_activators(getFeatureSettings("activators")),
    canBeUsedOnlyByTheOwner(getFeatureSettings("canBeUsedOnlyByTheOwner", true)),
    cancelEventIfNotOwner(getFeatureSettings("cancelEventIfNotOwner")),
    onlyOwnerBlackListedActivators(getFeatureSettings("onlyOwnerBlackListedActivators")),
    EXECUTABLEITEM(getFeatureSettings("EXECUTABLEITEM")),
    lore(getFeatureSettings("lore")),
    glow(getFeatureSettings("glow")),
    glowDuration(getFeatureSettings("glowDuration")),
    disableEnchantGlide(getFeatureSettings("disableEnchantGlide")),
    disableStack(getFeatureSettings("disableStack", true)),
    customStackSize(getFeatureSettings("customStackSize", true)),
    keepItemOnDeath(getFeatureSettings("keepItemOnDeath")),
    storeItemInfo(getFeatureSettings("storeItemInfo")),
    keepDefaultAttributes(getFeatureSettings("keepDefaultAttributes")),
    ignoreKeepDefaultAttributesFeature(getFeatureSettings("ignoreKeepDefaultAttributesFeature")),
    unbreakable(getFeatureSettings("unbreakable")),
    usage(getFeatureSettings("usage")),
    usageLimit(getFeatureSettings("usageLimit", true)),
    customModelData_ei(getFeatureSettings("customModelData")),
    whitelistedWorlds(getFeatureSettings("whitelistedWorlds")),
    armorColor(getFeatureSettings("armorColor")),
    fireworkColor(getFeatureSettings("fireworkColor")),
    recognitions(getFeatureSettings("recognitions", true, SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    foodFeatures(getFeatureSettings("foodFeatures")),
    nutrition(getFeatureSettings("nutrition")),
    maxFurnitureRendered(getFeatureSettings("maxFurnitureRendered")),
    viewDistanceFurniture(getFeatureSettings("viewDistanceFurniture")),
    maxDistanceBoundingBoxCalculation(getFeatureSettings("maxDistanceBoundingBoxCalculation")),
    saturation(getFeatureSettings("saturation")),
    isMeat(getFeatureSettings("isMeat")),
    canAlwaysEat(getFeatureSettings("canAlwaysEat")),
    eatSeconds(getFeatureSettings("eatSeconds")),
    blockState(getFeatureSettings("blockState")),
    blockStatePlus(getFeatureSettings("blockStatePlus")),
    bundleContent(getFeatureSettings("bundleContent")),
    itemRarity(getFeatureSettings("itemRarity")),
    rarity(getFeatureSettings("rarity")),
    enableRarity(getFeatureSettings("enableRarity")),
    durability_features(getFeatureSettings("durability")),
    maxDurability(getFeatureSettings("maxDurability")),
    durability(getFeatureSettings("durability")),
    isDurabilityBasedOnUsage(getFeatureSettings("isDurabilityBasedOnUsage")),
    displayConditions(getFeatureSettings("displayConditions")),
    enableFeature(getFeatureSettings("enableFeature")),
    nbt(getFeatureSettings("nbt")),
    autoUpdateFeatures(getFeatureSettings("autoUpdateFeatures")),
    autoUpdateItem(getFeatureSettings("autoUpdateItem")),
    updateMaterial(getFeatureSettings("updateMaterial", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    updateName(getFeatureSettings("updateName", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    updateLore(getFeatureSettings("updateLore", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    updateDurability(getFeatureSettings("updateDurability", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    updateAttributes(getFeatureSettings("updateAttributes", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    updateEnchants(getFeatureSettings("updateEnchants", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    updateCustomModelData(getFeatureSettings("updateCustomModelData", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    updateArmorSettings(getFeatureSettings("updateArmorSettings", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    updateHiders(getFeatureSettings("updateHiders", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    updateEquippable(getFeatureSettings("updateEquippable", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    updateTooltipModel(getFeatureSettings("updateTooltipModel", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    brewingStandFeatures(getFeatureSettings("brewingStandFeatures", true)),
    brewingStandSpeed(getFeatureSettings("brewingStandSpeed", true)),
    boundingBoxZones(getFeatureSettings("boundingBoxZones")),
    boundingBoxZone(getFeatureSettings("boundingBoxZone")),
    from(getFeatureSettings("from")),
    to(getFeatureSettings("to")),
    containerFeatures(getFeatureSettings("containerFeatures")),
    whitelistMaterials(getFeatureSettings("whitelistMaterials")),
    blacklistMaterials(getFeatureSettings("blacklistMaterials")),
    isLocked(getFeatureSettings("isLocked")),
    lockedName(getFeatureSettings("lockedName")),
    inventoryTitle(getFeatureSettings("inventoryTitle")),
    furnaceFeatures(getFeatureSettings("furnaceFeatures", true)),
    furnaceSpeed(getFeatureSettings("furnaceSpeed", true)),
    infiniteFuel(getFeatureSettings("infiniteFuel", true)),
    infiniteVisualLit(getFeatureSettings("infiniteVisualLit", true)),
    fortuneChance(getFeatureSettings("fortuneChance", true)),
    fortuneMultiplier(getFeatureSettings("fortuneMultiplier", true)),
    directionalFeatures(getFeatureSettings("directionalFeatures")),
    forceBlockFaceOnPlace(getFeatureSettings("forceBlockFaceOnPlace")),
    blockFaceOnPlace(getFeatureSettings("blockFaceOnPlace")),
    hopperFeatures(getFeatureSettings("hopperFeatures", true)),
    amountItemsTransferred(getFeatureSettings("amountItemsTransferred", true)),
    chiseledBookshelfFeatures(getFeatureSettings("chiseledBookshelfFeatures")),
    occupiedSlots(getFeatureSettings("occupiedSlots")),
    InteractionZoneFeatures(getFeatureSettings("InteractionZoneFeatures")),
    width(getFeatureSettings("width")),
    height(getFeatureSettings("height")),
    isCollidable(getFeatureSettings("isCollidable")),
    DisplayFeatures(getFeatureSettings("DisplayFeatures")),
    scale(getFeatureSettings("scale")),
    aligned(getFeatureSettings("aligned")),
    customPitch(getFeatureSettings("customPitch")),
    customY(getFeatureSettings("customY")),
    clickToBreak(getFeatureSettings("clickToBreak")),
    EXECUTABLEBLOCK(getFeatureSettings("EXECUTABLEBLOCK")),
    EXECUTABLEBLOCKPLACED(getFeatureSettings("EXECUTABLEBLOCKPLACED")),
    EXECUTABLEEVENT(getFeatureSettings("EXECUTABLEEVENT")),
    FURNITURE(getFeatureSettings("FURNITURE")),
    FURNITUREPLACED(getFeatureSettings("FURNITUREPLACED")),
    RECIPE(getFeatureSettings("RECIPE")),
    RECIPEGROUP(getFeatureSettings("RECIPEGROUP")),
    creationType(getFeatureSettings("creationType")),
    spawnerType(getFeatureSettings("spawnerType")),
    spawnerFeatures(getFeatureSettings("spawnerFeatures")),
    spawnDelay(getFeatureSettings("spawnDelay")),
    minSpawnDelay(getFeatureSettings("minSpawnDelay")),
    maxSpawnDelay(getFeatureSettings("maxSpawnDelay")),
    spawnCount(getFeatureSettings("spawnCount")),
    spawnRange(getFeatureSettings("spawnRange")),
    requiredPlayerRange(getFeatureSettings("requiredPlayerRange")),
    maxNearbyEntities(getFeatureSettings("maxNearbyEntities")),
    potentialSpawns(getFeatureSettings("potentialSpawns")),
    addSpawnerNbtToItem(getFeatureSettings("addSpawnerNbtToItem")),
    dropType(getFeatureSettings("dropType")),
    cancelGravity(getFeatureSettings("cancelGravity")),
    cancelLiquidDestroy(getFeatureSettings("cancelLiquidDestroy")),
    onlyBreakableWithEI(getFeatureSettings("onlyBreakableWithEI")),
    dropBlockIfItIsBroken(getFeatureSettings("dropBlockIfItIsBroken")),
    dropBlockWhenItExplodes(getFeatureSettings("dropBlockWhenItExplodes")),
    dropBlockWhenItBurns(getFeatureSettings("dropBlockWhenItBurns")),
    resetInternalDatasWhenBroken(getFeatureSettings("resetInternalDatasWhenBroken")),
    canBeMoved(getFeatureSettings("canBeMoved")),
    itemsAdderID(getFeatureSettings("itemsAdderID")),
    oraxenID(getFeatureSettings("oraxenID")),
    oraxen(getFeatureSettings("oraxen")),
    nexoID(getFeatureSettings("nexoID")),
    nexo(getFeatureSettings("nexo")),
    myfurnitureID(getFeatureSettings("myfurnitureID")),
    interactionRange(getFeatureSettings("interactionRange")),
    enabled(getFeatureSettings("enabled")),
    editorIcon(getFeatureSettings("editorIcon")),
    disabledWorlds(getFeatureSettings("disabledWorlds")),
    displayFeatures(getFeatureSettings("displayFeatures")),
    blockLight(getFeatureSettings("blockLight")),
    skyLight(getFeatureSettings("skyLight")),
    glowColor(getFeatureSettings("glowColor")),
    TRADE(getFeatureSettings("TRADE")),
    description(getFeatureSettings("description")),
    delayOfTrade(getFeatureSettings("delayOfTrade")),
    glowWhenTrade(getFeatureSettings("glowWhenTrade")),
    requiredObject(getFeatureSettings("requiredObject")),
    SCREEN(getFeatureSettings("SCREEN")),
    SCREENUNIT(getFeatureSettings("SCREENUNIT")),
    leverUUID(getFeatureSettings("leverUUID")),
    interactionUUID(getFeatureSettings("interactionUUID")),
    rotation(getFeatureSettings("rotation")),
    glowWhenPowered(getFeatureSettings("glowWhenPowered")),
    EXECUTABLELEVER(getFeatureSettings("EXECUTABLELEVER")),
    startDate(getFeatureSettings("startDate")),
    endDate(getFeatureSettings("endDate")),
    when(getFeatureSettings("when")),
    animation(getFeatureSettings("animation")),
    hasConsumeParticles(getFeatureSettings("hasConsumeParticles")),
    consumeSeconds(getFeatureSettings("consumeSeconds")),
    consumableFeatures(getFeatureSettings("consumableFeatures")),
    scheduleFeatures(getFeatureSettings("scheduleFeatures")),
    viewRange(getFeatureSettings("viewRange")),
    recipesList(getFeatureSettings("recipesList")),
    typeOfCraftingTableRecipe(getFeatureSettings("typeOfCraftingTableRecipe")),
    cookingTime(getFeatureSettings("cookingTime")),
    experience(getFeatureSettings("experience")),
    result(getFeatureSettings("result")),
    recipeType(getFeatureSettings("recipeType")),
    damageReductions(getFeatureSettings("damageReductions")),
    damageReduction(getFeatureSettings("damageReduction")),
    baseDamageBlocked(getFeatureSettings("baseDamageBlocked")),
    factorDamageBlocked(getFeatureSettings("factorDamageBlocked")),
    horizontalBlockingAngle(getFeatureSettings("horizontalBlockingAngle")),
    damageTypes(getFeatureSettings("damageTypes")),
    blockAttacksFeatures(getFeatureSettings("blockAttacksFeatures")),
    blockDelay(getFeatureSettings("blockDelay")),
    blockSound(getFeatureSettings("blockSound")),
    disableSound(getFeatureSettings("disableSound")),
    disableCooldownScale(getFeatureSettings("disableCooldownScale")),
    bypassedBy(getFeatureSettings("bypassedBy")),
    itemTextures(getFeatureSettings("itemTextures")),
    itemAdvancedComponents(getFeatureSettings("itemAdvancedComponents")),
    ifGameMode(getFeatureSettings("ifGameMode", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifNearbyEntityCount(getFeatureSettings("ifNearbyEntityCount", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifLineOfSight(getFeatureSettings("ifLineOfSight", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifPlayerSpeed(getFeatureSettings("ifPlayerSpeed", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    ifNearbyPlayerCount(getFeatureSettings("ifNearbyPlayerCount", SavingVerbosityLevel.SAVE_ONLY_WHEN_DIFFERENT_DEFAULT)),
    suspiciousBlockLoot(getFeatureSettings("suspiciousBlockLoot")),
    papiParser(getFeatureSettings("papiParser"))

    ;

    private FeatureSettingsInterface settingsInterface;

    FeatureSettingsSCore(FeatureSettingsInterface settingsInterface) {
        this.settingsInterface = settingsInterface;
    }

    public static FeatureSettingsInterface[] getValues() {
        Locale locale = GeneralConfig.getInstance().getLocale();
        return getValues(locale);
    }

    public static FeatureSettingsInterface[] getValues(Locale locale) {
        switch (locale) {
            case FR:
                return FeatureSettingsSCoreFR.values();
            case EN:
                return FeatureSettingsSCoreEN.values();
            case ES:
                return FeatureSettingsSCoreES.values();
            case DE:
                return FeatureSettingsSCoreDE.values();
            case RU:
                return FeatureSettingsSCoreRU.values();
            case ZH:
                return FeatureSettingsSCoreZH.values();
            case ID:
                return FeatureSettingsSCoreID.values();
            case AR:
                return FeatureSettingsSCoreAR.values();
            case PT:
                return FeatureSettingsSCorePT.values();
            case HI:
                return FeatureSettingsSCoreHI.values();
            case IT:
                return FeatureSettingsSCoreIT.values();
            case PL:
                return FeatureSettingsSCorePL.values();
            case VI:
                return FeatureSettingsSCoreVI.values();
            default:
                return FeatureSettingsSCoreEN.values();
        }
    }


    public static FeatureSettingsInterface getFeatureSettings(String identifier, @Nullable String configName, boolean requirePremium, SavingVerbosityLevel savingVerbosityLevel) {
        if (configName == null) configName = identifier;
        FeatureSettingsInterface[] values = getValues();
        for (FeatureSettingsInterface value : values) {
            if (value.getIdentifier().equals(identifier)) {
                value.setName(configName);
                value.setRequirePremium(requirePremium);
                value.setSavingVerbosityLevel(savingVerbosityLevel);
                return value;
            }
        }
        Utils.sendConsoleMsg(SCore.NAME_COLOR + " &cNo feature Translation found for setting: &6" + identifier + " &cin &6" + GeneralConfig.getInstance().getLocale().name());

        /* try in english */
        return getEnglishFeatureSettings(identifier, configName, requirePremium, savingVerbosityLevel);
    }

    public static FeatureSettingsInterface getEnglishFeatureSettings(String identifier, @Nullable String configName, boolean requirePremium, SavingVerbosityLevel savingVerbosityLevel) {
        if (configName == null) configName = identifier;
        FeatureSettingsInterface[] values = FeatureSettingsSCoreEN.values();
        for (FeatureSettingsInterface value : values) {
            if (value.getIdentifier().equals(identifier)) {
                value.setName(configName);
                value.setRequirePremium(requirePremium);
                value.setSavingVerbosityLevel(savingVerbosityLevel);
                return value;
            }
        }
        throw new IllegalArgumentException("No feature settings found for config name: " + identifier);
    }

    public static String getAllNonTranslated() {
        StringBuilder sb = new StringBuilder();

        for (Locale locale : Locale.values()) {
            if (locale == Locale.EN) continue;
            sb.append("For Locale: " + locale.name() + " (" + locale.getName() + ")\n");
            int i = 0;
            for (FeatureSettingsSCore feature : FeatureSettingsSCore.values()) {
                FeatureSettingsInterface[] values = getValues(locale);
                boolean found = false;
                for (FeatureSettingsInterface value : values) {
                    if (value.getIdentifier().equals(feature.getIdentifier())) {
                        found = true;
                        break;
                    }
                }
                if (found) continue;
                if (i == 0) {
                    sb.append("Translations not found\n")
                            .append("You are an expert in this language and you can help us to translate it.\n")
                            .append("The settings you will translate are from Minecraft Plugins.\n")
                            .append("Avoid to translate the plugin name: SCore, ExecutableItems, EI, ExecutableEvents, EE, MyFurniture, MF, CustomPiglinsTrades, CPT, ExecutableBlocks, EB, ExecutableCrafting, EC.\n")
                            .append("The format of settings are IDENTIFIER(IDENTIFIER_STR, NAME_STR, DESCRIPTION_TAB_STR, ...)\n")
                            .append("You have to translate only the NAME and the DESCRIPTION, the other things must be the same.\n")
                            .append("You need to translate the following settings:\n");
                }
                i++;
                sb.append(getBrutEnglishLineFromClass(feature.getIdentifier()));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public static String getBrutEnglishLineFromClass(String identifier) {
        // Read class file from the jar
        StringBuilder sb = new StringBuilder();
        try {
            Class<?> clazz = FeatureSettingsSCoreEN.class;
            String className = clazz.getName().replace('.', '/') + "_FLAT.txt";
            InputStream inputStream = clazz.getClassLoader().getResourceAsStream(className);
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().startsWith(identifier + "(")) {
                        sb.append(line).append("\n");
                    }
                }
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static void reload() {
        for (FeatureSettingsSCore feature : FeatureSettingsSCore.values()) {
            feature.settingsInterface = getFeatureSettings(feature.getIdentifier(), feature.getName(), feature.getSavingVerbosityLevel());
        }
    }

    public void setFeatureSettingsInterface(FeatureSettingsInterface featureSettingsInterface) {
        this.settingsInterface = featureSettingsInterface;
    }

    public static FeatureSettingsInterface getFeatureSettings(String identifier) {
        return getFeatureSettings(identifier, null, false, SavingVerbosityLevel.SAVE_ALWAYS);
    }

    public static FeatureSettingsInterface getFeatureSettings(String identifier, SavingVerbosityLevel savingVerbosityLevel) {
        return getFeatureSettings(identifier, null, false, savingVerbosityLevel);
    }

    public static FeatureSettingsInterface getFeatureSettings(String identifier, @Nullable String configName) {
        return getFeatureSettings(identifier, configName, false, SavingVerbosityLevel.SAVE_ALWAYS);
    }

    public static FeatureSettingsInterface getFeatureSettings(String identifier, @Nullable String configName, SavingVerbosityLevel savingVerbosityLevel) {
        return getFeatureSettings(identifier, configName, false, savingVerbosityLevel);
    }

    public static FeatureSettingsInterface getFeatureSettings(String identifier, boolean requirePremium) {
        return getFeatureSettings(identifier, null, requirePremium, SavingVerbosityLevel.SAVE_ALWAYS);
    }

    public static FeatureSettingsInterface getFeatureSettings(String identifier, boolean requirePremium, SavingVerbosityLevel savingVerbosityLevel) {
        return getFeatureSettings(identifier, null, requirePremium, savingVerbosityLevel);
    }

    @Override
    public String getIdentifier() {
        return this.name();
    }

    @Override
    public String getName() {
        return settingsInterface.getName();
    }

    @Override
    public void setName(String name) {
        settingsInterface.setName(name);
    }

    @Override
    public String getEditorName() {
        return settingsInterface.getEditorName();
    }

    @Override
    public String[] getEditorDescription() {
        String[] lines = settingsInterface.getEditorDescription();
        // Add into the last line the config name
        String[] newLines = new String[lines.length + 1];
        System.arraycopy(lines, 0, newLines, 0, lines.length);
        newLines[lines.length] = "&8&o(" + settingsInterface.getName() + ")";
        return newLines;
    }

    @Override
    public String[] getEditorDescriptionBrut() {
        String[] lines = settingsInterface.getEditorDescription();
        return lines;
    }

    @Override
    public Material getEditorMaterial() {
        if (settingsInterface.getEditorMaterial() == null) return Material.LEVER;
        return settingsInterface.getEditorMaterial();
    }

    @Override
    public boolean isRequirePremium() {
        return settingsInterface.isRequirePremium();
    }

    @Override
    public void setRequirePremium(boolean requirePremium) {
        settingsInterface.setRequirePremium(requirePremium);
    }

    @Override
    public SavingVerbosityLevel getSavingVerbosityLevel() {
        return settingsInterface.getSavingVerbosityLevel();
    }

    @Override
    public void setSavingVerbosityLevel(SavingVerbosityLevel savingVerbosityLevel) {
        settingsInterface.setSavingVerbosityLevel(savingVerbosityLevel);
    }
}
