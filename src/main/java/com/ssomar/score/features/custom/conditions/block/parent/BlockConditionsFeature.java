package com.ssomar.score.features.custom.conditions.block.parent;

import com.ssomar.score.SCore;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.features.FeatureInterface;
import com.ssomar.score.features.FeatureParentInterface;
import com.ssomar.score.features.FeatureSettingsInterface;
import com.ssomar.score.features.FeatureWithHisOwnEditor;
import com.ssomar.score.features.custom.conditions.block.BlockConditionFeature;
import com.ssomar.score.features.custom.conditions.block.BlockConditionRequest;
import com.ssomar.score.features.custom.conditions.block.condition.*;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.messages.SendMessage;
import com.ssomar.score.utils.strings.StringConverter;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
public class BlockConditionsFeature extends FeatureWithHisOwnEditor<BlockConditionsFeature, BlockConditionsFeature, BlockConditionsFeatureEditor, BlockConditionsFeatureEditorManager> {

    private List<BlockConditionFeature> conditions;

    public BlockConditionsFeature(FeatureParentInterface parent, FeatureSettingsInterface featureSettings) {
        super(parent, featureSettings);
        reset();
    }

    @Override
    public void reset() {
        conditions = new ArrayList<>();
        /* Boolean features */
        conditions.add(new IfIsPowered(this));
        conditions.add(new IfMustBeNotPowered(this));
        conditions.add(new IfMustBeNatural(this));
        conditions.add(new IfMustBeNotNatural(this));
        conditions.add(new IfMustBeEB(this));
        conditions.add(new IfPlayerMustBeOnTheBlock(this));
        conditions.add(new IfNoPlayerMustBeOnTheBlock(this));
        if (!SCore.is1v12Less()) {
            conditions.add(new IfPlantFullyGrown(this));
            conditions.add(new IfPlantNotFullyGrown(this));
        }
        if (!SCore.is1v11Less()) {
            conditions.add(new IfContainerEmpty(this));
            conditions.add(new IfContainerNotEmpty(this));
            conditions.add(new IfContainerContains(this));
            conditions.add(new IfContainerContainsEI(this));
            conditions.add(new IfContainerContainsSellableItem(this));
        }

        /* Number condition features */
        if (!SCore.is1v12Less()) conditions.add(new IfBlockAge(this));
        conditions.add(new IfBlockLocationX(this));
        conditions.add(new IfBlockLocationY(this));
        conditions.add(new IfBlockLocationZ(this));
        conditions.add(new IfBlockWorld(this));
        conditions.add(new IfUsage(this));

        conditions.add(new AroundBlockConditions(this));

    }

    public boolean verifConditions(Block block, Optional<Player> playerOpt, SendMessage messageSender, @Nullable Event event) {


        BlockConditionRequest request = new BlockConditionRequest(block, playerOpt, messageSender.getSp(), event);
        for (BlockConditionFeature condition : conditions) {
            if (!condition.verifCondition(request)) {
                if (playerOpt.isPresent()) {
                    for (String error : request.getErrorsFinal()) {
                        messageSender.sendMessage(playerOpt.get(), error);
                    }
                }
                return false;
            }
        }
        return true;
    }


    @Override
    public List<String> load(SPlugin plugin, ConfigurationSection config, boolean isPremiumLoading) {
        List<String> error = new ArrayList<>();
        if (config.isConfigurationSection(getName())) {
            ConfigurationSection section = config.getConfigurationSection(getName());
            for (BlockConditionFeature condition : conditions) {
                error.addAll(condition.load(plugin, section, isPremiumLoading));
            }
        }

        return error;
    }

    @Override
    public void save(ConfigurationSection config) {
        config.set(getName(), null);
        ConfigurationSection section = config.createSection(getName());
        for (BlockConditionFeature condition : conditions) {
            condition.save(section);
        }
        if(isSavingOnlyIfDiffDefault() && section.getKeys(false).isEmpty()){
            config.set(getName(), null);
            return;
        }

        if (GeneralConfig.getInstance().isEnableCommentsInConfig())
            config.setComments(this.getName(), StringConverter.decoloredString(Arrays.asList(getFeatureSettings().getEditorDescriptionBrut())));

    }

    @Override
    public BlockConditionsFeature getValue() {
        return this;
    }

    @Override
    public BlockConditionsFeature initItemParentEditor(GUI gui, int slot) {
        String[] finalDescription = new String[getEditorDescription().length + 2];
        System.arraycopy(getEditorDescription(), 0, finalDescription, 0, getEditorDescription().length);
        finalDescription[finalDescription.length - 2] = "&7Block condition(s) enabled: &e" + getBlockConditionEnabledCount();
        finalDescription[finalDescription.length - 1] = GUI.CLICK_HERE_TO_CHANGE;


        gui.createItem(getEditorMaterial(), 1, slot, GUI.TITLE_COLOR + getEditorName(), false, false, finalDescription);
        return this;
    }

    public int getBlockConditionEnabledCount() {
        int i = 0;
        for (BlockConditionFeature condition : conditions) {
            if (condition.hasCondition()) {
                i++;
            }
        }
        return i;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {

    }

    @Override
    public BlockConditionsFeature clone(FeatureParentInterface newParent) {
        BlockConditionsFeature clone = new BlockConditionsFeature(newParent, getFeatureSettings());
        List<BlockConditionFeature> clones = new ArrayList<>();
        for (BlockConditionFeature condition : conditions) {
            clones.add((BlockConditionFeature) condition.clone(clone));
        }
        clone.setConditions(clones);
        return clone;
    }

    @Override
    public List<FeatureInterface> getFeatures() {
        return new ArrayList<>(conditions);
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
            if (feature instanceof BlockConditionsFeature && feature.getName().equals(getName())) {
                BlockConditionsFeature bCF = (BlockConditionsFeature) feature;
                List<BlockConditionFeature> clones = new ArrayList<>();
                for (BlockConditionFeature condition : conditions) {
                    clones.add((BlockConditionFeature) condition);
                }
                bCF.setConditions(clones);
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
        BlockConditionsFeatureEditorManager.getInstance().startEditing(player, this);
    }

}
