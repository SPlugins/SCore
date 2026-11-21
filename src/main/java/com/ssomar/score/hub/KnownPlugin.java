package com.ssomar.score.hub;

import com.ssomar.score.api.custompiglinstrades.CustomPiglinsTradesAPI;
import com.ssomar.score.api.executableblocks.ExecutableBlocksAPI;
import com.ssomar.score.api.executablecrafting.ExecutableCraftingAPI;
import com.ssomar.score.api.executableevents.ExecutableEventsAPI;
import com.ssomar.score.api.executableitems.ExecutableItemsAPI;
import com.ssomar.score.api.myfurniture.MyFurnitureAPI;

/**
 * The SPlugins plugins the Hub knows. {@code key} matches the "key" field of the public catalog.
 */
public enum KnownPlugin {

    SCORE("SCore", "SCore", null, "DIAMOND", ""),
    EXECUTABLE_ITEMS("ExecutableItems", "ExecutableItems", "ei", "DIAMOND_SWORD", "executableitems"),
    EXECUTABLE_BLOCKS("ExecutableBlocks", "ExecutableBlocks", "eb", "GRASS_BLOCK", "executableblocks"),
    EXECUTABLE_EVENTS("ExecutableEvents", "ExecutableEvents", "ee", "CLOCK", "executableevents"),
    EXECUTABLE_CRAFTING("ExecutableCrafting", "ExecutableCrafting", "ec", "CRAFTING_TABLE", "executablecrafting"),
    MY_FURNITURE("MyFurniture", "MyFurniture", "mf", "OAK_STAIRS", "myfurniture"),
    CUSTOM_PIGLINS_TRADES("CustomPiglinsTrades", "CustomPiglinsTrades", "cpt", "GOLD_INGOT", "custompiglinstrades"),
    EXECUTABLE_LEVERS("ExecutableLevers", "ExecutableLevers", null, "LEVER", ""),
    SCREEN_SHOP("ScreenShop", "ScreenShop", null, "ITEM_FRAME", "screenshop");

    private final String key;
    private final String pluginName;
    private final String command;
    private final String icon;
    private final String docsSlug;

    KnownPlugin(String key, String pluginName, String command, String icon, String docsSlug) {
        this.key = key;
        this.pluginName = pluginName;
        this.command = command;
        this.icon = icon;
        this.docsSlug = docsSlug;
    }

    public String getKey() { return key; }

    /** the plugin logo on media.ssomar.com (resource icons of splugins.net), null if none */
    public String getLogoUrl() {
        switch (this) {
            case SCORE: return "https://media.ssomar.com/m/docs-img-logo-score.png";
            case EXECUTABLE_ITEMS: return "https://media.ssomar.com/m/resource-icon-900";
            case EXECUTABLE_BLOCKS: return "https://media.ssomar.com/m/resource-icon-905";
            case EXECUTABLE_EVENTS: return "https://media.ssomar.com/m/resource-icon-906";
            case EXECUTABLE_CRAFTING: return "https://media.ssomar.com/m/resource-icon-907";
            case MY_FURNITURE: return "https://media.ssomar.com/m/resource-icon-908";
            case EXECUTABLE_LEVERS: return "https://media.ssomar.com/m/resource-icon-909";
            case CUSTOM_PIGLINS_TRADES: return "https://media.ssomar.com/m/resource-icon-910";
            default: return null;
        }
    }
    public String getPluginName() { return pluginName; }
    /** main command without the slash, or null */
    public String getCommand() { return command; }
    public String getIcon() { return icon; }
    public String getDocsUrl() { return "https://splugins.net/docs" + (docsSlug.isEmpty() ? "" : "/" + docsSlug); }

    public static KnownPlugin byKey(String key) {
        for (KnownPlugin k : values()) if (k.key.equalsIgnoreCase(key)) return k;
        return null;
    }

    /** number of loaded objects (items, blocks...), or -1 if unknown / not installed */
    public int countObjects() {
        try {
            switch (this) {
                case EXECUTABLE_ITEMS:
                    return ExecutableItemsAPI.isEnabled() ? ExecutableItemsAPI.getExecutableItemsManager().getExecutableItemIdsList().size() : -1;
                case EXECUTABLE_BLOCKS:
                    return ExecutableBlocksAPI.isEnabled() ? ExecutableBlocksAPI.getExecutableBlocksManager().getExecutableBlockIdsList().size() : -1;
                case EXECUTABLE_EVENTS:
                    return ExecutableEventsAPI.isEnabled() ? ExecutableEventsAPI.getExecutableEventsManager().getExecutableEventIdsList().size() : -1;
                case EXECUTABLE_CRAFTING:
                    return ExecutableCraftingAPI.isEnabled() ? ExecutableCraftingAPI.getExecutableCraftingManager().getRecipeIdsList().size() : -1;
                case MY_FURNITURE:
                    return MyFurnitureAPI.isEnabled() ? MyFurnitureAPI.getFurnitureManager().getFurnitureIdsList().size() : -1;
                case CUSTOM_PIGLINS_TRADES:
                    return CustomPiglinsTradesAPI.isEnabled() ? CustomPiglinsTradesAPI.getTradesManager().getTradeIdsList().size() : -1;
                default:
                    return -1;
            }
        } catch (Throwable t) {
            return -1;
        }
    }
}
