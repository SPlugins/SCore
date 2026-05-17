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

public class TeleportPlayerToEntity extends EntityCommand {

    public TeleportPlayerToEntity() {
        setNewSettingsMode(true);
    }

    @Override
    public void run(Player p, Entity entity, SCommandToExec sCommandToExec) {
        Location eLoc = entity.getLocation();

        if (!entity.isDead() && p.isOnline() && !p.isDead())
            if (SCore.isFolia())
                p.teleportAsync(new Location(entity.getWorld(), eLoc.getX(), eLoc.getY(), eLoc.getZ()));
            else
                p.teleport(new Location(entity.getWorld(), eLoc.getX(), eLoc.getY(), eLoc.getZ()));
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("TELEPORT_PLAYER_TO_ENTITY");
        return names;
    }

    @Override
    public String getTemplate() {
        return "TELEPORT_PLAYER_TO_ENTITY";
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
