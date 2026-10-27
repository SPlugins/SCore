package com.ssomar.score.usedapi;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TownyToolAPI {


    public static boolean playerCanBreakBlock(@NotNull UUID uuid, Location location) {

        Town town = null;
        try {
            town = TownyAPI.getInstance().getTownBlock(location).getTown();
        } catch (NotRegisteredException | NullPointerException e) {
            /* Not in a town so allow break */
            return true;
        }
        Resident resident = null;
        try {
            resident = TownyAPI.getInstance().getResident(uuid);
            return  town.hasResident(resident) || town.hasTrustedResident(resident);
        } catch (Exception e) {
            return true;
        }
    }

    public static boolean playerIsInHisTown(@NotNull Player player, Location location) {

        Town town = null;
        try {
            town = TownyAPI.getInstance().getTownBlock(location).getTown();
        } catch (NotRegisteredException | NullPointerException e) {
            return false;
        }

        return town.hasResident(player.getName());
    }

    public static boolean playerIsInHisTown(@NotNull UUID uuid, Location location) {

        Town town = null;
        try {
            town = TownyAPI.getInstance().getTownBlock(location).getTown();
        } catch (NotRegisteredException | NullPointerException e) {
            return false;
        }

        return town.hasResident(uuid);
    }
}
