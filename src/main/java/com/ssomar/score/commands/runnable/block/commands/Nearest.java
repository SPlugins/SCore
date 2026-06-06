package com.ssomar.score.commands.runnable.block.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.ActionInfo;
import com.ssomar.score.commands.runnable.ArgumentChecker;
import com.ssomar.score.commands.runnable.CommmandThatRunsCommand;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.block.BlockCommand;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Nearest extends BlockCommand {

    public Nearest() {
        setCanExecuteCommands(true);
    }


    @Override
    public void run(@Nullable Player p, @NotNull Block block, SCommandToExec sCommandToExec) {
        List<String> args = sCommandToExec.getOtherArgs();
        ActionInfo aInfo = sCommandToExec.getActionInfo();
        Location location = block.getLocation();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                double distance = Double.valueOf(args.get(0));

                Player target = location.getWorld().getPlayers().stream().filter(p -> p.getLocation().getWorld().equals(block.getLocation().getWorld()))
                        .min(Comparator.comparingDouble((p) -> p.getLocation().distanceSquared(location)))
                        .orElse(null);

                if (target == null || target.getLocation().distance(location) > distance) {
                    return;
                }

                if (target.hasMetadata("NPC")) return;
                List<Player> targets = new ArrayList<>();
                targets.add(target);

                CommmandThatRunsCommand.runPlayerCommands(targets, args.subList(1, args.size()), aInfo);

            }
        };
        SCore.schedulerHook.runLocationTask(runnable, block.getLocation(), 0);
    }

    @Override
    public Optional<String> verify(List<String> args, boolean isFinalVerification) {

        if (args.size() < 2) return Optional.of(notEnoughArgs + getTemplate());

        ArgumentChecker ac = checkDouble(args.get(0), isFinalVerification, getTemplate());
        if (!ac.isValid()) return Optional.of(ac.getError());

        return Optional.empty();
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("NEAREST");
        return names;
    }

    @Override
    public String getTemplate() {
        return "NEAREST {max distance} {Your commands here}";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.LIGHT_PURPLE;
    }

    @Override
    public ChatColor getExtraColor() {
        return ChatColor.DARK_PURPLE;
    }
}
