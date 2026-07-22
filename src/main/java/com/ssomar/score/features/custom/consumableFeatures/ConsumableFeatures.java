package com.ssomar.score.features.custom.consumableFeatures;

import com.ssomar.score.SCore;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.features.*;
import com.ssomar.score.features.custom.potioneffects.group.PotionEffectGroupFeature;
import com.ssomar.score.features.custom.potioneffects.potioneffect.PotionEffectFeature;
import com.ssomar.score.features.editor.GenericFeatureParentEditor;
import com.ssomar.score.features.editor.GenericFeatureParentEditorManager;
import com.ssomar.score.features.types.*;
import com.ssomar.score.features.types.list.ListPotionEffectTypeFeature;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.emums.ResetSetting;
import com.ssomar.score.utils.logging.Utils;
import com.ssomar.score.utils.strings.StringConverter;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Setter
public class ConsumableFeatures extends FeatureWithHisOwnEditor<ConsumableFeatures, ConsumableFeatures, GenericFeatureParentEditor, GenericFeatureParentEditorManager> implements FeatureForItemNewPaperComponents {


    private BooleanFeature enable;
    private ItemUseAnimationFeature animation;
    private SoundFeature duringEatingSound;
    private BooleanFeature consumeParticles;
    private IntegerFeature sound;
    private DoubleFeature teleportRandomly;
    private SoundFeature onConsumeSound;
    private BooleanFeature clearAllEffects;
    private ListPotionEffectTypeFeature listPotionEffectTypeToRemove;
    private PotionEffectGroupFeature listPotionEffectsToApply;

    public ConsumableFeatures(FeatureParentInterface parent) {
        super(parent, FeatureSettingsSCore.consumableFeatures);
        reset();
    }

    @Override
    public void reset() {
        enable = new BooleanFeature(this, false, FeatureSettingsSCore.enable);
        animation = new ItemUseAnimationFeature(this, Optional.of(ItemUseAnimation.EAT), FeatureSettingsSCore.animation);
        duringEatingSound = new SoundFeature(this, Optional.ofNullable(Registry.SOUNDS.get(NamespacedKey.fromString("entity.generic.eat"))), FeatureSettingsSCore.sound);
        consumeParticles = new BooleanFeature(this, false, FeatureSettingsSCore.hasConsumeParticles);
        sound = new IntegerFeature(this, Optional.of(3), FeatureSettingsSCore.consumeSeconds);
        teleportRandomly = new DoubleFeature(this, Optional.of(0d), FeatureSettingsSCore.teleportRandomly);
        onConsumeSound = new SoundFeature(this, Optional.ofNullable(Registry.SOUNDS.get(NamespacedKey.fromString("entity.generic.eat"))), FeatureSettingsSCore.onConsumeSound);
        clearAllEffects = new BooleanFeature(this, false, FeatureSettingsSCore.clearAllEffects);
        listPotionEffectTypeToRemove = new ListPotionEffectTypeFeature(this, new ArrayList<>(),FeatureSettingsSCore.listPotionEffectTypeToRemove);
        listPotionEffectsToApply = new PotionEffectGroupFeature(this, true);
    }

    @Override
    public List<String> load(SPlugin plugin, ConfigurationSection config, boolean isPremiumLoading) {
        List<String> errors = new ArrayList<>();
        if (config.isConfigurationSection(getName())) {
            ConfigurationSection section = config.getConfigurationSection(getName());
            errors.addAll(this.enable.load(plugin, section, isPremiumLoading));
            errors.addAll(this.animation.load(plugin, section, isPremiumLoading));
            errors.addAll(this.duringEatingSound.load(plugin, section, isPremiumLoading));
            errors.addAll(this.consumeParticles.load(plugin, section, isPremiumLoading));
            errors.addAll(this.sound.load(plugin, section, isPremiumLoading));
            errors.addAll(this.teleportRandomly.load(plugin, section, isPremiumLoading));
            errors.addAll(this.onConsumeSound.load(plugin, section, isPremiumLoading));
            errors.addAll(this.clearAllEffects.load(plugin, section, isPremiumLoading));
            errors.addAll(this.listPotionEffectTypeToRemove.load(plugin, section, isPremiumLoading));
            errors.addAll(this.listPotionEffectsToApply.load(plugin, section, isPremiumLoading));

        }

        return errors;
    }

    @Override
    public void save(ConfigurationSection config) {
        config.set(getName(), null);
        ConfigurationSection section = config.createSection(getName());
        this.enable.save(section);
        this.animation.save(section);
        this.duringEatingSound.save(section);
        this.consumeParticles.save(section);
        this.sound.save(section);
        this.teleportRandomly.save(section);
        this.onConsumeSound.save(section);
        this.clearAllEffects.save(section);
        this.listPotionEffectTypeToRemove.save(section);
        this.listPotionEffectsToApply.save(section);
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
        int len = 11;
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
        if (duringEatingSound.getValue().isPresent()) {
            finalDescription[finalDescription.length - len] = "&7Eating Sound: &e" + duringEatingSound.getValue().get().getKey();
        } else {
            finalDescription[finalDescription.length - len] = "&7Eating Sound: &enull";
        }
        len--;
        if (consumeParticles.getValue())
            finalDescription[finalDescription.length - len] = "&7Consume Particles: &a&l✔";
        else
            finalDescription[finalDescription.length - len] = "&7Consume Particles: &c&l✘";
        len--;
        finalDescription[finalDescription.length - len] = "&7Consume Seconds: &e" + sound.getValue().get();
        len--;
        finalDescription[finalDescription.length - len] = "&7Chorus FX Val: &e" + teleportRandomly.getValue().get();
        len--;
        if (onConsumeSound.getValue().isPresent()) {
            finalDescription[finalDescription.length - len] = "&7Eating Sound: &e" + onConsumeSound.getValue().get().getKey();
        } else {
            finalDescription[finalDescription.length - len] = "&7Eating Sound: &enull";
        }
        len--;
        if (clearAllEffects.getValue())
            finalDescription[finalDescription.length - len] = "&7Clear All Effects: &a&l✔";
        else
            finalDescription[finalDescription.length - len] = "&7Clear All Effects: &c&l✘";
        len--;
        if (!listPotionEffectTypeToRemove.getValue().isEmpty())
            finalDescription[finalDescription.length - len] = "&7Potion Effects To Clear: &a&l✔";
        else
            finalDescription[finalDescription.length - len] = "&7Potion Effects To Clear: &c&l✘";
        len--;
        finalDescription[finalDescription.length - len] = "&7Potion Effects To Apply: &e" + listPotionEffectsToApply.getEffects().size();


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
        dropFeatures.duringEatingSound = duringEatingSound.clone(dropFeatures);
        dropFeatures.consumeParticles = consumeParticles.clone(dropFeatures);
        dropFeatures.sound = sound.clone(dropFeatures);
        dropFeatures.teleportRandomly = teleportRandomly.clone(dropFeatures);
        dropFeatures.onConsumeSound = onConsumeSound.clone(dropFeatures);
        dropFeatures.clearAllEffects = clearAllEffects.clone(dropFeatures);
        dropFeatures.listPotionEffectTypeToRemove = listPotionEffectTypeToRemove.clone(dropFeatures);
        dropFeatures.listPotionEffectsToApply = listPotionEffectsToApply.clone(dropFeatures);

        return dropFeatures;
    }

    @Override
    public List<FeatureInterface> getFeatures() {
        List<FeatureInterface> features = new ArrayList<>();
        features.add(enable);
        features.add(animation);
        features.add(duringEatingSound);
        features.add(consumeParticles);
        features.add(sound);
        features.add(teleportRandomly);
        features.add(onConsumeSound);
        features.add(clearAllEffects);
        features.add(listPotionEffectTypeToRemove);
        features.add(listPotionEffectsToApply);
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
                hiders.setDuringEatingSound(duringEatingSound);
                hiders.setConsumeParticles(consumeParticles);
                hiders.setSound(sound);
                hiders.setTeleportRandomly(teleportRandomly);
                hiders.setOnConsumeSound(onConsumeSound);
                hiders.setClearAllEffects(clearAllEffects);
                hiders.setListPotionEffectTypeToRemove(listPotionEffectTypeToRemove);
                hiders.setListPotionEffectsToApply(listPotionEffectsToApply);
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
            if (duringEatingSound.getValue().isPresent()) consumable.sound(duringEatingSound.getValue().get().key());
            //else consumable.sound();
            consumable.hasConsumeParticles(consumeParticles.getValue());
            consumable.consumeSeconds(sound.getValue().get());

            if (teleportRandomly.getValue().isPresent() && teleportRandomly.getValue().get() > 0) consumable.addEffect(ConsumeEffect.teleportRandomlyEffect(Float.valueOf(teleportRandomly.getValue().get().toString())));
            if (onConsumeSound.getValue().isPresent()) consumable.addEffect(ConsumeEffect.playSoundConsumeEffect(onConsumeSound.getValue().get().key()));

            if (clearAllEffects.getValue()) consumable.addEffect(ConsumeEffect.clearAllStatusEffects());
            if (!listPotionEffectsToApply.getEffects().isEmpty()) {

                List<PotionEffect> effectsToApply = new ArrayList<>();

                for (PotionEffectFeature pEF : listPotionEffectsToApply.getEffects().values()) {
                    PotionEffect pE = new PotionEffect(
                            pEF.getType().getValue().get(),
                            pEF.getDuration().getValue().get(),
                            pEF.getAmplifier().getValue().get(),
                            pEF.getAmbient().getValue(),
                            pEF.getParticles().getValue(),
                            pEF.getIcon().getValue());
                    effectsToApply.add(pE);
                }

                consumable.addEffect(ConsumeEffect.applyStatusEffects(effectsToApply, 1));
            }
            if (!listPotionEffectTypeToRemove.getValue().isEmpty()) {
                List<TypedKey<PotionEffectType>> typedKeys = listPotionEffectTypeToRemove.getValue().stream()
                        .map(type -> TypedKey.create(RegistryKey.MOB_EFFECT, type.getKey()))
                        .collect(Collectors.toList());
                RegistryKeySet<PotionEffectType> keySet = RegistrySet.keySet(RegistryKey.MOB_EFFECT, typedKeys);
                consumable.addEffect(ConsumeEffect.removeEffects(keySet));
            }

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
            sound.setValue(Optional.of(Math.round(consumable.consumeSeconds())));
            duringEatingSound.setValue(Optional.of(Registry.SOUNDS.get(consumable.sound())));

            // ConsumeEffect properties are stored as interfaces that extend to ConsumeEffect
            for (ConsumeEffect consumeEffect : consumable.consumeEffects()) {
                if (consumeEffect instanceof ConsumeEffect.ClearAllStatusEffects) {
                    // If present, then it's true. The reason is that when I generated a give command
                    // that has this property, enabling/disabling it adds/removes said property from the give command
                    clearAllEffects.setValue(true);
                }
                else if (consumeEffect instanceof ConsumeEffect.ApplyStatusEffects) {
                    ConsumeEffect.ApplyStatusEffects data = (ConsumeEffect.ApplyStatusEffects) consumeEffect;

                    List<PotionEffect> potionEffects = data.effects();

                    int iteration = 0;
                    for (PotionEffect potionEffect : potionEffects) {
                        String id = "pEffect"+iteration;
                        PotionEffectFeature pEF = new PotionEffectFeature(this, id);

                        pEF.getType().setValue(Optional.of(potionEffect.getType()));
                        pEF.getAmplifier().setValue(Optional.of(potionEffect.getAmplifier()));
                        pEF.getDuration().setValue(Optional.of(potionEffect.getDuration()));
                        pEF.getAmbient().setValue(potionEffect.isAmbient());
                        pEF.getParticles().setValue(potionEffect.hasParticles());
                        pEF.getIcon().setValue(potionEffect.hasIcon());

                        listPotionEffectsToApply.getEffects().put(id, pEF);
                        iteration++;
                    }
                }
                else if (consumeEffect instanceof ConsumeEffect.RemoveStatusEffects) {
                    ConsumeEffect.RemoveStatusEffects data = (ConsumeEffect.RemoveStatusEffects) consumeEffect;
                    RegistryKeySet<PotionEffectType> effectList = data.removeEffects();
                    Registry<PotionEffectType> registry = RegistryAccess.registryAccess()
                            .getRegistry(RegistryKey.MOB_EFFECT);
                    List<PotionEffectType> list = new ArrayList<>();
                    for (TypedKey<PotionEffectType> key : effectList) {
                        PotionEffectType type = registry.get(key);
                        if (type != null) {
                            list.add(type);
                        }
                    }
                    listPotionEffectTypeToRemove.setValues(list);
                }
                else if (consumeEffect instanceof ConsumeEffect.PlaySound) {
                    ConsumeEffect.PlaySound data = (ConsumeEffect.PlaySound) consumeEffect;
                    onConsumeSound.setValue(Optional.ofNullable(Registry.SOUNDS.get(data.sound())));
                }
                else if (consumeEffect instanceof ConsumeEffect.TeleportRandomly) {
                    ConsumeEffect.TeleportRandomly data = (ConsumeEffect.TeleportRandomly) consumeEffect;
                    teleportRandomly.setValue(Optional.of(Double.valueOf(String.valueOf(data.diameter()))));
                }

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
