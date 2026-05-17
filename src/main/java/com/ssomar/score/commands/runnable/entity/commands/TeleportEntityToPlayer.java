package com.ssomar.score.commands.runnable.entity.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.entity.EntityCommand;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TeleportEntityToPlayer extends EntityCommand {

    public TeleportEntityToPlayer() {
        setNewSettingsMode(true);
    }

    @Override
    public void run(Player p, Entity entity, SCommandToExec sCommandToExec) {
        Location pLoc = p.getLocation();

        if (!entity.isDead() && p.isOnline() && !p.isDead())
            if (SCore.isFolia())
                entity.teleportAsync(new Location(p.getWorld(), pLoc.getX(), pLoc.getY(), pLoc.getZ()));
            else
                entity.teleport(new Location(p.getWorld(), pLoc.getX(), pLoc.getY(), pLoc.getZ()));
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("TELEPORT_ENTITY_TO_PLAYER");
        names.add("TELEPORT ENTITY TO PLAYER");
        return names;
    }

    @Override
    public String getTemplate() {
        return "TELEPORT_ENTITY_TO_PLAYER";
    }

    @Override
    public ChatColor getColor() {
        return null;
    }

    @Override
    public ChatColor getExtraColor() {
        return null;
    }

}
