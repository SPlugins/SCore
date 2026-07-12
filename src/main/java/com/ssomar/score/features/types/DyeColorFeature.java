package com.ssomar.score.features.types;

import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.editor.NewGUIManager;
import com.ssomar.score.features.*;
import com.ssomar.score.languages.messages.TM;
import com.ssomar.score.languages.messages.Text;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.strings.StringConverter;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

@Getter
@Setter
public class DyeColorFeature extends FeatureAbstract<Optional<DyeColor>, DyeColorFeature> implements FeatureRequireOnlyClicksInEditor {

    private Optional<DyeColor> value;
    private Optional<DyeColor> defaultValue;

    public DyeColorFeature(FeatureParentInterface parent, Optional<DyeColor> defaultValue, FeatureSettingsInterface settings) {
        super(parent, settings);
        this.defaultValue = defaultValue;
        this.value = Optional.empty();
    }

    @Override
    public List<String> load(SPlugin plugin, ConfigurationSection config, boolean isPremiumLoading) {
        List<String> errors = new ArrayList<>();
        String colorStr = config.getString(this.getName(), "NULL").toUpperCase();
        if (colorStr.equals("NULL")) {
            if (defaultValue.isPresent()) {
                value = defaultValue;
            } else {
                errors.add("&cERROR, Couldn't load the DyeColor value of " + this.getName() + " from config, value: " + colorStr + " &7&o" + getParent().getParentInfo() + " &6>> DyeColors available: https://jd.papermc.io/paper/1.21/org/bukkit/DyeColor.html");
                value = Optional.empty();
            }
            return errors;
        }
        try {
            DyeColor dyeColor = DyeColor.valueOf(colorStr.toUpperCase());
            value = Optional.ofNullable(dyeColor);
            FeatureReturnCheckPremium<DyeColor> checkPremium = checkPremium("DyeColor", dyeColor, defaultValue, isPremiumLoading);
            if (checkPremium.isHasError()) value = Optional.of(checkPremium.getNewValue());
        } catch (Exception e) {
            errors.add("&cERROR, Couldn't load the DyeColor value of " + this.getName() + " from config, value: " + colorStr + " &7&o" + getParent().getParentInfo() + " &6>> DyeColors available: https://jd.papermc.io/paper/1.21/org/bukkit/DyeColor.html");
            value = Optional.empty();
        }
        return errors;
    }

    @Override
    public void save(ConfigurationSection config) {
        if (getValue().isPresent()) {
            if (defaultValue.isPresent() && isSavingOnlyIfDiffDefault() && getValue().get().equals(defaultValue.get())) {
                config.set(this.getName(), null);
                return;
            } else config.set(this.getName(), getValue().get().name());
        }
        if (GeneralConfig.getInstance().isEnableCommentsInConfig())
            config.setComments(this.getName(), StringConverter.decoloredString(Arrays.asList(getFeatureSettings().getEditorDescriptionBrut())));

    }

    @Override
    public Optional<DyeColor> getValue() {
        if (value.isPresent()) return value;
        else return defaultValue;
    }

    @Override
    public DyeColorFeature initItemParentEditor(GUI gui, int slot) {
        String[] finalDescription = new String[getEditorDescription().length + 2];
        System.arraycopy(getEditorDescription(), 0, finalDescription, 0, getEditorDescription().length);
        finalDescription[finalDescription.length - 2] = gui.CLICK_HERE_TO_CHANGE;
        finalDescription[finalDescription.length - 1] = TM.g(Text.EDITOR_CURRENTLY_NAME);

        gui.createItem(getEditorMaterial(), 1, slot, gui.TITLE_COLOR + getEditorName(), false, false, finalDescription);
        return this;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {
        Optional<DyeColor> value = getValue();
        DyeColor finalValue = value.orElse(DyeColor.WHITE);
        updateDropType(finalValue, gui);
    }

    @Override
    public DyeColorFeature clone(FeatureParentInterface newParent) {
        DyeColorFeature clone = new DyeColorFeature(newParent, getDefaultValue(), getFeatureSettings());
        clone.value = value;
        return clone;
    }

    @Override
    public void reset() {
        this.value = defaultValue;
    }

    @Override
    public void clickParentEditor(Player editor, NewGUIManager manager) {
        return;
    }

    @Override
    public boolean noShiftclicked(Player editor, NewGUIManager manager) {
        return false;
    }

    @Override
    public boolean noShiftLeftclicked(Player editor, NewGUIManager manager) {
        return false;
    }

    @Override
    public boolean noShiftRightclicked(Player editor, NewGUIManager manager) {
        return false;
    }

    @Override
    public boolean shiftClicked(Player editor, NewGUIManager manager) {
        return false;
    }

    @Override
    public boolean shiftLeftClicked(Player editor, NewGUIManager manager) {
        return false;
    }

    @Override
    public boolean shiftRightClicked(Player editor, NewGUIManager manager) {
        return false;
    }

    @Override
    public boolean leftClicked(Player editor, NewGUIManager manager) {
        updateDropType(nextCreationType(getDropType((GUI) manager.getCache().get(editor))), (GUI) manager.getCache().get(editor));
        return true;
    }

    @Override
    public boolean rightClicked(Player editor, NewGUIManager manager) {
        updateDropType(prevCreationType(getDropType((GUI) manager.getCache().get(editor))), (GUI) manager.getCache().get(editor));
        return true;
    }

    @Override
    public boolean doubleClicked(Player editor, NewGUIManager manager) {
        return false;
    }

    @Override
    public boolean middleClicked(Player editor, NewGUIManager manager) {
        return false;
    }

    public DyeColor nextCreationType(DyeColor color) {
        boolean next = false;
        for (DyeColor check : getSortDropTypes()) {
            if (check.equals(color)) {
                next = true;
                continue;
            }
            if (next) return check;
        }
        return getSortDropTypes().get(0);
    }

    public DyeColor prevCreationType(DyeColor color) {
        int i = -1;
        int cpt = 0;
        for (DyeColor check : getSortDropTypes()) {
            if (check.equals(color)) {
                i = cpt;
                break;
            }
            cpt++;
        }
        if (i == 0) return getSortDropTypes().get(getSortDropTypes().size() - 1);
        else return getSortDropTypes().get(cpt - 1);
    }

    public void updateDropType(DyeColor color, GUI gui) {

        this.value = Optional.of(color);
        ItemStack item = gui.getByIdentifier(getEditorName());
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore().subList(0, getEditorDescription().length + 2);
        int maxSize = lore.size();
        maxSize += getSortDropTypes().size();
        if (maxSize > 17) maxSize = 17;
        boolean find = false;
        for (DyeColor check : getSortDropTypes()) {
            if (color.equals(check)) {
                lore.add(StringConverter.coloredString("&2➤ &a" + color.name()));
                find = true;
            } else if (find) {
                if (lore.size() == maxSize) break;
                lore.add(StringConverter.coloredString("&6✦ &e" + check.name()));
            }
        }
        for (DyeColor check : getSortDropTypes()) {
            if (lore.size() == maxSize) break;
            else {
                lore.add(StringConverter.coloredString("&6✦ &e" + check.name()));
            }
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        /* Update the gui only for the right click , for the left it updated automaticaly idk why */
        for (HumanEntity e : gui.getInv().getViewers()) {
            if (e instanceof Player) {
                Player p = (Player) e;
                p.updateInventory();
            }
        }
    }

    public DyeColor getDropType(GUI gui) {
        ItemStack item = gui.getByIdentifier(getEditorName());
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        for (String str : lore) {
            if (str.contains("➤ ")) {
                str = StringConverter.decoloredString(str).replaceAll(" Premium", "");
                return DyeColor.valueOf(str.split("➤ ")[1]);
            }
        }
        return null;
    }

    public List<DyeColor> getSortDropTypes() {
        SortedMap<String, DyeColor> map = new TreeMap<String, DyeColor>();
        for (DyeColor l : DyeColor.values()) {
            map.put(l.name(), l);
        }
        return new ArrayList<>(map.values());
    }

}
