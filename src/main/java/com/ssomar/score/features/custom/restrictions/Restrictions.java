package com.ssomar.score.features.custom.restrictions;

import com.ssomar.score.SCore;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.features.FeatureInterface;
import com.ssomar.score.features.FeatureParentInterface;
import com.ssomar.score.features.FeatureSettingsSCore;
import com.ssomar.score.features.FeatureWithHisOwnEditor;
import com.ssomar.score.features.editor.GenericFeatureParentEditor;
import com.ssomar.score.features.editor.GenericFeatureParentEditorManager;
import com.ssomar.score.features.types.BooleanFeature;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.strings.StringConverter;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

@Getter
@Setter
public class Restrictions extends FeatureWithHisOwnEditor<Restrictions, Restrictions, GenericFeatureParentEditor, GenericFeatureParentEditorManager> {

    private Map<RestrictionEnum, BooleanFeature> restrictions;
    private Map<RestrictionEnum, Boolean> defaultValues;
    private List<RestrictionEnum> notFor1_11_less;
    private List<RestrictionEnum> notFor1_13_less;
    private List<RestrictionEnum> notFor1_18_less;
    private List<RestrictionEnum> notFor1_19_less;

    public Restrictions(FeatureParentInterface parent, Map<RestrictionEnum, Boolean> defaultValues) {
        super(parent, FeatureSettingsSCore.restrictions);
        this.defaultValues = defaultValues;
        this.notFor1_11_less = new ArrayList<>();
        this.notFor1_11_less.add(RestrictionEnum.CANCEL_SWAPHAND);
        this.notFor1_13_less = new ArrayList<>();
        this.notFor1_13_less.add(RestrictionEnum.CANCEL_STONE_CUTTER);
        this.notFor1_13_less.add(RestrictionEnum.CANCEL_CARTOGRAPHY);
        this.notFor1_13_less.add(RestrictionEnum.CANCEL_COMPOSTER);
        this.notFor1_13_less.add(RestrictionEnum.CANCEL_LECTERN);
        this.notFor1_13_less.add(RestrictionEnum.CANCEL_LOOM);
        this.notFor1_13_less.add(RestrictionEnum.CANCEL_GRIND_STONE);
        this.notFor1_13_less.add(RestrictionEnum.CANCEL_SMITHING_TABLE);
        this.notFor1_18_less = new ArrayList<>();
        this.notFor1_18_less.add(RestrictionEnum.CANCEL_HORN);
        this.notFor1_19_less = new ArrayList<>();
        this.notFor1_19_less.add(RestrictionEnum.CANCEL_DECORATED_POT);
        this.notFor1_19_less.add(RestrictionEnum.CANCEL_CRAFTER);
        reset();
    }

    @Override
    public void reset() {
        restrictions = new LinkedHashMap<>();
        for (RestrictionEnum restriction : RestrictionEnum.values()) {
            if (SCore.is1v11Less() && notFor1_11_less.contains(restriction)) continue;
            if (SCore.is1v13Less() && notFor1_13_less.contains(restriction)) continue;
            if (!SCore.is1v19Plus() && notFor1_18_less.contains(restriction)) continue;
            if (!SCore.is1v20Plus() && notFor1_19_less.contains(restriction)) continue;
            /* shelves exist since 1.21.9 */
            if (restriction == RestrictionEnum.CANCEL_SHELF && org.bukkit.Material.matchMaterial("OAK_SHELF") == null) continue;
            restrictions.put(restriction, new BooleanFeature(this, defaultValues.get(restriction), restriction.featureSetting));
        }
    }

    @Override
    public List<String> load(SPlugin plugin, ConfigurationSection config, boolean isPremiumLoading) {
        List<String> error = new ArrayList<>();
        if (config.isConfigurationSection(getName())) {
            ConfigurationSection section = config.getConfigurationSection(getName());
            for (RestrictionEnum restriction : restrictions.keySet()) {
                restrictions.get(restriction).load(plugin, section, isPremiumLoading);
            }
        }

        return error;
    }


    @Override
    public void save(ConfigurationSection config) {
        config.set(getName(), null);
        ConfigurationSection section = config.createSection(getName());
        for (RestrictionEnum restriction : restrictions.keySet()) {
            restrictions.get(restriction).save(section);
        }
        if(isSavingOnlyIfDiffDefault() && section.getKeys(false).isEmpty()){
            config.set(getName(), null);
            return;
        }

        if (GeneralConfig.getInstance().isEnableCommentsInConfig())
            config.setComments(this.getName(), StringConverter.decoloredString(Arrays.asList(getFeatureSettings().getEditorDescriptionBrut())));

    }

    @Override
    public Restrictions getValue() {
        return this;
    }

    public boolean is(RestrictionEnum ask) {
        for (RestrictionEnum restriction : restrictions.keySet()) {
            if (restriction.equals(ask)) {
                return restrictions.get(restriction).getValue();
            }
        }
        return false;
    }

    public BooleanFeature get(RestrictionEnum ask) {
        for (RestrictionEnum restriction : restrictions.keySet()) {
            if (restriction.equals(ask)) {
                return restrictions.get(restriction);
            }
        }
        return null;
    }

    public int getRestrictionCount() {
        int count = 0;
        for (RestrictionEnum restriction : restrictions.keySet()) {
            if (restrictions.get(restriction).getValue()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public Restrictions initItemParentEditor(GUI gui, int slot) {
        String[] finalDescription = new String[getEditorDescription().length + 2];
        System.arraycopy(getEditorDescription(), 0, finalDescription, 0, getEditorDescription().length);
        finalDescription[finalDescription.length - 2] = GUI.CLICK_HERE_TO_CHANGE;
        finalDescription[finalDescription.length - 1] = "&7Restrictions activated: &e" + getRestrictionCount();

        gui.createItem(getEditorMaterial(), 1, slot, GUI.TITLE_COLOR + getEditorName(), false, false, finalDescription);
        return this;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {

    }

    @Override
    public Restrictions clone(FeatureParentInterface newParent) {
        Restrictions restrictions = new Restrictions(getParent(), getDefaultValues());
        Map<RestrictionEnum, BooleanFeature> clone = new LinkedHashMap<>();
        for (RestrictionEnum restriction : this.restrictions.keySet()) {
            clone.put(restriction, this.restrictions.get(restriction).clone(restrictions));
        }
        restrictions.setRestrictions(clone);
        return restrictions;
    }

    @Override
    public List<FeatureInterface> getFeatures() {
        List<FeatureInterface> features = new ArrayList<>();
        for (RestrictionEnum restriction : restrictions.keySet()) {
            features.add(restrictions.get(restriction));
        }
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
            if (feature instanceof Restrictions) {
                Restrictions restrictions = (Restrictions) feature;
                Map<RestrictionEnum, BooleanFeature> reload = new LinkedHashMap<>();
                for (RestrictionEnum restriction : this.restrictions.keySet()) {
                    reload.put(restriction, this.restrictions.get(restriction));
                }
                restrictions.setRestrictions(reload);
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
