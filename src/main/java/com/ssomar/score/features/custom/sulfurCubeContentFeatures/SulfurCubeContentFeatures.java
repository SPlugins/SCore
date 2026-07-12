package com.ssomar.score.features.custom.sulfurCubeContentFeatures;

import com.ssomar.score.SCore;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.features.*;
import com.ssomar.score.features.editor.GenericFeatureParentEditor;
import com.ssomar.score.features.editor.GenericFeatureParentEditorManager;
import com.ssomar.score.features.types.BooleanFeature;
import com.ssomar.score.features.types.ItemStackFeature;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.emums.ResetSetting;
import com.ssomar.score.utils.logging.Utils;
import com.ssomar.score.utils.strings.StringConverter;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.SulfurCubeContent;
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
 * The sulfur_cube_content data component, added in Minecraft 26.2.
 * Stores the item absorbed by a sulfur cube mob.
 */
@Getter
@Setter
public class SulfurCubeContentFeatures extends FeatureWithHisOwnEditor<SulfurCubeContentFeatures, SulfurCubeContentFeatures, GenericFeatureParentEditor, GenericFeatureParentEditorManager> implements FeatureForItemNewPaperComponents {

    private BooleanFeature enable;
    private ItemStackFeature absorbedItem;

    public SulfurCubeContentFeatures(FeatureParentInterface parent) {
        super(parent, FeatureSettingsSCore.sulfurCubeContentFeatures);
        reset();
    }

    @Override
    public void reset() {
        enable = new BooleanFeature(this, false, FeatureSettingsSCore.enable);
        absorbedItem = new ItemStackFeature(this, Optional.empty(), FeatureSettingsSCore.absorbedItem);
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
    public SulfurCubeContentFeatures getValue() {
        return this;
    }

    @Override
    public SulfurCubeContentFeatures initItemParentEditor(GUI gui, int slot) {
        int len = 3;
        String[] finalDescription = new String[getEditorDescription().length + len];
        System.arraycopy(getEditorDescription(), 0, finalDescription, 0, getEditorDescription().length);
        finalDescription[finalDescription.length - len] = GUI.CLICK_HERE_TO_CHANGE;
        len--;

        finalDescription[finalDescription.length - len] = "&7Enable: &e" + (enable.getValue() ? "&a&l✔" : "&c&l✘");
        len--;
        finalDescription[finalDescription.length - len] = "&7Absorbed Item: &e" + (absorbedItem.getValue().isPresent() ? absorbedItem.getValue().get().getType().name() : "none");
        len--;

        gui.createItem(getEditorMaterial(), 1, slot, GUI.TITLE_COLOR + getEditorName(), false, false, finalDescription);
        return this;
    }

    @Override
    public boolean isAvailable() {
        return SCore.isPaperOrFork() && SCore.is26v2Plus();
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
        if (isAvailable() && enable.getValue() && absorbedItem.getValue().isPresent()) {
            ItemStack item = args.getItem();
            try {
                item.setData(DataComponentTypes.SULFUR_CUBE_CONTENT, SulfurCubeContent.sulfurCubeContent(absorbedItem.getValue().get()));
            } catch (Exception e) {
                Utils.sendConsoleMsg(SCore.plugin, "&cError while applying the sulfur cube content features on an item");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void loadFromItem(@NotNull FeatureForItemArgs args) {
        if (isAvailable()) {
            ItemStack item = args.getItem();
            if (item.hasData(DataComponentTypes.SULFUR_CUBE_CONTENT)) {
                SulfurCubeContent content = item.getData(DataComponentTypes.SULFUR_CUBE_CONTENT);
                if (content != null) {
                    enable.setValue(true);
                    absorbedItem.setValue(Optional.ofNullable(content.absorbedItem()));
                }
            }
        }
    }

    @Override
    public ResetSetting getResetSetting() {
        return ResetSetting.SULFUR_CUBE_CONTENT;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {
    }

    @Override
    public SulfurCubeContentFeatures clone(FeatureParentInterface newParent) {
        SulfurCubeContentFeatures clone = new SulfurCubeContentFeatures(newParent);
        clone.setEnable(enable.clone(clone));
        clone.setAbsorbedItem(absorbedItem.clone(clone));
        return clone;
    }

    @Override
    public List<FeatureInterface> getFeatures() {
        List<FeatureInterface> features = new ArrayList<>();
        features.add(enable);
        features.add(absorbedItem);
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
            if (feature instanceof SulfurCubeContentFeatures) {
                SulfurCubeContentFeatures sCF = (SulfurCubeContentFeatures) feature;
                sCF.setEnable(enable);
                sCF.setAbsorbedItem(absorbedItem);
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
