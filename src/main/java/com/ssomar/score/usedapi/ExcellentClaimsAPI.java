package com.ssomar.score.usedapi;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentclaims.ClaimsAPI;
import su.nightexpress.excellentclaims.api.claim.Claim;
import su.nightexpress.excellentclaims.claim.ClaimManager;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExcellentClaimsAPI {

    public static boolean playerIsInHisClaim(@NotNull Player p, Location location, boolean acceptWilderness) {
        ClaimManager claimManager = ClaimsAPI.getClaimManager();

        if (!claimManager.isClaimed(location)) {
            return acceptWilderness;
        }

        Claim claim = claimManager.getPrioritizedClaim(location);
        if (claim == null) {
            return acceptWilderness;
        }

        UUID pUUID = p.getUniqueId();

        // Check if player is owner or member of the claim
        return claim.isOwner(pUUID) || claim.isMember(pUUID);
    }

    public static boolean playerCanBreakClaimBlock(@NotNull UUID pUUID, @NotNull Location location) {
        Player player = Bukkit.getPlayer(pUUID);
        if (player == null) return false;

        ClaimManager claimManager = ClaimsAPI.getClaimManager();

        if (!claimManager.isClaimed(location)) {
            return true;
        }

        Claim claim = claimManager.getPrioritizedClaim(location);
        if (claim == null) {
            return true;
        }

        // Check if player can build (which includes breaking blocks)
        return claim.isOwner(pUUID) || claim.isMember(pUUID);
    }

    public static boolean playerCanPlaceClaimBlock(@NotNull UUID pUUID, @NotNull Location location) {
        Player player = Bukkit.getPlayer(pUUID);
        if (player == null) return false;

        ClaimManager claimManager = ClaimsAPI.getClaimManager();

        if (!claimManager.isClaimed(location)) {
            return true;
        }

        Claim claim = claimManager.getPrioritizedClaim(location);
        if (claim == null) {
            return true;
        }

        // Check if player can build (which includes placing blocks)
        return claim.isOwner(pUUID) || claim.isMember(pUUID);
    }

    public static boolean playerCanOpenClaimBlock(@NotNull UUID pUUID, @NotNull Location location) {
        Player player = Bukkit.getPlayer(pUUID);
        if (player == null) return false;

        ClaimManager claimManager = ClaimsAPI.getClaimManager();

        if (!claimManager.isClaimed(location)) {
            return true;
        }

        Claim claim = claimManager.getPrioritizedClaim(location);
        if (claim == null) {
            return true;
        }

        // Check if player can interact (which includes opening containers)
        return claim.isOwner(pUUID) || claim.isMember(pUUID);
    }
}
