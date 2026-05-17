package com.ssomar.score.commands.runnable.mixed_player_entity.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.ArgumentChecker;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.mixed_player_entity.MixedCommand;
import com.ssomar.score.utils.numbers.NTools;
import com.ssomar.score.utils.scheduler.ScheduledTask;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/* SPIN {duration ticks} {velocity} */
public class Spin extends MixedCommand {

    @Override
    public void run(Player p, Entity receiver, SCommandToExec sCommandToExec) {
        List<String> args = sCommandToExec.getOtherArgs();

        Integer duration = NTools.getInteger(args.get(0)).get();
        Float velocity = NTools.getFloat(args.get(1)).get();

        AtomicReference<ScheduledTask> task = new AtomicReference<>();

        Runnable runnable = new Runnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= duration) {
                    task.get().cancel();
                    return;
                }
                if (SCore.isFolia())
                    receiver.teleportAsync(receiver.getLocation().setDirection(receiver.getLocation().getDirection()
                            .rotateAroundY(Math.toRadians(velocity))));
                else
                    receiver.teleport(receiver.getLocation().setDirection(receiver.getLocation().getDirection()
                        .rotateAroundY(Math.toRadians(velocity))));
                ticks++;
            }
        };
        task.set(SCore.schedulerHook.runRepeatingTask(runnable, 0, 1));

        //PacketEventsTool.sendTest((Player) receiver);
    }

    @Override
    public Optional<String> verify(List<String> args, boolean isFinalVerification) {
        if (args.size() < 2) return Optional.of(notEnoughArgs + getTemplate());

        ArgumentChecker ac = checkInteger(args.get(0), isFinalVerification, getTemplate());
        if (!ac.isValid()) return Optional.of(ac.getError());

        ArgumentChecker ac1 = checkDouble(args.get(1), isFinalVerification, getTemplate());
        if (!ac1.isValid()) return Optional.of(ac1.getError());

        return Optional.empty();
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("SPIN");
        return names;
    }

    @Override
    public String getTemplate() {
        return "SPIN {duration} {velocity}";
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
