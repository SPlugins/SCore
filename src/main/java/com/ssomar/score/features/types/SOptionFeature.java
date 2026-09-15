package com.ssomar.score.features.types;

import com.ssomar.score.SsomarDev;
import com.ssomar.score.editor.NewGUIManager;
import com.ssomar.score.features.FeatureAbstract;
import com.ssomar.score.features.FeatureParentInterface;
import com.ssomar.score.features.FeatureRequireOnlyClicksInEditor;
import com.ssomar.score.features.FeatureSettingsInterface;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.sobject.sactivator.SOption;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.item.UpdateItemInGUI;
import com.ssomar.score.utils.strings.StringConverter;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

@Getter
@Setter
public class SOptionFeature extends FeatureAbstract<SOption, SOptionFeature> implements FeatureRequireOnlyClicksInEditor {

    private SOption value;
    private SPlugin plugin;
    private SOption builderInstance;
    /** True when the config named an option that does not exist: the value is the default one only so the
     *  editor keeps working, and the activator must never run (see SActivator#isDisabledByInvalidOption). */
    private boolean invalidInConfig = false;

    public SOptionFeature(SPlugin sPlugin, SOption builderInstance, FeatureParentInterface parent, FeatureSettingsInterface featureSettings) {
        super(parent, featureSettings);
        this.plugin = sPlugin;
        this.builderInstance = builderInstance;
        reset();
    }

    @Override
    public List<String> load(SPlugin plugin, ConfigurationSection config, boolean isPremiumLoading) {
        List<String> errors = new ArrayList<>();
        invalidInConfig = false;
        String colorStr = config.getString(this.getName(), "NULL").toUpperCase();
        try {
            SOption option = builderInstance.getOption(colorStr);
            if (option == null) {
                if (config.contains(this.getName())) {
                    // A value that names no option (typo, option of another plugin…): do not silently run the
                    // activator on the default option (a mining ability that fell back to PLAYER_ALL_CLICK fired
                    // on every click). The default is kept for the editor, the activator is disabled.
                    invalidInConfig = true;
                    errors.add("&cERROR, Couldn't load the Option value of " + this.getName() + " from config, value: " + colorStr + " &7&o" + getParent().getParentInfo() + " &6>> This activator is DISABLED until the option is fixed. Did you mean: &e" + String.join("&6, &e", closestOptionNames(colorStr, 3)) + " &6? Options available: https://docs.ssomar.com/");
                } else {
                    // Key absent: existing configs relied on the default option, keep that (with the error) unchanged.
                    errors.add("&cERROR, Couldn't load the Option value of " + this.getName() + " from config, value: " + colorStr + " &7&o" + getParent().getParentInfo() + " &6>> Options available: https://docs.ssomar.com/");
                }
                option = builderInstance.getDefaultValue();
            }
            this.value = option;
            if (!isPremiumLoading && builderInstance.getPremiumOption().contains(option)) {
                errors.add("&cERROR, Couldn't load the Option value of " + this.getName() + " from config, value: " + value + " &7&o" + getParent().getParentInfo() + " &6>> Because it's a premium Option !");
                value = builderInstance.getDefaultValue();
            }
        } catch (Exception e) {
            errors.add("&cERROR, Couldn't load the Option value of " + this.getName() + " from config, value: " + colorStr + " &7&o" + getParent().getParentInfo() + " &6>> Options available: https://docs.ssomar.com/");
            this.value = builderInstance.getDefaultValue();
        }
        return errors;
    }

    @Override
    public void save(ConfigurationSection config) {
        config.set(this.getName(), value.toString());
    }

    @Override
    public SOption getValue() {
        return value;
    }

    @Override
    public SOptionFeature initItemParentEditor(GUI gui, int slot) {
        String[] finalDescription = new String[getEditorDescription().length + 3];
        System.arraycopy(getEditorDescription(), 0, finalDescription, 0, getEditorDescription().length);
        finalDescription[finalDescription.length - 3] = GUI.CLICK_HERE_TO_CHANGE;
        finalDescription[finalDescription.length - 2] = "&8>> &6SHIFT : &eBOOST SCROLL";
        finalDescription[finalDescription.length - 1] = "&8>> &6UP: &eRIGHT | &6DOWN: &eLEFT";

        gui.createItem(getEditorMaterial(), 1, slot, GUI.TITLE_COLOR + getEditorName(), false, false, finalDescription);
        return this;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {
        updateOption(getValue(), gui, !getPlugin().isLotOfWork(), true);
    }

    @Override
    public SOptionFeature clone(FeatureParentInterface newParent) {
        SOptionFeature clone = new SOptionFeature(plugin, builderInstance, newParent, getFeatureSettings());
        clone.setValue(value);
        clone.setInvalidInConfig(invalidInConfig);
        return clone;
    }

    @Override
    public void reset() {
        this.value = builderInstance.getDefaultValue();
        this.invalidInConfig = false;
    }

    public void setValue(SOption value) {
        this.value = value;
        // chosen from the editor / API: the option is valid again
        this.invalidInConfig = false;
    }

    /** Names of the available options closest to {@code typed}, for the error message. */
    private List<String> closestOptionNames(String typed, int max) {
        List<String> names = new ArrayList<>();
        for (SOption option : builderInstance.getValues()) names.add(option.getName());
        return closestNames(typed, names, max);
    }

    /**
     * The {@code max} names of {@code candidates} with the smallest edit distance to {@code typed}
     * (case-insensitive), best first. Pure helper, unit-tested.
     */
    public static List<String> closestNames(String typed, List<String> candidates, int max) {
        String t = typed == null ? "" : typed.toUpperCase();
        SortedMap<Integer, List<String>> byDistance = new TreeMap<>();
        List<String> typedWords = sortedWords(t);
        for (String candidate : candidates) {
            if (candidate == null) continue;
            String c = candidate.toUpperCase();
            // Same words in another order (PLAYER_BREAK_BLOCK vs PLAYER_BLOCK_BREAK) is the most likely intent.
            int d = typedWords.equals(sortedWords(c)) ? 0 : editDistance(t, c);
            byDistance.computeIfAbsent(d, k -> new ArrayList<>()).add(candidate);
        }
        List<String> result = new ArrayList<>();
        for (List<String> group : byDistance.values()) {
            for (String name : group) {
                if (result.size() >= max) return result;
                result.add(name);
            }
        }
        return result;
    }

    private static List<String> sortedWords(String name) {
        List<String> words = new ArrayList<>(Arrays.asList(name.split("_")));
        Collections.sort(words);
        return words;
    }

    private static int editDistance(String a, String b) {
        int[] prev = new int[b.length() + 1];
        int[] cur = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) prev[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            cur[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev; prev = cur; cur = tmp;
        }
        return prev[b.length()];
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
        SOption option = getOption((GUI) manager.getCache().get(editor));
        option = nextOption(option);
        option = nextOption(option);
        option = nextOption(option);
        option = nextOption(option);
        option = nextOption(option);
        option = nextOption(option);
        option = nextOption(option);
        option = nextOption(option);
        option = nextOption(option);
        option = nextOption(option);
        option = nextOption(option);
        option = nextOption(option);
        option = nextOption(option);
        option = nextOption(option);
        option = nextOption(option);
        updateOption(option, (GUI) manager.getCache().get(editor), !getPlugin().isLotOfWork(), true);
        return true;
    }

    @Override
    public boolean shiftRightClicked(Player editor, NewGUIManager manager) {
        SOption option = getOption((GUI) manager.getCache().get(editor));
        option = prevOption(option);
        option = prevOption(option);
        option = prevOption(option);
        option = prevOption(option);
        option = prevOption(option);
        option = prevOption(option);
        option = prevOption(option);
        option = prevOption(option);
        option = prevOption(option);
        option = prevOption(option);
        option = prevOption(option);
        option = prevOption(option);
        option = prevOption(option);
        option = prevOption(option);
        option = prevOption(option);
        updateOption(option, (GUI) manager.getCache().get(editor), !getPlugin().isLotOfWork(), false);
        return true;
    }

    @Override
    public boolean leftClicked(Player editor, NewGUIManager manager) {
        updateOption(nextOption(getOption((GUI) manager.getCache().get(editor))), (GUI) manager.getCache().get(editor), !getPlugin().isLotOfWork(), true);
        return true;
    }

    @Override
    public boolean rightClicked(Player editor, NewGUIManager manager) {
        updateOption(prevOption(getOption((GUI) manager.getCache().get(editor))), (GUI) manager.getCache().get(editor), !getPlugin().isLotOfWork(), false);
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

    public SOption nextOption(SOption option) {
        boolean next = false;
        for (SOption check : getSortOptions()) {
            if (check.equals(option)) {
                next = true;
                continue;
            }
            if (next) return check;
        }
        return getSortOptions().get(0);
    }

    public SOption prevOption(SOption option) {
        int i = -1;
        int cpt = 0;
        for (SOption check : getSortOptions()) {
            if (check.equals(option)) {
                i = cpt;
                break;
            }
            cpt++;
        }
        if (i == 0) return getSortOptions().get(getSortOptions().size() - 1);
        else return getSortOptions().get(cpt - 1);
    }

    public void updateOption(SOption option, GUI gui, boolean isPremiumLoading, boolean next) {
       SsomarDev.testMsg("updateOption  "+ option+" >>"+builderInstance.getPremiumOption().contains(option), true);
        while (!isPremiumLoading && builderInstance.getPremiumOption().contains(option)) {
            if (next) option = nextOption(option);
            else option = prevOption(option);
        }
        value = option;
        ItemStack item = gui.getByIdentifier(getEditorName());
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore().subList(0, getEditorDescription().length + 3);
        boolean find = false;
        for (SOption check : getSortOptions()) {
            if (option.equals(check)) {
                lore.add(StringConverter.coloredString("&2➤ &a" + option));
                find = true;
            } else if (find) {
                if (lore.size() == 17) break;
                if (!isPremiumLoading && builderInstance.getPremiumOption().contains(check))
                    lore.add(StringConverter.coloredString("&6✦ &e" + check + " &7Premium"));
                else
                    lore.add(StringConverter.coloredString("&6✦ &e" + check));
            }
        }
        for (SOption check : getSortOptions()) {
            if (lore.size() == 17) break;
            else {
                if (!isPremiumLoading && builderInstance.getPremiumOption().contains(check))
                    lore.add(StringConverter.coloredString("&6✦ &e" + check + " &7Premium"));
                else
                    lore.add(StringConverter.coloredString("&6✦ &e" + check));
            }
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        /* Bug item no update idk why */
        UpdateItemInGUI.updateItemInGUI(gui, getEditorName(), meta.getDisplayName(), lore, item.getType());
    }

    public SOption getOption(GUI gui) {
        ItemStack item = gui.getByIdentifier(getEditorName());
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        for (String str : lore) {
            if (str.contains("➤ ")) {
                str = StringConverter.decoloredString(str).replaceAll(" Premium", "");
                return builderInstance.getOption(str.split("➤ ")[1]);
            }
        }
        return null;
    }

    public List<SOption> getSortOptions() {
        SortedMap<String, SOption> map = new TreeMap<String, SOption>();
        for (SOption l : builderInstance.getValues()) {
            map.put(l.toString(), l);
        }
        return new ArrayList<>(map.values());
    }

}
