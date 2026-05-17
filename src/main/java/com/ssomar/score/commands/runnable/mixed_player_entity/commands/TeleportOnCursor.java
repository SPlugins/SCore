package com.ssomar.score.commands.runnable.mixed_player_entity.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.SsomarDev;
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
public class TeleportOnCursor extends MixedCommand {

    @Override
    public void run(Player p, Entity receiver, SCommandToExec sCommandToExec) {
        List<String> args = sCommandToExec.getOtherArgs();

        if(!(receiver instanceof LivingEntity)) return;
        LivingEntity livingReceiver = (LivingEntity) receiver;

        int amount = 200;
        boolean acceptAir = false;
        if (args.size() >= 1) {
            amount = Double.valueOf(args.get(0)).intValue();
        }
        if (args.size() >= 2) {
            acceptAir = Boolean.parseBoolean(args.get(1));
        }
        try {

            Location eyeLoc = livingReceiver.getEyeLocation();
            Vector eyeVec = eyeLoc.getDirection();
            Vector checkVec = eyeVec.clone().multiply(2);
            Location checkLoc = eyeLoc.clone().add(checkVec);

            Block faceBlock = checkLoc.add(eyeVec.multiply(0.5)).getBlock();
            if(!isAirBlock(faceBlock)) return;

            int cpt = 2;
            /* Invalid if its a cheat movement */
            boolean invalid = false;
            while (cpt < amount && isAirBlock((checkLoc = eyeLoc.clone().add(eyeVec.clone().multiply(cpt))).getBlock())) {
                if (!isAirBlock(checkLoc.clone().add(0, 1, 0).getBlock())) {
                    invalid = true;
                    break;
                }
                cpt++;
            }

            if (invalid) return;

            Material mat = checkLoc.getBlock().getType();
            boolean validTp = (acceptAir && isAirBlock(checkLoc.getBlock()) || (!isAirBlock(checkLoc.getBlock()) && mat.isBlock() && !mat.equals(Material.LAVA)));

            if (validTp && isAirBlock(checkLoc.clone().add(0, 1, 0).getBlock()) && isAirBlock(checkLoc.clone().add(0, 2, 0).getBlock())) {
                if (TeleportOnCursorManager.getInstance().canTp(receiver.getUniqueId())) {
                    if(SCore.isFolia())
                        receiver.teleportAsync(checkLoc.add(0, 1, 0));
                    else
                        receiver.teleport(checkLoc.add(0, 1, 0));
                    SsomarDev.testMsg("TeleportOnCursor command executed on entity: " + receiver.getType() + " at location: " + checkLoc, true);
                }

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

        if (args.size() >= 2) {
            ArgumentChecker ac2 = checkBoolean(args.get(1), isFinalVerification, getTemplate());
            if (!ac2.isValid()) return Optional.of(ac2.getError());
        }

        return Optional.empty();
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("TELEPORT_ON_CURSOR");
        names.add("TELEPORTONCURSOR");
        return names;
    }

    @Override
    public String getTemplate() {
        return "TELEPORT_ON_CURSOR [maxRange} [acceptAir true or false]";
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
