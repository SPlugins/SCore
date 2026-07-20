package com.ssomar.score.features.custom.consumableFeatures;
import com.ssomar.score.SCore;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.features.*;
import com.ssomar.score.features.editor.GenericFeatureParentEditor;
import com.ssomar.score.features.editor.GenericFeatureParentEditorManager;
import com.ssomar.score.features.types.list.ListPotionEffectTypeFeature;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.emums.ResetSetting;
import com.ssomar.score.utils.strings.StringConverter;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.ssomar.score.features.types.BooleanFeature;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class OnConsumeEffectsFeatures extends FeatureWithHisOwnEditor<OnConsumeEffectsFeatures, OnConsumeEffectsFeatures, GenericFeatureParentEditor, GenericFeatureParentEditorManager> implements FeatureForItemNewPaperComponents {

    private BooleanFeature clearAllEffects;
    private ListPotionEffectTypeFeature listPotionEffectTypeToRemove;


    public OnConsumeEffectsFeatures(FeatureParentInterface parent, FeatureSettingsInterface featureSettingsSCore) {
        super(parent, featureSettingsSCore);
        reset();
    }

    public OnConsumeEffectsFeatures(FeatureParentInterface parent) {
        super(parent, FeatureSettingsSCore.onConsumeEffects);
        reset();
    }


    @Override
    public List<FeatureInterface> getFeatures() {
        List<FeatureInterface> features = new ArrayList<>();
        features.add(clearAllEffects);
        features.add(listPotionEffectTypeToRemove);
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
            if (feature instanceof OnConsumeEffectsFeatures) {
                OnConsumeEffectsFeatures hiders = (OnConsumeEffectsFeatures) feature;
                hiders.setClearAllEffects(clearAllEffects);
                hiders.setListPotionEffectTypeToRemove(listPotionEffectTypeToRemove);
            }
        }
    }

    @Override
    public void openEditor(@NotNull Player player) {
        GenericFeatureParentEditorManager.getInstance().startEditing(player, this);
    }

    @Override
    public void openBackEditor(@NotNull Player player) {
        getParent().openEditor(player);

    }

    @Override
    public List<String> load(SPlugin plugin, ConfigurationSection config, boolean isPremiumLoading) {
        List<String> errors = new ArrayList<>();
        if (config.isConfigurationSection(getName())) {
            ConfigurationSection section = config.getConfigurationSection(getName());
            errors.addAll(this.clearAllEffects.load(plugin, section, isPremiumLoading));
            errors.addAll(this.listPotionEffectTypeToRemove.load(plugin, section, isPremiumLoading));

        }
        return errors;
    }

    @Override
    public void save(ConfigurationSection config) {
        config.set(getName(), null);
        ConfigurationSection section = config.createSection(getName());
        this.clearAllEffects.save(section);
        this.listPotionEffectTypeToRemove.save(section);
        if(isSavingOnlyIfDiffDefault() && section.getKeys(false).isEmpty()){
            config.set(getName(), null);
            return;
        }

        if (GeneralConfig.getInstance().isEnableCommentsInConfig())
            config.setComments(this.getName(), StringConverter.decoloredString(Arrays.asList(getFeatureSettings().getEditorDescriptionBrut())));
    }

    @Override
    public OnConsumeEffectsFeatures getValue() {
        return this;
    }

    @Override
    public OnConsumeEffectsFeatures initItemParentEditor(GUI gui, int slot) {
        int len = 3;
        String[] finalDescription = new String[getEditorDescription().length + len];
        System.arraycopy(getEditorDescription(), 0, finalDescription, 0, getEditorDescription().length);
        finalDescription[finalDescription.length - len] = GUI.CLICK_HERE_TO_CHANGE;
        len--;
        if (clearAllEffects.getValue())
            finalDescription[finalDescription.length - len] = "&7clearAllEffects: &a&l✔";
        else
            finalDescription[finalDescription.length - len] = "&7clearAllEffects: &c&l✘";
        len--;
        if (!listPotionEffectTypeToRemove.getValue().isEmpty())
            finalDescription[finalDescription.length - len] = "&7Enabled: &a&l✔";
        else
            finalDescription[finalDescription.length - len] = "&7Disabled: &c&l✘";
        gui.createItem(getEditorMaterial(), 1, slot, GUI.TITLE_COLOR + getEditorName(), false, false, finalDescription);
        return this;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {

    }

    @Override
    public void reset() {
        clearAllEffects = new BooleanFeature(this, false, FeatureSettingsSCore.clearAllEffects);
        listPotionEffectTypeToRemove = new ListPotionEffectTypeFeature(this, new ArrayList<>(),FeatureSettingsSCore.listPotionEffectTypeToRemove);
    }

    @Override
    public OnConsumeEffectsFeatures clone(FeatureParentInterface newParent) {
        OnConsumeEffectsFeatures dropFeatures = new OnConsumeEffectsFeatures(newParent);
        dropFeatures.clearAllEffects = clearAllEffects.clone(dropFeatures);
        dropFeatures.listPotionEffectTypeToRemove = listPotionEffectTypeToRemove.clone(dropFeatures);
        return dropFeatures;
    }

    @Override
    public void applyOnItem(@NotNull FeatureForItemArgs args) {
        // nothing for now. busy dealing with other features
    }

    @Override
    public void loadFromItem(@NotNull FeatureForItemArgs args) {
        // nothing for now. busy dealing with other features
    }

    @Override
    public boolean isAvailable() {
        return SCore.is1v21v4Plus() && SCore.isPaperOrFork();
    }

    @Override
    public boolean isApplicable(@NotNull FeatureForItemArgs args) {
        return true;
    }

    @Override
    public void applyOnItemMeta(@NotNull FeatureForItemArgs args) {}

    @Override
    public void loadFromItemMeta(@NotNull FeatureForItemArgs args) {}

    @Override
    public ResetSetting getResetSetting() {
        return ResetSetting.CONSUMABLE;
    }
}
