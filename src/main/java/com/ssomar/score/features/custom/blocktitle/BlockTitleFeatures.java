package com.ssomar.score.features.custom.blocktitle;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Modules.Holograms.CMIHologram;
import com.ssomar.score.SCore;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.features.FeatureInterface;
import com.ssomar.score.features.FeatureParentInterface;
import com.ssomar.score.features.FeatureSettingsSCore;
import com.ssomar.score.features.FeatureWithHisOwnEditor;
import com.ssomar.score.features.editor.GenericFeatureParentEditor;
import com.ssomar.score.features.editor.GenericFeatureParentEditorManager;
import com.ssomar.score.features.types.BooleanFeature;
import com.ssomar.score.features.types.DoubleFeature;
import com.ssomar.score.features.types.list.ListColoredStringFeature;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.placeholders.StringPlaceholder;
import com.ssomar.score.utils.strings.StringConverter;
import eu.decentsoftware.holograms.api.DHAPI;
import lombok.Getter;
import lombok.Setter;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

@Getter
@Setter
public class BlockTitleFeatures extends FeatureWithHisOwnEditor<BlockTitleFeatures, BlockTitleFeatures, GenericFeatureParentEditor, GenericFeatureParentEditorManager> {

    private static final Map<Location, UUID> textDisplayCache = new HashMap<>();

    private ListColoredStringFeature title;
    private DoubleFeature titleAjustement;
    private BooleanFeature activeTitle;

    public BlockTitleFeatures(FeatureParentInterface parent) {
        super(parent, FeatureSettingsSCore.titleFeatures);
        reset();
    }

    @Override
    public void reset() {
        this.activeTitle = new BooleanFeature(this, false, FeatureSettingsSCore.activeTitle);
        this.title = new ListColoredStringFeature(this, new ArrayList<>(), FeatureSettingsSCore.title, Optional.empty());
        this.titleAjustement = new DoubleFeature(this, Optional.of(0.5), FeatureSettingsSCore.titleAdjustment);
    }

    @Override
    public List<String> load(SPlugin plugin, ConfigurationSection config, boolean isPremiumLoading) {
        List<String> error = new ArrayList<>();
        if (config.isConfigurationSection(getName())) {
            error.addAll(this.activeTitle.load(plugin, config.getConfigurationSection(getName()), isPremiumLoading));
            error.addAll(this.title.load(plugin, config.getConfigurationSection(getName()), isPremiumLoading));
            error.addAll(this.titleAjustement.load(plugin, config.getConfigurationSection(getName()), isPremiumLoading));
        }

        return error;
    }

    @Override
    public void save(ConfigurationSection config) {
        config.set(getName(), null);
        ConfigurationSection section = config.createSection(getName());
        this.activeTitle.save(section);
        this.title.save(section);
        this.titleAjustement.save(section);
    }

    public BlockTitleFeatures getValue() {
        return this;
    }

    @Override
    public BlockTitleFeatures initItemParentEditor(GUI gui, int slot) {
        String[] finalDescription = new String[getEditorDescription().length + 4];
        System.arraycopy(getEditorDescription(), 0, finalDescription, 0, getEditorDescription().length);
        if (isRequirePremium() && !isPremium()) finalDescription[finalDescription.length - 4] = GUI.PREMIUM;
        else finalDescription[finalDescription.length - 4] = GUI.CLICK_HERE_TO_CHANGE;
        if (activeTitle.getValue())
            finalDescription[finalDescription.length - 3] = "&7Active title: &a&l✔";
        else
            finalDescription[finalDescription.length - 3] = "&7Active title: &c&l✘";

        finalDescription[finalDescription.length - 2] = "&7Title lines: &e&l" + title.getValues().size();

        finalDescription[finalDescription.length - 1] = "&7Title Ajustement: &e" + titleAjustement.getValue().get();

        gui.createItem(getEditorMaterial(), 1, slot, GUI.TITLE_COLOR + getEditorName(), false, false, finalDescription);
        return this;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {

    }

    @Override
    public BlockTitleFeatures clone(FeatureParentInterface newParent) {
        BlockTitleFeatures dropFeatures = new BlockTitleFeatures(newParent);
        dropFeatures.setActiveTitle(this.activeTitle.clone(dropFeatures));
        dropFeatures.setTitle(this.title.clone(dropFeatures));
        dropFeatures.setTitleAjustement(this.titleAjustement.clone(dropFeatures));
        return dropFeatures;
    }

    @Override
    public List<FeatureInterface> getFeatures() {
        List<FeatureInterface> features = new ArrayList<>();
        features.add(this.activeTitle);
        features.add(this.title);
        features.add(this.titleAjustement);
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
            if (feature instanceof BlockTitleFeatures) {
                BlockTitleFeatures dropFeatures = (BlockTitleFeatures) feature;
                dropFeatures.setActiveTitle(this.activeTitle);
                dropFeatures.setTitle(this.title);
                dropFeatures.setTitleAjustement(this.titleAjustement);
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

    public String getSimpleLocString(Location loc) {
        return loc.getWorld().getName() + "-" + loc.getBlockX() + "-" + loc.getBlockY() + "-" + loc.getBlockZ();
    }

    /**
     * Return the location of the Holo
     **/
    public Location spawn(@NotNull Location location, StringPlaceholder sp) {
        if (!activeTitle.getValue()) return null;

        String pluginToUse = GeneralConfig.getInstance().getHologramsPlugin().toUpperCase();

        List<String> lines = new ArrayList<>();
        for (String s : getTitle().getValues()) {
            s = StringConverter.coloredString(s);
            lines.add(s);
        }
        lines = sp.replacePlaceholders(lines);
        final List<String> finalLines = lines;
        if (SCore.hasCMI && (!SCore.is1v20v4Plus() || pluginToUse.equals("CMI"))) {
            CMIHologram holo = new CMIHologram(UUID.randomUUID().toString(), location.clone().add(0, 0.5 + getTitleAjustement().getValue().get(), 0));
            holo.setLines(lines);
            CMI.getInstance().getHologramManager().addHologram(holo);
            holo.update();
            //SsomarDev.testMsg("Hologram spawned >> "+holo.getCenterLocation());
            return holo.getLocation().getBukkitLoc();
        } else if (SCore.hasDecentHolograms && (!SCore.is1v20v4Plus() || pluginToUse.equals("DECENT_HOLOGRAMS"))) {
            Location loc = location.clone().add(0, 0.5 + getTitleAjustement().getValue().get(), 0);

            // 26/01/2023 Double creation needed required to avoid hologram not updating when we use SETEXECUTABLEBLOCK on an EB
            // One creation sync and one with delay
            // -> When we use SETEXECUTABLEBLOCK on an EB the title of the EB replaced stay and the title of the EB set is not placed
            // Idk why it doesnt update without, it's something in DecentHologram
            if (DHAPI.getHologram(getSimpleLocString(loc)) != null) remove(loc);
            DHAPI.createHologram(getSimpleLocString(loc), loc, lines);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (DHAPI.getHologram(loc.toString()) != null) {
                        remove(loc);
                        eu.decentsoftware.holograms.api.holograms.Hologram hologram = DHAPI.createHologram(getSimpleLocString(loc), loc, finalLines);
                        hologram.updateAll();
                    }
                    // if null it means that the hologram has been removed during the tick and we don't want to recreate/update it
                }
            };
            SCore.schedulerHook.runLocationTask(runnable, loc, 1);
            return loc;
        } else if (SCore.hasHolographicDisplays && (!SCore.is1v20v4Plus() || pluginToUse.equals("HOLOGRAPHIC_DISPLAYS"))) {
            Hologram holo = HolographicDisplaysAPI.get(SCore.plugin).createHologram(location.clone().add(0, 0.5 + getTitleAjustement().getValue().get(), 0));
            for (String s : lines) {
                if (s.contains("ITEM::")) {
                    Material material = Material.STONE;
                    try {
                        material = Material.valueOf(s.split("ITEM::")[1].trim().toUpperCase());
                    } catch (Exception ignored) {
                    }
                    holo.getLines().appendItem(new ItemStack(material));
                } else holo.getLines().appendText(s);
            }
            return holo.getPosition().toLocation();
        } else {

            if (SCore.is1v20v4Plus()) {
                Location loc = location.clone().add(0, 0.5 + getTitleAjustement().getValue().get(), 0);
                // Because Textdisplay doesnt have yaw, and it causes problem for remove if we let it.
                loc.setYaw(0);
                TextDisplay textDisplay = loc.getWorld().spawn(loc, TextDisplay.class);
                textDisplay.setSeeThrough(true);
                textDisplay.setBillboard(Display.Billboard.CENTER);
                textDisplay.setViewRange(25);
                StringBuilder sb = new StringBuilder();
                for (String s : lines) {
                    sb.append(s).append("\n");
                }
                if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
                textDisplay.setText(sb.toString());
                textDisplayCache.put(loc.clone(), textDisplay.getUniqueId());
                return loc;
            } else return null;
        }
    }

    /**
     * location is the location of the Holo
     **/
    public void remove(@NotNull Location location) {
        String pluginToUse = GeneralConfig.getInstance().getHologramsPlugin().toUpperCase();
        //SsomarDev.testMsg("Hologram in remove >> "+location, true);
        if (SCore.hasCMI && (!SCore.is1v20v4Plus() || pluginToUse.equals("CMI"))) {
            CMIHologram holo = CMI.getInstance().getHologramManager().getByLoc(location);
            if (holo != null) holo.remove();
        } else if (SCore.hasDecentHolograms && (!SCore.is1v20v4Plus() || pluginToUse.equals("DECENT_HOLOGRAMS"))) {
            //SsomarDev.testMsg("Hologram in remove  DecentHolograms, find the placeholder ?>> "+(DHAPI.getHologram(location.toString()) != null), true);
            eu.decentsoftware.holograms.api.holograms.Hologram hologram;
            if ((hologram = DHAPI.getHologram(getSimpleLocString(location))) != null) {
                hologram.destroy();
                //SsomarDev.testMsg("Hologram removed  DecentHolograms", true);
            }
        } else if (SCore.hasHolographicDisplays  && (!SCore.is1v20v4Plus() || pluginToUse.equals("HOLOGRAPHIC_DISPLAYS"))) {
            // SsomarDev.testMsg("Hologram removed >> " + location);
            for (Hologram holo : HolographicDisplaysAPI.get(SCore.plugin).getHolograms()) {
                //SsomarDev.testMsg("Hologram location >> " + holo.getPosition().toLocation());

                if (holo.getPosition().toLocation().equals(location)) {
                    holo.delete();
                }
            }
        } else if (SCore.is1v20v4Plus()) {
            UUID cachedUuid = textDisplayCache.remove(location);
            if (cachedUuid != null) {
                Entity entity = Bukkit.getEntity(cachedUuid);
                if (entity instanceof TextDisplay) {
                    entity.remove();
                    return;
                }
            }
            // Fallback for cache miss (e.g., after server restart)
            for (Entity entity : location.getWorld().getNearbyEntities(location, 0.5, 0.5, 0.5)) {
                if (entity instanceof TextDisplay && entity.getLocation().equals(location)) {
                    entity.remove();
                    break;
                }
            }
        }
    }

    /**
     * @param location       is the location of the hologram
     * @param objectLocation is the location of the object (block / player) that has the hologram
     **/
    public Location update(@Nullable Location location, @NotNull Location objectLocation, StringPlaceholder sp) {

        if(objectLocation.getWorld() == null) return location;
        try {
            if(!objectLocation.isWorldLoaded() || !objectLocation.isChunkLoaded()) return location;
        } catch (NoSuchMethodError e) {
            // isChunkLoaded() not available on older Spigot — check manually
            if(!objectLocation.getWorld().isChunkLoaded(objectLocation.getBlockX() >> 4, objectLocation.getBlockZ() >> 4)) return location;
        }

        String pluginToUse = GeneralConfig.getInstance().getHologramsPlugin().toUpperCase();

        //System.out.println(">>>>>>>>>>>>>>>>>>>> Update title at location: " + location);
        if (!activeTitle.getValue()) {
            if (location != null) remove(location);
            //System.out.println(">>>>>>>>>>>>>>>>>> Title is not active");
            return null;
        }

        if(location == null){
            //System.out.println(">>>>>>>>>>>>>> Location is null");
            return spawn(objectLocation, sp);
        }

        List<String> lines = new ArrayList<>();
        for (String s : getTitle().getValues()) {
            s = StringConverter.coloredString(s);
            lines.add(s);
        }
        lines = sp.replacePlaceholders(lines);

        if (SCore.hasCMI && (!SCore.is1v20v4Plus() || pluginToUse.equals("CMI"))) {
            CMIHologram holo = CMI.getInstance().getHologramManager().getByLoc(location);
            if (holo != null) {
                holo.setLines(lines);
                holo.update();
                return location;
            } else  return  spawn(objectLocation, sp);
        } else if (SCore.hasDecentHolograms && (!SCore.is1v20v4Plus() || pluginToUse.equals("DECENT_HOLOGRAMS"))) {
            eu.decentsoftware.holograms.api.holograms.Hologram hologram = DHAPI.getHologram(location.toString());
            if (hologram != null) {
                DHAPI.setHologramLines(hologram, lines);
                return location;
            } else return spawn(objectLocation, sp);
        }
        /* not opti */
        else if (SCore.hasHolographicDisplays  && (!SCore.is1v20v4Plus() || pluginToUse.equals("HOLOGRAPHIC_DISPLAYS"))) {
            remove(location);
            return spawn(objectLocation, sp);
        } else if (SCore.is1v20v4Plus()) {
            UUID cachedUuid = textDisplayCache.get(location);
            if (cachedUuid != null) {
                Entity entity = Bukkit.getEntity(cachedUuid);
                if (entity instanceof TextDisplay) {
                    TextDisplay textDisplay = (TextDisplay) entity;
                    StringBuilder sb = new StringBuilder();
                    for (String s : lines) {
                        sb.append(s).append("\n");
                    }
                    if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
                    SCore.schedulerHook.runEntityTask(()->{textDisplay.setText(sb.toString());}, ()->{}, textDisplay, 0);
                    return location;
                }
            }
            remove(location);
            return spawn(objectLocation, sp);
        }
        return location;
    }
}
