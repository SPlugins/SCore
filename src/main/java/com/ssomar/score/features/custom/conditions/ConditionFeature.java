package com.ssomar.score.features.custom.conditions;

import com.ssomar.score.commands.runnable.ActionInfo;
import com.ssomar.score.features.*;
import com.ssomar.score.features.custom.commands.console.ConsoleCommandsFeature;
import com.ssomar.score.features.editor.GenericFeatureParentEditor;
import com.ssomar.score.features.editor.GenericFeatureParentEditorManager;
import com.ssomar.score.features.types.BooleanFeature;
import com.ssomar.score.features.types.ColoredStringFeature;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.item.MakeItemGlow;
import com.ssomar.score.utils.strings.StringConverter;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
public abstract class ConditionFeature<Y extends FeatureAbstract, T extends ConditionFeature<Y, T>> extends FeatureWithHisOwnEditor<T, T, GenericFeatureParentEditor, GenericFeatureParentEditorManager> {

    private Y condition;
    private ColoredStringFeature errorMessage;
    private BooleanFeature cancelEventIfError;
    private ConsoleCommandsFeature consoleCommandsIfError;
    private BooleanFeature silenceOutput;

    private boolean cancelDefaultValue = false;
    /** Default message of a failed condition, not sent by passive activators (LOOP) that the player did not trigger */
    public static final String DEFAULT_ERROR_MESSAGE = "&4[ERROR] &cYou can't activate this item > invalid condition";
    private static Optional<String> DEFAULT_DEFAULT_ERROR = Optional.of(DEFAULT_ERROR_MESSAGE);

    public ConditionFeature(FeatureParentInterface parent, FeatureSettingsInterface featureSettings) {
        super(parent, featureSettings);
        reset();
    }

    public ConditionFeature(FeatureParentInterface parent, FeatureSettingsInterface featureSettings, boolean cancelDefaultValue) {
        super(parent, featureSettings);
        this.cancelDefaultValue = cancelDefaultValue;
        reset();
    }


    @Override
    public Material getEditorMaterial() {
       return super.getEditorMaterial() == null ? Material.ANVIL : super.getEditorMaterial();
    }

    @Override
    public void reset() {
        this.errorMessage = new ColoredStringFeature(this, DEFAULT_DEFAULT_ERROR, FeatureSettingConditions.getInstance().getErrorMessage(getFeatureSettings().getName()));
        this.cancelEventIfError = new BooleanFeature(this, cancelDefaultValue, FeatureSettingConditions.getInstance().getCancelEventIfError(getFeatureSettings().getName()));
        this.consoleCommandsIfError = new ConsoleCommandsFeature(this, FeatureSettingConditions.getInstance().getConsoleCommandsIfError(getFeatureSettings().getName()), true);
        this.silenceOutput = new BooleanFeature(this, false, FeatureSettingsSCore.silenceOutput);
        subReset();
    }

    public abstract void subReset();

    @Override
    public List<String> load(SPlugin plugin, ConfigurationSection config, boolean isPremiumLoading) {
        List<String> error = new ArrayList<>();
        error.addAll(condition.load(plugin, config, isPremiumLoading));
        error.addAll(errorMessage.load(plugin, config, isPremiumLoading));
        error.addAll(cancelEventIfError.load(plugin, config, isPremiumLoading));
        error.addAll(consoleCommandsIfError.load(plugin, config, isPremiumLoading));
        error.addAll(silenceOutput.load(plugin, config, isPremiumLoading));

        return error;
    }

    @Override
    public void save(ConfigurationSection config) {
        condition.save(config);
        errorMessage.save(config);
        cancelEventIfError.save(config);
        consoleCommandsIfError.save(config);
        silenceOutput.save(config);
    }

    public abstract boolean hasCondition();

    public void addInErrorsInRequest(ConditionRequest request) {
        if (hasErrorMsg())
            request.getErrors().add(errorMessage.getValue().get());
    }

    public boolean hasErrorMsg() {
        return errorMessage.getValue().isPresent() && StringConverter.decoloredString(errorMessage.getValue().get().trim()).length() > 0;
    }

    public void cancelEvent(ConditionRequest request) {
        Event event = request.getEvent();
        if (event != null && cancelEventIfError.getValue() && event instanceof Cancellable)
            ((Cancellable) event).setCancelled(true);
    }

    public void runInvalidCondition(ConditionRequest request){

        addInErrorsInRequest(request);
        cancelEvent(request);
        ActionInfo actionInfo = new ActionInfo("", request.getSp());
        actionInfo.setSilenceOutput(silenceOutput.getValue());
        consoleCommandsIfError.runCommands(actionInfo, "");
    }


    @Override
    public T initItemParentEditor(GUI gui, int slot) {
        String[] finalDescription = new String[getEditorDescription().length + 5];
        System.arraycopy(getEditorDescription(), 0, finalDescription, 0, getEditorDescription().length);

        if (hasCondition())
            finalDescription[finalDescription.length - 5] = "&7Enable: &a&l✔";
        else
            finalDescription[finalDescription.length - 5] = "&7Enable: &c&l✘";

        if (getErrorMessage().getValue().isPresent()) {
            finalDescription[finalDescription.length - 4] = "&7Error Message: &e" + getErrorMessage().getValue().get();
        } else {
            finalDescription[finalDescription.length - 4] = "&7Error Message: &cNO MESSAGE";
        }

        if (getCancelEventIfError().getValue())
            finalDescription[finalDescription.length - 3] = "&7Cancel Event If Error: &a&l✔";
        else
            finalDescription[finalDescription.length - 3] = "&7Cancel Event If Error: &c&l✘";

        finalDescription[finalDescription.length - 2] = "&7Console commands If Error: &e"+consoleCommandsIfError.getCurrentValues().size();

        finalDescription[finalDescription.length - 1] = GUI.CLICK_HERE_TO_CHANGE;

        for (int i = 0; i < finalDescription.length; i++) {
            String command = finalDescription[i];
            if(command == null) command = "";
            if (command.length() > 40) command = command.substring(0, 39) + "...";
            finalDescription[i] = command;
        }

        ItemStack item = new ItemStack(getEditorMaterial());
        if (hasCondition()) {
            ItemMeta meta = item.getItemMeta();
            meta = MakeItemGlow.makeGlow(meta);
            item.setItemMeta(meta);
        }
        gui.createItem(item, 1, slot, GUI.TITLE_COLOR + getEditorName(), false, false, finalDescription);
        return (T) this;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {

    }

    @Override
    public T clone(FeatureParentInterface newParent) {
        T clone = getNewInstance(newParent);
        clone.setCondition((Y) condition.clone(clone));
        clone.setErrorMessage(errorMessage.clone(clone));
        clone.setCancelEventIfError(cancelEventIfError.clone(clone));
        clone.setConsoleCommandsIfError(consoleCommandsIfError.clone(clone));
        clone.setSilenceOutput(silenceOutput.clone(clone));
        return clone;
    }

    public abstract T getNewInstance(FeatureParentInterface newParent);

    @Override
    public List<FeatureInterface> getFeatures() {
        return new ArrayList<>(Arrays.asList(condition, errorMessage, cancelEventIfError, consoleCommandsIfError, silenceOutput));
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
            if (feature.getClass() == getNewInstance(getParent()).getClass() && feature.getName().equals(getName())) {
                T cdt = (T) feature;
                cdt.setCondition(condition);
                cdt.setErrorMessage(errorMessage);
                cdt.setCancelEventIfError(cancelEventIfError);
                cdt.setConsoleCommandsIfError(consoleCommandsIfError);
                cdt.setSilenceOutput(silenceOutput);
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
