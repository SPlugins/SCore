package com.ssomar.score.usedapi;

import com.ssomar.score.SCore;
import com.ssomar.score.utils.logging.Utils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public enum Dependency {

    EXECUTABLE_ITEMS("ExecutableItems"),

    EXECUTABLE_BLOCKS("ExecutableBlocks"),

    EXECUTABLE_CRAFTING("ExecutableCrafting"),

    EXECUTABLE_EVENTS("ExecutableEvents"),

    CUSTOM_PIGLINS_TRADES("CustomPiglinsTrades"),

    SPARKOUR("SParkour"),

    PLACEHOLDER_API("PlaceholderAPI"),

    WORLD_GUARD("WorldGuard"),

    WORLD_GUARD_EXTRA_FLAGS("WorldGuardExtraFlags"),

    VAULT("Vault"),

    IRIDIUM_SKYBLOCK("IridiumSkyblock"),

    SUPERIOR_SKYBLOCK2("SuperiorSkyblock2"),

    BENTO_BOX("BentoBox"),

    FABLED_SKYBLOCK("FabledSkyBlock"),

    MULTIVERSE_CORE("Multiverse-Core"),

    LANDS("Lands"),

    TOWNY("Towny"),

    GRIEF_PREVENTION("GriefPrevention"),

    GRIEF_DEFENDER("GriefDefender"),

    CORE_PROTECT("CoreProtect"),

    FACTIONS_UUID("Factions"),

    PROTOCOL_LIB("ProtocolLib"),
    NBTAPI("NBTAPI"),

    RESIDENCE("Residence"),
    PLOT_SQUARED("PlotSquared"),


    HEAD_DATABASE("HeadDatabase"),

    HEAD_DB("HeadDB"),

    JOBS("Jobs"),

    MCMMO("mcMMO"),

    MYTHIC_MOBS("MythicMobs"),

    DECENT_HOLOGRAMS("DecentHolograms"),

    HOLOGRAPHIC_DISPLAYS("HolographicDisplays"),

    CMI("CMI"),

    AURA_SKILLS("AuraSkills"),

    AURELIUM_SKILLS("AureliumSkills"),

    ITEMS_ADDER("ItemsAdder"),

    ORAXEN("Oraxen"),

    NEXO("Nexo"),

    SHOP_GUI_PLUS("ShopGUIPlus"),

    ROSE_LOOT("RoseLoot"),

    ROSE_STACKER("RoseStacker"),

    MMO_CORE("MMOCore"),

    PROTECTION_STONES("ProtectionStones"),

    EXCELLENT_CLAIMS("ExcellentClaims"),

    TAB("TAB"),

    TERRA("Terra"),

    JETS_MINIONS("JetsMinions"),

    ECO_SKILLS("EcoSkills"),

    WILD_STACKER("WildStacker"),

    MY_FURNITURE("MyFurniture"),

    CUSTOM_CRAFTING("CustomCrafting"),

    PACKET_EVENTS("packetevents"),

    SCORE("SCore"),

    MYWORLDS("MyWorlds"),

    WORLD_EDIT("WorldEdit"),

    CUSTOM_FISHING("CustomFishing"),

    FAST_ASYNC_WORLD_EDIT("FastAsyncWorldEdit");

    private final String name;

    private boolean installed = false;

    private boolean enabled = false;

    Dependency(String name) {
        this.name = name;
    }

    public boolean hookSoftDependency() {
        Plugin softDepend;
        if ((softDepend = Bukkit.getPluginManager().getPlugin(name)) != null) {
            String when = " &8&oLoad Before";
            if (!softDepend.isEnabled()) {
                when = "&8&oLoad After";
            }
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7" + name + " hooked !  &6(" + softDepend.getDescription().getVersion() + "&6) " + when);
            return true;
        }
        return false;
    }

    public boolean isInstalled() {
        if(installed) return true;
        installed = Bukkit.getPluginManager().getPlugin(name) != null;
        return installed;
    }

    public boolean isEnabled() {
        if(enabled) return true;
        Plugin softDepend;
        if ((softDepend = Bukkit.getPluginManager().getPlugin(name)) != null) {
            enabled = softDepend.isEnabled();
            return enabled;
        }
        return false;
    }
}
