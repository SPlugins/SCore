package com.ssomar.score.features.custom.loop;

import com.ssomar.score.features.FeatureInterface;
import com.ssomar.score.features.FeatureParentInterface;
import com.ssomar.score.features.FeatureSettingsSCore;
import com.ssomar.score.features.FeatureWithHisOwnEditor;
import com.ssomar.score.features.editor.GenericFeatureParentEditor;
import com.ssomar.score.features.editor.GenericFeatureParentEditorManager;
import com.ssomar.score.features.types.BooleanFeature;
import com.ssomar.score.features.types.IntegerFeature;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.splugin.SPlugin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
public class LoopFeatures extends FeatureWithHisOwnEditor<LoopFeatures, LoopFeatures, GenericFeatureParentEditor, GenericFeatureParentEditorManager> {

    private IntegerFeature delay;
    private BooleanFeature delayInTick;

    public LoopFeatures(FeatureParentInterface parent) {
        super(parent, FeatureSettingsSCore.loop);
        reset();
    }

    @Override
    public void reset() {
        this.delay = new IntegerFeature(this, Optional.of(1), FeatureSettingsSCore.delay);
        this.delayInTick = new BooleanFeature(this,  false, FeatureSettingsSCore.delayInTick);
    }

    @Override
    public List<String> load(SPlugin plugin, ConfigurationSection config, boolean isPremiumLoading) {
        List<String> errors = new ArrayList<>(this.delay.load(plugin, config, isPremiumLoading));
        if (delay.getValue().get() <= 0) {
            delay.setValue(Optional.of(1));
        }
        errors.addAll(this.delayInTick.load(plugin, config, isPremiumLoading));

        return errors;
    }

    @Override
    public void save(ConfigurationSection config) {
        delay.save(config);
        delayInTick.save(config);
    }

    /**
     * The delay between two activations, in ticks (delay is in seconds unless delayInTick is true)
     */
    public int getDelayInTicks() {
        int value = delay.getValue().orElse(1);
        return delayInTick.getValue() ? value : value * 20;
    }

    @Override
    public LoopFeatures getValue() {
        return this;
    }

    @Override
    public LoopFeatures initItemParentEditor(GUI gui, int slot) {
        String[] finalDescription = new String[getEditorDescription().length + 3];
        System.arraycopy(getEditorDescription(), 0, finalDescription, 0, getEditorDescription().length);
        finalDescription[finalDescription.length - 3] = GUI.CLICK_HERE_TO_CHANGE;
        finalDescription[finalDescription.length - 2] = "&7Delay: &e" + delay.getValue().get();
        if (delayInTick.getValue()) {
            finalDescription[finalDescription.length - 1] = "&7In ticks: &a&l✔";
        } else {
            finalDescription[finalDescription.length - 1] = "&7In ticks: &c&l✘";
        }

        gui.createItem(getEditorMaterial(), 1, slot, GUI.TITLE_COLOR + getEditorName(), false, false, finalDescription);
        return this;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {

    }

    @Override
    public LoopFeatures clone(FeatureParentInterface newParent) {
        LoopFeatures loopFeatures = new LoopFeatures(newParent);
        loopFeatures.setDelay(this.delay.clone(loopFeatures));
        loopFeatures.setDelayInTick(this.delayInTick.clone(loopFeatures));
        return loopFeatures;
    }

    @Override
    public List<FeatureInterface> getFeatures() {
        return new ArrayList<>(Arrays.asList(delay, delayInTick));
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
            if (feature instanceof LoopFeatures) {
                LoopFeatures loopFeatures = (LoopFeatures) feature;
                loopFeatures.setDelay(this.delay);
                loopFeatures.setDelayInTick(this.delayInTick);
                break;
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
