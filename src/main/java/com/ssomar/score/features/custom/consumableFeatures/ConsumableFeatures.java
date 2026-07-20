package com.ssomar.score.features.custom.consumableFeatures;

import com.ssomar.score.SCore;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.features.*;
import com.ssomar.score.features.editor.GenericFeatureParentEditor;
import com.ssomar.score.features.editor.GenericFeatureParentEditorManager;
import com.ssomar.score.features.types.BooleanFeature;
import com.ssomar.score.features.types.IntegerFeature;
import com.ssomar.score.features.types.ItemUseAnimationFeature;
import com.ssomar.score.features.types.SoundFeature;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.emums.ResetSetting;
import com.ssomar.score.utils.logging.Utils;
import com.ssomar.score.utils.strings.StringConverter;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
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
public class ConsumableFeatures extends FeatureWithHisOwnEditor<ConsumableFeatures, ConsumableFeatures, GenericFeatureParentEditor, GenericFeatureParentEditorManager> implements FeatureForItemNewPaperComponents {


    private BooleanFeature enable;
    private ItemUseAnimationFeature animation;
    private SoundFeature sound;
    private BooleanFeature consumeParticles;
    private IntegerFeature consumeSeconds;
    private OnConsumeEffectsFeatures onConsumeEffects;
    private IntegerFeature teleportRandomly;
    private SoundFeature onConsumeSound;

    public ConsumableFeatures(FeatureParentInterface parent) {
        super(parent, FeatureSettingsSCore.consumableFeatures);
        reset();
    }

    @Override
    public void reset() {
        enable = new BooleanFeature(this, false, FeatureSettingsSCore.enable);
        animation = new ItemUseAnimationFeature(this, Optional.of(ItemUseAnimation.EAT), FeatureSettingsSCore.animation);
        sound = new SoundFeature(this, Optional.empty(), FeatureSettingsSCore.sound);
        consumeParticles = new BooleanFeature(this, false, FeatureSettingsSCore.hasConsumeParticles);
        consumeSeconds = new IntegerFeature(this, Optional.of(3), FeatureSettingsSCore.consumeSeconds);
        onConsumeEffects = new OnConsumeEffectsFeatures(this, FeatureSettingsSCore.onConsumeEffects);
        teleportRandomly = new IntegerFeature(this, Optional.of(0), FeatureSettingsSCore.teleportRandomly);
        onConsumeSound = new SoundFeature(this, Optional.empty(), FeatureSettingsSCore.onConsumeSound);
    }

    @Override
    public List<String> load(SPlugin plugin, ConfigurationSection config, boolean isPremiumLoading) {
        List<String> errors = new ArrayList<>();
        if (config.isConfigurationSection(getName())) {
            ConfigurationSection section = config.getConfigurationSection(getName());
            errors.addAll(this.enable.load(plugin, section, isPremiumLoading));
            errors.addAll(this.animation.load(plugin, section, isPremiumLoading));
            errors.addAll(this.sound.load(plugin, section, isPremiumLoading));
            errors.addAll(this.consumeParticles.load(plugin, section, isPremiumLoading));
            errors.addAll(this.consumeSeconds.load(plugin, section, isPremiumLoading));
            errors.addAll(this.onConsumeEffects.load(plugin, section, isPremiumLoading));
            errors.addAll(this.teleportRandomly.load(plugin, section, isPremiumLoading));
            errors.addAll(this.onConsumeSound.load(plugin, section, isPremiumLoading));

        }

        return errors;
    }

    @Override
    public void save(ConfigurationSection config) {
        config.set(getName(), null);
        ConfigurationSection section = config.createSection(getName());
        this.enable.save(section);
        this.animation.save(section);
        this.sound.save(section);
        this.consumeParticles.save(section);
        this.consumeSeconds.save(section);
        this.onConsumeEffects.save(section);
        this.teleportRandomly.save(section);
        this.onConsumeSound.save(section);
        if(isSavingOnlyIfDiffDefault() && section.getKeys(false).isEmpty()){
            config.set(getName(), null);
            return;
        }

        if (GeneralConfig.getInstance().isEnableCommentsInConfig())
            config.setComments(this.getName(), StringConverter.decoloredString(Arrays.asList(getFeatureSettings().getEditorDescriptionBrut())));
    }

    @Override
    public ConsumableFeatures getValue() {
        return this;
    }

    @Override
    public ConsumableFeatures initItemParentEditor(GUI gui, int slot) {
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
        finalDescription[finalDescription.length - len] = "&7Animation: &e" + animation.getValue().orElse(ItemUseAnimation.EAT).name();
        len--;
        if (sound.getValue().isPresent()) {
            finalDescription[finalDescription.length - len] = "&7Eating Sound: &e" + sound.getValue().get().getKey();
        } else {
            finalDescription[finalDescription.length - len] = "&7Eating Sound: &enull";
        }
        len--;
        if (consumeParticles.getValue())
            finalDescription[finalDescription.length - len] = "&7Consume Particles: &a&l✔";
        else
            finalDescription[finalDescription.length - len] = "&7Consume Particles: &c&l✘";
        len--;
        finalDescription[finalDescription.length - len] = "&7Consume Seconds: &e" + consumeSeconds.getValue().get();
        len--;
        finalDescription[finalDescription.length - len] = "&7Chorus FX Val: &e" + teleportRandomly.getValue().get();
        len--;
        if (onConsumeSound.getValue().isPresent()) {
            finalDescription[finalDescription.length - len] = "&7Eating Sound: &e" + onConsumeSound.getValue().get().getKey();
        } else {
            finalDescription[finalDescription.length - len] = "&7Eating Sound: &enull";
        }


        gui.createItem(getEditorMaterial(), 1, slot, GUI.TITLE_COLOR + getEditorName(), false, false, finalDescription);
        return this;
    }

    public void applyOnItemMeta(ItemMeta meta){}

    @Override
    public void updateItemParentEditor(GUI gui) {}

    @Override
    public ConsumableFeatures clone(FeatureParentInterface newParent) {
        ConsumableFeatures dropFeatures = new ConsumableFeatures(newParent);
        dropFeatures.enable = enable.clone(dropFeatures);
        dropFeatures.animation = animation.clone(dropFeatures);
        dropFeatures.sound = sound.clone(dropFeatures);
        dropFeatures.consumeParticles = consumeParticles.clone(dropFeatures);
        dropFeatures.consumeSeconds = consumeSeconds.clone(dropFeatures);
        dropFeatures.onConsumeEffects = onConsumeEffects.clone(dropFeatures);
        dropFeatures.teleportRandomly = teleportRandomly.clone(dropFeatures);
        dropFeatures.onConsumeSound = onConsumeSound.clone(dropFeatures);

        return dropFeatures;
    }

    @Override
    public List<FeatureInterface> getFeatures() {
        List<FeatureInterface> features = new ArrayList<>();
        features.add(enable);
        features.add(animation);
        features.add(sound);
        features.add(consumeParticles);
        features.add(consumeSeconds);
        features.add(onConsumeEffects);
        features.add(teleportRandomly);
        features.add(onConsumeSound);
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
            if (feature instanceof ConsumableFeatures) {
                ConsumableFeatures hiders = (ConsumableFeatures) feature;
                hiders.setEnable(enable);
                hiders.setAnimation(animation);
                hiders.setSound(sound);
                hiders.setConsumeParticles(consumeParticles);
                hiders.setConsumeSeconds(consumeSeconds);
                hiders.setOnConsumeEffects(onConsumeEffects);
                hiders.setTeleportRandomly(teleportRandomly);
                hiders.setOnConsumeSound(onConsumeSound);
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

    @Override
    public void applyOnItem(@NotNull FeatureForItemArgs args) {
        ItemStack item = args.getItem();
        try {
            if (!enable.getValue()) return;
            Consumable.Builder consumable = Consumable.consumable().animation(animation.getValue().orElse(ItemUseAnimation.EAT));
            if (sound.getValue().isPresent()) consumable.sound(sound.getValue().get().key());
            //else consumable.sound();
            consumable.hasConsumeParticles(consumeParticles.getValue());
            consumable.consumeSeconds(consumeSeconds.getValue().get());

            if (teleportRandomly.getValue().isPresent() && teleportRandomly.getValue().get() > 0) consumable.addEffect(ConsumeEffect.teleportRandomlyEffect(teleportRandomly.getValue().get()));
            if (onConsumeSound.getValue().isPresent()) consumable.addEffect(ConsumeEffect.playSoundConsumeEffect(onConsumeSound.getValue().get().key()));

            item.setData(DataComponentTypes.CONSUMABLE, consumable);
        } catch (Exception e) {
            Utils.sendConsoleMsg("&4Error while applying ConsumableFeatures , it's probably due to Paper");
            e.printStackTrace();
        }
    }

    @Override
    public void loadFromItem(@NotNull FeatureForItemArgs args) {
        Consumable consumable = args.getItem().getData(DataComponentTypes.CONSUMABLE);
        if(consumable != null) {
            animation.setValue(Optional.of(consumable.animation()));
            consumeParticles.setValue(consumable.hasConsumeParticles());
            consumeSeconds.setValue(Optional.of(Math.round(consumable.consumeSeconds())));

            /// temp for now. idk what the consume effect values look like
            for (ConsumeEffect consumeEffect : consumable.consumeEffects()) {
                System.out.println(consumeEffect);
            }


            enable.setValue(true);
        }

    }

    @Override
    public boolean isAvailable() {
        return SCore.is1v21v4Plus() && SCore.isPaperOrFork();
    }

    @Override
    public boolean isApplicable(@NotNull FeatureForItemArgs args) {
        return true;
    }

    @Override
    public void applyOnItemMeta(@NotNull FeatureForItemArgs args) {}

    @Override
    public void loadFromItemMeta(@NotNull FeatureForItemArgs args) {}

    @Override
    public ResetSetting getResetSetting() {
        return ResetSetting.CONSUMABLE;
    }
}
