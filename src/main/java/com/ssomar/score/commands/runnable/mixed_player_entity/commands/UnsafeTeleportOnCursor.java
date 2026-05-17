package com.ssomar.score.commands.runnable.mixed_player_entity.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.ArgumentChecker;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.mixed_player_entity.MixedCommand;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/* TELEPORTONCURSOR {range}:Integer {acceptAir}:boolean */
public class UnsafeTeleportOnCursor extends MixedCommand {

    @Override
    public void run(Player p, Entity receiver, SCommandToExec sCommandToExec) {
        List<String> args = sCommandToExec.getOtherArgs();

        if(!(receiver instanceof LivingEntity)) return;
        LivingEntity livingReceiver = (LivingEntity) receiver;

        int amount = 200;
        if (args.size() >= 1) {
            amount = Double.valueOf(args.get(0)).intValue();
            if (amount <= 0) amount = 200;
        }
        try {

            Location eyeLoc = livingReceiver.getEyeLocation();
            Vector eyeVec = eyeLoc.getDirection();
            Vector checkVec = eyeVec.clone().multiply(2);
            Location checkLoc = eyeLoc.clone().add(checkVec);

            int cpt = 2;
            /* Invalid if its a cheat movement */
            Block lastAirBlock = null;
            while (cpt < amount && isAirBlock((checkLoc = eyeLoc.clone().add(eyeVec.clone().multiply(cpt))).getBlock())) {
                if (!isAirBlock(checkLoc.clone().getBlock())) {
                    break;
                } else lastAirBlock = checkLoc.clone().getBlock();
                cpt++;
            }

            if (lastAirBlock != null && TeleportOnCursorManager.getInstance().canTp(receiver.getUniqueId())) {
                Location toTeleport = lastAirBlock.getLocation();
                toTeleport.setPitch(receiver.getLocation().getPitch());
                toTeleport.setYaw(receiver.getLocation().getYaw());
                if (SCore.isFolia())
                    receiver.teleportAsync(toTeleport);
                else
                    receiver.teleport(toTeleport);
            }

        } catch (Exception ignored) {
        }
    }

    public boolean isAirBlock(Block block) {
        Material mat = block.getType();
        return (SCore.is1v16Plus() && mat.isAir()) || (!SCore.is1v16Plus() && mat.equals(Material.AIR));
    }

    @Override
    public Optional<String> verify(List<String> args, boolean isFinalVerification) {
        if (args.size() >= 1) {
            ArgumentChecker ac2 = checkInteger(args.get(0), isFinalVerification, getTemplate());
            if (!ac2.isValid()) return Optional.of(ac2.getError());
        }

        return Optional.empty();
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("UNSAFE_TELEPORT_ON_CURSOR");
        names.add("UNSAFE_TELEPORTONCURSOR");
        return names;
    }

    @Override
    public String getTemplate() {
        return "UNSAFE_TELEPORT_ON_CURSOR [maxRange]";
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
