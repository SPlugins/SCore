package com.ssomar.score.features.custom.attackRangeFeatures;

import com.ssomar.score.SCore;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.features.*;
import com.ssomar.score.features.editor.GenericFeatureParentEditor;
import com.ssomar.score.features.editor.GenericFeatureParentEditorManager;
import com.ssomar.score.features.types.BooleanFeature;
import com.ssomar.score.features.types.DoubleFeature;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.emums.ResetSetting;
import com.ssomar.score.utils.logging.Utils;
import com.ssomar.score.utils.strings.StringConverter;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.AttackRange;
import lombok.Getter;
import lombok.Setter;
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
 * The attack_range data component, added in Minecraft 1.21.11.
 * Customizes the reach (and creative reach) of a weapon.
 */
@Getter
@Setter
public class AttackRangeFeatures extends FeatureWithHisOwnEditor<AttackRangeFeatures, AttackRangeFeatures, GenericFeatureParentEditor, GenericFeatureParentEditorManager> implements FeatureForItemNewPaperComponents {

    private BooleanFeature enable;
    private DoubleFeature minReach;
    private DoubleFeature maxReach;
    private DoubleFeature minCreativeReach;
    private DoubleFeature maxCreativeReach;
    private DoubleFeature hitboxMargin;
    private DoubleFeature mobFactor;

    public AttackRangeFeatures(FeatureParentInterface parent) {
        super(parent, FeatureSettingsSCore.attackRangeFeatures);
        reset();
    }

    @Override
    public void reset() {
        enable = new BooleanFeature(this, false, FeatureSettingsSCore.enable);
        minReach = new DoubleFeature(this, Optional.of(0.0), FeatureSettingsSCore.minReach);
        maxReach = new DoubleFeature(this, Optional.of(3.0), FeatureSettingsSCore.maxReach);
        minCreativeReach = new DoubleFeature(this, Optional.of(0.0), FeatureSettingsSCore.minCreativeReach);
        maxCreativeReach = new DoubleFeature(this, Optional.of(6.0), FeatureSettingsSCore.maxCreativeReach);
        hitboxMargin = new DoubleFeature(this, Optional.of(1.0), FeatureSettingsSCore.hitboxMargin);
        mobFactor = new DoubleFeature(this, Optional.of(2.0), FeatureSettingsSCore.mobFactor);
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
    public AttackRangeFeatures getValue() {
        return this;
    }

    @Override
    public AttackRangeFeatures initItemParentEditor(GUI gui, int slot) {
        int len = 8;
        String[] finalDescription = new String[getEditorDescription().length + len];
        System.arraycopy(getEditorDescription(), 0, finalDescription, 0, getEditorDescription().length);
        finalDescription[finalDescription.length - len] = GUI.CLICK_HERE_TO_CHANGE;
        len--;

        finalDescription[finalDescription.length - len] = "&7Enable: &e" + (enable.getValue() ? "&a&l✔" : "&c&l✘");
        len--;
        finalDescription[finalDescription.length - len] = "&7Min Reach: &e" + minReach.getValue().get();
        len--;
        finalDescription[finalDescription.length - len] = "&7Max Reach: &e" + maxReach.getValue().get();
        len--;
        finalDescription[finalDescription.length - len] = "&7Min Creative Reach: &e" + minCreativeReach.getValue().get();
        len--;
        finalDescription[finalDescription.length - len] = "&7Max Creative Reach: &e" + maxCreativeReach.getValue().get();
        len--;
        finalDescription[finalDescription.length - len] = "&7Hitbox Margin: &e" + hitboxMargin.getValue().get();
        len--;
        finalDescription[finalDescription.length - len] = "&7Mob Factor: &e" + mobFactor.getValue().get();
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
                item.setData(DataComponentTypes.ATTACK_RANGE, AttackRange.attackRange()
                        .minReach(minReach.getValue().get().floatValue())
                        .maxReach(maxReach.getValue().get().floatValue())
                        .minCreativeReach(minCreativeReach.getValue().get().floatValue())
                        .maxCreativeReach(maxCreativeReach.getValue().get().floatValue())
                        .hitboxMargin(hitboxMargin.getValue().get().floatValue())
                        .mobFactor(mobFactor.getValue().get().floatValue()));
            } catch (Exception e) {
                Utils.sendConsoleMsg(SCore.plugin, "&cError while applying the attack range features on an item");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void loadFromItem(@NotNull FeatureForItemArgs args) {
        if (isAvailable()) {
            ItemStack item = args.getItem();
            if (item.hasData(DataComponentTypes.ATTACK_RANGE)) {
                AttackRange attackRange = item.getData(DataComponentTypes.ATTACK_RANGE);
                enable.setValue(true);
                minReach.setValue(Optional.of((double) attackRange.minReach()));
                maxReach.setValue(Optional.of((double) attackRange.maxReach()));
                minCreativeReach.setValue(Optional.of((double) attackRange.minCreativeReach()));
                maxCreativeReach.setValue(Optional.of((double) attackRange.maxCreativeReach()));
                hitboxMargin.setValue(Optional.of((double) attackRange.hitboxMargin()));
                mobFactor.setValue(Optional.of((double) attackRange.mobFactor()));
            }
        }
    }

    @Override
    public ResetSetting getResetSetting() {
        return ResetSetting.ATTACK_RANGE;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {
    }

    @Override
    public AttackRangeFeatures clone(FeatureParentInterface newParent) {
        AttackRangeFeatures clone = new AttackRangeFeatures(newParent);
        clone.setEnable(enable.clone(clone));
        clone.setMinReach(minReach.clone(clone));
        clone.setMaxReach(maxReach.clone(clone));
        clone.setMinCreativeReach(minCreativeReach.clone(clone));
        clone.setMaxCreativeReach(maxCreativeReach.clone(clone));
        clone.setHitboxMargin(hitboxMargin.clone(clone));
        clone.setMobFactor(mobFactor.clone(clone));
        return clone;
    }

    @Override
    public List<FeatureInterface> getFeatures() {
        List<FeatureInterface> features = new ArrayList<>();
        features.add(enable);
        features.add(minReach);
        features.add(maxReach);
        features.add(minCreativeReach);
        features.add(maxCreativeReach);
        features.add(hitboxMargin);
        features.add(mobFactor);
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
            if (feature instanceof AttackRangeFeatures) {
                AttackRangeFeatures aRF = (AttackRangeFeatures) feature;
                aRF.setEnable(enable);
                aRF.setMinReach(minReach);
                aRF.setMaxReach(maxReach);
                aRF.setMinCreativeReach(minCreativeReach);
                aRF.setMaxCreativeReach(maxCreativeReach);
                aRF.setHitboxMargin(hitboxMargin);
                aRF.setMobFactor(mobFactor);
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
