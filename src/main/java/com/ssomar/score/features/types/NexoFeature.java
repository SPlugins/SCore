package com.ssomar.score.features.types;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import com.nexomc.nexo.utils.drops.Drop;
import com.ssomar.score.SCore;
import com.ssomar.score.SsomarDev;
import com.ssomar.score.editor.NewGUIManager;
import com.ssomar.score.features.FeatureAbstract;
import com.ssomar.score.features.FeatureParentInterface;
import com.ssomar.score.features.FeatureRequireOnlyClicksInEditor;
import com.ssomar.score.features.FeatureSettingsInterface;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.item.UpdateItemInGUI;
import com.ssomar.score.utils.strings.StringConverter;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

@Getter
@Setter
public class NexoFeature extends FeatureAbstract<Optional<String>, NexoFeature> implements FeatureRequireOnlyClicksInEditor {

    private static final boolean DEBUG = false;
    private Optional<String> value;

    public NexoFeature(FeatureParentInterface parent, FeatureSettingsInterface featureSettings) {
        super(parent, featureSettings);
        this.value = Optional.empty();
    }

    @Override
    public List<String> load(SPlugin plugin, ConfigurationSection config, boolean isPremiumLoading) {
        List<String> errors = new ArrayList<>();
        String colorStr = config.getString(this.getName(), "");
        if (colorStr.isEmpty()) {
            value = Optional.empty();
        } else {
            value = Optional.of(colorStr);
        }
        return errors;
    }

    @Override
    public void save(ConfigurationSection config) {
        value.ifPresent(s -> config.set(this.getName(), s));
    }

    @Override
    public Optional<String> getValue() {
        if (value.isPresent() && SCore.hasNexo) {
            if (NexoItems.itemFromId(value.get()) != null) {
                return value;
            }
        }
        return Optional.empty();
    }

    public Optional<ItemStack> getItemStack() {
        if (getValue().isPresent()) {
            return Optional.of(NexoItems.itemFromId(value.get()).build());
        }
        return Optional.empty();
    }

    public boolean placeNexo(Location location, ItemStack itemStack) {
        if (getValue().isPresent()) {
            String id = getValue().get();
            //SsomarDev.testMsg("id: " + id, true);
            if (NexoBlocks.isCustomBlock(id)) {
                //SsomarDev.testMsg("isNexoBlock", true);
                NexoBlocks.place(id, location);
                return true;
            }
        }
        return false;
    }

    public void removeBlock(Block block, Optional<Entity> blockEntity) {
        if (SCore.hasNexo) {
            if (NexoBlocks.isCustomBlock(block)) {
                SsomarDev.testMsg("isNexoBlock", true);
                //Drop drop = new Drop(new ArrayList<>(), false, false, NexoBlocks.stringMechanic(block).getItemID());
                //NexoBlocks.remove(block.getLocation(), null, null);
                NexoBlocks.remove(block.getLocation(), null, Drop.emptyDrop());
            }
            else {
                SsomarDev.testMsg("isNothing", true);
            }
        }
    }


    @Override
    public NexoFeature initItemParentEditor(GUI gui, int slot) {
        String[] finalDescription = new String[getEditorDescription().length + 3];
        System.arraycopy(getEditorDescription(), 0, finalDescription, 0, getEditorDescription().length);
        finalDescription[finalDescription.length - 3] = GUI.CLICK_HERE_TO_CHANGE;
        finalDescription[finalDescription.length - 2] = "&8>> &6SHIFT : &eBOOST SCROLL";
        finalDescription[finalDescription.length - 1] = "&8>> &6UP: &eRIGHT | &6DOWN: &eLEFT";

        ItemStack item = new ItemStack(getEditorMaterial());
        if (getValue().isPresent()) {
            item = getItemStack().get();
        }

        gui.createItem(item, 1, slot, GUI.TITLE_COLOR + getEditorName(), false, false, finalDescription);
        return this;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {
        if (SCore.hasNexo) {
            if (getValue().isPresent()) updateNexo(value.get(), gui);
            else{
                List<String> sortNexo = getSortNexo();
                if(!sortNexo.isEmpty()) updateNexo(sortNexo.get(0), gui);
            }
        } else
            updateNexo(null, gui);
    }

    @Override
    public NexoFeature clone(FeatureParentInterface newParent) {
        NexoFeature clone = new NexoFeature(newParent, getFeatureSettings());
        clone.value = value;
        return clone;
    }

    @Override
    public void reset() {
        this.value = Optional.empty();
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
        if (SCore.hasNexo) {
            String id = getNexo((GUI) manager.getCache().get(editor)).get();
            id = nextNexo(id);
            id = nextNexo(id);
            id = nextNexo(id);
            id = nextNexo(id);
            id = nextNexo(id);
            id = nextNexo(id);
            id = nextNexo(id);
            id = nextNexo(id);
            id = nextNexo(id);
            id = nextNexo(id);
            id = nextNexo(id);
            id = nextNexo(id);
            id = nextNexo(id);
            id = nextNexo(id);
            id = nextNexo(id);
            updateNexo(id, (GUI) manager.getCache().get(editor));
        }
        return true;
    }

    @Override
    public boolean shiftRightClicked(Player editor, NewGUIManager manager) {
        if (SCore.hasNexo) {
            String id = getNexo((GUI) manager.getCache().get(editor)).get();
            id = prevNexo(id);
            id = prevNexo(id);
            id = prevNexo(id);
            id = prevNexo(id);
            id = prevNexo(id);
            id = prevNexo(id);
            id = prevNexo(id);
            id = prevNexo(id);
            id = prevNexo(id);
            id = prevNexo(id);
            id = prevNexo(id);
            id = prevNexo(id);
            id = prevNexo(id);
            id = prevNexo(id);
            id = prevNexo(id);
            updateNexo(id, (GUI) manager.getCache().get(editor));
        }
        return true;
    }

    @Override
    public boolean leftClicked(Player editor, NewGUIManager manager) {
        if (SCore.hasNexo)
            updateNexo(nextNexo(getNexo((GUI) manager.getCache().get(editor)).orElse(null)), (GUI) manager.getCache().get(editor));
        return true;
    }

    @Override
    public boolean rightClicked(Player editor, NewGUIManager manager) {
        if (SCore.hasNexo)
            updateNexo(prevNexo(getNexo((GUI) manager.getCache().get(editor)).orElse(null)), (GUI) manager.getCache().get(editor));
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

    public String nextNexo(String id) {
        if (id == null) {
            if(getSortNexo().isEmpty()) return "No Nexo found";
            else return getSortNexo().get(0);
        }

        boolean next = false;
        for (String check : getSortNexo()) {
            if (check.equals(id)) {
                next = true;
                continue;
            }
            if (next) return check;
        }
        return getSortNexo().get(0);
    }

    public String prevNexo(String id) {
        if (id == null) {
            if(getSortNexo().isEmpty()) return "No Nexo found";
            else return getSortNexo().get(0);
        }
        int i = -1;
        int cpt = 0;
        for (String check : getSortNexo()) {
            if (check.equals(id)) {
                i = cpt;
                break;
            }
            cpt++;
        }
        if (i == 0) return getSortNexo().get(getSortNexo().size() - 1);
        else return getSortNexo().get(cpt - 1);
    }

    public void updateNexo(String id, GUI gui) {

        ItemStack item = gui.getByIdentifier(getEditorName());
        int slot = gui.getInv().first(item);
        initItemParentEditor(gui, slot);
        item = gui.getByIdentifier(getEditorName());

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore().subList(0, getEditorDescription().length + 3);

        if (id == null) {
            if(getSortNexo().isEmpty()) lore.add(StringConverter.coloredString("&4➤ &cNo Nexo found"));
            value = Optional.empty();
        }
        else value = Optional.of(id);

        if (SCore.hasNexo) {
            boolean find = false;
            for (String check : getSortNexo()) {
                if (id.equals(check)) {
                    lore.add(StringConverter.coloredString("&2➤ &a" + id));
                    find = true;
                } else if (find) {
                    if (lore.size() == 17) break;
                    lore.add(StringConverter.coloredString("&6✦ &e" + check));
                }
            }
            for (String check : getSortNexo()) {
                if (lore.size() == 17) break;
                else {
                    lore.add(StringConverter.coloredString("&6✦ &e" + check));
                }
            }
        } else {
            lore.add(StringConverter.coloredString("&4➤ &cYou must have &6Nexo"));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);

        /* Bug item no update idk why */
        UpdateItemInGUI.updateItemInGUI(gui, getEditorName(), meta.getDisplayName(), lore, item.getType());
    }

    public Optional<String> getNexo(GUI gui) {
        ItemStack item = gui.getByIdentifier(getEditorName());
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        for (String str : lore) {
            if (str.contains("➤ ")) {
                if (str.contains("You must have &6Nexo")) return Optional.empty();
                str = StringConverter.decoloredString(str).replaceAll(" Premium", "");
                return Optional.ofNullable(str.split("➤ ")[1]);
            }
        }
        return Optional.empty();
    }

    public List<String> getSortNexo() {
        SortedMap<String, String> map = new TreeMap<String, String>();
        if (SCore.hasNexo) {
            for (ItemBuilder itemBuilder : NexoItems.items()) {
                String itemId = NexoItems.idFromItem(itemBuilder);
                map.put(itemId, itemId);
            }
        }
        return new ArrayList<>(map.values());
    }

}
