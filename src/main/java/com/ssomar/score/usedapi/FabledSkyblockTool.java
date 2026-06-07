package com.ssomar.score.usedapi;

import com.songoda.skyblock.api.SkyBlockAPI;
import com.songoda.skyblock.api.island.Island;
import com.songoda.skyblock.api.island.IslandRole;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class FabledSkyblockTool {

    public static boolean playerIsOnHisIsland(Player player) {
        return playerIsOnHisIsland(player.getUniqueId(), player.getLocation());
    }

    public static boolean playerIsOnHisIsland(@NotNull UUID pUUID, @NotNull Location location) {
        Island island = SkyBlockAPI.getIslandManager().getIslandAtLocation(location);
        if (island != null) {
            if (pUUID.equals(island.getOwnerUUID())) return true;
            if (island.getRole(IslandRole.Operator).contains(pUUID)) return true;
            if (island.getRole(IslandRole.Member).contains(pUUID)) return true;
            if (island.getRole(IslandRole.Coop).contains(pUUID)) return true;
            return false;
        }

        /* He is not on an Island */
        return true;
    }

    public static boolean playerCanBreakIslandBlock(@NotNull UUID pUUID, @NotNull Location location) {
        return playerIsOnHisIsland(pUUID, location);
    }

    public static boolean playerCanPlaceIslandBlock(@NotNull UUID pUUID, @NotNull Location location) {
        return playerIsOnHisIsland(pUUID, location);
    }

    public static boolean playerCanOpenIslandBlock(@NotNull UUID pUUID, @NotNull Location location) {
        return playerIsOnHisIsland(pUUID, location);
    }
}
