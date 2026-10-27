package com.ssomar.score.usedapi;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SuperiorSkyblockTool {

    public static boolean playerIsOnHisIsland(Player player) {
        return playerIsOnHisIsland(player.getUniqueId(), player.getLocation());
    }

    public static boolean playerIsOnHisIsland(@NotNull UUID pUUID, @NotNull Location location) {
        Island island = SuperiorSkyblockAPI.getIslandAt(location);
        if (island != null) {
            if(island.isCoop(SuperiorSkyblockAPI.getPlayer(pUUID))) return true;
            List<SuperiorPlayer> members = island.getIslandMembers(true);
            for (SuperiorPlayer user : members) {
                if (pUUID.equals(user.getUniqueId())) return true;
            }
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
