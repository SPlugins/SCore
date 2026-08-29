package com.ssomar.score.features.types.list;

import com.ssomar.executableblocks.api.ExecutableBlocksAPI;
import com.ssomar.executableblocks.executableblocks.ExecutableBlockObject;
import com.ssomar.executableitems.executableitems.ExecutableItemObject;
import com.ssomar.score.SCore;
import com.ssomar.score.SsomarDev;
import com.ssomar.score.api.executableblocks.config.placed.ExecutableBlockPlacedInterface;
import com.ssomar.score.api.executableitems.ExecutableItemsAPI;
import com.ssomar.score.editor.NewGUIManager;
import com.ssomar.score.editor.Suggestion;
import com.ssomar.score.features.FeatureParentInterface;
import com.ssomar.score.features.FeatureSettingsInterface;
import com.ssomar.score.menu.EditorCreator;
import com.ssomar.score.usedapi.Dependency;
import com.ssomar.score.usedapi.ItemsAdderAPI;
import com.ssomar.score.utils.emums.MaterialWithGroups;
import com.ssomar.score.utils.strings.StringConverter;
import com.ssomar.score.utils.tags.MinecraftTags;
import de.tr7zw.nbtapi.NBTItem;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class ListDetailedMaterialFeature extends ListFeatureAbstract<String, ListDetailedMaterialFeature> implements Serializable {

    private static final String symbolStart = "{";
    private static final String symbolEnd = "}";
    private static final String symbolEquals = ":";
    private static final String symbolSeparator = "\\+";
    private static final Boolean DEBUG = true;

    private static final String symbolStartMaterialTag = "#"; // #minecraft:mineable/pickaxe
    private static final String customListPrefix = "SCORE:";
    private List<String> listOfCustomBlocksPluginSupported;

    /* If it checks blocks tags or not, if not it checks item tags */
    private boolean forBlocks;

    private boolean specificationOfAtLeastOneState;

    public ListDetailedMaterialFeature(FeatureParentInterface parent, List<String> defaultValue, FeatureSettingsInterface featureSettings, boolean forBlocks) {
        super(parent,"List of Materials",  defaultValue, featureSettings);
        this.listOfCustomBlocksPluginSupported = new ArrayList<>();
        if (SCore.hasItemsAdder) listOfCustomBlocksPluginSupported.add("ITEMSADDER");
        if (SCore.hasExecutableItems) listOfCustomBlocksPluginSupported.add("EXECUTABLEITEMS");
        if (SCore.hasExecutableBlocks) listOfCustomBlocksPluginSupported.add("EXECUTABLEBLOCKS");
        //if(SCore.hasOraxen) listOfCustomBlocksPluginSupported.add("ORAXEN");
        this.forBlocks = forBlocks;
        reset();
    }

    private static final Map<String, ListDetailedMaterialFeature> RAW_LIST_CACHE = new ConcurrentHashMap<>();

    /**
     * Parses a raw comma separated list (as written in a command argument) once and caches it:
     * the callers run on every command execution. The returned feature is read-only afterwards.
     * The cache is cleared by SCore#onReload, after the custom lists it may reference.
     */
    public static ListDetailedMaterialFeature getCachedForBlocks(@NotNull String rawList) {
        return RAW_LIST_CACHE.computeIfAbsent(rawList, raw -> {
            ListDetailedMaterialFeature feature = new ListDetailedMaterialFeature(true);
            List<String> list = new ArrayList<>();
            if (raw.contains(",")) list = Arrays.asList(raw.split(","));
            else list.add(raw);
            feature.load(SCore.plugin, list, true);
            return feature;
        });
    }

    public static void clearRawListCache() {
        RAW_LIST_CACHE.clear();
    }

    public ListDetailedMaterialFeature(boolean forBlocks) {
        super();
        this.listOfCustomBlocksPluginSupported = new ArrayList<>();
        if (SCore.hasItemsAdder) listOfCustomBlocksPluginSupported.add("ITEMSADDER");
        if (SCore.hasExecutableItems) listOfCustomBlocksPluginSupported.add("EXECUTABLEITEMS");
        if (Dependency.EXECUTABLE_BLOCKS.isInstalled()) listOfCustomBlocksPluginSupported.add("EXECUTABLEBLOCKS");
        //if(SCore.hasOraxen) listOfCustomBlocksPluginSupported.add("ORAXEN");
        this.forBlocks = forBlocks;
        reset();
    }

    @Override
    public List<String> loadValues(List<String> entries, List<String> errors) {
        List<String> values = new ArrayList<>();
        //DONT ADD BECAUSE IT RESET IT AT THE SECOND TIME WHEN THE LOADING OF BLACKLIST specificationOfAtLeastOneState = false;

        // Expand custom lists
        List<String> expandedEntries = new ArrayList<>();
        for (String s : entries) {
            s = StringConverter.decoloredString(s);
            String checkValue = s.replace("!", "");

            // Check if this is a custom list reference
            if (checkValue.toUpperCase().startsWith(customListPrefix)) {
                String listName = checkValue.substring(customListPrefix.length());
                Optional<List<String>> customList = com.ssomar.score.features.custom.customlists.CustomListsManager.getInstance().getBlockList(listName);
                if (customList.isPresent()) {
                    // Add all blocks from the custom list
                    boolean isNegated = s.startsWith("!");
                    for (String block : customList.get()) {
                        expandedEntries.add(isNegated ? "!" + block : block);
                    }
                } else {
                    String parentInfo = "";
                    if(getParent() != null) parentInfo = getParent().getParentInfo();
                    errors.add("&cERROR, Custom block list '" + listName + "' not found in SCore config.yml &7&o" + parentInfo);
                }
                continue;
            }

            expandedEntries.add(s);
        }

        for (String s : expandedEntries) {
            s = StringConverter.decoloredString(s);
            String materialStr = s;

            boolean isCustomBlock = false;
            for (String customPlugin : listOfCustomBlocksPluginSupported) {
                if (materialStr.startsWith(customPlugin)) {
                    //SsomarDev.testMsg(">> loadValues: " + s, DEBUG);
                    values.add(s);
                    isCustomBlock = true;
                    break;
                }
            }
            if (isCustomBlock) continue;

            // Uppercase only for material & tags
            materialStr = materialStr.toUpperCase();

            boolean isMaterialTag = false;
            if (s.startsWith(symbolStartMaterialTag)) {
                isMaterialTag = true;
                values.add(s);
            }
            if(isMaterialTag) continue;

            if (s.contains(symbolStart)) {
                specificationOfAtLeastOneState = true;
                materialStr = s.split("\\" + symbolStart)[0];

                String datas = s.split("\\" + symbolStart)[1].replace(symbolEnd, "");
                for (String data : datas.split(symbolSeparator)) {
                    String[] dataSplit = data.split(symbolEquals);
                    if (dataSplit.length != 2) {
                        String parentInfo = "";
                        if(getParent() != null) parentInfo = getParent().getParentInfo();

                        errors.add("&cERROR, Couldn't load the The tags value of " + this.getName() + " from config, value: " + s + " &7&o" + parentInfo + " &6>> Example : FURNACE{lit:true}  BEETROOTS{age:3}, List of tags: https://minecraft.fandom.com/wiki/Block_states");
                        continue;
                    }
                }
            }

            if (!MaterialWithGroups.getMaterialWithGroups(materialStr.toUpperCase()).isPresent()) {
                String parentInfo = "";
                if(getParent() != null) parentInfo = getParent().getParentInfo();
                errors.add("&cERROR, Couldn't load the Material + GROUPS value of " + this.getName() + " from config, value: " + s + " &7&o" + parentInfo + " &6>> Check the wiki if you want the list of materials and groups");
                continue;
            }
            values.add(s);
        }

        return values;
    }

    @Override
    public String transfromToString(String value) {
        return value;
    }

    /**
     * Return map with material and tags
     **/
    public Map<String, List<Map<String, String>>> extractConditions(List<String> values) {
        Map<String, List<Map<String, String>>> conditions = new HashMap<>();
        for (String s : values) {
            //SsomarDev.testMsg(">> extractConditions: " + s, DEBUG);
            String materialStr = s;

            boolean isCustomBlock = false;
            for (String customPlugin : listOfCustomBlocksPluginSupported) {
                if (materialStr.startsWith(customPlugin)) {
                    isCustomBlock = true;
                    break;
                }
            }
            if (isCustomBlock) continue;


            Map<String, String> tags = new HashMap<>();

            if (s.contains(symbolStart)) {
                materialStr = s.split("\\" + symbolStart)[0];
                String datas = s.split("\\" + symbolStart)[1].replace(symbolEnd, "");
                for (String data : datas.split(symbolSeparator)) {
                    String[] dataSplit = data.split(symbolEquals);
                    tags.put(dataSplit[0].toUpperCase(), dataSplit[1]);
                }
            }

            if (conditions.containsKey(materialStr.toUpperCase())) {
                conditions.get(materialStr.toUpperCase()).add(tags);
            } else {
                conditions.put(materialStr.toUpperCase(), new ArrayList<>(Collections.singletonList(tags)));
            }
            SsomarDev.testMsg(" >> conditions size: " + conditions.get(materialStr.toUpperCase()).size(), DEBUG);
        }
        return conditions;
    }

    public boolean isValidMaterial(@NotNull Material material, Optional<String> statesStrOpt) {
        boolean forValuesBool = isValidMaterial(true, getValues(), material, statesStrOpt);
        SsomarDev.testMsg(">> verif forValuesBool: " + forValuesBool, DEBUG);
        boolean forBlacklistValuesBool = isValidMaterial(false, getBlacklistedValues(), material, statesStrOpt);
        SsomarDev.testMsg(">> verif forBlacklistValuesBool: " + forBlacklistValuesBool, DEBUG);
        return forValuesBool && !forBlacklistValuesBool;
    }

    public boolean isValidMaterial(boolean ifEmpty, List<String> references, @NotNull Material material, Optional<String> statesStrOpt) {

        for (String s : references) SsomarDev.testMsg(">> verif references: " + s, DEBUG);
        Map<String, List<Map<String, String>>> conditions = extractConditions(references);
        if (conditions.isEmpty()) {
            SsomarDev.testMsg(">> verif conditions.isEmpty(): " + conditions.isEmpty(), DEBUG);
            return ifEmpty;
        }

        String symbolStart = "{";
        String symbolStartSplit = "\\{";
        String symbolEnd = "}";
        String symbolEquals = ":";
        String symbolSeparator = ",";

        if (forBlocks) {
            symbolStart = "[";
            symbolStartSplit = "\\[";
            symbolEnd = "]";
            symbolEquals = "=";
            symbolSeparator = ",";
        }

        // TODO PROBLEM FOR ITEM TAGS IT WORKS ONLY WITH CUSTOMODELDATA
        Map<String, String> states = new HashMap<>();
        try {
            SsomarDev.testMsg(">> verif statesStrOpt: " + statesStrOpt.isPresent(), DEBUG);
            if (statesStrOpt.isPresent()) {
                SsomarDev.testMsg(">> verif statesStr: " + statesStrOpt.get(), DEBUG);
                String statesStr = statesStrOpt.get().toUpperCase();
                if (statesStr.contains(symbolStart)) {
                    /* States are store like that TORCH[STATE1=VALUE1,STATE2=VALUE2] */

                    String[] spliter1 = statesStr.split(symbolEnd);
                    String[] spliter2 = spliter1[0].split(symbolStartSplit);

                    // Otherwise that means states are empty
                    if (spliter2.length >= 2) {
                        SsomarDev.testMsg(">> spliter2: " + spliter2[1], DEBUG);

                        String[] spliterStates = spliter2[1].split(symbolSeparator);

                        for (String state : spliterStates) {
                            String[] spliterState = state.split(symbolEquals);
                            if(spliterState.length < 2) {
                                SsomarDev.testMsg(">> spliterState.length: " + spliterState.length, DEBUG);
                                continue;
                            }
                            SsomarDev.testMsg(">> spliterState: " + spliterState[0] + "=" + spliterState[1], DEBUG);
                            states.put(spliterState[0].toUpperCase(), spliterState[1].toUpperCase());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String mat : conditions.keySet()) {

            // Verif custom material tag
            if(!SCore.is1v11Less() && mat.startsWith(symbolStartMaterialTag)) {
                mat = mat.substring(1).toLowerCase();
                SsomarDev.testMsg(">> verif tag: " + mat, DEBUG);
                Tag<Material> tag = MinecraftTags.getInstance().getTag(mat);
                if(tag != null) {
                    if(MinecraftTags.getInstance().checkIfTagged(material, tag)) return true;
                    else {
                        SsomarDev.testMsg(">> verif tag not found: " + mat+" for material "+material, DEBUG);
                    }
                }
                else {
                    SsomarDev.testMsg(">> verif tag not found: " + mat, DEBUG);
                }

            }
            else if (MaterialWithGroups.verif(material, mat)) {
                SsomarDev.testMsg(">> verif mat: " + mat, DEBUG);
                List<Map<String, String>> tagsList = conditions.get(mat);
                SsomarDev.testMsg(">> verif tagsList empty ?: " + tagsList.isEmpty(), DEBUG);
                if (tagsList.isEmpty()) return true;
                for (Map<String, String> tags : tagsList) {
                    boolean invalid = false;
                    if (tags.isEmpty()) return true;
                    else {
                        for (String key : tags.keySet()) {
                            SsomarDev.testMsg(">> verif key: " + key, DEBUG);
                            String keyUpper = key.toUpperCase();
                            if (states.containsKey(keyUpper)) {
                                if (!states.get(key).equalsIgnoreCase(tags.get(key))) {
                                    SsomarDev.testMsg(">> verif states.get(key): " + states.get(key)+ " != tags.get(key): " + tags.get(key), DEBUG);
                                    invalid = true;
                                    break;
                                }
                            } else invalid = true;

                            if (invalid) break;
                        }

                        if (invalid) continue;

                        return true;
                    }
                }
            }
        }
        return false;
    }

    public List<Tag<Material>> asTagList(){
        List<Tag<Material>> tags = new ArrayList<>();
        for (String s : getValues()) {
            String materialStr = s;
            SsomarDev.testMsg(">> materialStr: " + materialStr, DEBUG);
            if (!SCore.is1v11Less() && materialStr.startsWith(symbolStartMaterialTag)) {
                materialStr = materialStr.substring(1).toLowerCase();
                SsomarDev.testMsg(">> verif tag: " + materialStr, DEBUG);
                Tag<Material> tag = MinecraftTags.getInstance().getTag(materialStr);
                if(tag != null) {
                    tags.add(tag);
                } else {
                    SsomarDev.testMsg(">> verif tag not found: " + materialStr, DEBUG);
                }
            }
        }
        return tags;
    }

    public List<Material> asMaterialList() {
        List<Material> materials = new ArrayList<>();
        for (String s : getValues()) {
            String materialStr = s;
            try{
                Material mat = Material.valueOf(materialStr.toUpperCase());
                materials.add(mat);
            } catch (Exception e) {
                SsomarDev.testMsg(">> verif material not found: " + materialStr, DEBUG);
            }
        }
        return materials;
    }

    /* Plugin name - list of ID */
    public Map<String, List<String>> extractCustomBlocksConditions(List<String> values) {
        Map<String, List<String>> conditions = new HashMap<>();
        for (String s : values) {
            String materialStr = s;
            SsomarDev.testMsg(">> materialStr: " + materialStr, DEBUG);
            for (String customPlugin : listOfCustomBlocksPluginSupported) {
                if (materialStr.startsWith(customPlugin)) {
                    if (conditions.containsKey(customPlugin))
                        conditions.get(customPlugin).add(materialStr.split(":")[1]);
                    else
                        conditions.put(customPlugin, new ArrayList<>(Collections.singletonList(materialStr.split(":")[1])));
                    break;
                }
            }
        }
        return conditions;
    }

    public boolean isValidCustomBlockOnly(@NotNull Block block) {
        boolean forValuesBool = isValidCustomBlock(true, getValues(), block);
        SsomarDev.testMsg(">> CUSTOMBLOCK verif forValuesBool: " + forValuesBool, DEBUG);

        boolean forBlacklistValuesBool = isValidCustomBlock(false, getBlacklistedValues(), block);
        SsomarDev.testMsg(">> CUSTOMBLOCK verif forBlacklistValuesBool: " + forBlacklistValuesBool, DEBUG);

        return forValuesBool && !forBlacklistValuesBool;
    }

    public boolean isValidCustomBlock(boolean ifEmpty, List<String> references, @NotNull Block block) {
        Map<String, List<String>> conditions = extractCustomBlocksConditions(references);
        if (conditions.isEmpty()) return ifEmpty;
        for (String customPlugin : listOfCustomBlocksPluginSupported) {
            SsomarDev.testMsg(">> verif customPlugin: " + customPlugin, DEBUG);
            if (conditions.containsKey(customPlugin)) {
                SsomarDev.testMsg(">> verif conditions: YES ", DEBUG);
                if (customPlugin.equals("ITEMSADDER") && SCore.hasItemsAdder) {
                    Optional<String> customOpt = ItemsAdderAPI.getCustomBlockID(block);
                    SsomarDev.testMsg(">> IA customOpt: " + customOpt.isPresent(), DEBUG);
                    if (customOpt.isPresent()) {
                        SsomarDev.testMsg(">> IA customOpt: " + customOpt.get(), DEBUG);
                        for (String id : conditions.get(customPlugin)) {
                            SsomarDev.testMsg(">> IA id: " + id, DEBUG);
                            if (customOpt.get().equalsIgnoreCase(id)) return true;
                        }
                    }
                } else if (customPlugin.equals("EXECUTABLEBLOCKS") && SCore.hasExecutableBlocks) {
                    Optional<ExecutableBlockPlacedInterface> customOpt = ExecutableBlocksAPI.getExecutableBlocksPlacedManager().getExecutableBlockPlaced(block);
                    SsomarDev.testMsg(">> EB customOpt: " + customOpt.isPresent(), DEBUG);
                    if (customOpt.isPresent()) {
                        SsomarDev.testMsg(">> EB customOpt: " + customOpt.get().getEB_ID(), DEBUG);
                        for (String id : conditions.get(customPlugin)) {
                            SsomarDev.testMsg(">> EB id: " + id, DEBUG);
                            if (customOpt.get().getEB_ID().equalsIgnoreCase(id)) return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean verifBlock(@NotNull Block block) {
        return verifBlock(block, null, Optional.empty());
    }

    public boolean verifBlock(@NotNull Block block, @Nullable Material material, @NotNull Optional<String> statesStrOpt) {
        if (material == null) material = block.getType();
        // To only run if there is a state to check
        SsomarDev.testMsg(">> verif specificationOfAtLeastOneState: " + specificationOfAtLeastOneState, DEBUG);
        if (specificationOfAtLeastOneState && !statesStrOpt.isPresent()) {
            if (!SCore.is1v12Less()) statesStrOpt = Optional.of(block.getBlockData().getAsString(true));
            else statesStrOpt = Optional.empty();
        }

        boolean forValuesBoolMat = isValidMaterial(getValue().isEmpty(), getValues(), material, statesStrOpt);
        SsomarDev.testMsg(">> verif forValuesBool: " + forValuesBoolMat, DEBUG);

        boolean forBlacklistValuesBoolMat = isValidMaterial(false, getBlacklistedValues(), material, statesStrOpt);
        SsomarDev.testMsg(">> verif forBlacklistValuesBool: " + forBlacklistValuesBoolMat, DEBUG);


        boolean forValuesBool = isValidCustomBlock(getValue().isEmpty(), getValues(), block);
        SsomarDev.testMsg(">> CUSTOMBLOCK verif forValuesBool: " + forValuesBool, DEBUG);

        boolean forBlacklistValuesBool = isValidCustomBlock(false, getBlacklistedValues(), block);
        SsomarDev.testMsg(">> CUSTOMBLOCK verif forBlacklistValuesBool: " + forBlacklistValuesBool, DEBUG);

        return (forValuesBool || forValuesBoolMat) && (!forBlacklistValuesBool && !forBlacklistValuesBoolMat);
    }

    public boolean isValidCustomItemOnly(@NotNull ItemStack item) {
        boolean forValuesBool = isValidCustomItem(true, getValues(), item);
        boolean forBlacklistValuesBool = isValidCustomItem(false, getBlacklistedValues(), item);
        return forValuesBool && !forBlacklistValuesBool;
    }

    public boolean isValidCustomItem(boolean ifEmpty, List<String> references, @NotNull ItemStack item) {
        Map<String, List<String>> conditions = extractCustomBlocksConditions(references);
        if (conditions.isEmpty()) return ifEmpty;
        for (String customPlugin : listOfCustomBlocksPluginSupported) {
            SsomarDev.testMsg(">> verif customPlugin: " + customPlugin, DEBUG);
            if (conditions.containsKey(customPlugin)) {
                SsomarDev.testMsg(">> verif conditions: YES ", DEBUG);
                if (customPlugin.equals("ITEMSADDER") && SCore.hasItemsAdder) {
                    Optional<String> customOpt = ItemsAdderAPI.getCustomItemID(item);
                    SsomarDev.testMsg(">> customOpt: " + customOpt.isPresent(), DEBUG);
                    if (customOpt.isPresent()) {
                        SsomarDev.testMsg(">> customOpt: " + customOpt.get(), DEBUG);
                        for (String id : conditions.get(customPlugin)) {
                            SsomarDev.testMsg(">> id: " + id, DEBUG);
                            if (customOpt.get().equalsIgnoreCase(id)) return true;
                        }
                    }
                }
                else if (customPlugin.equals("EXECUTABLEBLOCKS") && SCore.hasExecutableBlocks) {
                    ExecutableBlockObject customOpt = (ExecutableBlockObject) ExecutableBlocksAPI.getExecutableBlockObject(item);
                    if (!customOpt.isValid()) continue;
                    for (String id : conditions.get(customPlugin)) {
                        SsomarDev.testMsg(">> id: " + id, DEBUG);
                        if (customOpt.getConfig().getId().equalsIgnoreCase(id)) return true;
                    }

                } else if (customPlugin.equals("EXECUTABLEITEMS") && SCore.hasExecutableItems) {
                    ExecutableItemObject customOpt = (ExecutableItemObject) ExecutableItemsAPI.getExecutableItemObject(item);
                    if (!customOpt.isValid()) continue;
                    for (String id : conditions.get(customPlugin)) {
                        SsomarDev.testMsg(">> id EI: " + id, DEBUG);
                        if (customOpt.getConfig().getId().equalsIgnoreCase(id)) return true;
                    }



                }
            }
        }

        return false;
    }

    public boolean verifItem(@NotNull ItemStack item) {
        Material material = item.getType();
        Optional<String> statesStrOpt;
        if (SCore.hasNBTAPI && !item.getType().equals(Material.AIR)) {
            NBTItem nbti = new NBTItem(item);
            String str = nbti.toString();
            statesStrOpt = Optional.of(str);
        } else statesStrOpt = Optional.empty();

        boolean forValuesBoolMat = isValidMaterial(getValue().isEmpty(), getValues(), material, statesStrOpt);
        SsomarDev.testMsg(">> verif forValuesBool: " + forValuesBoolMat, DEBUG);

        boolean forBlacklistValuesBoolMat = isValidMaterial(false, getBlacklistedValues(), material, statesStrOpt);
        SsomarDev.testMsg(">> verif forBlacklistValuesBool: " + forBlacklistValuesBoolMat, DEBUG);


        boolean forValuesBool = isValidCustomItem(getValue().isEmpty(), getValues(), item);
        SsomarDev.testMsg(">> CUSTOMBLOCK verif forValuesBool: " + forValuesBool, DEBUG);

        boolean forBlacklistValuesBool = isValidCustomItem(getValue().isEmpty(), getBlacklistedValues(), item);
        SsomarDev.testMsg(">> CUSTOMBLOCK verif forBlacklistValuesBool: " + forBlacklistValuesBool, DEBUG);

        return (forValuesBool || forValuesBoolMat) && (!forBlacklistValuesBool && !forBlacklistValuesBoolMat);
    }

    @Override
    public ListDetailedMaterialFeature clone(FeatureParentInterface newParent) {
        ListDetailedMaterialFeature clone = new ListDetailedMaterialFeature(newParent, getDefaultValue(),getFeatureSettings(), isForBlocks());
        clone.setValues(getValues());
        clone.setBlacklistedValues(getBlacklistedValues());
        return clone;
    }

    @Override
    public Optional<String> verifyMessage(String message) {
        String s = StringConverter.decoloredString(message).replace("!", "");
        String str = s;

        // Allow custom list references
        if (str.toUpperCase().startsWith(customListPrefix)) {
            String listName = str.substring(customListPrefix.length());
            if (com.ssomar.score.features.custom.customlists.CustomListsManager.getInstance().hasBlockList(listName)) {
                return Optional.empty();
            } else {
                return Optional.of("&4&l[ERROR] &cCustom block list '" + listName + "' not found in SCore config.yml");
            }
        }

        boolean isCustomBlock = false;
        for (String customPlugin : listOfCustomBlocksPluginSupported) {
            if (str.startsWith(customPlugin)) {
                isCustomBlock = true;
                break;
            }
        }
        if (isCustomBlock) return Optional.empty();

        boolean isMaterialTag = false;
        if (s.startsWith(symbolStartMaterialTag)) {
            isMaterialTag = true;
        }
        if(isMaterialTag) return Optional.empty();

        if (s.contains(symbolStart)) {
            str = s.split("\\" + symbolStart)[0];
            String datas = s.split("\\" + symbolStart)[1].replace(symbolEnd, "");
            for (String data : datas.split(symbolSeparator)) {
                String[] dataSplit = data.split(symbolEquals);
                if (dataSplit.length != 2) {
                    return Optional.of("&4&l[ERROR] &cThe message you entered contains an invalid format  &6>> Example : FURNACE{lit:true}  BEETROOTS{age:3}, List of tags: https://minecraft.fandom.com/wiki/Block_states and &7(Check the wiki if you want more examples)");
                }
            }
        }
        if (!MaterialWithGroups.getMaterialWithGroups(str.toUpperCase()).isPresent()) {
            return Optional.of("&4&l[ERROR] &cThe message you entered contains an invalid Material + GROUPS ! (Check the wiki if you want the list)");
        }

        return Optional.empty();
    }

    @Override
    public List<TextComponent> getMoreInfo() {
        return new ArrayList<>();
    }

    @Override
    public List<Suggestion> getSuggestions() {
        List<Suggestion> suggestions = new ArrayList<>();
        for (MaterialWithGroups materialWithGroups : MaterialWithGroups.values()) {
            suggestions.add(new Suggestion(materialWithGroups.toString(), "&6[&e" + materialWithGroups + "&6]", "&7Add &e" + materialWithGroups));
        }
        return suggestions;
    }

    @Override
    public String getTips() {
        if (forBlocks) return "&8Example &7&oFURNACE &8- &7&oBEETROOTS{age:3} &8- &7&oITEMSADDER:turquoise_block &8- &e(for custom lists) &7&oSCORE:LIST_NAME";
        else return "&8Example &7&oDIAMOND_SWORD &8- &7&oTORCH{CustomModelData:3} &8- &7&oITEMSADDER:ruby_sword &8- &7&oEXECUTABLEITEMS:my_ei_sword &8- &e(for custom lists) &7&oSCORE:LIST_NAME";
    }

    @Override
    public void sendBeforeTextEditor(Player playerEditor, NewGUIManager manager) {
        List<String> beforeMenu = new ArrayList<>();
        beforeMenu.add("&7➤ Your detailed " + getEditorName() + ":");

        HashMap<String, String> suggestions = new HashMap<>();

        EditorCreator editor = new EditorCreator(beforeMenu, (List<String>) manager.currentWriting.get(playerEditor), getEditorName() + ":", true, true, true, true,
                true, true, false, "", suggestions);
        editor.generateTheMenuAndSendIt(playerEditor);
    }
}
