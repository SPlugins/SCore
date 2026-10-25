package com.ssomar.score.features.types.list;

import com.ssomar.score.editor.NewGUIManager;
import com.ssomar.score.editor.Suggestion;
import com.ssomar.score.features.FeatureAbstract;
import com.ssomar.score.features.FeatureParentInterface;
import com.ssomar.score.features.FeatureRequireSubTextEditorInEditor;
import com.ssomar.score.features.FeatureSettingsInterface;
import com.ssomar.score.languages.messages.TM;
import com.ssomar.score.languages.messages.Text;
import com.ssomar.score.menu.EditorCreator;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.strings.StringConverter;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Function;

@Getter
@Setter
public class ListEnchantAndLevelFeature extends FeatureAbstract<Map<Enchantment, Integer>, ListEnchantAndLevelFeature> implements FeatureRequireSubTextEditorInEditor {

    /** Written instead of the level (LOYALTY:*), like no level at all (LOYALTY): every level of the enchantment */
    public static final String ALL_LEVELS = "*";

    /* One level per enchantment, the last one written */
    private Map<Enchantment, Integer> value;
    private Map<Enchantment, Integer> defaultValue;
    private boolean notSaveIfEqualsToDefaultValue;
    /* Every level written for each enchantment, in the written order, an empty set means every level */
    private Map<Enchantment, Set<Integer>> levels;

    public ListEnchantAndLevelFeature(FeatureParentInterface parent, Map<Enchantment, Integer> defaultValue, FeatureSettingsInterface featureSettings, boolean notSaveIfEqualsToDefaultValue) {
        super(parent, featureSettings);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.notSaveIfEqualsToDefaultValue = notSaveIfEqualsToDefaultValue;
        reset();
    }

    @Override
    public List<String> load(SPlugin plugin, ConfigurationSection config, boolean isPremiumLoading) {
        List<String> errors = new ArrayList<>();
        setEnchants(config.getStringList(this.getName()), errors);
        return errors;
    }

    /** One level per enchantment (the last one written), see getLevels() for all the levels */
    public Map<Enchantment, Integer> transformEnchants(List<String> enchantsConfig, List<String> errorList) {
        Map<Enchantment, Integer> result = new HashMap<>();
        transformEnchants(enchantsConfig, errorList, new LinkedHashMap<>(), result);
        return result;
    }

    public void transformEnchants(List<String> enchantsConfig, List<String> errorList, Map<Enchantment, Set<Integer>> levels, Map<Enchantment, Integer> lastLevels) {
        for (String s : enchantsConfig) {
            if (!readEntry(s, ListEnchantAndLevelFeature::getEnchantment, levels, lastLevels))
                errorList.add("&cERROR, Couldn't load the Enchant with level value of " + this.getName() + " from config, value: " + s + " &7&o" + getParent().getParentInfo() + " &6>> correct form > ENCHANTMENT:LEVEL or ENCHANTMENT (all its levels)  example> POWER:1 !");
        }
    }

    private void setEnchants(List<String> enchantsConfig, List<String> errorList) {
        Map<Enchantment, Set<Integer>> newLevels = new LinkedHashMap<>();
        Map<Enchantment, Integer> newValue = new HashMap<>();
        transformEnchants(enchantsConfig, errorList, newLevels, newValue);
        this.levels = newLevels;
        this.value = newValue;
    }

    private static Enchantment getEnchantment(String name) {
        try {
            return Enchantment.getByName(name);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Reads one entry: ENCHANTMENT:LEVEL, or ENCHANTMENT / ENCHANTMENT:* for all its levels.
     * The enchantment can have a namespace: minecraft:loyalty:2, minecraft:loyalty
     *
     * @param levels     receives every level written for each enchantment (an empty set = all the levels)
     * @param lastLevels receives the last level written for each enchantment
     * @return false if the entry is not valid
     */
    public static <E> boolean readEntry(String entry, Function<String, E> getEnchantment, Map<E, Set<Integer>> levels, Map<E, Integer> lastLevels) {
        String name = entry;
        String levelStr = ALL_LEVELS;
        boolean canBeNamespace = false;
        if (entry.contains(":")) {
            String[] decomp = entry.split(":");
            if (decomp.length >= 3) {
                name = decomp[0] + ":" + decomp[1];
                levelStr = decomp[2];
            } else if (decomp.length == 2) {
                name = decomp[0];
                levelStr = decomp[1];
                canBeNamespace = true;
            } else return false;
        }

        E enchant = getEnchantment.apply(name);
        Integer level = null;
        if (enchant != null && !ALL_LEVELS.equals(levelStr)) {
            try {
                level = Integer.parseInt(levelStr);
            } catch (NumberFormatException e) {
                enchant = null;
            }
        }
        if (enchant == null && canBeNamespace) {
            enchant = getEnchantment.apply(entry);
            level = null;
        }
        if (enchant == null) return false;

        Set<Integer> written = levels.get(enchant);
        if (level == null) {
            if (written == null) levels.put(enchant, new LinkedHashSet<>());
            else written.clear();
        } else {
            if (written == null) {
                written = new LinkedHashSet<>();
                written.add(level);
                levels.put(enchant, written);
            } else if (!written.isEmpty()) written.add(level);
            lastLevels.put(enchant, level);
        }
        return true;
    }

    public static boolean hasLevel(Set<Integer> written, int level) {
        return written.isEmpty() || written.contains(level);
    }

    /** True if every enchantment of the list is in the enchants, at one of the levels written for it */
    public static <E> boolean hasAll(Map<E, Integer> enchants, Map<E, Set<Integer>> levels) {
        for (Map.Entry<E, Set<Integer>> entry : levels.entrySet()) {
            Integer level = enchants.get(entry.getKey());
            if (level == null || !hasLevel(entry.getValue(), level)) return false;
        }
        return true;
    }

    /** True if one enchantment of the list is in the enchants, at one of the levels written for it */
    public static <E> boolean hasOne(Map<E, Integer> enchants, Map<E, Set<Integer>> levels) {
        for (Map.Entry<E, Set<Integer>> entry : levels.entrySet()) {
            Integer level = enchants.get(entry.getKey());
            if (level != null && hasLevel(entry.getValue(), level)) return true;
        }
        return false;
    }

    /** ENCHANTMENT:LEVEL for each level written, ENCHANTMENT alone for all the levels */
    public static <E> List<String> toStrings(Map<E, Set<Integer>> levels, Function<E, String> getName) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<E, Set<Integer>> entry : levels.entrySet()) {
            String name = getName.apply(entry.getKey());
            if (entry.getValue().isEmpty()) result.add(name);
            else {
                for (Integer level : entry.getValue()) {
                    result.add(name + ":" + level);
                }
            }
        }
        return result;
    }

    @Override
    public void save(ConfigurationSection config) {
        config.set(this.getName(), null);
        if (notSaveIfEqualsToDefaultValue && levels.isEmpty()) return;
        config.set(this.getName(), this.getCurrentValues());
    }

    public Map<Enchantment, Integer> getValues() {
        return value;
    }

    /** One level per enchantment */
    public void setValue(Map<Enchantment, Integer> value) {
        this.value = value;
        this.levels = new LinkedHashMap<>();
        for (Map.Entry<Enchantment, Integer> entry : value.entrySet()) {
            Set<Integer> level = new LinkedHashSet<>();
            level.add(entry.getValue());
            this.levels.put(entry.getKey(), level);
        }
    }

    @Override
    public ListEnchantAndLevelFeature initItemParentEditor(GUI gui, int slot) {
        String[] finalDescription = new String[getEditorDescription().length + 2];
        System.arraycopy(getEditorDescription(), 0, finalDescription, 0, getEditorDescription().length);
        finalDescription[finalDescription.length - 2] = GUI.CLICK_HERE_TO_CHANGE;
        finalDescription[finalDescription.length - 1] = TM.g(Text.EDITOR_CURRENTLY_NAME);

        gui.createItem(getEditorMaterial(), 1, slot, GUI.TITLE_COLOR + getEditorName(), false, false, finalDescription);
        return this;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {
        gui.updateConditionList(getEditorName(), this.getCurrentValues(), "&cEMPTY");
    }

    @Override
    public ListEnchantAndLevelFeature clone(FeatureParentInterface newParent) {
        ListEnchantAndLevelFeature clone = new ListEnchantAndLevelFeature(newParent, getDefaultValue(), getFeatureSettings(), isNotSaveIfEqualsToDefaultValue());
        clone.value = value;
        clone.levels = levels;
        return clone;
    }

    @Override
    public void reset() {
        setValue(new HashMap<>(defaultValue));
    }

    @Override
    public Optional<String> verifyMessageReceived(String message) {
        String s = StringConverter.decoloredString(message);
        if (!readEntry(s, ListEnchantAndLevelFeature::getEnchantment, new HashMap<>(), new HashMap<>()))
            return Optional.of("&4&l[ERROR] &cThe message you entered &8(&7" + s + "&8)&c is not an enchant with level &6>> correct form > ENCHANTMENT:LEVEL or ENCHANTMENT (all its levels)  example> POWER:1 !");
        return Optional.empty();
    }

    @Override
    public List<String> getCurrentValues() {
        return toStrings(levels, Enchantment::getName);
    }

    @Override
    public List<TextComponent> getMoreInfo() {
        return null;
    }

    @Override
    public List<Suggestion> getSuggestions() {
        SortedMap<String, Suggestion> map = new TreeMap<String, Suggestion>();
        for (Enchantment enchant : Enchantment.values()) {
            map.put(enchant.getName(), new Suggestion(enchant.getName() + ":1", "&6[" + "&e" + enchant.getName() + "&6]", "&7Add &e" + enchant.getName()));
        }
        return new ArrayList<>(map.values());
    }

    @Override
    public String getTips() {
        return "&8Example &7&oPOWER:1 &8or &7&oPOWER &8(all its levels)";
    }

    @Override
    public void finishEditInSubEditor(Player editor, NewGUIManager manager) {
        List<String> preResult = (List<String>) manager.currentWriting.get(editor);
        for (int i = 0; i < preResult.size(); i++) {
            preResult.set(i, StringConverter.decoloredString(preResult.get(i)));
        }
        setEnchants(preResult, new ArrayList<>());
        manager.requestWriting.remove(editor);
        manager.activeTextEditor.remove(editor);
        updateItemParentEditor((GUI) manager.getCache().get(editor));
    }


    @Override
    public void sendBeforeTextEditor(Player playerEditor, NewGUIManager manager) {
        List<String> beforeMenu = new ArrayList<>();
        beforeMenu.add("&7➤ Your custom " + getEditorName() + ":");

        HashMap<String, String> suggestions = new HashMap<>();

        EditorCreator editor = new EditorCreator(beforeMenu, (List<String>) manager.currentWriting.get(playerEditor), getEditorName() + ":", true, true, true, true,
                true, true, true, "", suggestions);
        editor.generateTheMenuAndSendIt(playerEditor);
    }
}
