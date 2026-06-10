package com.ssomar.score.usedapi;

import com.songoda.skyblock.api.SkyBlockAPI;
import com.songoda.skyblock.api.island.Island;
import com.songoda.skyblock.api.island.IslandRole;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
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
            OfflinePlayer offP = Bukkit.getOfflinePlayer(pUUID);
            if (island.getRole(offP) == IslandRole.OPERATOR) return true;
            if ((island.getRole(offP) == IslandRole.MEMBER)) return true;
            if ((island.getRole(offP) == IslandRole.COOP)) return true;
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
