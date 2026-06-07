package com.ssomar.score.utils.safeplace;

import com.ssomar.score.SCore;
import com.ssomar.score.SsomarDev;
import com.ssomar.score.usedapi.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SafePlace {

    private static final boolean DEBUG = false;

    public static void placeBlockWithEvent(@NotNull final Block block, @NotNull Material material, Optional<Map<String, String>> statesOpt, @Nullable final UUID playerUUID, boolean generatePlaceEvent, boolean verifSafePlace){
        placeBlockWithEvent(block, material, statesOpt, playerUUID, generatePlaceEvent, verifSafePlace, true);
    }

    public static void placeBlockWithEvent(@NotNull final Block block, @NotNull Material material, Optional<Map<String, String>> statesOpt, @Nullable final UUID playerUUID, boolean generatePlaceEvent, boolean verifSafePlace, boolean enablePhysics) {

        SsomarDev.testMsg("DEBUG SAFE PLACE 1", DEBUG);
        if (playerUUID == null) {
            block.setType(material, enablePhysics);
            return;
        }
        SsomarDev.testMsg("DEBUG SAFE PLACE 1.5", DEBUG);

        if (verifSafePlace && !verifSafePlace(playerUUID, block)) return;

        Player player = Bukkit.getServer().getPlayer(playerUUID);
        Location location = block.getLocation();
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                SsomarDev.testMsg("DEBUG SAFE BREAK 2", DEBUG);
                if (player != null) {
                    SsomarDev.testMsg("DEBUG SAFE BREAK 3 >> "+material, DEBUG);
            /*boolean canceled = false;
            if(generateBreakEvent) {
                SsomarDev.testMsg("DEBUG SAFE BREAK 4");
                BlockBreakEvent bbE = new BlockBreakEventExtension(block, player, true);
                bbE.setCancelled(false);

                Bukkit.getPluginManager().callEvent(bbE);
                canceled = bbE.isCancelled();
            }

            if (!canceled) {*/
                    setBlockType(block, material);
                    // }
                } else {
                    setBlockType(block, material);
                }
            }
        };
        // delay for playerblockbreak event if not the block is nto placed
        SCore.schedulerHook.runLocationTask(runnable, location, 0);

    }

    public static void setBlockType(Block block, Material material){
        /*if(SCore.isFolia()){
            Location location = block.getLocation();
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    block.setType(material);
                }
            };
            SCore.schedulerHook.runLocationTaskAsap(runnable, location);
        } else {*/
            block.setType(material);
        //}
    }

    public static boolean verifSafePlace(@NotNull final UUID playerUUID, @NotNull Block block) {
        return verifSafePlace(playerUUID, block.getLocation());
    }

    public static boolean verifSafePlace(@NotNull final UUID playerUUID, @NotNull Location location) {

        if (Bukkit.getOfflinePlayer(playerUUID).isOp()) return true;

        SsomarDev.testMsg("DEBUG SAFE BREAK CDT 1", DEBUG);

        if (SCore.hasGriefPrevention)
            if (!GriefPreventionAPI.playerCanPlaceClaimBlock(playerUUID, location)) return false;

        SsomarDev.testMsg("DEBUG SAFE BREAK CDT 2", DEBUG);

        if (SCore.hasIridiumSkyblock)
            if (!IridiumSkyblockTool.playerCanPlaceIslandBlock(playerUUID, location)) return false;

        if (SCore.hasSuperiorSkyblock2)
            if (!SuperiorSkyblockTool.playerCanPlaceIslandBlock(playerUUID, location)) return false;

        if (SCore.hasBentoBox)
            if (!BentoBoxAPI.playerCanPlaceIslandBlock(playerUUID, location)) return false;

        if (SCore.hasFabledSkyblock)
            if (!FabledSkyblockTool.playerCanPlaceIslandBlock(playerUUID, location)) return false;

        SsomarDev.testMsg("DEBUG SAFE BREAK CDT 3", DEBUG);

        if (SCore.hasLands)
            if (!new LandsIntegrationAPI(SCore.plugin).playerCanPlaceClaimBlock(playerUUID, location))
                return false;

        if(SCore.hasFactionsUUID)
            if(!new FactionsUUIDAPI().playerCanPlaceClaimBlock(playerUUID, location)) return false;

        SsomarDev.testMsg("DEBUG SAFE BREAK CDT 4", DEBUG);

        if (SCore.hasWorldGuard)
            if (!WorldGuardAPI.playerCanPlaceInRegion(playerUUID, location)) return false;

        SsomarDev.testMsg("DEBUG SAFE BREAK CDT 5", DEBUG);

        if (SCore.hasResidence)
            if (!ResidenceAPI.playerCanPlaceClaimBlock(playerUUID, location)) return false;

        SsomarDev.testMsg("DEBUG SAFE BREAK CDT 6", DEBUG);

        if(SCore.hasProtectionStones)
            if(!ProtectionStonesAPI.playerCanPlaceClaimBlock(playerUUID, location)) return false;

        if(SCore.hasExcellentClaims)
            if(!ExcellentClaimsAPI.playerCanPlaceClaimBlock(playerUUID, location)) return false;

        return true;
    }
}
