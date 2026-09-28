package com.ssomar.score.usedapi;


import com.onarandombox.MultiverseCore.api.Core;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.ssomar.score.SCore;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class MultiverseAPI {

    public static World getWorld(String worldStr) {

        try {
            Core core;
            core = (Core) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
            MVWorldManager multiverseManager = core.getMVWorldManager();
            MultiverseWorld mv;
            if ((mv = multiverseManager.getMVWorld(worldStr)) != null)
                return mv.getCBWorld();
        } catch (NoClassDefFoundError e) {
            SCore.hasMultiverse = false;
            for (World w : Bukkit.getWorlds()) {
                if (w.getName().equalsIgnoreCase(worldStr)) {
                    return w;
                }
            }
            return null;
        } catch (RuntimeException e) {
            /* Multiverse-Core can be enabled after SCore: its world manager is not ready yet */
            for (World w : Bukkit.getWorlds()) {
                if (w.getName().equalsIgnoreCase(worldStr)) {
                    return w;
                }
            }
            return null;
        }
        return null;
    }

    public static List<String> getWorlds() {
        List<String> worlds = new ArrayList<>();
        try {
            Core core;
            core = (Core) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
            MVWorldManager multiverseManager = core.getMVWorldManager();
            for (MultiverseWorld mv : multiverseManager.getMVWorlds()) {
                worlds.add(mv.getName());
            }
        } catch (NoClassDefFoundError e) {
            SCore.hasMultiverse = false;
            for (World w : Bukkit.getWorlds()) {
                worlds.add(w.getName());
            }
            return worlds;
        } catch (RuntimeException e) {
            /* Multiverse-Core can be enabled after SCore: its world manager is not ready yet */
            for (World w : Bukkit.getWorlds()) {
                worlds.add(w.getName());
            }
            return worlds;
        }
        return worlds;
    }

    public static List<World> getWorldsCB() {
        List<World> worlds = new ArrayList<>();
        try {
            Core core;
            core = (Core) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
            MVWorldManager multiverseManager = core.getMVWorldManager();
            for (MultiverseWorld mv : multiverseManager.getMVWorlds()) {
                worlds.add(mv.getCBWorld());
            }
        } catch (NoClassDefFoundError e) {
            SCore.hasMultiverse = false;
            worlds.addAll(Bukkit.getWorlds());
            return worlds;
        } catch (RuntimeException e) {
            /* Multiverse-Core can be enabled after SCore: its world manager is not ready yet */
            worlds.addAll(Bukkit.getWorlds());
            return worlds;
        }
        return worlds;
    }
}
