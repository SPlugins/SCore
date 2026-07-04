package com.ssomar.score.features.types;

import com.ssomar.score.SCore;
import com.ssomar.score.editor.NewGUIManager;
import com.ssomar.score.features.*;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.RequiredObject;
import com.ssomar.score.utils.strings.StringConverter;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

@Getter
@Setter
public class RequiredObjectFeature extends FeatureAbstract<Optional<RequiredObject>, RequiredObjectFeature> implements FeatureRequireOnlyClicksInEditor {

    private Optional<RequiredObject> value;
    private Optional<RequiredObject> defaultValue;

    public RequiredObjectFeature(FeatureParentInterface parent, Optional<RequiredObject> defaultValue, FeatureSettingsInterface settings) {
        super(parent, settings);
        this.defaultValue = defaultValue;
        this.value = Optional.empty();
    }

    @Override
    public List<String> load(SPlugin plugin, ConfigurationSection config, boolean isPremiumLoading) {
        List<String> errors = new ArrayList<>();
        String colorStr = config.getString(this.getName(), "NULL").toUpperCase();
        try {
            RequiredObject attributeSlot = RequiredObject.valueOf(colorStr.toUpperCase());
            value = Optional.ofNullable(attributeSlot);
            FeatureReturnCheckPremium<RequiredObject> checkPremium = checkPremium("RequiredObject", attributeSlot, defaultValue, isPremiumLoading);
            if (checkPremium.isHasError()) value = Optional.of(checkPremium.getNewValue());
        } catch (Exception e) {
            errors.add("&cERROR, Couldn't load the RequiredObject value of " + this.getName() + " from config, value: " + colorStr + " &7&o" + getParent().getParentInfo() + " &6>> HEAD, CHEST, FEET, LEGS, HAND, OFF_HAND, ALL_SLOTS");
            value = Optional.empty();
        }
        return errors;
    }

    @Override
    public void save(ConfigurationSection config) {
        Optional<RequiredObject> value = getValue();
        if (value.isPresent()) config.set(this.getName(), value.get().name());
    }

    @Override
    public Optional<RequiredObject> getValue() {
        if (value.isPresent()) return value;
        else return defaultValue;
    }

    public Optional<EquipmentSlot> getEquipmentSlotValue() {
        if (getValue().isPresent()) {
            try {
                return Optional.of(EquipmentSlot.valueOf(getValue().get().name().toUpperCase()));
            } catch (Exception e) {
                return Optional.empty();
            }
        } else return Optional.empty();
    }

    @Override
    public RequiredObjectFeature initItemParentEditor(GUI gui, int slot) {
        String[] finalDescription = new String[getEditorDescription().length + 2];
        System.arraycopy(getEditorDescription(), 0, finalDescription, 0, getEditorDescription().length);
        finalDescription[finalDescription.length - 2] = gui.CLICK_HERE_TO_CHANGE;
        finalDescription[finalDescription.length - 1] = "&7currently: ";

        gui.createItem(getEditorMaterial(), 1, slot, gui.TITLE_COLOR + getEditorName(), false, false, finalDescription);
        return this;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {
        Optional<RequiredObject> value = getValue();
        RequiredObject finalValue = value.orElse(RequiredObject.REQUIRE_VANILLA_ITEM);
        updateRequiredObject(finalValue, gui, false);
    }

    @Override
    public RequiredObjectFeature clone(FeatureParentInterface newParent) {
        RequiredObjectFeature clone = new RequiredObjectFeature(newParent, getDefaultValue(), getFeatureSettings());
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
        RequiredObject slot = getRequiredObject((GUI) manager.getCache().get(editor));
        slot = nextRequiredObject(slot);
        slot = nextRequiredObject(slot);
        slot = nextRequiredObject(slot);
        slot = nextRequiredObject(slot);
        slot = nextRequiredObject(slot);
        slot = nextRequiredObject(slot);
        slot = nextRequiredObject(slot);
        slot = nextRequiredObject(slot);
        slot = nextRequiredObject(slot);
        slot = nextRequiredObject(slot);
        slot = nextRequiredObject(slot);
        slot = nextRequiredObject(slot);
        slot = nextRequiredObject(slot);
        slot = nextRequiredObject(slot);
        slot = nextRequiredObject(slot);
        updateRequiredObject(slot, (GUI) manager.getCache().get(editor), true);
        return true;
    }

    @Override
    public boolean shiftRightClicked(Player editor, NewGUIManager manager) {
        RequiredObject slot = getRequiredObject((GUI) manager.getCache().get(editor));
        slot = prevRequiredObject(slot);
        slot = prevRequiredObject(slot);
        slot = prevRequiredObject(slot);
        slot = prevRequiredObject(slot);
        slot = prevRequiredObject(slot);
        slot = prevRequiredObject(slot);
        slot = prevRequiredObject(slot);
        slot = prevRequiredObject(slot);
        slot = prevRequiredObject(slot);
        slot = prevRequiredObject(slot);
        slot = prevRequiredObject(slot);
        slot = prevRequiredObject(slot);
        slot = prevRequiredObject(slot);
        slot = prevRequiredObject(slot);
        slot = prevRequiredObject(slot);
        updateRequiredObject(slot, (GUI) manager.getCache().get(editor), true);
        return true;
    }

    @Override
    public boolean leftClicked(Player editor, NewGUIManager manager) {
        updateRequiredObject(nextRequiredObject(getRequiredObject((GUI) manager.getCache().get(editor))), (GUI) manager.getCache().get(editor), false);
        return true;
    }

    @Override
    public boolean rightClicked(Player editor, NewGUIManager manager) {
        updateRequiredObject(prevRequiredObject(getRequiredObject((GUI) manager.getCache().get(editor))), (GUI) manager.getCache().get(editor), true);
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

    public RequiredObject nextRequiredObject(RequiredObject slot) {
        boolean next = false;
        for (RequiredObject check : getSortRequiredObjects()) {
            if (check.equals(slot)) {
                next = true;
                continue;
            }
            if (next) return check;
        }
        return getSortRequiredObjects().get(0);
    }

    public RequiredObject prevRequiredObject(RequiredObject slot) {
        int i = -1;
        int cpt = 0;
        for (RequiredObject check : getSortRequiredObjects()) {
            if (check.equals(slot)) {
                i = cpt;
                break;
            }
            cpt++;
        }
        if (i == 0) return getSortRequiredObjects().get(getSortRequiredObjects().size() - 1);
        else return getSortRequiredObjects().get(cpt - 1);
    }

    public void updateRequiredObject(RequiredObject rO, GUI gui, boolean previous) {
        if
        (
                (rO.equals(RequiredObject.REQUIRE_ITEM_FROM_EXECUTABLEITEMS) && !SCore.hasExecutableItems
                        || (rO.equals(RequiredObject.REQUIRE_ITEM_FROM_ITEMADDERS) && !SCore.hasItemsAdder)
                        || (rO.equals(RequiredObject.REQUIRE_ITEM_FROM_ORAXEN) && !SCore.hasOraxen)
                        || (rO.equals(RequiredObject.REQUIRE_ITEM_FROM_NEXO) && !SCore.hasNexo)
                )
        )
        {
            if (previous) {
                updateRequiredObject(prevRequiredObject(rO), gui, previous);
            } else {
                updateRequiredObject(nextRequiredObject(rO), gui, previous);
            }
            return;
        }
        this.value = Optional.of(rO);
        ItemStack item = gui.getByIdentifier(getEditorName());
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore().subList(0, getEditorDescription().length + 2);
        int maxSize = lore.size();
        maxSize += getSortRequiredObjects().size();
        if (maxSize > 17) maxSize = 17;
        boolean find = false;
        for (RequiredObject check : getSortRequiredObjects()) {
            if (rO.equals(check)) {
                lore.add(StringConverter.coloredString("&2➤ &a" + rO.name()));
                find = true;
            } else if (find) {
                if (lore.size() == maxSize) break;
                lore.add(StringConverter.coloredString("&6✦ &e" + check.name()));
            }
        }
        for (RequiredObject check : getSortRequiredObjects()) {
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

    public RequiredObject getRequiredObject(GUI gui) {
        ItemStack item = gui.getByIdentifier(getEditorName());
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        for (String str : lore) {
            if (str.contains("➤ ")) {
                str = StringConverter.decoloredString(str).replaceAll(" Premium", "");
                return RequiredObject.valueOf(str.split("➤ ")[1]);
            }
        }
        return null;
    }

    public List<RequiredObject> getSortRequiredObjects() {
        SortedMap<String, RequiredObject> map = new TreeMap<String, RequiredObject>();
        for (RequiredObject l : RequiredObject.values()) {
            map.put(l.name(), l);
        }
        return new ArrayList<>(map.values());
    }

}
