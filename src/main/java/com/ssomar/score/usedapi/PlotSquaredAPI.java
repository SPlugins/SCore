package com.ssomar.score.usedapi;

import com.plotsquared.core.plot.Plot;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PlotSquaredAPI {


    public static boolean playerIsInHisPlot(@NotNull Player p, Location location) {

        com.plotsquared.core.location.Location loc = com.plotsquared.core.location.Location.at(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());

        Plot plot = Plot.getPlot(loc);
        if (plot != null) {
            if (plot.getMembers().contains(p.getUniqueId()) || plot.getTrusted().contains(p.getUniqueId()) || plot.getOwner().equals(p.getUniqueId()))
                return true;
        }

        return false;

    }


    public static boolean playerCanBreakIslandBlock(@NotNull UUID pUUID, @NotNull Location location) {

        com.plotsquared.core.location.Location loc = com.plotsquared.core.location.Location.at(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        if (loc.isPlotRoad()) return false;

        Plot plot = Plot.getPlot(loc);
        if (plot != null) {
            if (!(plot.getMembers().contains(pUUID) || plot.getTrusted().contains(pUUID) || plot.getOwner().equals(pUUID)))
                return false;
        }

        return true;
    }
}
