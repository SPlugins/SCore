package com.ssomar.score.features.custom.kineticWeaponFeatures;

import com.ssomar.score.SCore;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.features.*;
import com.ssomar.score.features.editor.GenericFeatureParentEditor;
import com.ssomar.score.features.editor.GenericFeatureParentEditorManager;
import com.ssomar.score.features.types.BooleanFeature;
import com.ssomar.score.features.types.DoubleFeature;
import com.ssomar.score.features.types.IntegerFeature;
import com.ssomar.score.features.types.SoundFeature;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.backward_compatibility.SoundUtils;
import net.kyori.adventure.key.Key;
import com.ssomar.score.utils.emums.ResetSetting;
import com.ssomar.score.utils.logging.Utils;
import com.ssomar.score.utils.strings.StringConverter;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.KineticWeapon;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The kinetic_weapon data component, added in Minecraft 1.21.11 (spears).
 * Drives charge/lunge attacks where damage and effects depend on the player's speed.
 * The three condition sets (dismount / knockback / damage) are flattened into
 * scalar fields for editor simplicity.
 */
@Getter
@Setter
public class KineticWeaponFeatures extends FeatureWithHisOwnEditor<KineticWeaponFeatures, KineticWeaponFeatures, GenericFeatureParentEditor, GenericFeatureParentEditorManager> implements FeatureForItemNewPaperComponents {

    private BooleanFeature enable;
    private IntegerFeature contactCooldownTicks;
    private IntegerFeature delayTicks;
    private DoubleFeature forwardMovement;
    private DoubleFeature damageMultiplier;

    private IntegerFeature dismountMaxDurationTicks;
    private DoubleFeature dismountMinSpeed;
    private DoubleFeature dismountMinRelativeSpeed;

    private IntegerFeature knockbackMaxDurationTicks;
    private DoubleFeature knockbackMinSpeed;
    private DoubleFeature knockbackMinRelativeSpeed;

    private IntegerFeature damageMaxDurationTicks;
    private DoubleFeature damageMinSpeed;
    private DoubleFeature damageMinRelativeSpeed;

    private BooleanFeature enableSound;
    private SoundFeature sound;
    private BooleanFeature enableHitSound;
    private SoundFeature hitSound;

    public KineticWeaponFeatures(FeatureParentInterface parent) {
        super(parent, FeatureSettingsSCore.kineticWeaponFeatures);
        reset();
    }

    @Override
    public void reset() {
        enable = new BooleanFeature(this, false, FeatureSettingsSCore.enable);
        contactCooldownTicks = new IntegerFeature(this, Optional.of(20), FeatureSettingsSCore.contactCooldownTicks);
        delayTicks = new IntegerFeature(this, Optional.of(0), FeatureSettingsSCore.delayTicks);
        forwardMovement = new DoubleFeature(this, Optional.of(1.0), FeatureSettingsSCore.forwardMovement);
        damageMultiplier = new DoubleFeature(this, Optional.of(1.0), FeatureSettingsSCore.damageMultiplier);

        dismountMaxDurationTicks = new IntegerFeature(this, Optional.of(20), FeatureSettingsSCore.dismountMaxDurationTicks);
        dismountMinSpeed = new DoubleFeature(this, Optional.of(0.0), FeatureSettingsSCore.dismountMinSpeed);
        dismountMinRelativeSpeed = new DoubleFeature(this, Optional.of(0.0), FeatureSettingsSCore.dismountMinRelativeSpeed);

        knockbackMaxDurationTicks = new IntegerFeature(this, Optional.of(20), FeatureSettingsSCore.knockbackMaxDurationTicks);
        knockbackMinSpeed = new DoubleFeature(this, Optional.of(0.0), FeatureSettingsSCore.knockbackMinSpeed);
        knockbackMinRelativeSpeed = new DoubleFeature(this, Optional.of(0.0), FeatureSettingsSCore.knockbackMinRelativeSpeed);

        damageMaxDurationTicks = new IntegerFeature(this, Optional.of(20), FeatureSettingsSCore.damageMaxDurationTicks);
        damageMinSpeed = new DoubleFeature(this, Optional.of(0.0), FeatureSettingsSCore.damageMinSpeed);
        damageMinRelativeSpeed = new DoubleFeature(this, Optional.of(0.0), FeatureSettingsSCore.damageMinRelativeSpeed);

        enableSound = new BooleanFeature(this, false, FeatureSettingsSCore.enableSound);
        sound = new SoundFeature(this, Optional.of(Sound.ITEM_SPEAR_LUNGE_1), FeatureSettingsSCore.sound);
        enableHitSound = new BooleanFeature(this, false, FeatureSettingsSCore.enableHitSound);
        hitSound = new SoundFeature(this, Optional.of(Sound.ITEM_SPEAR_HIT), FeatureSettingsSCore.hitSound);
    }

    @Override
    public List<String> load(SPlugin plugin, ConfigurationSection config, boolean isPremiumLoading) {
        List<String> errors = new ArrayList<>();
        if (config.isConfigurationSection(getName())) {
            ConfigurationSection section = config.getConfigurationSection(getName());
            for (FeatureInterface feature : getFeatures()) {
                errors.addAll(feature.load(plugin, section, isPremiumLoading));
            }
        }
        return errors;
    }

    @Override
    public void save(ConfigurationSection config) {
        config.set(getName(), null);
        ConfigurationSection section = config.createSection(getName());
        for (FeatureInterface feature : getFeatures()) {
            feature.save(section);
        }
        if (isSavingOnlyIfDiffDefault() && section.getKeys(false).isEmpty()) {
            config.set(getName(), null);
            return;
        }
        if (GeneralConfig.getInstance().isEnableCommentsInConfig())
            config.setComments(this.getName(), StringConverter.decoloredString(Arrays.asList(getFeatureSettings().getEditorDescriptionBrut())));
    }

    @Override
    public KineticWeaponFeatures getValue() {
        return this;
    }

    @Override
    public KineticWeaponFeatures initItemParentEditor(GUI gui, int slot) {
        int len = 19;
        String[] finalDescription = new String[getEditorDescription().length + len];
        System.arraycopy(getEditorDescription(), 0, finalDescription, 0, getEditorDescription().length);
        finalDescription[finalDescription.length - len] = GUI.CLICK_HERE_TO_CHANGE;
        len--;

        finalDescription[finalDescription.length - len] = "&7Enable: &e" + (enable.getValue() ? "&a&l✔" : "&c&l✘");
        len--;
        finalDescription[finalDescription.length - len] = "&7Contact Cooldown Ticks: &e" + contactCooldownTicks.getValue().get();
        len--;
        finalDescription[finalDescription.length - len] = "&7Delay Ticks: &e" + delayTicks.getValue().get();
        len--;
        finalDescription[finalDescription.length - len] = "&7Forward Movement: &e" + forwardMovement.getValue().get();
        len--;
        finalDescription[finalDescription.length - len] = "&7Damage Multiplier: &e" + damageMultiplier.getValue().get();
        len--;
        finalDescription[finalDescription.length - len] = "&7Dismount - Max Duration Ticks: &e" + dismountMaxDurationTicks.getValue().get();
        len--;
        finalDescription[finalDescription.length - len] = "&7Dismount - Min Speed: &e" + dismountMinSpeed.getValue().get();
        len--;
        finalDescription[finalDescription.length - len] = "&7Dismount - Min Relative Speed: &e" + dismountMinRelativeSpeed.getValue().get();
        len--;
        finalDescription[finalDescription.length - len] = "&7Knockback - Max Duration Ticks: &e" + knockbackMaxDurationTicks.getValue().get();
        len--;
        finalDescription[finalDescription.length - len] = "&7Knockback - Min Speed: &e" + knockbackMinSpeed.getValue().get();
        len--;
        finalDescription[finalDescription.length - len] = "&7Knockback - Min Relative Speed: &e" + knockbackMinRelativeSpeed.getValue().get();
        len--;
        finalDescription[finalDescription.length - len] = "&7Damage - Max Duration Ticks: &e" + damageMaxDurationTicks.getValue().get();
        len--;
        finalDescription[finalDescription.length - len] = "&7Damage - Min Speed: &e" + damageMinSpeed.getValue().get();
        len--;
        finalDescription[finalDescription.length - len] = "&7Damage - Min Relative Speed: &e" + damageMinRelativeSpeed.getValue().get();
        len--;
        finalDescription[finalDescription.length - len] = "&7Enable Sound: &e" + (enableSound.getValue() ? "&a&l✔" : "&c&l✘");
        len--;
        finalDescription[finalDescription.length - len] = "&7Sound: &e" + SoundUtils.getSounds().get(sound.getValue().get());
        len--;
        finalDescription[finalDescription.length - len] = "&7Enable Hit Sound: &e" + (enableHitSound.getValue() ? "&a&l✔" : "&c&l✘");
        len--;
        finalDescription[finalDescription.length - len] = "&7Hit Sound: &e" + SoundUtils.getSounds().get(hitSound.getValue().get());
        len--;

        gui.createItem(getEditorMaterial(), 1, slot, GUI.TITLE_COLOR + getEditorName(), false, false, finalDescription);
        return this;
    }

    @Override
    public boolean isAvailable() {
        return SCore.isPaperOrFork() && SCore.is1v21v11Plus();
    }

    @Override
    public boolean isApplicable(FeatureForItemArgs args) {
        return true;
    }

    @Override
    public void applyOnItemMeta(FeatureForItemArgs args) {
    }

    @Override
    public void loadFromItemMeta(FeatureForItemArgs args) {
    }

    @Override
    public void applyOnItem(@NotNull FeatureForItemArgs args) {
        if (isAvailable() && enable.getValue()) {
            ItemStack item = args.getItem();
            try {
                KineticWeapon.Builder builder = KineticWeapon.kineticWeapon()
                        .contactCooldownTicks(contactCooldownTicks.getValue().get())
                        .delayTicks(delayTicks.getValue().get())
                        .forwardMovement(forwardMovement.getValue().get().floatValue())
                        .damageMultiplier(damageMultiplier.getValue().get().floatValue())
                        .dismountConditions(KineticWeapon.condition(dismountMaxDurationTicks.getValue().get(), dismountMinSpeed.getValue().get().floatValue(), dismountMinRelativeSpeed.getValue().get().floatValue()))
                        .knockbackConditions(KineticWeapon.condition(knockbackMaxDurationTicks.getValue().get(), knockbackMinSpeed.getValue().get().floatValue(), knockbackMinRelativeSpeed.getValue().get().floatValue()))
                        .damageConditions(KineticWeapon.condition(damageMaxDurationTicks.getValue().get(), damageMinSpeed.getValue().get().floatValue(), damageMinRelativeSpeed.getValue().get().floatValue()));
                if (enableSound.getValue() && sound.getValue().isPresent())
                    builder.sound(sound.getValue().get().key());
                if (enableHitSound.getValue() && hitSound.getValue().isPresent())
                    builder.hitSound(hitSound.getValue().get().key());
                item.setData(DataComponentTypes.KINETIC_WEAPON, builder);
            } catch (Exception e) {
                Utils.sendConsoleMsg(SCore.plugin, "&cError while applying the kinetic weapon features on an item");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void loadFromItem(@NotNull FeatureForItemArgs args) {
        if (isAvailable()) {
            ItemStack item = args.getItem();
            if (item.hasData(DataComponentTypes.KINETIC_WEAPON)) {
                KineticWeapon kineticWeapon = item.getData(DataComponentTypes.KINETIC_WEAPON);
                enable.setValue(true);
                contactCooldownTicks.setValue(Optional.of(kineticWeapon.contactCooldownTicks()));
                delayTicks.setValue(Optional.of(kineticWeapon.delayTicks()));
                forwardMovement.setValue(Optional.of((double) kineticWeapon.forwardMovement()));
                damageMultiplier.setValue(Optional.of((double) kineticWeapon.damageMultiplier()));
                loadCondition(kineticWeapon.dismountConditions(), dismountMaxDurationTicks, dismountMinSpeed, dismountMinRelativeSpeed);
                loadCondition(kineticWeapon.knockbackConditions(), knockbackMaxDurationTicks, knockbackMinSpeed, knockbackMinRelativeSpeed);
                loadCondition(kineticWeapon.damageConditions(), damageMaxDurationTicks, damageMinSpeed, damageMinRelativeSpeed);
                Key soundKey = kineticWeapon.sound();
                enableSound.setValue(soundKey != null);
                Sound loadedSound = SoundUtils.getSoundFromKey(soundKey);
                if (loadedSound != null) sound.setValue(Optional.of(loadedSound));
                Key hitSoundKey = kineticWeapon.hitSound();
                enableHitSound.setValue(hitSoundKey != null);
                Sound loadedHitSound = SoundUtils.getSoundFromKey(hitSoundKey);
                if (loadedHitSound != null) hitSound.setValue(Optional.of(loadedHitSound));
            }
        }
    }

    private void loadCondition(KineticWeapon.Condition condition, IntegerFeature maxDurationTicks, DoubleFeature minSpeed, DoubleFeature minRelativeSpeed) {
        if (condition == null) return;
        maxDurationTicks.setValue(Optional.of(condition.maxDurationTicks()));
        minSpeed.setValue(Optional.of((double) condition.minSpeed()));
        minRelativeSpeed.setValue(Optional.of((double) condition.minRelativeSpeed()));
    }

    @Override
    public ResetSetting getResetSetting() {
        return ResetSetting.KINETIC_WEAPON;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {
    }

    @Override
    public KineticWeaponFeatures clone(FeatureParentInterface newParent) {
        KineticWeaponFeatures clone = new KineticWeaponFeatures(newParent);
        clone.setEnable(enable.clone(clone));
        clone.setContactCooldownTicks(contactCooldownTicks.clone(clone));
        clone.setDelayTicks(delayTicks.clone(clone));
        clone.setForwardMovement(forwardMovement.clone(clone));
        clone.setDamageMultiplier(damageMultiplier.clone(clone));
        clone.setDismountMaxDurationTicks(dismountMaxDurationTicks.clone(clone));
        clone.setDismountMinSpeed(dismountMinSpeed.clone(clone));
        clone.setDismountMinRelativeSpeed(dismountMinRelativeSpeed.clone(clone));
        clone.setKnockbackMaxDurationTicks(knockbackMaxDurationTicks.clone(clone));
        clone.setKnockbackMinSpeed(knockbackMinSpeed.clone(clone));
        clone.setKnockbackMinRelativeSpeed(knockbackMinRelativeSpeed.clone(clone));
        clone.setDamageMaxDurationTicks(damageMaxDurationTicks.clone(clone));
        clone.setDamageMinSpeed(damageMinSpeed.clone(clone));
        clone.setDamageMinRelativeSpeed(damageMinRelativeSpeed.clone(clone));
        clone.setEnableSound(enableSound.clone(clone));
        clone.setSound(sound.clone(clone));
        clone.setEnableHitSound(enableHitSound.clone(clone));
        clone.setHitSound(hitSound.clone(clone));
        return clone;
    }

    @Override
    public List<FeatureInterface> getFeatures() {
        List<FeatureInterface> features = new ArrayList<>();
        features.add(enable);
        features.add(contactCooldownTicks);
        features.add(delayTicks);
        features.add(forwardMovement);
        features.add(damageMultiplier);
        features.add(dismountMaxDurationTicks);
        features.add(dismountMinSpeed);
        features.add(dismountMinRelativeSpeed);
        features.add(knockbackMaxDurationTicks);
        features.add(knockbackMinSpeed);
        features.add(knockbackMinRelativeSpeed);
        features.add(damageMaxDurationTicks);
        features.add(damageMinSpeed);
        features.add(damageMinRelativeSpeed);
        features.add(enableSound);
        features.add(sound);
        features.add(enableHitSound);
        features.add(hitSound);
        return features;
    }

    @Override
    public String getParentInfo() {
        return getParent().getParentInfo();
    }

    @Override
    public ConfigurationSection getConfigurationSection() {
        return getParent().getConfigurationSection();
    }

    @Override
    public File getFile() {
        return getParent().getFile();
    }

    @Override
    public void reload() {
        for (FeatureInterface feature : (List<FeatureInterface>) getParent().getFeatures()) {
            if (feature instanceof KineticWeaponFeatures) {
                KineticWeaponFeatures kWF = (KineticWeaponFeatures) feature;
                kWF.setEnable(enable);
                kWF.setContactCooldownTicks(contactCooldownTicks);
                kWF.setDelayTicks(delayTicks);
                kWF.setForwardMovement(forwardMovement);
                kWF.setDamageMultiplier(damageMultiplier);
                kWF.setDismountMaxDurationTicks(dismountMaxDurationTicks);
                kWF.setDismountMinSpeed(dismountMinSpeed);
                kWF.setDismountMinRelativeSpeed(dismountMinRelativeSpeed);
                kWF.setKnockbackMaxDurationTicks(knockbackMaxDurationTicks);
                kWF.setKnockbackMinSpeed(knockbackMinSpeed);
                kWF.setKnockbackMinRelativeSpeed(knockbackMinRelativeSpeed);
                kWF.setDamageMaxDurationTicks(damageMaxDurationTicks);
                kWF.setDamageMinSpeed(damageMinSpeed);
                kWF.setDamageMinRelativeSpeed(damageMinRelativeSpeed);
                kWF.setEnableSound(enableSound);
                kWF.setSound(sound);
                kWF.setEnableHitSound(enableHitSound);
                kWF.setHitSound(hitSound);
            }
        }
    }

    @Override
    public void openBackEditor(@NotNull Player player) {
        getParent().openEditor(player);
    }

    @Override
    public void openEditor(@NotNull Player player) {
        GenericFeatureParentEditorManager.getInstance().startEditing(player, this);
    }

}
