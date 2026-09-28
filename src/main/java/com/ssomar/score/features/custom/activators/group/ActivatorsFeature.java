package com.ssomar.score.features.custom.activators.group;

import com.ssomar.score.SsomarDev;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.events.loop.LoopManager;
import com.ssomar.score.features.*;
import com.ssomar.score.features.custom.activators.activator.SActivator;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.scheduler.CustomTriggerScheduler;
import com.ssomar.score.sobject.sactivator.SOption;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.strings.StringConverter;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

@Getter
@Setter
public class ActivatorsFeature extends FeatureWithHisOwnEditor<ActivatorsFeature, ActivatorsFeature, ActivatorsFeatureEditor, ActivatorsFeatureEditorManager> implements FeaturesGroup<SActivator> {

    private LinkedHashMap<String, SActivator> activators;
    private SActivator builderInstance;
    private int premiumLimit = 1;

    public ActivatorsFeature(FeatureParentInterface parent, SActivator<?, ?, ?> builderInstance) {
        super(parent, FeatureSettingsSCore.activators);
        this.builderInstance = builderInstance;
        reset();
    }

    @Override
    public void reset() {
        this.activators = new LinkedHashMap<>();
    }

    @Override
    public List<String> load(SPlugin plugin, ConfigurationSection config, boolean isPremiumLoading) {
        List<String> error = new ArrayList<>();
        if (config.isConfigurationSection(this.getName())) {
            ConfigurationSection activatorsSection = config.getConfigurationSection(this.getName());
            for (String activatorID : activatorsSection.getKeys(false)) {
                if (activators.size() >= premiumLimit && !isPremiumLoading) {
                    error.add("&cERROR, Couldn't load the Activator of " + activatorID + " from config, &7&o" + getParent().getParentInfo() + " &6>> Because it requires the premium version to have more than 1 activator !");
                    return error;
                }
                SActivator<?, ?, ?> activator = builderInstance.getBuilderInstance(this, activatorID);
                List<String> subErrors = activator.load(plugin, activatorsSection, isPremiumLoading);
                if (subErrors.size() > 0) {
                    error.addAll(subErrors);
                }
                //SsomarDev.testMsg("Activator " + activatorID + " loaded", true);
                if (activator.getOption().isLoopOption()) {
                    //SsomarDev.testMsg("Activator " + activatorID + " is a loop activator", true);
                    LoopManager.getInstance().addLoopActivator(activator);
                    //SsomarDev.testMsg("loopmanager size: "+LoopManager.getInstance().getLoopActivators().size(), true);
                }
                if (activator.getOption().isCustomTriggerOption()) {
                    SsomarDev.testMsg("Activator " + activatorID + " is a custom trigger activator", true);

                    CustomTriggerScheduler.getInstance().addCustomTriggerScheduler(plugin.getShortName()+"_"+activatorID, activator.getRunnableForAll() , activator.getScheduleFeatures());
                }
                activators.put(activatorID, activator);
            }
        }
        return error;
    }

    @Override
    public void save(ConfigurationSection config) {
        config.set(this.getName(), null);
        ConfigurationSection attributesSection = config.createSection(this.getName());
        for (String actId : activators.keySet()) {
            activators.get(actId).save(attributesSection);
        }

        if(isSavingOnlyIfDiffDefault() && attributesSection.getKeys(false).isEmpty()){
            config.set(getName(), null);
            return;
        }

        if (GeneralConfig.getInstance().isEnableCommentsInConfig())
            config.setComments(this.getName(), StringConverter.decoloredString(Arrays.asList(getFeatureSettings().getEditorDescriptionBrut())));

    }

    @Override
    public ActivatorsFeature getValue() {
        return this;
    }

    public List<SActivator> getActivators(SOption option, List<String> whitelistActivatorsId, List<SActivator> whitelistActivators) {

        /* for(String whitelistActivatorId : whitelistActivatorsId) {
            SsomarDev.testMsg("whitelistActivatorId: "+whitelistActivatorId, true);
        }
        for (SActivator whitelistActivator : whitelistActivators) {
            SsomarDev.testMsg("whitelistActivator: "+whitelistActivator.getId(), true);
        }*/

        List<SActivator> result = new ArrayList<>();
        for (SActivator activator : activators.values()) {
            //SsomarDev.testMsg("check activator >> "+activator.getId(), true);
            if (activator.isDisabledByInvalidOption()) continue;
            if (activator.getOption().equals(option)) {
                if (!whitelistActivatorsId.isEmpty() && !whitelistActivatorsId.contains(activator.getId())) {
                    //SsomarDev.testMsg("activator not valid", true);
                    continue;
                }
                if ((activator.getOption().isLoopOption() || activator.getOption().isCustomTriggerOption()) && !whitelistActivators.isEmpty()) {
                    boolean valid = false;
                    for (SActivator activatorCheck : whitelistActivators) {
                        if (activatorCheck.isEqualsOrAClone(activator)) {
                            valid = true;
                            //SsomarDev.testMsg("activator valid", true);
                        }
                    }
                    if (!valid){
                        //SsomarDev.testMsg("activator not valid", true);
                        continue;
                    }
                }
                result.add(activator);
            }
            else {
               // SsomarDev.testMsg("activator not valid >> "+activator.getOption() +" different than "+option, true);
            }
        }
        return result;
    }

    @Override
    public ActivatorsFeature initItemParentEditor(GUI gui, int slot) {
        String[] finalDescription = new String[getEditorDescription().length + 2];
        System.arraycopy(getEditorDescription(), 0, finalDescription, 0, getEditorDescription().length);
        finalDescription[finalDescription.length - 2] = GUI.CLICK_HERE_TO_CHANGE;
        finalDescription[finalDescription.length - 1] = "&7&oActivator(s) added: &e" + activators.size();

        gui.createItem(getEditorMaterial(), 1, slot, GUI.TITLE_COLOR + getEditorName(), false, false, finalDescription);
        return this;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {

    }

    @Override
    public SActivator getTheChildFeatureClickedParentEditor(String featureClicked) {
        for (SActivator x : activators.values()) {
            if (x.isTheFeatureClickedParentEditor(featureClicked)) return x;
        }
        return null;
    }

    @Override
    public ActivatorsFeature clone() {
        return null;
    }

    @Override
    public ActivatorsFeature clone(FeatureParentInterface newParent) {
        ActivatorsFeature eF = new ActivatorsFeature(newParent, builderInstance);
        LinkedHashMap<String, SActivator> newActivators = new LinkedHashMap<>();
        for (String key : activators.keySet()) {
            SActivator clone = (SActivator) activators.get(key).clone(eF);
            newActivators.put(key, clone);
        }
        eF.setActivators(newActivators);
        return eF;
    }

    @Override
    public List<FeatureInterface> getFeatures() {
        return new ArrayList<>(activators.values());
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
            if (feature instanceof ActivatorsFeature) {
                ActivatorsFeature eF = (ActivatorsFeature) feature;
                eF.setActivators(activators);
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
        ActivatorsFeatureEditorManager.getInstance().startEditing(player, this);
    }

    @Override
    public void createNewFeature(@NotNull Player editor) {
        if (!isPremium() && activators.size() >= premiumLimit) return;
        String baseId = "activator";
        for (int i = 0; i < 1000; i++) {
            String id = baseId + i;
            if (!activators.containsKey(id)) {
                //SsomarDev.testMsg("INSTANCE CREATE: " + this.hashCode());
                SActivator activator = builderInstance.getBuilderInstance(this, id);
                activators.put(id, activator);
                activator.openEditor(editor);
                break;
            }
        }
    }

    @Override
    public void deleteFeature(@NotNull Player editor, SActivator feature) {
        LoopManager.getInstance().removeLoopActivator(activators.get(feature.getId()));
        activators.remove(feature.getId());
    }
}
