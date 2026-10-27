package com.ssomar.score.usedapi;

import dev.espi.protectionstones.PSRegion;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProtectionStonesAPI {

    public static boolean playerIsInHisClaim(@NotNull Player p, Location location, boolean acceptWilderness) {
        PSRegion r1 = PSRegion.fromLocation(location);

        if (r1 == null) return acceptWilderness;


        return  r1.getMembers().contains(p.getUniqueId()) || r1.isOwner(p.getUniqueId());
    }


    public static boolean playerCanBreakClaimBlock(@NotNull UUID pUUID, @NotNull Location location) {
        PSRegion r1 = PSRegion.fromLocation(location);

        if (r1 == null) return true;

        Player player = Bukkit.getPlayer(pUUID);
        if (player == null) return false;

        return r1.getMembers().contains(player.getUniqueId())  | r1.isOwner(pUUID);
    }

    public static boolean playerCanPlaceClaimBlock(@NotNull UUID pUUID, @NotNull Location location) {
        PSRegion r1 = PSRegion.fromLocation(location);

        if (r1 == null) return true;

        Player player = Bukkit.getPlayer(pUUID);
        if (player == null) return false;

        return r1.getMembers().contains(player.getUniqueId()) || r1.isOwner(pUUID);
    }

    public static boolean playerCanOpenClaimBlock(@NotNull UUID pUUID, @NotNull Location location) {
        PSRegion r1 = PSRegion.fromLocation(location);

        if (r1 == null) return true;

        Player player = Bukkit.getPlayer(pUUID);
        if (player == null) return false;

        return r1.getMembers().contains(player.getUniqueId()) || r1.isOwner(pUUID);
    }

}
