package com.ssomar.score.usedapi;

import com.ssomar.score.SCore;
import com.ssomar.score.SsomarDev;
import io.lumine.mythic.api.exceptions.InvalidMobTypeException;
import io.lumine.mythic.bukkit.BukkitAPIHelper;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MythicMobsAPI {

    public static boolean isMythicMob(Entity entity, String mobName) {
        if (SCore.hasMythicMobs) {
            BukkitAPIHelper api = MythicBukkit.inst().getAPIHelper();
            if (api.isMythicMob(entity)) {
                ActiveMob activeMob = api.getMythicMobInstance(entity);
                //SsomarDev.testMsg("Name/ID: " + activeMob.getType().getInternalName());
                return mobName.equals(activeMob.getType().getInternalName());
            }
        }
        return false;
    }

    public static boolean isMythicMob(Entity entity, List<String> mobNames) {
        if (SCore.hasMythicMobs) {
            BukkitAPIHelper api = MythicBukkit.inst().getAPIHelper();
            //SsomarDev.testMsg("isMythicMob: " + api.isMythicMob(entity.getUniqueId())+ " "+entity.getType(), true);
            if (api.isMythicMob(entity.getUniqueId())) {
                ActiveMob activeMob = api.getMythicMobInstance(entity);
                //SsomarDev.testMsg("Name/ID: " + activeMob.getType().getInternalName(), true);
                return mobNames.contains(activeMob.getType().getInternalName());
            }
        }
        return false;
    }

    public static Entity buildMythicMob(String id, Location loc) {
        if (SCore.hasMythicMobs) {
            BukkitAPIHelper api = MythicBukkit.inst().getAPIHelper();
            try {
                SsomarDev.testMsg("Changing to MythicMob SPAWN: " + id, true);
                return api.spawnMythicMob(id, loc);
            } catch (InvalidMobTypeException e) {}
        }
        return null;
    }
}
