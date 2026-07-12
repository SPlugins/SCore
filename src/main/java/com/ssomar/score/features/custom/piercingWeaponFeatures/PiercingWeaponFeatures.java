package com.ssomar.score.features.custom.piercingWeaponFeatures;

import com.ssomar.score.SCore;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.features.*;
import com.ssomar.score.features.editor.GenericFeatureParentEditor;
import com.ssomar.score.features.editor.GenericFeatureParentEditorManager;
import com.ssomar.score.features.types.BooleanFeature;
import com.ssomar.score.features.types.SoundFeature;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.backward_compatibility.SoundUtils;
import net.kyori.adventure.key.Key;
import com.ssomar.score.utils.emums.ResetSetting;
import com.ssomar.score.utils.logging.Utils;
import com.ssomar.score.utils.strings.StringConverter;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.PiercingWeapon;
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
 * The piercing_weapon data component, added in Minecraft 1.21.11.
 * Makes a weapon pierce through multiple entities in a single attack.
 */
@Getter
@Setter
public class PiercingWeaponFeatures extends FeatureWithHisOwnEditor<PiercingWeaponFeatures, PiercingWeaponFeatures, GenericFeatureParentEditor, GenericFeatureParentEditorManager> implements FeatureForItemNewPaperComponents {

    private BooleanFeature enable;
    private BooleanFeature dealsKnockback;
    private BooleanFeature dismounts;
    private BooleanFeature enableSound;
    private SoundFeature sound;
    private BooleanFeature enableHitSound;
    private SoundFeature hitSound;

    public PiercingWeaponFeatures(FeatureParentInterface parent) {
        super(parent, FeatureSettingsSCore.piercingWeaponFeatures);
        reset();
    }

    @Override
    public void reset() {
        enable = new BooleanFeature(this, false, FeatureSettingsSCore.enable);
        dealsKnockback = new BooleanFeature(this, true, FeatureSettingsSCore.dealsKnockback);
        dismounts = new BooleanFeature(this, false, FeatureSettingsSCore.dismounts);
        enableSound = new BooleanFeature(this, false, FeatureSettingsSCore.enableSound);
        sound = new SoundFeature(this, Optional.of(Sound.ITEM_SPEAR_ATTACK), FeatureSettingsSCore.sound);
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
    public PiercingWeaponFeatures getValue() {
        return this;
    }

    @Override
    public PiercingWeaponFeatures initItemParentEditor(GUI gui, int slot) {
        int len = 8;
        String[] finalDescription = new String[getEditorDescription().length + len];
        System.arraycopy(getEditorDescription(), 0, finalDescription, 0, getEditorDescription().length);
        finalDescription[finalDescription.length - len] = GUI.CLICK_HERE_TO_CHANGE;
        len--;

        finalDescription[finalDescription.length - len] = "&7Enable: &e" + (enable.getValue() ? "&a&l✔" : "&c&l✘");
        len--;
        finalDescription[finalDescription.length - len] = "&7Deals Knockback: &e" + (dealsKnockback.getValue() ? "&a&l✔" : "&c&l✘");
        len--;
        finalDescription[finalDescription.length - len] = "&7Dismounts: &e" + (dismounts.getValue() ? "&a&l✔" : "&c&l✘");
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
                PiercingWeapon.Builder builder = PiercingWeapon.piercingWeapon()
                        .dealsKnockback(dealsKnockback.getValue())
                        .dismounts(dismounts.getValue());
                if (enableSound.getValue() && sound.getValue().isPresent())
                    builder.sound(sound.getValue().get().key());
                if (enableHitSound.getValue() && hitSound.getValue().isPresent())
                    builder.hitSound(hitSound.getValue().get().key());
                item.setData(DataComponentTypes.PIERCING_WEAPON, builder);
            } catch (Exception e) {
                Utils.sendConsoleMsg(SCore.plugin, "&cError while applying the piercing weapon features on an item");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void loadFromItem(@NotNull FeatureForItemArgs args) {
        if (isAvailable()) {
            ItemStack item = args.getItem();
            if (item.hasData(DataComponentTypes.PIERCING_WEAPON)) {
                PiercingWeapon piercingWeapon = item.getData(DataComponentTypes.PIERCING_WEAPON);
                enable.setValue(true);
                dealsKnockback.setValue(piercingWeapon.dealsKnockback());
                dismounts.setValue(piercingWeapon.dismounts());
                Key soundKey = piercingWeapon.sound();
                enableSound.setValue(soundKey != null);
                Sound loadedSound = SoundUtils.getSoundFromKey(soundKey);
                if (loadedSound != null) sound.setValue(Optional.of(loadedSound));
                Key hitSoundKey = piercingWeapon.hitSound();
                enableHitSound.setValue(hitSoundKey != null);
                Sound loadedHitSound = SoundUtils.getSoundFromKey(hitSoundKey);
                if (loadedHitSound != null) hitSound.setValue(Optional.of(loadedHitSound));
            }
        }
    }

    @Override
    public ResetSetting getResetSetting() {
        return ResetSetting.PIERCING_WEAPON;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {
    }

    @Override
    public PiercingWeaponFeatures clone(FeatureParentInterface newParent) {
        PiercingWeaponFeatures clone = new PiercingWeaponFeatures(newParent);
        clone.setEnable(enable.clone(clone));
        clone.setDealsKnockback(dealsKnockback.clone(clone));
        clone.setDismounts(dismounts.clone(clone));
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
        features.add(dealsKnockback);
        features.add(dismounts);
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
            if (feature instanceof PiercingWeaponFeatures) {
                PiercingWeaponFeatures pWF = (PiercingWeaponFeatures) feature;
                pWF.setEnable(enable);
                pWF.setDealsKnockback(dealsKnockback);
                pWF.setDismounts(dismounts);
                pWF.setEnableSound(enableSound);
                pWF.setSound(sound);
                pWF.setEnableHitSound(enableHitSound);
                pWF.setHitSound(hitSound);
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
