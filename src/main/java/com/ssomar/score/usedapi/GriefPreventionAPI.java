package com.ssomar.score.usedapi;

import com.ssomar.score.utils.ToolsListMaterial;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GriefPreventionAPI {

    public static boolean playerIsInHisClaim(@NotNull Player p, Location location, boolean acceptWilderness) {
        DataStore dataStore = GriefPrevention.instance.dataStore;
        Claim claim = dataStore.getClaimAt(location, false, null);
        if (claim == null || claim.getOwnerID() == null) return acceptWilderness;

        return claim.getOwnerID().equals(p.getUniqueId()) || claim.getPermission("public") != null || (claim.hasExplicitPermission(p, ClaimPermission.Build) && claim.hasExplicitPermission(p, ClaimPermission.Access) && claim.hasExplicitPermission(p, ClaimPermission.Inventory));
    }


    public static boolean playerCanBreakClaimBlock(@NotNull UUID pUUID, @NotNull Location location) {
        DataStore dataStore = GriefPrevention.instance.dataStore;
        Claim claim = dataStore.getClaimAt(location, false, null);

        if (claim == null) return true;

        Player player = Bukkit.getPlayer(pUUID);
        if (player == null) return false;

        Block block = location.getBlock();
        Material material = block.getType();

        return claim.allowBreak(player, location.getBlock().getType()) == null || (material != null && ToolsListMaterial.getInstance().getPlantWithGrowth().contains(material) && null == claim.allowContainers(player));
    }

    public static boolean playerCanPlaceClaimBlock(@NotNull UUID pUUID, @NotNull Location location) {
        DataStore dataStore = GriefPrevention.instance.dataStore;
        Claim claim = dataStore.getClaimAt(location, false, null);

        if (claim == null) return true;

        Player player = Bukkit.getPlayer(pUUID);
        if (player == null) return false;

        return claim.allowBuild(player, Material.STONE) == null;
    }

    public static boolean playerCanOpenClaimBlock(@NotNull UUID pUUID, @NotNull Location location) {
        DataStore dataStore = GriefPrevention.instance.dataStore;
        Claim claim = dataStore.getClaimAt(location, false, null);

        if (claim == null) return true;

        Player player = Bukkit.getPlayer(pUUID);
        if (player == null) return false;

        return claim.allowContainers(player) == null;
    }

}
