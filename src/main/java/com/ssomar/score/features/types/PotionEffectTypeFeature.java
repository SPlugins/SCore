package com.ssomar.score.features.types;

import com.ssomar.score.editor.NewGUIManager;
import com.ssomar.score.features.*;
import com.ssomar.score.languages.messages.TM;
import com.ssomar.score.languages.messages.Text;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.item.UpdateItemInGUI;
import com.ssomar.score.utils.strings.StringConverter;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

@Getter
@Setter
public class PotionEffectTypeFeature extends FeatureAbstract<Optional<PotionEffectType>, PotionEffectTypeFeature> implements FeatureRequireOnlyClicksInEditor {

    private Optional<PotionEffectType> value;
    private Optional<PotionEffectType> defaultValue;

    public PotionEffectTypeFeature(FeatureParentInterface parent, Optional<PotionEffectType> defaultValue, FeatureSettingsInterface featureSettings) {
        super(parent, featureSettings);
        this.defaultValue = defaultValue;
        this.value = Optional.empty();
    }

    @Override
    public List<String> load(SPlugin plugin, ConfigurationSection config, boolean isPremiumLoading) {
        List<String> errors = new ArrayList<>();
        String colorStr = config.getString(this.getName(), "NULL").toUpperCase();
        try {
            PotionEffectType attributeSlot = PotionEffectType.getByName(colorStr.toUpperCase());
            value = Optional.ofNullable(attributeSlot);
            FeatureReturnCheckPremium<PotionEffectType> checkPremium = checkPremium("PotionEffectType", attributeSlot, defaultValue, isPremiumLoading);
            if (checkPremium.isHasError()) value = Optional.of(checkPremium.getNewValue());
        } catch (Exception e) {
            errors.add("&cERROR, Couldn't load the PotionEffectType value of " + this.getName() + " from config, value: " + colorStr + " &7&o" + getParent().getParentInfo() + " &6>> List: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html");
            value = Optional.empty();
        }
        return errors;
    }

    @Override
    public void save(ConfigurationSection config) {
        Optional<PotionEffectType> value = getValue();
        value.ifPresent(potionEffectType -> config.set(this.getName(), potionEffectType.getName()));
    }

    @Override
    public Optional<PotionEffectType> getValue() {
        if (value.isPresent()) return value;
        else return defaultValue;
    }

    @Override
    public PotionEffectTypeFeature initItemParentEditor(GUI gui, int slot) {
        String[] finalDescription = new String[getEditorDescription().length + 2];
        System.arraycopy(getEditorDescription(), 0, finalDescription, 0, getEditorDescription().length);
        finalDescription[finalDescription.length - 2] = GUI.CLICK_HERE_TO_CHANGE;
        finalDescription[finalDescription.length - 1] = TM.g(Text.EDITOR_CURRENTLY_NAME);

        gui.createItem(getEditorMaterial(), 1, slot, GUI.TITLE_COLOR + getEditorName(), false, false, finalDescription);
        return this;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {
        Optional<PotionEffectType> value = getValue();
        PotionEffectType finalValue = value.orElse(PotionEffectType.getByName("HEAL"));
        updatePotionEffectType(finalValue, gui);
    }

    @Override
    public PotionEffectTypeFeature clone(FeatureParentInterface newParent) {
        PotionEffectTypeFeature clone = new PotionEffectTypeFeature(newParent, getDefaultValue(), getFeatureSettings());
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
        PotionEffectType slot = getPotionEffectType((GUI) manager.getCache().get(editor));
        slot = nextPotionEffectType(slot);
        slot = nextPotionEffectType(slot);
        slot = nextPotionEffectType(slot);
        slot = nextPotionEffectType(slot);
        slot = nextPotionEffectType(slot);
        slot = nextPotionEffectType(slot);
        slot = nextPotionEffectType(slot);
        slot = nextPotionEffectType(slot);
        slot = nextPotionEffectType(slot);
        slot = nextPotionEffectType(slot);
        slot = nextPotionEffectType(slot);
        slot = nextPotionEffectType(slot);
        slot = nextPotionEffectType(slot);
        slot = nextPotionEffectType(slot);
        slot = nextPotionEffectType(slot);
        updatePotionEffectType(slot, (GUI) manager.getCache().get(editor));
        return true;
    }

    @Override
    public boolean shiftRightClicked(Player editor, NewGUIManager manager) {
        PotionEffectType slot = getPotionEffectType((GUI) manager.getCache().get(editor));
        slot = prevPotionEffectType(slot);
        slot = prevPotionEffectType(slot);
        slot = prevPotionEffectType(slot);
        slot = prevPotionEffectType(slot);
        slot = prevPotionEffectType(slot);
        slot = prevPotionEffectType(slot);
        slot = prevPotionEffectType(slot);
        slot = prevPotionEffectType(slot);
        slot = prevPotionEffectType(slot);
        slot = prevPotionEffectType(slot);
        slot = prevPotionEffectType(slot);
        slot = prevPotionEffectType(slot);
        slot = prevPotionEffectType(slot);
        slot = prevPotionEffectType(slot);
        slot = prevPotionEffectType(slot);
        updatePotionEffectType(slot, (GUI) manager.getCache().get(editor));
        return true;
    }

    @Override
    public boolean leftClicked(Player editor, NewGUIManager manager) {
        updatePotionEffectType(nextPotionEffectType(getPotionEffectType((GUI) manager.getCache().get(editor))), (GUI) manager.getCache().get(editor));
        return true;
    }

    @Override
    public boolean rightClicked(Player editor, NewGUIManager manager) {
        updatePotionEffectType(prevPotionEffectType(getPotionEffectType((GUI) manager.getCache().get(editor))), (GUI) manager.getCache().get(editor));
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

    public PotionEffectType nextPotionEffectType(PotionEffectType slot) {
        boolean next = false;
        for (PotionEffectType check : getSortPotionEffectTypes()) {
            if (check.equals(slot)) {
                next = true;
                continue;
            }
            if (next) return check;
        }
        return getSortPotionEffectTypes().get(0);
    }

    public PotionEffectType prevPotionEffectType(PotionEffectType slot) {
        int i = -1;
        int cpt = 0;
        for (PotionEffectType check : getSortPotionEffectTypes()) {
            if (check.equals(slot)) {
                i = cpt;
                break;
            }
            cpt++;
        }
        if (i == 0) return getSortPotionEffectTypes().get(getSortPotionEffectTypes().size() - 1);
        else return getSortPotionEffectTypes().get(cpt - 1);
    }

    public void updatePotionEffectType(PotionEffectType slot, GUI gui) {
        value = Optional.of(slot);
        ItemStack item = gui.getByIdentifier(getEditorName());
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore().subList(0, getEditorDescription().length + 2);
        int maxSize = lore.size();
        maxSize += getSortPotionEffectTypes().size();
        if (maxSize > 17) maxSize = 17;
        boolean find = false;
        for (PotionEffectType check : getSortPotionEffectTypes()) {
            if (slot.equals(check)) {
                lore.add(StringConverter.coloredString("&2➤ &a" + slot.getName()));
                find = true;
            } else if (find) {
                if (lore.size() == maxSize) break;
                lore.add(StringConverter.coloredString("&6✦ &e" + check.getName()));
            }
        }
        for (PotionEffectType check : getSortPotionEffectTypes()) {
            if (lore.size() == maxSize) break;
            else {
                lore.add(StringConverter.coloredString("&6✦ &e" + check.getName()));
            }
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        /* Bug item no update idk why */
        UpdateItemInGUI.updateItemInGUI(gui, getEditorName(), meta.getDisplayName(), lore, item.getType());
    }

    public PotionEffectType getPotionEffectType(GUI gui) {
        ItemStack item = gui.getByIdentifier(getEditorName());
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        for (String str : lore) {
            if (str.contains("➤ ")) {
                str = StringConverter.decoloredString(str).replaceAll(" Premium", "");
                return PotionEffectType.getByName(str.split("➤ ")[1]);
            }
        }
        return null;
    }

    public List<PotionEffectType> getSortPotionEffectTypes() {
        SortedMap<String, PotionEffectType> map = new TreeMap<String, PotionEffectType>();
        for (PotionEffectType l : PotionEffectType.values()) {
            /* l can be equals to null in 1.8 ?? */
            if (l != null)
                map.put(l.getName(), l);
        }
        return new ArrayList<>(map.values());
    }

}
