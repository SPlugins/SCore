package com.ssomar.score.commands.runnable.mixed_player_entity.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.mixed_player_entity.MixedCommand;
import com.ssomar.score.commands.runnable.player.events.StunEvent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StunEnable extends MixedCommand {

    @Override
    public void run(Player p, Entity receiver, SCommandToExec sCommandToExec) {
        if(!(receiver instanceof LivingEntity)) return;
        LivingEntity livingReceiver = (LivingEntity) receiver;

        Location correctAnimation = receiver.getLocation();
        if(receiver instanceof Player) {
            correctAnimation.setPitch(-10);
            livingReceiver.setGliding(true);
        }
        else {
            correctAnimation.setPitch(45F);
            livingReceiver.setAI(false);
        }
        if (SCore.isFolia())
            receiver.teleportAsync(correctAnimation);
        else
            receiver.teleport(correctAnimation);
        StunEvent.stunPlayers.put(receiver.getUniqueId(), false);
        Runnable runnable3 = new Runnable() {
            @Override
            public void run() {
                if (StunEvent.stunPlayers.containsKey(receiver.getUniqueId()))
                    StunEvent.stunPlayers.put(receiver.getUniqueId(), true);
            }
        };
        SCore.schedulerHook.runTask(runnable3, 20);
    }


    @Override
    public Optional<String> verify(List<String> args, boolean isFinalVerification) {
        return Optional.empty();
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("STUN_ENABLE");
        return names;
    }

    @Override
    public String getTemplate() {
        return "STUN_ENABLE";
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