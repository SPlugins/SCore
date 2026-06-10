package com.ssomar.score;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.plotsquared.core.PlotAPI;
import com.ssomar.score.actionbar.ActionbarHandler;
import com.ssomar.score.commands.runnable.CommandsHandler;
import com.ssomar.score.commands.runnable.FilterManager;
import com.ssomar.score.commands.runnable.player.commands.Bossbar;
import com.ssomar.score.commands.score.CommandsClass;
import com.ssomar.score.config.Config;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.configs.messages.Message;
import com.ssomar.score.configs.messages.MessageInterface;
import com.ssomar.score.configs.messages.MessageMain;
import com.ssomar.score.data.Database;
import com.ssomar.score.events.EventsHandler;
import com.ssomar.score.events.loop.LoopManager;
import com.ssomar.score.features.FeatureSettingsSCore;
import com.ssomar.score.features.custom.cooldowns.CooldownsHandler;
import com.ssomar.score.features.custom.usage.useperday.manager.UsagePerDayManager;
import com.ssomar.score.hardness.HardnessesHandler;
import com.ssomar.score.hardness.hardness.Hardness;
import com.ssomar.score.hardness.hardness.loader.HardnessLoader;
import com.ssomar.score.languages.messages.TM;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.pack.custom.PackManager;
import com.ssomar.score.pack.spigot.InjectSpigot;
import com.ssomar.score.projectiles.SProjectile;
import com.ssomar.score.projectiles.loader.SProjectileLoader;
import com.ssomar.score.sobject.SObject;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.sparticles.ParticleToggleManager;
import com.ssomar.score.usedapi.Dependency;
import com.ssomar.score.usedapi.PlaceholderAPISCoreExpansion;
import com.ssomar.score.usedapi.ProtocolLibAPI;
import com.ssomar.score.utils.display.Display;
import com.ssomar.score.utils.display.PacketManager;
import com.ssomar.score.utils.logging.Utils;
import com.ssomar.score.utils.scheduler.*;
import com.ssomar.score.variables.Variable;
import com.ssomar.score.variables.loader.VariablesLoader;
import com.ssomar.score.variables.manager.VariablesManager;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;

import static com.ssomar.score.usedapi.Dependency.PROTOCOL_LIB;

public final class SCore extends JavaPlugin implements SPlugin {

    public static final String NAME = "SCore";
    public static final String NAME_COLOR = "&eSCore";
    public static final String NAME_COLOR_WITH_BRACKETS = "&e[SCore]";
    public static SCore plugin;

    public static Plugin pluginHolder;
    public static ClassLoader classLoader;
    public static File dataFolder;

    private static InjectSpigot injectSpigot;

    /**
     * If the value of {@link com.ssomar.score.SCore#isFolia()} is <code>true</code>, SCore will use {@link RegionisedSchedulerHook} to perform scheduled tasks.
     * Otherwise, it will use {@link BukkitSchedulerHook}.<br/>
     * <br/>
     * See {@link SCore#initLibPartOfSCore(Plugin, ClassLoader)} for the logic and details of the decision.
     */
    public static SchedulerHook schedulerHook;
    public static boolean hasPlaceholderAPI = false;
    public static boolean hasExecutableItems = false;
    public static boolean hasExecutableBlocks = false;
    public static boolean hasExecutableEvents = false;
    public static boolean hasCustomPiglinsTrades = false;
    public static boolean hasSParkour = false;
    public static boolean hasWorldGuard = false;

    public static boolean hasWorldEdit = false;
    public static boolean hasVault = false;
    public static boolean hasIridiumSkyblock = false;
    public static boolean hasSuperiorSkyblock2 = false;
    public static boolean hasBentoBox = false;
    public static boolean hasFabledSkyblock = false;
    public static boolean hasMultiverse = false;
    public static boolean hasLands = false;
    public static boolean hasTowny = false;
    public static boolean hasGriefPrevention = false;
    public static boolean hasGriefDefender = false;
    public static boolean hasCoreProtect = false;
    public static boolean hasProtocolLib = false;
    public static boolean hasPlotSquared = false;
    public static boolean hasResidence = false;
    public static boolean hasNBTAPI = false;
    public static ProtocolManager protocolManager;
    public static boolean hasHeadDatabase = false;
    public static boolean hasHeadDB = false;
    public static boolean hasMythicMobs = false;
    public static boolean hasHolographicDisplays = false;
    public static boolean hasDecentHolograms = false;
    public static boolean hasCMI = false;
    public static boolean hasAureliumSkills = false;
    public static boolean hasItemsAdder = false;
    public static boolean hasOraxen = false;
    public static boolean hasNexo = false;
    public static boolean hasShopGUIPlus = false;
    public static boolean hasRoseLoot = false;
    public static boolean hasCustomFishing = false;
    public static boolean hasFastAsyncWorldEdit = false;

    public static boolean hasRoseStacker = false;
    public static boolean hasMMOCore = false;
    public static boolean hasMcMMO = false;
    public static boolean hasProtectionStones = false;
    public static boolean hasExcellentClaims = false;

    public static boolean hasTerra = false;

    public static boolean hasJetsMinions = false;

    public static boolean hasEcoSkills = false;

    public static boolean hasFactionsUUID = false;
    public static boolean hasCustomCrafting = false;
    public static boolean hasWildStacker = false;

    public static boolean hasMyFurniture = false;

    public static boolean hasTAB = false;

    private static boolean is1v8 = false;
    private static boolean is1v9 = false;
    private static boolean is1v10 = false;
    private static boolean is1v11 = false;
    private static boolean is1v12 = false;
    private static boolean is1v13 = false;
    private static boolean is1v14 = false;
    private static boolean is1v15 = false;
    private static boolean is1v16 = false;
    private static boolean is1v16v1 = false;
    private static boolean is1v17 = false;
    private static boolean is1v18 = false;
    private static boolean is1v19 = false;
    private static boolean is1v19v1 = false;
    private static boolean is1v19v4 = false;
    private static boolean is1v20 = false;

    private static boolean is1v20v1 = false;

    private static boolean is1v20v4 = false;

    private static boolean is1v20v5 = false;

    private static boolean is1v20v6 = false;

    private static boolean is1v21 = false;

    private static boolean is1v21v2 = false;
    private static boolean is1v21v3 = false;
    private static boolean is1v21v4 = false;
    private static boolean is1v21v5 = false;
    private static boolean is1v21v6 = false;
    private static boolean is1v21v7 = false;
    private static boolean is1v21v8 = false;
    private static boolean is1v21v9 = false;
    private static boolean is1v21v10 = false;
    private static boolean is1v21v11 = false;

    private static boolean is26v1 = false;



    private static boolean isSpigot = false;
    private static boolean isPaper = false;
    private static boolean isPaperOrForkFor1v20lus = false;
    private static boolean isFolia = false;

    private static boolean isLuminol = false;
    private static boolean isMohist = false;

    private static boolean isPurpur = false;
    private static boolean isPufferfish = false;

    private CommandsClass commandClass;

    /* The server is folia? */

    /**
     * Used for adjusting code logic for these following server types:
     * <ul>
     *     <li>Folia</li>
     *     <li>Luminol</li>
     *     <li>PaperSpigot (or its forks) 1.20+</li>
     * </ul>
     * @return boolean
     */
    public static boolean isFolia() {
        return isFolia || isLuminol || isPaperOrForkFor1v20lus /* Paper include threaded region of Folia in 1.20 +*/ || hasClass("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
    }

    /* The server is spigot? */
    public static boolean isSpigot() {
        return isSpigot;
    }

    /* The server is paper? */
    public static boolean isPaper() {
        return isPaper;
    }

    /* The server is mohist? */
    public static boolean isMohist() {
        return isMohist;
    }

    /* The server is purpur? */
    public static boolean isPurpur() {
        return isPurpur;
    }

    public static boolean isPufferfish() {
        return isPufferfish;
    }

    /* The server is in 1.8 ? */
    public static boolean is1v8() {
        return is1v8;
    }

    /* The server is in 1.9 ? */
    public static boolean is1v9() {
        return is1v9;
    }

    /* The server is in 1.10 ? */
    public static boolean is1v10() {
        return is1v10;
    }

    /* The server is in 1.11 ? */
    public static boolean is1v11() {
        return is1v11;
    }

    /* The server is in 1.12 ? */
    public static boolean is1v12() {
        return is1v12;
    }

    /* The server is in 1.13 ? */
    public static boolean is1v13() {
        return is1v13;
    }

    /* The server is in 1.14 ? */
    public static boolean is1v14() {
        return is1v14;
    }

    /* The server is in 1.15 ? */
    public static boolean is1v15() {
        return is1v15;
    }

    /* The server is in 1.16 ? */
    public static boolean is1v16() {
        return is1v16;
    }

    /* The server is in 1.16 ? */
    public static boolean is1v16v1() {
        return is1v16v1;
    }

    /* The server is in 1.17 ? */
    public static boolean is1v17() {
        return is1v17;
    }

    /* The server is in 1.18 ? */
    public static boolean is1v18() {
        return is1v18;
    }

    /* The server is in 1.19 ? */
    public static boolean is1v19() {
        return is1v19;
    }

    /* The server is in 1.19 ? */
    public static boolean is1v19v1() {
        return is1v19v1;
    }

    public static boolean is1v19v4() {
        return is1v19v4;
    }

    /* The server is in 1.20? */
    public static boolean is1v20() {
        return is1v20;
    }

    /* The server is in 1.20.1? */
    public static boolean is1v20v1() {
        return is1v20v1;
    }

    /* The server is in 1.20.4? */
    public static boolean is1v20v4() {
        return is1v20v4;
    }

    /* The server is in 1.20.5? */
    public static boolean is1v20v5() {
        return is1v20v5;
    }

    /* The server is in 1.20.6? */
    public static boolean is1v20v6() {
        return is1v20v6;
    }

    /* The server is in 1.21? */
    public static boolean is1v21() {
        return is1v21;
    }

    public static boolean is1v21v2() {
        return is1v21v2;
    }

    public static boolean is1v21v3() {
        return is1v21v3;
    }

    public static boolean is1v21v4() {
        return is1v21v4;
    }

    public static boolean is1v21v5() {
        return is1v21v5;
    }

    public static boolean is1v21v6() {
        return is1v21v6;
    }

    public static boolean is1v21v7() {
        return is1v21v7;
    }

    public static boolean is1v21v8() {
        return is1v21v8;
    }

    public static boolean is1v21v9() {
        return is1v21v9;
    }

    public static boolean is1v21v10() {
        return is1v21v10;
    }

    public static boolean is1v21v11() {
        return is1v21v11;
    }

    public static boolean is26v1() {
        return is26v1;
    }


    /* The server is in 1.10 or - ? */
    public static boolean is1v10Less() {
        return is1v8() || is1v9() || is1v10();
    }

    /* The server is in 1.11 or - ? */
    public static boolean is1v11Less() {
        return is1v8() || is1v9() || is1v10() || is1v11();
    }

    /* The server is in 1.12 or - ? */
    public static boolean is1v12Less() {
        return is1v8() || is1v9() || is1v10() || is1v11() || is1v12();
    }

    /* The server is in 1.12 or + ? */
    public static boolean is1v12Plus() { return is1v12() || is1v13() || is1v14() || is1v15() || is1v16Plus(); }

    /* The server is in 1.13 or - ? */
    public static boolean is1v13Less() {
        return is1v8() || is1v9() || is1v10() || is1v11() || is1v12() || is1v13();
    }

    /* The server is in 1.16 or + ? */
    public static boolean is1v16Plus() {
        return is1v16() || is1v17Plus();
    }

    /* The server is in 1.17 or + ? */
    public static boolean is1v17Plus() {
        return is1v17() || is1v18Plus();
    }

    /* The server is in 1.18 or + ? */
    public static boolean is1v18Plus() {
        return is1v18() || is1v19Plus();
    }

    /* The server is in 1.19 or + ? */
    public static boolean is1v19Plus() {
        return is1v19() || is1v19v1() || is1v19v4Plus();
    }

    public static boolean is1v19v4Plus() {
        return is1v19v4() || is1v20Plus();
    }

    /* The server is in 1.20 or + ? */
    public static boolean is1v20Plus() {
        return is1v20() || is1v20v1Plus();
    }

    /* The server is in 1.20.1 or + ? */
    public static boolean is1v20v1Plus() {
        return is1v20v1() || is1v20v4Plus();
    }

    /* The server is in 1.20.4 or + ? */
    public static boolean is1v20v4Plus() {
        return is1v20v4() || is1v20v5Plus();
    }

    /* The server is in 1.20.5 or + ? */
    public static boolean is1v20v5Plus() {
        return is1v20v5() || is1v20v6Plus();
    }
    /* The server is in 1.20.6 or + ? */
    public static boolean is1v20v6Plus() {
        return  is1v20v6() || is1v21Plus();
    }

    public static boolean is1v21Plus() {
        return is1v21() || is1v21v2Plus();
    }

    public static boolean is1v21v2Plus() {
        return is1v21v2()  || is1v21v3() || is1v21v4Plus();
    }


    public static boolean is1v21v4Plus() {
        return is1v21v4() || is1v21v5Plus();
    }

    public static boolean is1v21v5Plus() {
        return is1v21v5() || is1v21v6Plus();
    }

    public static boolean is1v21v6Plus() {
        return is1v21v6() || is1v21v7Plus();
    }

    public static boolean is1v21v7Plus() {
        return  is1v21v7() || is1v21v8() || is1v21v9() || is1v21v10() || is1v21v11() || is26v1Plus();
    }

    public static boolean is26v1Plus() {
        return is26v1() ;
    }

    public static boolean isVersionBetween(String version1, String version2) {
        version1 = version1.replace(".yml", "").replace("_",".");
        version2 = version2.replace(".yml", "").replace("_",".");
        int version1Int = 0;
        int version2Int = 0;

        String[] version1Split = version1.split("\\.");
        for (int i = 0; i < version1Split.length; i++) {
            version1Int += (int) (Integer.parseInt(version1Split[i].trim()) * Math.pow(100, 3 - i));
        }
        if(!version2.isEmpty()) {
            String[] version2Split = version2.split("\\.");
            for (int i = 0; i < version2Split.length; i++) {
                version2Int += (int) (Integer.parseInt(version2Split[i].trim()) * Math.pow(100, 3 - i));
            }
        }
        else version2Int = Integer.MAX_VALUE;

        String serverVersion = Bukkit.getServer().getVersion().split(":")[1].replace(")","");
        int serverVersionInt = 0;
        String[] serverVersionSplit = serverVersion.split("\\.");
        for (int i = 0; i < serverVersionSplit.length; i++) {
            serverVersionInt += (int) (Integer.parseInt(serverVersionSplit[i].trim()) * Math.pow(100, 3 - i));
        }


        /* SsomarDev.testMsg("serverVersion: "+serverVersion+" >> "+serverVersionInt, true);
        SsomarDev.testMsg("version1: "+version1+" >> "+version1Int, true);
        SsomarDev.testMsg("version2: "+version2+" >> "+version2Int, true);*/

        return serverVersionInt >= version1Int && serverVersionInt <= version2Int;

    }




    public static boolean isSpigotOrFork(){
        return isSpigot() || isPaperOrFork();
    }

    public static boolean isPaperOrFork(){
        return isPaper() || isFolia() || isMohist() || isPurpur() || isPufferfish() || isPaperOrForkFor1v20lus;
    }


    public static void initLibPartOfSCore(Plugin plugin, ClassLoader scoreClassLoader) {

        initVersion();

        Utils.sendConsoleMsg(SCore.NAME_COLOR+" &7The library part of SCore is initializing ... (by &e"+plugin.getName()+"&7)");

        pluginHolder = plugin;
        classLoader = scoreClassLoader;

        if(plugin instanceof SCore) dataFolder = plugin.getDataFolder();
        else dataFolder = new File(plugin.getDataFolder().getParentFile(), "SCore");

        injectSpigot = InjectSpigot.INSTANCE;

        if (isFolia()) schedulerHook = new RegionisedSchedulerHook(plugin);
        else schedulerHook = new BukkitSchedulerHook(plugin);

        loadDependency();

        GeneralConfig.getInstance();

        TM.getInstance().load();

        TM.getInstance().loadTexts();

        GUI.init();

        /* Database */
        Database.getInstance().load();

        /* Events instance part */
        EventsHandler.getInstance().setup(pluginHolder);

        CommandsHandler.getInstance().onEnable();

        UsagePerDayManager.getInstance();

        CooldownsHandler.loadCooldowns();
        CooldownsHandler.connectAllOnlinePlayers();
    }


    @Override
    public void onEnable() {
        plugin = this;

        initLibPartOfSCore(this, this.getClassLoader());

        commandClass = new CommandsClass(this);

        Utils.sendConsoleMsg("&7================ " + NAME_COLOR + " &7================");

        if (isFolia()) {
            Utils.sendConsoleMsg(NAME_COLOR + " &7is running on &eFolia");
        }
        if (isMohist()) {
            Utils.sendConsoleMsg(NAME_COLOR + " &7is running on &eMohist");
        }
        if (isPaperOrFork()) {
            Utils.sendConsoleMsg(NAME_COLOR + " &7is running on &ePaper or fork");
        }
        this.displayVersion();

        MessageMain.getInstance().load();

        MessageMain.getInstance().loadMessagesOf(plugin, MessageInterface.getMessagesEnum(Message.values()));

        /* Loop instance part */
        LoopManager.getInstance();

        ActionbarHandler.getInstance().load();

        /* Commands part */
        this.getCommand("score").setExecutor(commandClass);


        //this.getCommand("/").setExecutor(new TestCmdClass());

        /* Projectiles instance part */
        SProjectileLoader.getInstance().load();
        /* Hardness's instance part */
        HardnessLoader.getInstance().load();

        /* Variables instance part */
        VariablesLoader.getInstance().load();

        /* Custom Lists instance part */
        com.ssomar.score.features.custom.customlists.CustomListsManager.getInstance().load();

        if (SCore.hasPlaceholderAPI) {
            new PlaceholderAPISCoreExpansion(this).register();
        }

        FilterManager.getInstance().reload();

        /* Particle toggle instance part */
        ParticleToggleManager.getInstance();

        Utils.sendConsoleMsg("&7================ " + NAME_COLOR + " &7================");

    }

    public static void loadDependency() {
        /* Soft-Dependency part */
        hasExecutableItems = Dependency.EXECUTABLE_ITEMS.hookSoftDependency();

        hasExecutableBlocks = Dependency.EXECUTABLE_BLOCKS.hookSoftDependency();

        hasExecutableEvents = Dependency.EXECUTABLE_EVENTS.hookSoftDependency();

        hasCustomPiglinsTrades = Dependency.CUSTOM_PIGLINS_TRADES.hookSoftDependency();

        hasSParkour = Dependency.SPARKOUR.hookSoftDependency();

        hasPlaceholderAPI = Dependency.PLACEHOLDER_API.hookSoftDependency();

        hasWorldGuard = Dependency.WORLD_GUARD.hookSoftDependency();

        hasVault = Dependency.VAULT.hookSoftDependency();

        hasIridiumSkyblock = Dependency.IRIDIUM_SKYBLOCK.hookSoftDependency();

        hasSuperiorSkyblock2 = Dependency.SUPERIOR_SKYBLOCK2.hookSoftDependency();

        hasBentoBox = Dependency.BENTO_BOX.hookSoftDependency();

        hasFabledSkyblock = Dependency.FABLED_SKYBLOCK.hookSoftDependency();

        hasMultiverse = Dependency.MULTIVERSE_CORE.hookSoftDependency();

        hasLands = Dependency.LANDS.hookSoftDependency();

        hasTowny = Dependency.TOWNY.hookSoftDependency();

        hasGriefPrevention = Dependency.GRIEF_PREVENTION.hookSoftDependency();

        hasGriefDefender = Dependency.GRIEF_DEFENDER.hookSoftDependency();

        hasCoreProtect = Dependency.CORE_PROTECT.hookSoftDependency();

        hasFactionsUUID = Dependency.FACTIONS_UUID.hookSoftDependency();

        hasFastAsyncWorldEdit = Dependency.FAST_ASYNC_WORLD_EDIT.hookSoftDependency();

        Dependency.PACKET_EVENTS.hookSoftDependency();

        /* Test for verzante and qvazzar */
        //hasProtocolLib = false;


        if (PROTOCOL_LIB.isEnabled()) {
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7ProtocolLib hooked !");
            hasProtocolLib = true;

            protocolManager = ProtocolLibrary.getProtocolManager();
            ProtocolLibAPI.reduceDamageIndicator();

            if(!SCore.is1v12Less()) {
                //Display.registerDisplayModule(new TryDisplayModule());
                PacketManager.newDisplay();

                Runnable runnable3 = new Runnable() {
                    @Override
                    public void run() {
                        //System.out.println(">>> RUNNABLE DISPLAY");
                        /* if no modification its not necessary to update the inv of all players  for nothing */
                        if(!Display.isSomethingToModify()) return;
                        Bukkit.getServer().getOnlinePlayers().forEach(player -> {
                            //System.out.println(">>>"+player.getName());
                            ItemStack itemStack = player.getItemOnCursor();
                            if (player.getGameMode() != GameMode.CREATIVE && (itemStack == null || itemStack.getType() == Material.AIR)) {
                                //System.out.println(">>>"+player.getName()+" update");
                                player.updateInventory();
                            }
                        /* else {
                            System.out.println(">>>"+player.getName()+" no update cursor >> "+player.getOpenInventory().getCursor().getType());
                        }*/
                        });
                    }
                };
                SCore.schedulerHook.runAsyncRepeatingTask(runnable3, 0, 20);
            }


            new HardnessesHandler().registerListener();
        }

        hasNBTAPI = Dependency.NBTAPI.hookSoftDependency();

        hasResidence = Dependency.RESIDENCE.hookSoftDependency();


        if (Dependency.PLOT_SQUARED.isInstalled()) {
            try {
                PlotAPI plotAPI = new PlotAPI();
                Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7PlotSquared hooked !");
                hasPlotSquared = true;
            } catch (NoClassDefFoundError e) {
                SCore.plugin.getServer().getLogger().severe("[" + NAME + "] PlotSquared hooked BUT you haven't the good version ! (try to update it) !");
                hasPlotSquared = false;
            }
        }

        hasHeadDatabase = Dependency.HEAD_DATABASE.hookSoftDependency();

        hasHeadDB = Dependency.HEAD_DB.hookSoftDependency();

        hasMythicMobs = Dependency.MYTHIC_MOBS.hookSoftDependency();

        hasDecentHolograms = Dependency.DECENT_HOLOGRAMS.hookSoftDependency();

        if (Dependency.HOLOGRAPHIC_DISPLAYS.isEnabled()) {
            try {
                HolographicDisplaysAPI.get(SCore.plugin);
                Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7HolographicDisplays hooked !");
                hasHolographicDisplays = true;
            } catch (Exception | Error e) {
                SCore.plugin.getServer().getLogger().severe("[" + NAME + "] HolographicDisplays hooked BUT you haven't the good version ! (require 3.0 or higher) !");
                e.printStackTrace();
                hasHolographicDisplays = false;
            }
        }

        hasCMI = Dependency.CMI.hookSoftDependency();

        hasAureliumSkills = Dependency.AURELIUM_SKILLS.hookSoftDependency();

        hasItemsAdder = Dependency.ITEMS_ADDER.hookSoftDependency();

        hasOraxen = Dependency.ORAXEN.hookSoftDependency();

        hasNexo = Dependency.NEXO.hookSoftDependency();

        hasShopGUIPlus = Dependency.SHOP_GUI_PLUS.hookSoftDependency();

        hasRoseLoot = Dependency.ROSE_LOOT.hookSoftDependency();

        hasRoseStacker = Dependency.ROSE_STACKER.hookSoftDependency();

        hasMMOCore = Dependency.MMO_CORE.hookSoftDependency();

        hasMcMMO = Dependency.MCMMO.hookSoftDependency();

        hasProtectionStones = Dependency.PROTECTION_STONES.hookSoftDependency();

        hasExcellentClaims = Dependency.EXCELLENT_CLAIMS.hookSoftDependency();

        hasTAB = Dependency.TAB.hookSoftDependency();

        hasTerra = Dependency.TERRA.hookSoftDependency();

        hasJetsMinions = Dependency.JETS_MINIONS.hookSoftDependency();

        hasEcoSkills = Dependency.ECO_SKILLS.hookSoftDependency();

        hasWildStacker = Dependency.WILD_STACKER.hookSoftDependency();

        hasMyFurniture = Dependency.MY_FURNITURE.hookSoftDependency();

        hasCustomCrafting = Dependency.CUSTOM_CRAFTING.hookSoftDependency();

        hasWorldEdit = Dependency.WORLD_EDIT.hookSoftDependency();

        hasCustomFishing = Dependency.CUSTOM_FISHING.hookSoftDependency();
    }

    @Override
    public void onDisable() {

        injectSpigot.unregisterAllInjectors();

        if (GeneralConfig.getInstance().isUseMySQL()) {
            VariablesManager.getInstance().updateAllLoadedMySQL(VariablesManager.MODE.IMPORT);
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Save &6" + VariablesManager.getInstance().getLoadedObjects().size() + " &7variables from your MySQL Database !");
        }

        if (!SCore.is1v11Less())  Bossbar.getInstance().clearTasks();

        Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Save UsagePerDay....");
        UsagePerDayManager.getInstance().save();
        Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Save UsagePerDay done !");
        Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Save Commands...");
        CommandsHandler.getInstance().onDisable();
        Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Save Commands done !");
        Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Save Cooldowns...");
        CooldownsHandler.closeServerSaveAll();
        Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Save Cooldowns done !");

        Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Run delayed saving tasks...");
        RunnableManager.getInstance().forceRunTasks();
        Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Run delayed saving tasks done !");

        Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Clear scheduled tasks...");
        ScheduledTaskManager.getInstance().cancelScheduledTask();
        Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Clear scheduled tasks done !");

        // Unregister all packs
        PackManager.getInstance().removeAllPacks();

        try {
            Database.getInstance().connect().close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void onReload() {
        Utils.sendConsoleMsg("&7================ " + NAME_COLOR + " &7================");

        Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Run delayed saving tasks...");
        RunnableManager.getInstance().forceRunTasks();
        Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Run delayed saving tasks done !");
        GeneralConfig.getInstance().reload();

        FeatureSettingsSCore.reload();

        LoopManager.getInstance();

        /* Projectiles instance part */
        SProjectileLoader.getInstance().reload();

        /* Variables instance part */
        VariablesLoader.getInstance().reload();

        /* Custom Lists instance part */
        com.ssomar.score.features.custom.customlists.CustomListsManager.getInstance().reload();

        TM.getInstance().load();

        TM.getInstance().loadTexts();

        MessageMain.getInstance().loadMessagesOf(plugin, MessageInterface.getMessagesEnum(Message.values()));

        GUI.init();

        FilterManager.getInstance().reload();

        Utils.sendConsoleMsg("&7================ " + NAME_COLOR + " &7================");
    }

    @Override
    public String getShortName() {
        return NAME;
    }

    @Override
    public String getNameWithBrackets() {
        return "[" + NAME + "]";
    }

    @Override
    public String getNameDesign() {
        return NAME_COLOR;
    }

    @Override
    public String getNameDesignWithBrackets() {
        return NAME_COLOR_WITH_BRACKETS;
    }

    @Override
    public String getObjectName() {
        return null;
    }

    @Override
    public String getObjectNameForPermission(SObject sObject) {
        if(sObject instanceof Variable) return "";
        else if(sObject instanceof SProjectile) return "";
        else if(sObject instanceof Hardness) return "";
        return "";
    }

    @Override
    public SCore getPlugin() {
        return plugin;
    }

    @Override
    public boolean isLotOfWork() {
        return false;
    }

    @Override
    public int getMaxSObjectsLimit() {
        return Integer.MAX_VALUE;
    }

    public CommandsClass getCommandClass() {
        return commandClass;
    }

    public static void initVersion() {
        is26v1 = Bukkit.getServer().getVersion().contains("26.1");
        is1v21v11 = Bukkit.getServer().getVersion().contains("1.21.11");
        is1v21v10 = Bukkit.getServer().getVersion().contains("1.21.10");
        is1v21v9 = Bukkit.getServer().getVersion().contains("1.21.9");
        is1v21v8 = Bukkit.getServer().getVersion().contains("1.21.8");
        is1v21v7 = Bukkit.getServer().getVersion().contains("1.21.7");
        is1v21v6 = Bukkit.getServer().getVersion().contains("1.21.6");
        is1v21v5 = Bukkit.getServer().getVersion().contains("1.21.5");
        is1v21v4 = Bukkit.getServer().getVersion().contains("1.21.4");
        is1v21v3 = Bukkit.getServer().getVersion().contains("1.21.3");
        is1v21v2 = Bukkit.getServer().getVersion().contains("1.21.2");
        is1v21 = Bukkit.getServer().getVersion().contains("1.21");
        is1v20v6 = Bukkit.getServer().getVersion().contains("1.20.6");
        is1v20v5 = Bukkit.getServer().getVersion().contains("1.20.5");
        is1v20v4 = Bukkit.getServer().getVersion().contains("1.20.4");
        is1v20v1 = Bukkit.getServer().getVersion().contains("1.20.1");
        is1v20 = Bukkit.getServer().getVersion().contains("1.20");
        is1v19v4 = Bukkit.getServer().getVersion().contains("1.19.4");
        is1v19v1 = Bukkit.getServer().getVersion().contains("1.19.1");
        is1v19 = Bukkit.getServer().getVersion().contains("1.19");
        is1v18 = Bukkit.getServer().getVersion().contains("1.18");
        is1v17 = Bukkit.getServer().getVersion().contains("1.17");
        is1v16v1 = Bukkit.getServer().getVersion().contains("1.16.1");
        is1v16 = Bukkit.getServer().getVersion().contains("1.16");
        is1v15 = Bukkit.getServer().getVersion().contains("1.15");
        is1v14 = Bukkit.getServer().getVersion().contains("1.14");
        is1v13 = Bukkit.getServer().getVersion().contains("1.13");
        is1v12 = Bukkit.getServer().getVersion().contains("1.12");
        is1v11 = Bukkit.getServer().getVersion().contains("1.11") && !is1v21v11;
        is1v10 = Bukkit.getServer().getVersion().contains("1.10") && !is1v21v10;
        is1v9 = Bukkit.getServer().getVersion().contains("1.9") && !is1v21v9;;
        is1v8 = Bukkit.getServer().getVersion().contains("1.8") && !is1v21v8;

        isSpigot = Bukkit.getServer().getVersion().contains("Spigot") || Bukkit.getServer().getVersion().contains("spigot");
        isMohist = Bukkit.getServer().getName().contains("Mohist") || Bukkit.getServer().getVersion().contains("Mohist");
        isPaper = Bukkit.getServer().getVersion().contains("Paper") || Bukkit.getServer().getVersion().contains("paper");
        isPaperOrForkFor1v20lus = is1v20v1Plus() && (hasClass("com.destroystokyo.paper.PaperConfig") || hasClass("io.papermc.paper.configuration.Configuration"));
        isFolia = Bukkit.getServer().getVersion().contains("Folia") || Bukkit.getServer().getVersion().contains("folia");
        isLuminol = Bukkit.getServer().getVersion().contains("Luminol") || Bukkit.getServer().getVersion().contains("luminol");
        isPurpur = Bukkit.getServer().getVersion().contains("Purpur") || Bukkit.getServer().getVersion().contains("purpur");
        isPufferfish = Bukkit.getServer().getVersion().contains("Pufferfish") || Bukkit.getServer().getVersion().contains("pufferfish");
    }

    public static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean hasMethod(String className, String methodName) {
        try {
            Class.forName(className).getMethod(methodName);
            return true;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            return false;
        }
    }

    public void displayVersion() {
        Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Version of the server &6" + Bukkit.getServer().getVersion()+ " &7!");
        if(false){
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is Folia &6" + SCore.isFolia() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is Spigot &6" + SCore.isSpigot() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is Paper &6" + SCore.isPaper() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is Mohist &6" + SCore.isMohist() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is Purpur &6" + SCore.isPurpur() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is Pufferfish &6" + SCore.isPufferfish() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v8 &6" + SCore.is1v8() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v9 &6" + SCore.is1v9() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v10 &6" + SCore.is1v10() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v11 &6" + SCore.is1v11() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v12 &6" + SCore.is1v12() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v13 &6" + SCore.is1v13() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v14 &6" + SCore.is1v14() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v15 &6" + SCore.is1v15() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v16 &6" + SCore.is1v16() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v16v1 &6" + SCore.is1v16v1() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v17 &6" + SCore.is1v17() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v18 &6" + SCore.is1v18() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v19 &6" + SCore.is1v19() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v19v1 &6" + SCore.is1v19v1() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v19v4 &6" + SCore.is1v19v4() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v20 &6" + SCore.is1v20() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v20v1 &6" + SCore.is1v20v1() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v20v4 &6" + SCore.is1v20v4() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v20v5 &6" + SCore.is1v20v5() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v20v6 &6" + SCore.is1v20v6() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v21 &6" + SCore.is1v21() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v21v2 &6" + SCore.is1v21v2() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v21v3 &6" + SCore.is1v21v3() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v21v4 &6" + SCore.is1v21v4() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v21v5 &6" + SCore.is1v21v5() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v21v6 &6" + SCore.is1v21v6() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v21v7 &6" + SCore.is1v21v7() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v21v8 &6" + SCore.is1v21v8() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v21v9 &6" + SCore.is1v21v9() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v21v10 &6" + SCore.is1v21v10() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v21v11 &6" + SCore.is1v21v11() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 26v1 &6" + SCore.is26v1() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v10Less &6" + SCore.is1v10Less() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v11Less &6" + SCore.is1v11Less() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v12Less &6" + SCore.is1v12Less() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v12Plus &6" + SCore.is1v12Plus() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v13Less &6" + SCore.is1v13Less() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v16Plus &6" + SCore.is1v16Plus() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v17Plus &6" + SCore.is1v17Plus() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v18Plus &6" + SCore.is1v18Plus() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v19Plus &6" + SCore.is1v19Plus() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v19v4Plus &6" + SCore.is1v19v4Plus() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v20Plus &6" + SCore.is1v20Plus() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v20v1Plus &6" + SCore.is1v20v1Plus() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v20v4Plus &6" + SCore.is1v20v4Plus() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v20v5Plus &6" + SCore.is1v20v5Plus() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v20v6Plus &6" + SCore.is1v20v6Plus() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v21Plus &6" + SCore.is1v21Plus() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v21v2Plus &6" + SCore.is1v21v2Plus() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v21v4Plus &6" + SCore.is1v21v4Plus() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v21v5Plus &6" + SCore.is1v21v5Plus() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v21v6Plus &6" + SCore.is1v21v6Plus() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 1v21v7Plus &6" + SCore.is1v21v7Plus() + " &7!");
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Is 26v1Plus &6" + SCore.is26v1Plus() + " &7!");
        }
    }

    @Override
    public Config getPluginConfig() {
        return GeneralConfig.getInstance();
    }

    public static SchedulerHook getSchedulerHook(Plugin plugin) {
        if (isFolia()) schedulerHook = new RegionisedSchedulerHook(plugin);
        else schedulerHook = new BukkitSchedulerHook(plugin);
        return schedulerHook;
    }
}
