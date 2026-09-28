package com.ssomar.score.features.custom.conditions.placeholders.group;

import com.ssomar.score.commands.runnable.ActionInfo;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.features.*;
import com.ssomar.score.features.custom.conditions.placeholders.placeholder.PlaceholderConditionFeature;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.messages.SendMessage;
import com.ssomar.score.utils.placeholders.StringPlaceholder;
import com.ssomar.score.utils.strings.StringConverter;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

@Getter
@Setter
public class PlaceholderConditionGroupFeature extends FeatureWithHisOwnEditor<PlaceholderConditionGroupFeature, PlaceholderConditionGroupFeature, PlaceholderConditionGroupFeatureEditor, PlaceholderConditionGroupFeatureEditorManager> implements FeaturesGroup<PlaceholderConditionFeature> {

    private LinkedHashMap<String, PlaceholderConditionFeature> placeholdersConditions;

    private boolean notSaveIfItIsEmpty = false;

    public PlaceholderConditionGroupFeature(FeatureParentInterface parent) {
        super(parent, FeatureSettingsSCore.placeholdersConditions);
        reset();
    }

    public PlaceholderConditionGroupFeature(FeatureParentInterface parent, FeatureSettingsInterface settings) {
        super(parent, settings);
        reset();
    }

    public PlaceholderConditionGroupFeature(FeatureParentInterface parent, FeatureSettingsInterface settings, boolean notSaveIfItIsEmpty) {
        super(parent, settings);
        this.notSaveIfItIsEmpty = notSaveIfItIsEmpty;
        reset();
    }


    @Override
    public void reset() {
        this.placeholdersConditions = new LinkedHashMap<>();
    }

    public boolean verify(Player player, Player target, Event event) {
        return verify(player, target, null, event);
    }

    public boolean verify(Player player, Player target, @Nullable StringPlaceholder sp, Event event) {
        boolean valid = true;
        for (PlaceholderConditionFeature attribute : placeholdersConditions.values()) {

            if (!attribute.verify(player, target, sp)) {
                String message = attribute.getMessageIfNotValid().getValue().get();
                String messageForTarget = attribute.getMessageIfNotValidForTarget().getValue().get();
                if (sp != null) {
                    message = sp.replacePlaceholder(message);
                    messageForTarget = sp.replacePlaceholder(messageForTarget);
                }
                String[] split = message.split("\n");
                for (String s : split) {
                    SendMessage.sendMessageNoPlch(player, s);
                }
                if(target != null){
                    String[] split2 = messageForTarget.split("\n");
                    for (String s : split2) {
                        SendMessage.sendMessageNoPlch(target, s);
                    }
                }
                if (event != null && event instanceof Cancellable && attribute.getCancelEventIfNotValid().getValue()) {
                    ((Cancellable) event).setCancelled(true);
                }

                ActionInfo actionInfo = new ActionInfo("", sp);
                attribute.getConsoleCommandsIfError().runCommands(actionInfo, "");

                if (attribute.getStopCheckingOtherConditionsIfNotValid().getValue()) return false;
                else valid = false;
            }
        }
        return valid;
    }

    public boolean verifConditions(Player player, List<String> errors) {
        return verifConditions((OfflinePlayer) player, errors);
    }

    public boolean verifConditions(OfflinePlayer player, List<String> errors) {
        for (PlaceholderConditionFeature attribute : placeholdersConditions.values()) {
            if (!attribute.verify(player, null, new StringPlaceholder())) {
                String message = attribute.getMessageIfNotValid().getValue().get();
                String[] split = message.split("\n");
                errors.addAll(Arrays.asList(split));
                return false;
            }
        }
        return true;
    }

    @Override
    public List<String> load(SPlugin plugin, ConfigurationSection config, boolean isPremiumLoading) {
        List<String> error = new ArrayList<>();
        if (config.isConfigurationSection(this.getName())) {
            ConfigurationSection enchantmentsSection = config.getConfigurationSection(this.getName());
            for (String attributeID : enchantmentsSection.getKeys(false)) {
                PlaceholderConditionFeature attribute = new PlaceholderConditionFeature(this, attributeID);
                List<String> subErrors = attribute.load(plugin, enchantmentsSection, isPremiumLoading);
                if (subErrors.size() > 0) {
                    error.addAll(subErrors);
                    // The invalid condition is skipped (unchanged behavior): say it, the activator runs without this guard.
                    error.add("&cWARNING, the placeholder condition &6" + attributeID + " &cis invalid and is &lIGNORED&c: what it protects will run WITHOUT this condition &7&o" + getParentInfo() + " &6>> Fix the condition above to restore the check !");
                    continue;
                }
                placeholdersConditions.put(attributeID, attribute);
            }
        }
        return error;
    }

    @Override
    public void save(ConfigurationSection config) {
        config.set(this.getName(), null);
        if(notSaveIfItIsEmpty && placeholdersConditions.isEmpty()) return;
        ConfigurationSection attributesSection = config.createSection(this.getName());
        for (String enchantmentID : placeholdersConditions.keySet()) {
            placeholdersConditions.get(enchantmentID).save(attributesSection);
        }
        if(isSavingOnlyIfDiffDefault() && attributesSection.getKeys(false).isEmpty()){
            config.set(getName(), null);
            return;
        }

        if (GeneralConfig.getInstance().isEnableCommentsInConfig())
            config.setComments(this.getName(), StringConverter.decoloredString(Arrays.asList(getFeatureSettings().getEditorDescriptionBrut())));

    }

    public String getConfigAsString() {
        String configuration = "";
        Reader reader = new java.io.StringReader(configuration);
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(reader);
        save(yamlConfiguration);
        configuration = yamlConfiguration.saveToString();
        return configuration;
    }

    @Override
    public PlaceholderConditionGroupFeature getValue() {
        return this;
    }

    @Override
    public PlaceholderConditionGroupFeature initItemParentEditor(GUI gui, int slot) {
        String[] finalDescription = new String[getEditorDescription().length + 2];
        System.arraycopy(getEditorDescription(), 0, finalDescription, 0, getEditorDescription().length);
        finalDescription[finalDescription.length - 2] = "&7&oPlaceholder cdt(s) added: &e" + placeholdersConditions.size();
        finalDescription[finalDescription.length - 1] = GUI.CLICK_HERE_TO_CHANGE;

        gui.createItem(getEditorMaterial(), 1, slot, GUI.TITLE_COLOR + getEditorName(), false, false, finalDescription);
        return this;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {

    }

    @Override
    public PlaceholderConditionFeature getTheChildFeatureClickedParentEditor(String featureClicked) {
        for (PlaceholderConditionFeature x : placeholdersConditions.values()) {
            if (x.isTheFeatureClickedParentEditor(featureClicked)) return x;
        }
        return null;
    }

    @Override
    public PlaceholderConditionGroupFeature clone(FeatureParentInterface newParent) {
        PlaceholderConditionGroupFeature eF = new PlaceholderConditionGroupFeature(newParent, getFeatureSettings());
        eF.setNotSaveIfItIsEmpty(this.notSaveIfItIsEmpty);
        LinkedHashMap<String, PlaceholderConditionFeature> newAttributes = new LinkedHashMap<>();
        for (String x : placeholdersConditions.keySet()) {
            newAttributes.put(x, placeholdersConditions.get(x).clone(eF));
        }
        eF.setPlaceholdersConditions(newAttributes);
        return eF;
    }

    @Override
    public List<FeatureInterface> getFeatures() {
        return new ArrayList<>(placeholdersConditions.values());
    }

    @Override
    public String getParentInfo() {
        return getParent().getParentInfo();
    }

    @Override
    public ConfigurationSection getConfigurationSection() {
        ConfigurationSection section = getParent().getConfigurationSection();
        if (section.isConfigurationSection(this.getName())) {
            return section.getConfigurationSection(this.getName());
        } else return section.createSection(this.getName());
    }

    @Override
    public File getFile() {
        return getParent().getFile();
    }

    @Override
    public void reload() {
        for (FeatureInterface feature : (List<FeatureInterface>) getParent().getFeatures()) {
            if (feature instanceof PlaceholderConditionGroupFeature && feature.getName().equals(getName())) {
                PlaceholderConditionGroupFeature eF = (PlaceholderConditionGroupFeature) feature;
                eF.setPlaceholdersConditions(this.getPlaceholdersConditions());
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
        PlaceholderConditionGroupFeatureEditorManager.getInstance().startEditing(player, this);
    }

    @Override
    public void createNewFeature(@NotNull Player editor) {
        String baseId = "plchCdt";
        for (int i = 0; i < 1000; i++) {
            String id = baseId + i;
            if (!placeholdersConditions.containsKey(id)) {
                PlaceholderConditionFeature eF = new PlaceholderConditionFeature(this, id);
                placeholdersConditions.put(id, eF);
                eF.openEditor(editor);
                break;
            }
        }
    }

    @Override
    public void deleteFeature(@NotNull Player editor, PlaceholderConditionFeature feature) {
        placeholdersConditions.remove(feature.getId());
    }

}
