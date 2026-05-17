package com.ssomar.score.features.custom.cooldowns;

import com.ssomar.score.SCore;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.configs.messages.Message;
import com.ssomar.score.configs.messages.MessageMain;
import com.ssomar.score.features.*;
import com.ssomar.score.features.custom.activators.activator.SActivator;
import com.ssomar.score.features.custom.conditions.placeholders.group.PlaceholderConditionGroupFeature;
import com.ssomar.score.features.editor.GenericFeatureParentEditor;
import com.ssomar.score.features.editor.GenericFeatureParentEditorManager;
import com.ssomar.score.features.types.BooleanFeature;
import com.ssomar.score.features.types.ColoredStringFeature;
import com.ssomar.score.features.types.IntegerFeature;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.sobject.SObject;
import com.ssomar.score.sobject.SObjectBuildable;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.placeholders.StringPlaceholder;
import com.ssomar.score.utils.strings.StringConverter;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

@Getter
@Setter
public class CooldownFeature extends FeatureWithHisOwnEditor<CooldownFeature, CooldownFeature, GenericFeatureParentEditor, GenericFeatureParentEditorManager> {

    /* Cooldowns / delay */
    private IntegerFeature cooldown;
    private ColoredStringFeature cooldownMessage;
    private BooleanFeature displayCooldownMessage;
    private BooleanFeature isCooldownInTicks;
    private BooleanFeature cancelEventIfInCooldown;
    private BooleanFeature pauseWhenOffline;
    private PlaceholderConditionGroupFeature pausePlaceholdersConditions;
    /* 1.21.3 feature cooldown visually */
    private BooleanFeature enableVisualCooldown;

    private String cooldownId;
    private SPlugin sPlugin;
    private boolean enableCooldownForOp;

    private boolean isPremium;
    private boolean isGlobal;

    public CooldownFeature(FeatureParentInterface parent, FeatureSettingsInterface featureSettings, SPlugin sPlugin, boolean enableCooldownForOp, boolean isGlobal) {
        super(parent, featureSettings);
        this.sPlugin = sPlugin;
        this.isPremium = !sPlugin.isLotOfWork();
        if (getParent() instanceof SActivator) {
            SActivator newSActivator = (SActivator) getParent();
            //String id = newSActivator.getParentObjectId();
            //SsomarDev.testMsg("CREATE NEW COOLDOWN FEATURE FOR ACTIVATOR >> "+ newSActivator.getId()+" >>>> item "+id);
            this.cooldownId = sPlugin.getShortName() + ":" + newSActivator.getParentObjectId() + ":" + newSActivator.getId();
        } else this.cooldownId = UUID.randomUUID().toString();
        this.enableCooldownForOp = enableCooldownForOp;
        this.isGlobal = isGlobal;
        reset();
    }

    public CooldownFeature(FeatureParentInterface parent, FeatureSettingsInterface featureSettings, SPlugin sPlugin, boolean enableCooldownForOp) {
        super(parent, featureSettings);
        this.sPlugin = sPlugin;
        this.isPremium = !sPlugin.isLotOfWork();
        if (getParent() instanceof SActivator) {
            SActivator newSActivator = (SActivator) getParent();
            //String id = newSActivator.getParentObjectId();
            //SsomarDev.testMsg("CREATE NEW COOLDOWN FEATURE FOR ACTIVATOR >> "+ newSActivator.getId()+" >>>> item "+id);
            this.cooldownId = sPlugin.getShortName() + ":" + newSActivator.getParentObjectId() + ":" + newSActivator.getId();
        } else this.cooldownId = UUID.randomUUID().toString();
        this.enableCooldownForOp = enableCooldownForOp;
        this.isGlobal = false;
        reset();
    }

    /**
     * @param entity The entity
     * @param event  The event
     * @param sp The placeholder associate to the event
     * @return True in No coooldown, false if its on cooldown
     */
    public boolean checkCooldown(Entity entity, @Nullable Event event, StringPlaceholder sp, SObject sObject) {
        //SsomarDev.testMsg("CHECK COOLDOWN FOR ENTITY " + entity.getName() + " >>>> " + cooldownId, true);
        /* Check if the activator is in cooldown for the player or not  */
        if (!(entity instanceof Permissible) || !hasNoCDPerm(entity, sObject)) {
            Optional<Cooldown> inCooldownOpt = CooldownsManager.getInstance().getCooldown(sPlugin, cooldownId, entity.getUniqueId(), isGlobal);
            if (inCooldownOpt.isPresent()) {
                //SsomarDev.testMsg("COOLDOWN PRESENT", true);
                if (this.displayCooldownMessage.getValue() && entity instanceof CommandSender) {
                    //SsomarDev.testMsg("DISPLAY COOLDOWN MESSAGE >> "+inCooldownOpt.get().getTimeLeft());
                    displayCooldownMessage(entity, inCooldownOpt.get().getTimeLeft(), sp);
                }
                //SsomarDev.testMsg("COOLDOWN cancel >>"+(e != null && e instanceof Cancellable)+  " >>>"+this.cancelEventIfInCooldown.getValue() );
                if (event != null && event instanceof Cancellable && this.cancelEventIfInCooldown.getValue()) {
                    //SsomarDev.testMsg("COOLDOWN cancel event");
                    ((Cancellable) event).setCancelled(true);
                }
                return false;
            }
            //else SsomarDev.testMsg("NO COOLDOWN BECAUSE OF NO COOLDOWN FOUND with id: " + cooldownId, true);
        }
        //else SsomarDev.testMsg("NO COOLDOWN BECAUSE OF PERMISSION", true);
        return true;
    }

    public void displayCooldownMessage(CommandSender commandSender, double timeLeft, StringPlaceholder sp) {
        String message = cooldownMessage.getValue().get();
        if (message.isEmpty()) {
            message = MessageMain.getInstance().getMessage(sPlugin.getPlugin(), Message.TIME_LEFT);
        }
        sp.getTimePlch().setTimePlcHldr(timeLeft);
        message = sp.replacePlaceholder(message);
        commandSender.sendMessage(StringConverter.coloredString(message));
    }

    /**
     * Add the cooldown for a specific player
     * @param entity         The entity
     * @param sObject   The sObject
     * @param sp      The saved placeholder
     */
    public void addCooldown(Entity entity, @NotNull SObject sObject, @Nullable StringPlaceholder sp) {
        if(sp == null) sp = new StringPlaceholder();

        if (!hasNoCDPerm(entity, sObject) && this.cooldown.getValue(entity.getUniqueId(), sp).get() != 0) {
            Cooldown cooldown = new Cooldown(sPlugin, cooldownId, entity.getUniqueId(), this.cooldown.getValue(entity.getUniqueId(), sp).get(), isCooldownInTicks.getValue(), System.currentTimeMillis(), false);
            cooldown.setPauseFeatures(pauseWhenOffline.getValue(), pausePlaceholdersConditions);
            CooldownsManager.getInstance().addCooldown(cooldown);

            if(SCore.is1v21v2Plus() && enableVisualCooldown.getValue() && entity instanceof HumanEntity && sObject instanceof SObjectBuildable){
                SObjectBuildable sObjectBuildable = (SObjectBuildable) sObject;
                ItemStack item = sObjectBuildable.buildItem(1, Optional.empty());
                HumanEntity humanEntity = (HumanEntity) entity;
                if (this.isCooldownInTicks.getValue())
                    humanEntity.setCooldown(item, cooldown.getTimeLeftFlatValue());
                else
                    humanEntity.setCooldown(item, cooldown.getTimeLeftFlatValue()*20);
                //SsomarDev.testMsg("SET COOLDOWN FOR "+entity.getName()+" mat>> "+item+" >> "+cooldown.getTimeLeftFlatValue(), true);
            }
        }
    }

    /**
     * Add the cooldown for a specific player
     * @param entity         The entity
     * @param sObject   The sObject
     * @param time      The cooldown
     * @param isInTicks Define if the cooldown is in ticks or in secs
     */
    public void addCooldown(Entity entity, @NotNull SObject sObject, int time, boolean isInTicks) {
        // StringPlaceholder sp = new StringPlaceholder();
        // System.out.println("DEBUG TEST CD 2: " + sp.replacePlaceholder(cooldown.getValue().get()));
        if (!hasNoCDPerm(entity, sObject) && time != 0) {
            Cooldown cooldown = new Cooldown(sPlugin, cooldownId, entity.getUniqueId(), time, isInTicks, System.currentTimeMillis(), false);
            cooldown.setPauseFeatures(pauseWhenOffline.getValue(), pausePlaceholdersConditions);
            CooldownsManager.getInstance().addCooldown(cooldown);

            if(SCore.is1v21v2Plus() && enableVisualCooldown.getValue() && entity instanceof HumanEntity && sObject instanceof SObjectBuildable){
                SObjectBuildable sObjectBuildable = (SObjectBuildable) sObject;
                ItemStack item = sObjectBuildable.buildItem(1, Optional.empty());
                HumanEntity humanEntity = (HumanEntity) entity;
                humanEntity.setCooldown(item, cooldown.getTimeLeftFlatValue()*20);
                //SsomarDev.testMsg("SET COOLDOWN FOR "+entity.getName()+" mat>> "+item+" >> "+cooldown.getTimeLeftFlatValue(), true);
            }
        }
    }

    /**
     * Add the cooldown for all players
     * @param time      The cooldown
     * @param isInTicks Define if the cooldown is in ticks or in secs
     */
    public void addGlobalCooldown(int time, boolean isInTicks) {
        Cooldown cooldown = new Cooldown(sPlugin, cooldownId, null, time, isInTicks, System.currentTimeMillis(), true);
        CooldownsManager.getInstance().addCooldown(cooldown);
    }

    public void addGlobalCooldown(@NotNull SObject sObject) {
        StringPlaceholder sp = new StringPlaceholder();
        Cooldown cooldown = new Cooldown(sPlugin, cooldownId, null, this.cooldown.getValue(null, sp).get(), isCooldownInTicks.getValue(), System.currentTimeMillis(), true);
        CooldownsManager.getInstance().addCooldown(cooldown);
    }

    public boolean hasNoCDPerm(Permissible p, SObject sObject) {
        String id = sObject.getId();

        if (sPlugin.isLotOfWork()) return false;

        boolean hasNoCDPerm = p.hasPermission(sPlugin.getName() + ".nocd." + id) ||
                p.hasPermission(sPlugin.getShortName().toLowerCase() + ".nocd." + id) ||
                p.hasPermission(sPlugin.getName() + ".nocd.*") ||
                p.hasPermission(sPlugin.getShortName().toLowerCase() + ".nocd.*");

        if (p.isOp() || p.hasPermission("*")) {
            return !enableCooldownForOp;
        } else {
            return hasNoCDPerm;
        }
    }


    @Override
    public List<String> load(SPlugin plugin, ConfigurationSection config, boolean isPremiumLoading) {
        List<String> errors = new ArrayList<>();
        if (config.isConfigurationSection(getName())) {
            ConfigurationSection section = config.getConfigurationSection(getName());
            errors.addAll(cooldown.load(plugin, section, isPremiumLoading));
            errors.addAll(displayCooldownMessage.load(plugin, section, isPremiumLoading));
            errors.addAll(cancelEventIfInCooldown.load(plugin, section, isPremiumLoading));
            errors.addAll(cooldownMessage.load(plugin, section, isPremiumLoading));
            errors.addAll(isCooldownInTicks.load(plugin, section, isPremiumLoading));
            errors.addAll(pauseWhenOffline.load(plugin, section, isPremiumLoading));
            errors.addAll(pausePlaceholdersConditions.load(plugin, section, isPremiumLoading));
            errors.addAll(enableVisualCooldown.load(plugin, section, isPremiumLoading));
        }
        return errors;
    }

    @Override
    public CooldownFeature clone(FeatureParentInterface newParent) {
        CooldownFeature clone = new CooldownFeature(newParent, getFeatureSettings(), getSPlugin(), isEnableCooldownForOp());
        clone.setCooldown(cooldown.clone(clone));
        clone.setDisplayCooldownMessage(displayCooldownMessage.clone(clone));
        clone.setCancelEventIfInCooldown(cancelEventIfInCooldown.clone(clone));
        clone.setCooldownMessage(cooldownMessage.clone(clone));
        clone.setIsCooldownInTicks(isCooldownInTicks.clone(clone));
        clone.setCooldownId(cooldownId);
        clone.setPauseWhenOffline(pauseWhenOffline.clone(clone));
        clone.setPausePlaceholdersConditions(pausePlaceholdersConditions.clone(clone));
        clone.setEnableVisualCooldown(enableVisualCooldown.clone(clone));
        clone.setGlobal(isGlobal);
        clone.setPremium(isPremium);
        return clone;
    }

    @Override
    public void save(ConfigurationSection config) {
        config.set(getName(), null);
        ConfigurationSection section = config.createSection(getName());
        this.cooldown.save(section);
        this.isCooldownInTicks.save(section);
        this.cooldownMessage.save(section);
        this.displayCooldownMessage.save(section);
        this.cancelEventIfInCooldown.save(section);
        if(!isGlobal) {
            this.pauseWhenOffline.save(section);
            this.pausePlaceholdersConditions.save(section);
        }
        else {
            section.set(FeatureSettingsSCore.pauseWhenOffline.getName(), null);
            section.set(FeatureSettingsSCore.pausePlaceholdersConditions.getName(), null);
        }
        this.enableVisualCooldown.save(section);

        if(isSavingOnlyIfDiffDefault() && section.getKeys(false).isEmpty()){
            config.set(getName(), null);
            return;
        }

        if (GeneralConfig.getInstance().isEnableCommentsInConfig())
            config.setComments(this.getName(), StringConverter.decoloredString(Arrays.asList(getFeatureSettings().getEditorDescriptionBrut())));

    }

    @Override
    public CooldownFeature getValue() {
        return this;
    }

    @Override
    public CooldownFeature initItemParentEditor(GUI gui, int slot) {
        String[] finalDescription = new String[getEditorDescription().length + 6];
        System.arraycopy(getEditorDescription(), 0, finalDescription, 0, getEditorDescription().length);
        finalDescription[finalDescription.length - 6] = GUI.CLICK_HERE_TO_CHANGE;

        finalDescription[finalDescription.length - 5] = "&7Cooldown: &e" + getCooldown().getValue().get();

        if (isCooldownInTicks.getValue()) {
            finalDescription[finalDescription.length - 4] = "&7Cooldown in ticks: &a&l✔";
        } else {
            finalDescription[finalDescription.length - 4] = "&7Cooldown in ticks: &c&l✘";
        }
        if (displayCooldownMessage.getValue()) {
            finalDescription[finalDescription.length - 3] = "&7Display cooldown message: &a&l✔";
        } else {
            finalDescription[finalDescription.length - 3] = "&7Display cooldown message: &c&l✘";
        }
        if (cancelEventIfInCooldown.getValue()) {
            finalDescription[finalDescription.length - 2] = "&7Cancel event if in cooldown: &a&l✔";
        } else {
            finalDescription[finalDescription.length - 2] = "&7Cancel event if in cooldown: &c&l✘";
        }
        finalDescription[finalDescription.length - 1] = "&7Cooldown bypass perm: &b" + sPlugin.getShortName().toLowerCase() + ".nocd.*";

        gui.createItem(getEditorMaterial(), 1, slot, GUI.TITLE_COLOR + getEditorName(), false, false, finalDescription);
        return this;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {

    }

    @Override
    public void reset() {
        this.cooldown = new IntegerFeature(this, Optional.of(0), FeatureSettingsSCore.cooldown);
        this.isCooldownInTicks = new BooleanFeature(this,  false, FeatureSettingsSCore.isCooldownInTicks);
        this.cooldownMessage = new ColoredStringFeature(this, Optional.of("&cYou are in cooldown ! &7(&e%time_H%&6H &e%time_M%&6M &e%time_S%&6S&7)"), FeatureSettingsSCore.cooldownMsg);
        this.displayCooldownMessage = new BooleanFeature(this,  true, FeatureSettingsSCore.displayCooldownMessage);
        this.cancelEventIfInCooldown = new BooleanFeature(this,  false, FeatureSettingsSCore.cancelEventIfInCooldown);
        this.pauseWhenOffline = new BooleanFeature(this, false, FeatureSettingsSCore.pauseWhenOffline);
        this.pausePlaceholdersConditions = new PlaceholderConditionGroupFeature(this, FeatureSettingsSCore.pausePlaceholdersConditions);
        this.enableVisualCooldown = new BooleanFeature(this, false, FeatureSettingsSCore.enableVisualCooldown);
    }

    @Override
    public List<FeatureInterface> getFeatures() {
        List<FeatureInterface> featureInterfaces =  new ArrayList<>(Arrays.asList(cooldown, isCooldownInTicks, cooldownMessage, displayCooldownMessage, cancelEventIfInCooldown));
        if(!isGlobal){
            featureInterfaces.add(pauseWhenOffline);
            featureInterfaces.add(pausePlaceholdersConditions);
        }
        if(SCore.is1v21v2Plus()) featureInterfaces.add(enableVisualCooldown);
        return featureInterfaces;
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
            if (feature instanceof CooldownFeature && feature.getName().equals(getName())) {
                CooldownFeature coolodwn = (CooldownFeature) feature;
                coolodwn.setCooldown(this.cooldown);
                coolodwn.setIsCooldownInTicks(this.isCooldownInTicks);
                coolodwn.setCooldownMessage(this.cooldownMessage);
                coolodwn.setDisplayCooldownMessage(this.displayCooldownMessage);
                coolodwn.setCancelEventIfInCooldown(this.cancelEventIfInCooldown);
                coolodwn.setPauseWhenOffline(this.pauseWhenOffline);
                coolodwn.setPausePlaceholdersConditions(this.pausePlaceholdersConditions);
                coolodwn.setEnableVisualCooldown(this.enableVisualCooldown);
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
