package com.ssomar.score.features.types.list;

import com.ssomar.score.SCore;
import com.ssomar.score.SsomarDev;
import com.ssomar.score.editor.NewGUIManager;
import com.ssomar.score.editor.Suggestion;
import com.ssomar.score.features.*;
import com.ssomar.score.languages.messages.TM;
import com.ssomar.score.languages.messages.Text;
import com.ssomar.score.menu.EditorCreator;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.usedapi.Dependency;
import com.ssomar.score.usedapi.MythicMobsAPI;
import com.ssomar.score.utils.numbers.NTools;
import com.ssomar.score.utils.strings.StringConverter;
import de.tr7zw.nbtapi.NBTEntity;
import de.tr7zw.nbtapi.NBTType;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
@Setter
public class ListDetailedEntityFeature extends FeatureAbstract<List<String>, ListDetailedEntityFeature> implements FeatureRequireSubTextEditorInEditor {

    private static final String symbolStart = "{";
    private static final String symbolEnd = "}";
    private static final String symbolEquals = ":";
    private static final String symbolSeparator = "\\+";
    private static final String mythicMobsSymbol = "MM-";

    private static final String symbolNegation = "!";
    private static final String customListPrefix = "SCORE:";

    private static final Boolean DEBUG = true;
    private List<String> value;
    private List<String> defaultValue;
    private boolean notSaveIfEqualsToDefaultValue;

    public ListDetailedEntityFeature(FeatureParentInterface parent, List<String> defaultValue, FeatureSettingsInterface featureSettings, boolean notSaveIfEqualsToDefaultValue) {
        super(parent, featureSettings);
        this.defaultValue = defaultValue;
        this.notSaveIfEqualsToDefaultValue = notSaveIfEqualsToDefaultValue;
        reset();
    }

    public List<String> load(SPlugin plugin, List<String> entries, boolean isPremiumLoading) {
        List<String> errors = new ArrayList<>();
        value = new ArrayList<>();

        // Expand custom lists
        List<String> expandedEntries = new ArrayList<>();
        for (String s : entries) {
            String baseValue = StringConverter.decoloredString(s);
            String checkValue = baseValue;
            boolean isNegated = false;
            if(checkValue.startsWith(symbolNegation)) {
                checkValue = checkValue.substring(1);
                isNegated = true;
            }

            // Check if this is a custom list reference
            if (checkValue.toUpperCase().startsWith(customListPrefix)) {
                String listName = checkValue.substring(customListPrefix.length());
                Optional<List<String>> customList = com.ssomar.score.features.custom.customlists.CustomListsManager.getInstance().getEntityList(listName);
                if (customList.isPresent()) {
                    // Add all entities from the custom list
                    for (String entity : customList.get()) {
                        expandedEntries.add(isNegated ? symbolNegation + entity : entity);
                    }
                } else {
                    errors.add("&cERROR, Custom entity list '" + listName + "' not found in SCore config.yml &7&o" + getParent().getParentInfo());
                }
                continue;
            }

            expandedEntries.add(baseValue);
        }

        for (String s : expandedEntries) {
            String baseValue = s;
            String checkValue = baseValue;
            if(checkValue.startsWith(symbolNegation)) checkValue = checkValue.substring(1);
            String entityTypeStr = checkValue;

            if (s.contains(mythicMobsSymbol)) {
                if (SCore.hasMythicMobs) value.add(baseValue);
                //TODO make a little verification
                continue;
            } else if (checkValue.contains(symbolStart)) {
                entityTypeStr = checkValue.split("\\" + symbolStart)[0];
                if (SCore.hasNBTAPI) {
                    String datas = checkValue.split("\\" + symbolStart)[1].replace(symbolEnd, "");
                    for (String data : datas.split(symbolSeparator)) {
                        String[] dataSplit = data.split(symbolEquals);
                        if (dataSplit.length != 2) {
                            errors.add("&cERROR, Couldn't load the The tags value of " + this.getName() + " from config, value: " + s + " &7&o" + getParent().getParentInfo() + " &6>> Example : ZOMBIE{CustomName:\"My name\"}   PIG{Invulnerable:1} ");
                            continue;
                        }
                    }
                } else
                    errors.add("&cERROR, Couldn't load the The tags value of " + this.getName() + " from config, value: " + s + " &7&o" + getParent().getParentInfo() + " &6>> Because it requires the plugin NBTAPI ");
            }

            try {
                if(!entityTypeStr.equals("*")) EntityType.valueOf(entityTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.add("&cERROR, Couldn't load the EntityType value of " + this.getName() + " from config, value: " + s + " &7&o" + getParent().getParentInfo() + " &6>> EntityTypes available: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html , you can also use MythicMobs entities with the prefix MM- , or accept all entities with *");
                continue;
            }
            value.add(baseValue);
        }
        FeatureReturnCheckPremium<List<String>> checkPremium = checkPremium("List of Detailed entities", value, Optional.of(defaultValue), isPremiumLoading);
        if (checkPremium.isHasError()) value = checkPremium.getNewValue();
        return errors;
    }

    @Override
    public List<String> load(SPlugin plugin, ConfigurationSection config, boolean isPremiumLoading) {
        return this.load(plugin, config.getStringList(this.getName()), isPremiumLoading);
    }

    public List<String> getWhiteListValue() {
        List<String> whiteList = new ArrayList<>();
        for (String s : value) {
            if (!s.startsWith(symbolNegation)) whiteList.add(s);
        }
        return whiteList;
    }

    public List<String> getBlackListValue() {
        List<String> blackList = new ArrayList<>();
        for (String s : value) {
            if (s.startsWith(symbolNegation)) blackList.add(s.substring(1));
        }
        return blackList;
    }

    /**
     * Return map with entitytype and tags
     **/
    public Map<EntityType, List<Map<String, String>>> extractCondition(List<String> values) {
        Map<EntityType, List<Map<String, String>>> conditions = new HashMap<>();
        for (String s : values) {
            if (s.contains(mythicMobsSymbol)) continue;
            String entityTypeStr = s;
            EntityType type;
            Map<String, String> tags = new HashMap<>();

            if (s.contains(symbolStart)) {
                entityTypeStr = s.split("\\" + symbolStart)[0];
                String datas = s.split("\\" + symbolStart)[1].replace(symbolEnd, "");
                for (String data : datas.split(symbolSeparator)) {
                    String[] dataSplit = data.split(symbolEquals);
                    tags.put(dataSplit[0], dataSplit[1]);
                }
            }
            if(entityTypeStr.equals("*")){
                for(EntityType entityType : EntityType.values()){
                    if (conditions.containsKey(entityType)) {
                        conditions.get(entityType).add(tags);
                    } else {
                        conditions.put(entityType, new ArrayList<>(Collections.singletonList(tags)));
                    }
                }
            }
            else {
                type = EntityType.valueOf(entityTypeStr.toUpperCase());

                if (conditions.containsKey(type)) {
                    conditions.get(type).add(tags);
                } else {
                    conditions.put(type, new ArrayList<>(Collections.singletonList(tags)));
                }
            }
        }
        return conditions;
    }

    public Map<EntityType, List<Map<String, String>>> extractWhiteListCondition() {
        return extractCondition(getWhiteListValue());
    }

    public Map<EntityType, List<Map<String, String>>> extractBlackListCondition() {
        return extractCondition(getBlackListValue());
    }

    /**
     * The list of MM ids
     **/
    public List<String> extractMMCondition(List<String> values) {
        List<String> mythicMobs = new ArrayList<>();
        for (String s : values) {
            if (s.contains(mythicMobsSymbol)) {
                mythicMobs.add(s.replace(mythicMobsSymbol, ""));
            }
        }
        return mythicMobs;
    }

    public List<String> extractWhiteListMMCondition() {
        return extractMMCondition(getWhiteListValue());
    }

    public List<String> extractBlackListMMCondition() {
        return extractMMCondition(getBlackListValue());
    }

    public boolean isValidEntity(@NotNull Entity entity) {

        /* Verif MythicMob */
        boolean hasWLMMCondition = !extractWhiteListMMCondition().isEmpty();
        boolean hasBLMMCondition = !extractBlackListMMCondition().isEmpty();

        if (SCore.hasMythicMobs) {
            //SsomarDev.testMsg("hasWLMMCondition: " + hasWLMMCondition, true);
            if (hasWLMMCondition && MythicMobsAPI.isMythicMob(entity, extractWhiteListMMCondition())) return true;
            //SsomarDev.testMsg("hasBLMMCondition: " + hasBLMMCondition, true);
            if (hasBLMMCondition && MythicMobsAPI.isMythicMob(entity, extractBlackListMMCondition())) return false;
        }

        Map<EntityType, List<Map<String, String>>> conditionsWL = extractWhiteListCondition();
        Map<EntityType, List<Map<String, String>>> conditionsBL = extractBlackListCondition();

        EntityType type = entity.getType();

        for (EntityType t : conditionsBL.keySet()) {
            if (t.equals(type)) {
                List<Map<String, String>> tagsList = conditionsBL.get(t);
                if (tagsList.isEmpty()) return false;
                if(verifTags(entity, tagsList)) return false;
            }
        }

        /* empty = accept all */
        if (conditionsWL.isEmpty()){
            if(hasWLMMCondition) return false;
            return true;
        }

        for (EntityType t : conditionsWL.keySet()) {
            if (t.equals(type)) {
                List<Map<String, String>> tagsList = conditionsWL.get(t);
                if (tagsList.isEmpty()) return true;
                if(verifTags(entity, tagsList)) return true;
            }
        }

        return false;
    }

    public boolean verifTags(Entity entity, List<Map<String, String>> tagsList) {
        for (Map<String, String> tags : tagsList) {
            boolean invalid = false;
            if (tags.isEmpty()) return true;
            else if(Dependency.NBTAPI.isEnabled()){
                NBTEntity nbtent = new NBTEntity(entity);
                for (String key : tags.keySet()) {
                    String value = tags.get(key);
                    NBTType nbtType = nbtent.getType(key);

                    SsomarDev.testMsg("VERIF key: " + key + " value: " + value + " type: " + entity.getType(), DEBUG);

                    /* NBT-API answers null (not NBTTagEnd) for a key it cannot read on this server:
                     * a whitelist with NBT tags then threw an NPE for EVERY entity scanned by
                     * MOB_AROUND, thousands of stack traces per minute (report of 2026-08-14).
                     * An unreadable tag simply does not match. */
                    if (nbtType == null) return false;

                    switch (nbtType) {
                        /* The tag was not found */
                        case NBTTagEnd:
                            return false;
                        case NBTTagByte:
                            value = value.replace("b", "");
                            if (NTools.isNumber(value)) {
                                SsomarDev.testMsg("Byte: " + nbtent.getByte(key), DEBUG);
                                if (nbtent.getByte(key) != Byte.parseByte(value)) invalid = true;
                            }
                            break;
                        case NBTTagShort:
                        case NBTTagByteArray:
                        case NBTTagIntArray:
                        case NBTTagList:
                        case NBTTagCompound:
                            break;
                        case NBTTagInt:
                            Optional<Integer> optInt = NTools.getInteger(value);
                            if (optInt.isPresent()) {
                                SsomarDev.testMsg("Int: " + nbtent.getInteger(key), DEBUG);
                                if (nbtent.getInteger(key) != optInt.get()) invalid = true;
                            }
                            break;
                        case NBTTagLong:
                            Optional<Long> optLong = NTools.getLong(value);
                            if (optLong.isPresent()) {
                                SsomarDev.testMsg("Long: " + nbtent.getLong(key), DEBUG);
                                if (nbtent.getLong(key) != optLong.get()) invalid = true;
                            }
                            break;
                        case NBTTagFloat:
                            Optional<Float> optFloat = NTools.getFloat(value);
                            if (optFloat.isPresent()) {
                                SsomarDev.testMsg("Float: " + nbtent.getFloat(key), DEBUG);
                                if (nbtent.getFloat(key) != optFloat.get()) invalid = true;
                            }
                            break;
                        case NBTTagDouble:
                            Optional<Double> optDouble = NTools.getDouble(value);
                            if (optDouble.isPresent()) {
                                SsomarDev.testMsg("Double: " + nbtent.getDouble(key), DEBUG);
                                if (nbtent.getDouble(key) != optDouble.get()) invalid = true;
                            }
                            break;
                        case NBTTagString:
                            if (value.toLowerCase().equals("true") || value.toLowerCase().equals("false")) {
                                SsomarDev.testMsg("Boolean: " + nbtent.getBoolean(key), DEBUG);
                                if (nbtent.getBoolean(key) != Boolean.parseBoolean(value)) invalid = true;
                            } else {
                                if (key.equalsIgnoreCase(("CustomName"))) {
                                    String customName = entity.getCustomName();
                                    SsomarDev.testMsg("String: " + customName, DEBUG);
                                    if(value.equals("*")) break;
                                    if (!StringConverter.decoloredString(customName).equals(value))
                                        invalid = true;
                                } else {
                                    SsomarDev.testMsg("String: " + nbtent.getString(key), DEBUG);
                                    if (!nbtent.getString(key).contains("\"" + value + "\"")) invalid = true;
                                }
                            }
                            break;
                    }
                    if (invalid) break;
                }
                if (invalid) continue;

                return true;
            }
        }
        return false;
    }

    @Override
    public void save(ConfigurationSection config) {
        if (notSaveIfEqualsToDefaultValue) {
            if (new HashSet<>(defaultValue).containsAll(value)) {
                config.set(this.getName(), null);
                return;
            }
        }
        config.set(this.getName(), value);
    }

    public List<String> getValues() {
        return value;
    }

    @Override
    public ListDetailedEntityFeature initItemParentEditor(GUI gui, int slot) {
        String[] finalDescription = new String[getEditorDescription().length + 2];
        System.arraycopy(getEditorDescription(), 0, finalDescription, 0, getEditorDescription().length);
        finalDescription[finalDescription.length - 2] = GUI.CLICK_HERE_TO_CHANGE;
        finalDescription[finalDescription.length - 1] = TM.g(Text.EDITOR_CURRENTLY_NAME);

        gui.createItem(getEditorMaterial(), 1, slot, GUI.TITLE_COLOR + getEditorName(), false, false, finalDescription);
        return this;
    }

    @Override
    public void updateItemParentEditor(GUI gui) {
        gui.updateConditionList(getEditorName(), getValues(), "&cEMPTY");
    }


    @Override
    public ListDetailedEntityFeature clone(FeatureParentInterface newParent) {
        ListDetailedEntityFeature clone = new ListDetailedEntityFeature(newParent, getDefaultValue(), getFeatureSettings(), isNotSaveIfEqualsToDefaultValue());
        clone.setValue(getValues());
        return clone;
    }

    @Override
    public void reset() {
        this.value = new ArrayList<>(defaultValue);
    }

    @Override
    public Optional<String> verifyMessageReceived(String message) {
        String s = StringConverter.decoloredString(message);
        if(s.startsWith(symbolNegation)) s = s.substring(1);
        String entityTypeStr = s;

        // Allow custom list references
        if (entityTypeStr.toUpperCase().startsWith(customListPrefix)) {
            String listName = entityTypeStr.substring(customListPrefix.length());
            if (com.ssomar.score.features.custom.customlists.CustomListsManager.getInstance().hasEntityList(listName)) {
                return Optional.empty();
            } else {
                return Optional.of("&4&l[ERROR] &cCustom entity list '" + listName + "' not found in SCore config.yml");
            }
        }

        if (s.contains(mythicMobsSymbol)) {
            return Optional.empty();
        }
        if (s.contains(symbolStart)) {
            entityTypeStr = s.split("\\" + symbolStart)[0];
            if (SCore.hasNBTAPI) {
                String datas = s.split("\\" + symbolStart)[1].replace(symbolEnd, "");
                for (String data : datas.split(symbolSeparator)) {
                    String[] dataSplit = data.split(symbolEquals);
                    if (dataSplit.length != 2) {
                        return Optional.of("&4&l[ERROR] &cThe message you entered contains an invalid format  &6>> Example : ZOMBIE{CustomName:\"My name\"}   PIG{Invulnerable:1} &7(Check the wiki if you want more examples)");
                    }
                }
            } else
                return Optional.of("&4&l[ERROR] &cThe message you entered contains Tags, but it requires the plugin NBTAPI ! so please install it or don't enter tags !");
        }

        try {
            EntityType.valueOf(entityTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Optional.of("&4&l[ERROR] &cThe message you entered contains an invalid EntityType ! (Check the wiki if you want the list)");
        }
        return Optional.empty();
    }

    @Override
    public List<String> getCurrentValues() {
        return value;
    }

    @Override
    public List<TextComponent> getMoreInfo() {
        return null;
    }

    @Override
    public List<Suggestion> getSuggestions() {
        return new ArrayList<>();
    }

    @Override
    public String getTips() {
        return "&8Example &7&oBLAZE &8- &7&oZOMBIE{CustomName:\"My name\"} &8- &e(for MythicMob) &7&oMM-&eID &8- &e(for custom lists) &7&oSCORE:LIST_NAME";
    }

    @Override
    public void finishEditInSubEditor(Player editor, NewGUIManager manager) {
        value = (List<String>) manager.currentWriting.get(editor);
        for (int i = 0; i < value.size(); i++) {
            value.set(i, StringConverter.decoloredString(value.get(i)));
        }
        manager.requestWriting.remove(editor);
        manager.activeTextEditor.remove(editor);
        updateItemParentEditor((GUI) manager.getCache().get(editor));
    }


    @Override
    public void sendBeforeTextEditor(Player playerEditor, NewGUIManager manager) {
        space(playerEditor);
        List<String> beforeMenu = new ArrayList<>();
        beforeMenu.add("&7➤ Your custom " + getEditorName() + ":");

        HashMap<String, String> suggestions = new HashMap<>();

        EditorCreator editor = new EditorCreator(beforeMenu, (List<String>) manager.currentWriting.get(playerEditor), getEditorName() + ":", true, true, true, true,
                true, true, false, "", suggestions);
        editor.generateTheMenuAndSendIt(playerEditor);
    }
}
