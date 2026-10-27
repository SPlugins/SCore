package com.ssomar.score.usedapi;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.lists.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BentoBoxAPI {

    public static boolean playerIsOnHisIsland(Player player) {
        return playerIsOnHisIsland(player.getUniqueId(), player.getLocation());
    }

    public static boolean playerIsOnHisIsland(@NotNull UUID pUUID, @NotNull Location location) {
        // Check if given user is a member on island at the given location.
        
        return BentoBox.getInstance().getIslandsManager().getIslandAt(location).
            map(island -> island.getMemberSet().contains(pUUID)).
            orElse(false);
    }

    public static boolean playerCanBreakIslandBlock(@NotNull UUID pUUID, @NotNull Location location) {
        // Checks if player is allowed to break blocks on island he is standing on,
        // or if he is allowed to break blocks in the world outside protection area.

        return !BentoBox.getInstance().getIWM().inWorld(location.getWorld()) ||
            BentoBox.getInstance().getIslandsManager().getIslandAt(location).
                map(island -> island.isAllowed(User.getInstance(pUUID), Flags.BREAK_BLOCKS)).
                orElse(Flags.BREAK_BLOCKS.isSetForWorld(location.getWorld()));
    }

    public static boolean playerCanPlaceIslandBlock(@NotNull UUID pUUID, @NotNull Location location) {
        // Checks if player is allowed to place blocks on island he is standing on,
        // or if he is allowed to place blocks in the world outside protection area.

        return !BentoBox.getInstance().getIWM().inWorld(location.getWorld()) ||
            BentoBox.getInstance().getIslandsManager().getIslandAt(location).
                map(island -> island.isAllowed(User.getInstance(pUUID), Flags.PLACE_BLOCKS)).
                orElse(Flags.PLACE_BLOCKS.isSetForWorld(location.getWorld()));
    }

    public static boolean playerCanOpenIslandBlock(@NotNull UUID pUUID, @NotNull Location location) {
        // Checks if player is allowed to place blocks on island he is standing on,
        // or if he is allowed to place blocks in the world outside protection area.

        return !BentoBox.getInstance().getIWM().inWorld(location.getWorld()) ||
                BentoBox.getInstance().getIslandsManager().getIslandAt(location).
                        map(island -> island.isAllowed(User.getInstance(pUUID), Flags.CONTAINER)).
                        orElse(Flags.CONTAINER.isSetForWorld(location.getWorld()));
    }

    public static World getWorld(String worldStr) {

        for(World w : BentoBox.getInstance().getIWM().getWorlds()){
            if (w.getName().equalsIgnoreCase(worldStr)) {
                return w;
            }
        }
        return null;
    }

    public static List<String> getWorlds() {
        List<String> worlds = new ArrayList<>();
        for(World w : BentoBox.getInstance().getIWM().getWorlds()){
            worlds.add(w.getName());
        }
        return worlds;
    }

    public static List<World> getWorldsCB() {
        List<World> worlds = new ArrayList<>();
        worlds.addAll(BentoBox.getInstance().getIWM().getWorlds());
        return worlds;
    }
}
