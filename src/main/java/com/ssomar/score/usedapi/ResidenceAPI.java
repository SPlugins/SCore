package com.ssomar.score.usedapi;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResidenceAPI {

    public static boolean playerIsInHisClaim(@NotNull Player p, Location location, boolean acceptWilderness) {

        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(location);
        if (res == null) return acceptWilderness;

        return res.getOwnerUUID().equals(p.getUniqueId()) || res.isTrusted(p);

    }

    public static boolean playerCanBreakClaimBlock(@NotNull UUID pUUID, @NotNull Location location) {
        ResidencePlayer rPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(pUUID);
        return rPlayer.canBreakBlock(location.getBlock(), false);
    }

    public static boolean playerCanPlaceClaimBlock(@NotNull UUID pUUID, @NotNull Location location) {
        ResidencePlayer rPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(pUUID);
        return rPlayer.canPlaceBlock(location.getBlock(), false);
    }

    public static boolean playerCanOpenClaimBlock(@NotNull UUID pUUID, @NotNull Location location) {
        ResidencePlayer rPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(pUUID);
        return rPlayer.canPlaceBlock(location.getBlock(), false);
    }
}
