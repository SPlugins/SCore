package com.ssomar.score.features.custom.toolrules.group;

import java.util.ArrayList;
import com.ssomar.score.utils.ToolsListMaterial;
import com.ssomar.score.SCore;
import com.ssomar.score.features.*;
import com.ssomar.score.features.custom.toolrules.toolrule.ToolRuleFeature;
import com.ssomar.score.features.types.BooleanFeature;
import com.ssomar.score.features.types.DoubleFeature;
import com.ssomar.score.features.types.IntegerFeature;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.emums.ResetSetting;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

@Getter
@Setter
public class ToolRulesGroupFeature extends FeatureWithHisOwnEditor<ToolRulesGroupFeature, ToolRulesGroupFeature, ToolRulesGroupFeatureEditor, ToolRulesGroupFeatureEditorManager> implements FeaturesGroup<ToolRuleFeature>, FeatureForItem {

    private BooleanFeature enable;
    private Map<String, ToolRuleFeature> toolRules;
    private DoubleFeature defaultMiningSpeed;
    private IntegerFeature damagePerBlock;
    private boolean notSaveIfNoValue;

    private int premiumLimit = 5;

    public ToolRulesGroupFeature(FeatureParentInterface parent, boolean notSaveIfNoValue) {
        super(parent, FeatureSettingsSCore.toolRules);
        this.notSaveIfNoValue = notSaveIfNoValue;
        reset();
    }

    @Override
    public void reset() {
        this.enable = new BooleanFeature(getParent(), false, FeatureSettingsSCore.enable);
        this.toolRules = new LinkedHashMap<>();
        this.defaultMiningSpeed = new DoubleFeature(this, Optional.of(1.0), FeatureSettingsSCore.defaultMiningSpeed);
        this.damagePerBlock = new IntegerFeature(this, Optional.of(1), FeatureSettingsSCore.damagePerBlock);
    }

    @Override
    public List<String> load(SPlugin plugin, ConfigurationSection config, boolean isPremiumLoading) {
        List<String> error = new ArrayList<>();
        if (config.isConfigurationSection(this.getName())) {
            ConfigurationSection enchantmentsSection = config.getConfigurationSection(this.getName());
            for (String attributeID : enchantmentsSection.getKeys(false)) {

                if (attributeID.equals(enable.getName()) || attributeID.equals(defaultMiningSpeed.getName()) || attributeID.equals(damagePerBlock.getName()))
                    continue;

                if (toolRules.size() >= premiumLimit && !isPremium()) {
                    error.add("&cERROR, Couldn't load the Tool rule of " + attributeID + " from config, &7&o" + getParent().getParentInfo() + " &6>> Because it requires the premium version to have more than 2 tool rule !");
                    return error;
                }
                ToolRuleFeature attribute = new ToolRuleFeature(this, attributeID);
                List<String> subErrors = attribute.load(plugin, enchantmentsSection, isPremiumLoading);
                if (subErrors.size() > 0) {
                    error.addAll(subErrors);
                    continue;
                }
                toolRules.put(attributeID, attribute);
            }
            error.addAll(enable.load(plugin, enchantmentsSection, isPremiumLoading));
            error.addAll(defaultMiningSpeed.load(plugin, enchantmentsSection, isPremiumLoading));
            error.addAll(damagePerBlock.load(plugin, enchantmentsSection, isPremiumLoading));
        }
        return error;
    }

    @Override
    public void save(ConfigurationSection config) {
        config.set(this.getName(), null);
        if (notSaveIfNoValue && toolRules.size() == 0) return;
        ConfigurationSection attributesSection = config.createSection(this.getName());
        for (String enchantmentID : toolRules.keySet()) {
            toolRules.get(enchantmentID).save(attributesSection);
        }
        enable.save(attributesSection);
        defaultMiningSpeed.save(attributesSection);
        damagePerBlock.save(attributesSection);
    }

    @Override
    public ToolRulesGroupFeature getValue() {
        return this;
    }

    @Override
    public ToolRulesGroupFeature initItemParentEditor(GUI gui, int slot) {
        String[] finalDescription = new String[getEditorDescription().length + 5];
        System.arraycopy(getEditorDescription(), 0, finalDescription, 0, getEditorDescription().length);
        finalDescription[finalDescription.length - 5] = GUI.CLICK_HERE_TO_CHANGE;
        finalDescription[finalDescription.length - 4] = enable.getValue() ? "&7&oEnabled: &a&l✔" : "&7&oEnabled: &c&l✘";
        finalDescription[finalDescription.length - 3] = "&7&oTool rule(s) added: &e" + toolRules.size();
        finalDescription[finalDescription.length - 2] = "&7&oDefault Mining Speed: &e" + defaultMiningSpeed.getValue().get();
        finalDescription[finalDescription.length - 1] = "&7&oDamage per block: &e" + damagePerBlock.getValue().get();

        gui.createItem(getEditorMaterial(), 1, slot, GUI.TITLE_COLOR + getEditorName(), false, false, finalDescription);
        return this;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {

    }

    @Override
    public ToolRuleFeature getTheChildFeatureClickedParentEditor(String featureClicked) {
        for (ToolRuleFeature x : toolRules.values()) {
            if (x.isTheFeatureClickedParentEditor(featureClicked)) return x;
        }
        return null;
    }

    @Override
    public ToolRulesGroupFeature clone(FeatureParentInterface newParent) {
        ToolRulesGroupFeature eF = new ToolRulesGroupFeature(newParent, isNotSaveIfNoValue());
        HashMap<String, ToolRuleFeature> newAttributes = new LinkedHashMap<>();
        for (String x : toolRules.keySet()) {
            newAttributes.put(x, toolRules.get(x).clone(eF));
        }
        eF.setToolRules(newAttributes);
        eF.setEnable(enable.clone(newParent));
        eF.setDefaultMiningSpeed(defaultMiningSpeed.clone(newParent));
        eF.setDamagePerBlock(damagePerBlock.clone(newParent));
        return eF;
    }

    @Override
    public List<FeatureInterface> getFeatures() {
        List<FeatureInterface> features = new ArrayList<>(toolRules.values());
        features.add(enable);
        features.add(defaultMiningSpeed);
        features.add(damagePerBlock);
        return features;
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
            if (feature instanceof ToolRulesGroupFeature) {
                ToolRulesGroupFeature eF = (ToolRulesGroupFeature) feature;
                eF.setToolRules(this.getToolRules());
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
        ToolRulesGroupFeatureEditorManager.getInstance().startEditing(player, this);
    }

    @Override
    public void createNewFeature(@NotNull Player editor) {
        if (!isPremium() && toolRules.size() >= premiumLimit) return;
        String baseId = "toolRule";
        for (int i = 0; i < 1000; i++) {
            String id = baseId + i;
            if (!toolRules.containsKey(id)) {
                ToolRuleFeature eF = new ToolRuleFeature(this, id);
                toolRules.put(id, eF);
                eF.openEditor(editor);
                break;
            }
        }
    }

    @Override
    public void deleteFeature(@NotNull Player editor, ToolRuleFeature feature) {
        toolRules.remove(feature.getId());
    }

    @Override
    public boolean isAvailable() {
        // in reality it should be 1.20.6
        return SCore.is1v20v5Plus();
    }

    @Override
    public boolean isApplicable(@NotNull FeatureForItemArgs args) {
        return true;
    }

    @Override
    public void applyOnItemMeta(@NotNull FeatureForItemArgs args) {

        if (!isAvailable() || !isApplicable(args)) return;

        if (getEnable().getValue()) {
            ItemMeta meta = args.getMeta();
            ToolComponent tool = meta.getTool();
            tool.setDamagePerBlock(getDamagePerBlock().getValue().get());
            tool.setDefaultMiningSpeed(getDefaultMiningSpeed().getValue().get().floatValue());

            for (ToolRuleFeature rule : getToolRules().values()) {
                float miningSpeed = rule.getMiningSpeed().getValue().get().floatValue();
                boolean correctForDrops = rule.getCorrectForDrops().getValue();
                List<Tag<Material>> tags = rule.getMaterials().asTagList();
                // One rule per material tag
                for (Tag<Material> tag : tags) {
                    tool.addRule(tag, miningSpeed, correctForDrops);
                }
                List<Material> blocks = blocksOnly(rule.getMaterials().asMaterialList());
                if (!blocks.isEmpty()) tool.addRule(blocks, miningSpeed, correctForDrops);
            }
            meta.setTool(tool);
        }
    }

    /**
     * The tool component only accepts blocks: an item material (POTATO, or a group such as ALL_CROPS that also lists
     * POTATO/CARROT) made Paper throw and the whole item failed to build (give, /ei editor...). An item is replaced by its
     * crop block when it has one (POTATO -> POTATOES), otherwise skipped.
     */
    public static List<Material> blocksOnly(List<Material> materials) {
        List<Material> blocks = new ArrayList<>();
        for (Material material : materials) {
            Material block = material.isBlock() ? material : ToolsListMaterial.getInstance().getBlockMaterialOfItem(material);
            if (block != null && block.isBlock() && !blocks.contains(block)) blocks.add(block);
        }
        return blocks;
    }

    @Override
    public void loadFromItemMeta(@NotNull FeatureForItemArgs args) {

        if (!isAvailable() || !isApplicable(args)) return;

        ItemMeta meta = args.getMeta();
        ToolComponent tool = meta.getTool();
        if (tool != null) {
            getDamagePerBlock().setValue(Optional.of(tool.getDamagePerBlock()));
            getDefaultMiningSpeed().setValue(Optional.of((double) tool.getDefaultMiningSpeed()));

            int i = 0;
            for (ToolComponent.ToolRule toolRule : tool.getRules()) {
                ToolRuleFeature rule = new ToolRuleFeature(this, "toolRule" + i);
                Collection<Material> materials = toolRule.getBlocks();
                List<String> materialNames = new ArrayList<>();
                for (Material material : materials) {
                   materialNames.add(material.toString());
                }
                rule.getMaterials().setValues(materialNames);
                if (toolRule.getSpeed() != null)
                    rule.getMiningSpeed().setValue(Optional.of((double) toolRule.getSpeed()));
                if (toolRule.isCorrectForDrops() != null)
                    rule.getCorrectForDrops().setValue(toolRule.isCorrectForDrops());
                toolRules.put(rule.getId(), rule);
                i++;
            }
        }
    }

    @Override
    public ResetSetting getResetSetting() {
        return ResetSetting.TOOL_RULES;
    }
}
