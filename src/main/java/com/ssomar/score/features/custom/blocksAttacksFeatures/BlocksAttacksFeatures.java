package com.ssomar.score.features.custom.blocksAttacksFeatures;

import com.ssomar.score.SCore;
import com.ssomar.score.SsomarDev;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.features.*;
import com.ssomar.score.features.custom.blocksAttacksFeatures.DamageReductionFeatures.group.DamageReductionGroupFeature;
import com.ssomar.score.features.editor.GenericFeatureParentEditor;
import com.ssomar.score.features.editor.GenericFeatureParentEditorManager;
import com.ssomar.score.features.types.*;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.backward_compatibility.SoundUtils;
import com.ssomar.score.utils.emums.ResetSetting;
import com.ssomar.score.utils.logging.Utils;
import com.ssomar.score.utils.strings.StringConverter;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BlocksAttacks;
import org.bukkit.damage.DamageType;
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

@Getter
@Setter
public class BlocksAttacksFeatures extends FeatureWithHisOwnEditor<BlocksAttacksFeatures, BlocksAttacksFeatures, GenericFeatureParentEditor, GenericFeatureParentEditorManager> implements FeatureForItemNewPaperComponents {

    private BooleanFeature enable;
    private IntegerFeature blockDelay;
    private SoundFeature blockSound;
    private SoundFeature disableSound;
    private DoubleFeature disableCooldownScale;
    private DamageReductionGroupFeature damageReductions;
    private DamageTypeFeature bypassedBy;

    public BlocksAttacksFeatures(FeatureParentInterface parent) {
        super(parent, FeatureSettingsSCore.blockAttacksFeatures);
        reset();
    }

    @Override
    public void reset() {
        enable = new BooleanFeature(this, false, FeatureSettingsSCore.enable);
        blockDelay = new IntegerFeature(this, Optional.of(0), FeatureSettingsSCore.blockDelay);
        blockSound = new SoundFeature(this, Optional.empty(), FeatureSettingsSCore.blockSound);
        disableSound = new SoundFeature(this, Optional.empty(), FeatureSettingsSCore.disableSound);
        disableCooldownScale = new DoubleFeature(this, Optional.of(1.0), FeatureSettingsSCore.disableCooldownScale);
        damageReductions = new DamageReductionGroupFeature(this, false);
        bypassedBy = new DamageTypeFeature(this, Optional.empty(), FeatureSettingsSCore.bypassedBy);
    }

    @Override
    public List<String> load(SPlugin plugin, ConfigurationSection config, boolean isPremiumLoading) {
        List<String> errors = new ArrayList<>();
        if (config.isConfigurationSection(getName())) {
            ConfigurationSection section = config.getConfigurationSection(getName());
            for(FeatureInterface feature : getFeatures()) {
               errors.addAll(feature.load(plugin, section, isPremiumLoading));
            }
        }

        return errors;
    }

    @Override
    public void save(ConfigurationSection config) {
        config.set(getName(), null);
        ConfigurationSection section = config.createSection(getName());
        for(FeatureInterface feature : getFeatures()) {
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
    public BlocksAttacksFeatures getValue() {
        return this;
    }

    @Override
    public BlocksAttacksFeatures initItemParentEditor(GUI gui, int slot) {
        int len = 8;
        String[] finalDescription = new String[getEditorDescription().length + len];
        System.arraycopy(getEditorDescription(), 0, finalDescription, 0, getEditorDescription().length);
        finalDescription[finalDescription.length - len] = GUI.CLICK_HERE_TO_CHANGE;
        len--;

        if (enable.getValue())
            finalDescription[finalDescription.length - len] = "&7Enabled: &a&l✔";
        else
            finalDescription[finalDescription.length - len] = "&7Disabled: &c&l✘";
        len--;
        finalDescription[finalDescription.length - len] = "&7Block delay: &e" + blockDelay.getValue().get();
        len--;
        finalDescription[finalDescription.length - len] = "&7Block sound: &e" + blockSound.getValue().orElse(null);
        len--;
        finalDescription[finalDescription.length - len] = "&7Disable sound: &e" + disableSound.getValue().orElse(null);
        len--;
        finalDescription[finalDescription.length - len] = "&7Disable cooldown scale: &e" + disableCooldownScale.getValue().get();
        len--;
        finalDescription[finalDescription.length - len] = "&7Damage reductions amount: &e" + damageReductions.asList().size();
        len--;
        finalDescription[finalDescription.length - len] = "&7Bypassed by: &e" + bypassedBy.getValue().orElse(null);
        len--;


        gui.createItem(getEditorMaterial(), 1, slot, GUI.TITLE_COLOR + getEditorName(), false, false, finalDescription);
        return this;
    }

    @Override
    public boolean isAvailable() {
        return SCore.isPaperOrFork() && SCore.is1v21v5Plus() && SCore.hasClass("io.papermc.paper.datacomponent.item.BlocksAttacks") && SCore.hasMethod("io.papermc.paper.datacomponent.item.BlocksAttacks", "damageReductions"); // Because first builds of paper 1.21.5 dont have the feature;
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
        if (isAvailable()) {
            if (enable.getValue()) {
                ItemStack item = args.getItem();
                SsomarDev.testMsg("blockattacks features applyOnItem2", false);
                try {
                    BlocksAttacks.Builder builder = BlocksAttacks.blocksAttacks()
                            .blockSound(blockSound.getValue().isPresent() ? blockSound.getValue().get().getKey() : null)
                            .blockDelaySeconds(blockDelay.getValue().get())
                            .disableSound(disableSound.getValue().isPresent() ? disableSound.getValue().get().getKey() : null)
                            .disableCooldownScale(disableCooldownScale.getValue().get().floatValue())
                            .damageReductions(damageReductions.asList());
                    if (bypassedBy.getValue().isPresent()) {
                        try {
                            // New Paper API (1.21.5 recent builds+) uses RegistryKeySet
                            builder.bypassedBy(bypassedBy.getValueRegistryKeySet());
                        } catch (NoSuchMethodError | NoClassDefFoundError e2) {
                            // Old Paper API uses TagKey - must use reflection since it won't compile against new API
                            try {
                                java.lang.reflect.Method m = builder.getClass().getMethod("bypassedBy", io.papermc.paper.registry.tag.TagKey.class);
                                m.invoke(builder, bypassedBy.getValueTagKey());
                            } catch (Exception e3) {
                                SsomarDev.testMsg("Could not set bypassedBy via TagKey fallback: " + e3.getMessage(), true);
                            }
                        }
                    }
                    item.setData(DataComponentTypes.BLOCKS_ATTACKS, builder);
                } catch (Exception e) {
                    Utils.sendConsoleMsg(SCore.plugin, "&cError while applying the block attacks features on an item");
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void loadFromItem(@NotNull FeatureForItemArgs args) {
        if (isAvailable()) {
            ItemStack item = args.getItem();
            if (item.hasData(DataComponentTypes.BLOCKS_ATTACKS)) {
                BlocksAttacks blocksAttacks = item.getData(DataComponentTypes.BLOCKS_ATTACKS);
                enable.setValue(true);
                blockDelay.setValue(Optional.of((int) blocksAttacks.blockDelaySeconds()));
                // blockSound() / disableSound() are nullable (e.g. custom blocks_attacks components without sounds)
                net.kyori.adventure.key.Key blockSoundKey = blocksAttacks.blockSound();
                blockSound.setValue(blockSoundKey == null ? Optional.empty() : Optional.ofNullable(SoundUtils.getSound(blockSoundKey.value())));
                net.kyori.adventure.key.Key disableSoundKey = blocksAttacks.disableSound();
                disableSound.setValue(disableSoundKey == null ? Optional.empty() : Optional.ofNullable(SoundUtils.getSound(disableSoundKey.value())));
                disableCooldownScale.setValue(Optional.of((double) blocksAttacks.disableCooldownScale()));
                damageReductions.setValues(blocksAttacks.damageReductions());
                // bypassedBy() returns RegistryKeySet on new Paper, TagKey on old Paper (1.21.5 - 1.21.x).
                // The return type is part of the compiled method descriptor, so a direct call compiled against
                // one API throws NoSuchMethodError on the other -> it must be invoked via reflection.
                Object bypassedByValue = null;
                try {
                    bypassedByValue = BlocksAttacks.class.getMethod("bypassedBy").invoke(blocksAttacks);
                } catch (Exception e) {
                    SsomarDev.testMsg("Could not read bypassedBy from the blocks_attacks component: " + e.getMessage(), true);
                }
                if (bypassedByValue instanceof io.papermc.paper.registry.set.RegistryKeySet) {
                    bypassedBy.setValue((io.papermc.paper.registry.set.RegistryKeySet<DamageType>) bypassedByValue);
                } else if (bypassedByValue instanceof io.papermc.paper.registry.tag.TagKey) {
                    bypassedBy.setValue((io.papermc.paper.registry.tag.TagKey<DamageType>) bypassedByValue);
                }
            }
        }
    }

    @Override
    public ResetSetting getResetSetting() {
        return ResetSetting.BLOCK_ATTACKS;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {

    }

    @Override
    public BlocksAttacksFeatures clone(FeatureParentInterface newParent) {
        BlocksAttacksFeatures dropFeatures = new BlocksAttacksFeatures(newParent);
        dropFeatures.setEnable(enable.clone(dropFeatures));
        dropFeatures.setBlockDelay(blockDelay.clone(dropFeatures));
        dropFeatures.setBlockSound(blockSound.clone(dropFeatures));
        dropFeatures.setDisableSound(disableSound.clone(dropFeatures));
        dropFeatures.setDisableCooldownScale(disableCooldownScale.clone(dropFeatures));
        dropFeatures.setDamageReductions(damageReductions.clone(dropFeatures));
        dropFeatures.setBypassedBy(bypassedBy.clone(dropFeatures));

        return dropFeatures;
    }

    @Override
    public List<FeatureInterface> getFeatures() {
        List<FeatureInterface> features = new ArrayList<>();
        features.add(enable);
        features.add(blockDelay);
        features.add(blockSound);
        features.add(disableSound);
        features.add(disableCooldownScale);
        features.add(damageReductions);
        features.add(bypassedBy);

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
            if (feature instanceof BlocksAttacksFeatures) {
                BlocksAttacksFeatures hiders = (BlocksAttacksFeatures) feature;
                hiders.setEnable(enable);
                hiders.setBlockDelay(blockDelay);
                hiders.setBlockSound(blockSound);
                hiders.setDisableSound(disableSound);
                hiders.setDisableCooldownScale(disableCooldownScale);
                hiders.setDamageReductions(damageReductions);
                hiders.setBypassedBy(bypassedBy);

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
