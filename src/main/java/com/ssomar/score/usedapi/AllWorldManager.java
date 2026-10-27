package com.ssomar.score.usedapi;

import com.ssomar.score.SCore;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AllWorldManager {

    public static Optional<World> getWorld(String worldStr) {

        if (SCore.hasMultiverse) {
            World world = MultiverseAPI.getWorld(worldStr);
            if (world != null) return Optional.of(world);
        }

        if(SCore.hasBentoBox){
            World world = BentoBoxAPI.getWorld(worldStr);
            if(world != null) return Optional.of(world);
        }

        /* for(World w : Bukkit.getWorlds()){
            SsomarDev.testMsg("WORLD: "+w.getName()+" worldstr >>>>>>>>>>>>>>>>>>"+worldStr+"<<<<<<<<<<<<<<<<<<", true);
        }*/

        return Optional.ofNullable(Bukkit.getServer().getWorld(worldStr));
    }

    public static Optional<World> getWorld(UUID worldUUID) {

        if (SCore.hasMultiverse) {
            for(String s : MultiverseAPI.getWorlds()){
                World world = MultiverseAPI.getWorld(s);
                if(world.getUID().equals(worldUUID)) return Optional.of(world);
            }
        }

        if(SCore.hasBentoBox){
            for(String s : BentoBoxAPI.getWorlds()){
                World world = BentoBoxAPI.getWorld(s);
                if(world.getUID().equals(worldUUID)) return Optional.of(world);
            }
        }


        return Optional.ofNullable(Bukkit.getServer().getWorld(worldUUID));
    }

    public static List<String> getWorlds() {
        List<String> worlds = new ArrayList<>();

        if (SCore.hasMultiverse) {
            worlds.addAll(MultiverseAPI.getWorlds());
        }

        if(SCore.hasBentoBox){
            for(String s : BentoBoxAPI.getWorlds()){
                if(!worlds.contains(s)) worlds.add(s);
            }
        }

        for(World w : Bukkit.getWorlds()){
            if(!worlds.contains(w.getName())){
                worlds.add(w.getName());
            }
        }

        return worlds;
    }

    public static List<World> getWorldsCB() {
        List<World> worlds = new ArrayList<>();

        if (SCore.hasMultiverse) {
            worlds.addAll(MultiverseAPI.getWorldsCB());
        }

        if(SCore.hasBentoBox){
            for(World s : BentoBoxAPI.getWorldsCB()){
                if(!worlds.contains(s)) worlds.add(s);
            }
        }

        for(World w : Bukkit.getWorlds()){
            if(!worlds.contains(w)){
                worlds.add(w);
            }
        }

        return worlds;
    }
}
