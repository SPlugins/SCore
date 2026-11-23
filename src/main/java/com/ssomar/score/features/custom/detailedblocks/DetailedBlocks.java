package com.ssomar.score.features.custom.detailedblocks;

import com.ssomar.score.SsomarDev;
import com.ssomar.score.features.*;
import com.ssomar.score.features.editor.GenericFeatureParentEditor;
import com.ssomar.score.features.editor.GenericFeatureParentEditorManager;
import com.ssomar.score.features.types.BooleanFeature;
import com.ssomar.score.features.types.ColoredStringFeature;
import com.ssomar.score.features.types.list.ListDetailedMaterialFeature;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.messages.SendMessage;
import com.ssomar.score.utils.placeholders.StringPlaceholder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
public class DetailedBlocks extends FeatureWithHisOwnEditor<DetailedBlocks, DetailedBlocks, GenericFeatureParentEditor, GenericFeatureParentEditorManager> implements Serializable {

    private ListDetailedMaterialFeature blocks;
    private BooleanFeature cancelEventIfNotValid;
    private transient ColoredStringFeature messageIfNotValid;

    private boolean disableCancelEventIfNotValid = false;
    private boolean disableMessageIfNotValid = false;

    private boolean notSaveIfEqualsToDefaultValue;

    private final boolean DEBUG = true;

    public DetailedBlocks(FeatureParentInterface parent, FeatureSettingsInterface featureSettings) {
        super(parent, featureSettings);
        reset();
        this.notSaveIfEqualsToDefaultValue = false;
    }

    public DetailedBlocks(FeatureParentInterface parent, FeatureSettingsInterface featureSettings, boolean disableCancelEventIfNotValid, boolean disableMessageIfNotValid, boolean notSaveIfEqualsToDefaultValue) {
        super(parent, featureSettings);
        reset();
        this.disableCancelEventIfNotValid = disableCancelEventIfNotValid;
        this.disableMessageIfNotValid = disableMessageIfNotValid;
        this.notSaveIfEqualsToDefaultValue = notSaveIfEqualsToDefaultValue;
    }

    public DetailedBlocks(FeatureParentInterface parent) {
        super(parent,  FeatureSettingsSCore.detailedBlocks);
        reset();
        this.notSaveIfEqualsToDefaultValue = false;
    }

    public DetailedBlocks(FeatureParentInterface parent, boolean disableCancelEventIfNotValid, boolean disableMessageIfNotValid) {
        super(parent,  FeatureSettingsSCore.detailedBlocks);
        reset();
        this.disableCancelEventIfNotValid = disableCancelEventIfNotValid;
        this.disableMessageIfNotValid = disableMessageIfNotValid;
        this.notSaveIfEqualsToDefaultValue = false;
    }


    @Override
    public void reset() {
        this.blocks = new ListDetailedMaterialFeature(this, new ArrayList<>(), FeatureSettingsSCore.blocks, true);
        this.cancelEventIfNotValid = new BooleanFeature(this,  false, FeatureSettingsSCore.cancelEventIfNotValid);
        this.messageIfNotValid = new ColoredStringFeature(this, Optional.empty(), FeatureSettingsSCore.messageIfNotValid);
    }

    @Override
    public List<String> load(SPlugin plugin, ConfigurationSection config, boolean isPremiumLoading) {
        List<String> errors = new ArrayList<>();
        if (config.isList(getName())) {
            /* Old format, a plain list (e.g. ifIsInTheBlock: [COBWEB]) as written before the conditions used DetailedBlocks (SCore 2025-08):
             * it was ignored, so the condition was empty and always true (Discord 1410160093139304499). Read it as the blocks list. */
            org.bukkit.configuration.MemoryConfiguration legacy = new org.bukkit.configuration.MemoryConfiguration();
            legacy.set(blocks.getName(), config.getStringList(getName()));
            errors.addAll(blocks.load(plugin, legacy, isPremiumLoading));
        } else if (config.isConfigurationSection(getName())) {
            ConfigurationSection section = config.getConfigurationSection(getName());
            errors.addAll(blocks.load(plugin, section, isPremiumLoading));
            if(!disableCancelEventIfNotValid) errors.addAll(cancelEventIfNotValid.load(plugin, section, isPremiumLoading));
            if(!disableMessageIfNotValid) errors.addAll(messageIfNotValid.load(plugin, section, isPremiumLoading));
        }

        return errors;
    }

    public boolean isValid(@NotNull Block block, Optional<Player> playerOpt, Event event, StringPlaceholder sp) {
       return isValid(block, playerOpt, event, sp, null, Optional.empty());
    }

    public boolean isValid(@NotNull Block block, Optional<Player> playerOpt, Event event, StringPlaceholder sp, @Nullable Material material, @NotNull Optional<String> statesStrOpt) {

        SsomarDev.testMsg("DetailedBlocks 1", DEBUG);
        if(blocks.getValues().isEmpty() && blocks.getBlacklistedValues().isEmpty()) return true;

        SsomarDev.testMsg("DetailedBlocks 2", DEBUG);
        if(blocks.verifBlock(block, material, statesStrOpt)) {
            SsomarDev.testMsg("DetailedBlocks 3 OK", DEBUG);
            return true;
        }
        else {
            SsomarDev.testMsg("DetailedBlocks 3 K0", DEBUG);
            if (event != null && cancelEventIfNotValid.getValue() && event instanceof Cancellable) {
                ((Cancellable) event).setCancelled(true);
            }
            if (playerOpt.isPresent() && messageIfNotValid != null && messageIfNotValid.getValue().isPresent()) {
                SendMessage.sendMessageNoPlch(playerOpt.get(), sp.replacePlaceholder(messageIfNotValid.getValue().get()));
            }
            return false;
        }
    }

    @Override
    public void save(ConfigurationSection config) {
        config.set(getName(), null);
        if(notSaveIfEqualsToDefaultValue && blocks.getValues().isEmpty() && blocks.getBlacklistedValues().isEmpty()) return;
        ConfigurationSection section = config.createSection(getName());
        blocks.save(section);
        if(!disableCancelEventIfNotValid) cancelEventIfNotValid.save(section);
        if(!disableMessageIfNotValid) messageIfNotValid.save(section);
    }

    @Override
    public DetailedBlocks getValue() {
        return this;
    }

    @Override
    public DetailedBlocks initItemParentEditor(GUI gui, int slot) {
        String[] finalDescription = new String[getEditorDescription().length + 4];
        System.arraycopy(getEditorDescription(), 0, finalDescription, 0, getEditorDescription().length);
        finalDescription[finalDescription.length - 4] = GUI.CLICK_HERE_TO_CHANGE;
        if (blocks.getValues().isEmpty() && !blocks.getBlacklistedValues().isEmpty())
            finalDescription[finalDescription.length - 3] = "&7Blocks: &e&lALL BLOCKS";
        else
            finalDescription[finalDescription.length - 3] = "&7Detailed Blocks: &a+" + (blocks.getCurrentValues().size());

        if (messageIfNotValid.getValue().isPresent()) {
            finalDescription[finalDescription.length - 2] = "&7Message if NV: &e" + messageIfNotValid.getValue().get();
        } else {
            finalDescription[finalDescription.length - 2] = "&7Message if NV: &cNO MESSAGE";
        }

        if (cancelEventIfNotValid.getValue())
            finalDescription[finalDescription.length - 1] = "&7Cancel Event if NV: &a&l✔";
        else
            finalDescription[finalDescription.length - 1] = "&7Cancel Event if NV: &c&l✘";

        for (int i = 0; i < finalDescription.length; i++) {
            String command = finalDescription[i];
            if (command.length() > 40) command = command.substring(0, 39) + "...";
            finalDescription[i] = command;
        }


        gui.createItem(getEditorMaterial(), 1, slot, GUI.TITLE_COLOR + getEditorName(), false, false, finalDescription);
        return this;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {

    }

    @Override
    public DetailedBlocks clone(FeatureParentInterface newParent) {
        DetailedBlocks dropFeatures = new DetailedBlocks(newParent, getFeatureSettings());
        dropFeatures.setBlocks(blocks.clone(dropFeatures));
        dropFeatures.setCancelEventIfNotValid(cancelEventIfNotValid.clone(dropFeatures));
        dropFeatures.setMessageIfNotValid(messageIfNotValid.clone(dropFeatures));
        dropFeatures.setDisableMessageIfNotValid(disableMessageIfNotValid);
        dropFeatures.setDisableCancelEventIfNotValid(disableCancelEventIfNotValid);
        dropFeatures.setNotSaveIfEqualsToDefaultValue(notSaveIfEqualsToDefaultValue);
        return dropFeatures;
    }

    @Override
    public List<FeatureInterface> getFeatures() {
        List<FeatureInterface> features =  new ArrayList<>(Arrays.asList(blocks));
        if(!disableCancelEventIfNotValid) features.add(cancelEventIfNotValid);
        if(!disableMessageIfNotValid) features.add(messageIfNotValid);
        return features;
    }

    @Override
    public String getParentInfo() {
        return getParent().getParentInfo()+ ".("+getName()+")";
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
            if (feature instanceof DetailedBlocks && feature.getEditorName().equals(getEditorName())) {
                DetailedBlocks hiders = (DetailedBlocks) feature;
                hiders.setBlocks(blocks);
                hiders.setCancelEventIfNotValid(cancelEventIfNotValid);
                hiders.setMessageIfNotValid(messageIfNotValid);
                hiders.disableMessageIfNotValid = disableMessageIfNotValid;
                hiders.disableCancelEventIfNotValid = disableCancelEventIfNotValid;
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
