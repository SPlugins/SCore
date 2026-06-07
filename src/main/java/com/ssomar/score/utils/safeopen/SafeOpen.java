package com.ssomar.score.utils.safeopen;

import com.ssomar.score.SCore;
import com.ssomar.score.SsomarDev;
import com.ssomar.score.usedapi.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SafeOpen {

    private static final boolean DEBUG = false;

    public static boolean verifSafeOpen(@NotNull final UUID playerUUID, @NotNull Block block) {
        return verifSafeOpen(playerUUID, block.getLocation());
    }

    public static boolean verifSafeOpen(@NotNull final UUID playerUUID, @NotNull Location location) {

        if (Bukkit.getOfflinePlayer(playerUUID).isOp()) return true;

        SsomarDev.testMsg("DEBUG SAFE BREAK CDT 1", DEBUG);

        if (SCore.hasGriefPrevention)
            if (!GriefPreventionAPI.playerCanOpenClaimBlock(playerUUID, location)) return false;

        SsomarDev.testMsg("DEBUG SAFE BREAK CDT 2", DEBUG);

        if (SCore.hasIridiumSkyblock)
            if (!IridiumSkyblockTool.playerCanOpenIslandBlock(playerUUID, location)) return false;

        if (SCore.hasSuperiorSkyblock2)
            if (!SuperiorSkyblockTool.playerCanOpenIslandBlock(playerUUID, location)) return false;

        if (SCore.hasBentoBox)
            if (!BentoBoxAPI.playerCanOpenIslandBlock(playerUUID, location)) return false;

        if (SCore.hasFabledSkyblock)
            if (!FabledSkyblockTool.playerCanOpenIslandBlock(playerUUID, location)) return false;

        SsomarDev.testMsg("DEBUG SAFE BREAK CDT 3", DEBUG);

        if (SCore.hasLands)
            if (!new LandsIntegrationAPI(SCore.plugin).playerCanOpenClaimBlock(playerUUID, location))
                return false;

        if(SCore.hasFactionsUUID)
            if(!new FactionsUUIDAPI().playerCanOpenClaimBlock(playerUUID, location)) return false;

        SsomarDev.testMsg("DEBUG SAFE BREAK CDT 4", DEBUG);

        if (SCore.hasWorldGuard)
            if (!WorldGuardAPI.playerCanOpenInRegion(playerUUID, location)) return false;

        SsomarDev.testMsg("DEBUG SAFE BREAK CDT 5", DEBUG);

        if (SCore.hasResidence)
            if (!ResidenceAPI.playerCanOpenClaimBlock(playerUUID, location)) return false;

        SsomarDev.testMsg("DEBUG SAFE BREAK CDT 6", DEBUG);

        if(SCore.hasProtectionStones)
            if(!ProtectionStonesAPI.playerCanOpenClaimBlock(playerUUID, location)) return false;

        if(SCore.hasExcellentClaims)
            if(!ExcellentClaimsAPI.playerCanOpenClaimBlock(playerUUID, location)) return false;

        return true;
    }
}
