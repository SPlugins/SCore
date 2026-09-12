package com.ssomar.score.commands.runnable.block.commands;

import com.ssomar.score.commands.runnable.ActionInfo;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.block.BlockCommand;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.ssomar.score.commands.runnable.mixed_player_entity.commands.AllMobs.mobAroundExecution;

public class AllMobs extends BlockCommand {
    public AllMobs(){
        setCanExecuteCommands(true);
    }
    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("ALL_MOBS");
        return names;
    }

    @Override
    public String getTemplate() {
        return "ALL_MOBS {Your commands here}";
    }

    @Override
    public ChatColor getColor() {
        return null;
    }

    @Override
    public ChatColor getExtraColor() {
        return null;
    }

    @Override
    public Optional<String> verify(List<String> args, boolean isFinalVerification) {
        String error = "";

        String around = "ALL_MOBS {Your commands here}";
        if (args.size() < 1) error = notEnoughArgs + around;

        return error.isEmpty() ? Optional.empty() : Optional.of(error);
    }


    @Override
    public void run(@Nullable Player p, @NotNull Block block, SCommandToExec sCommandToExec) {
        List<String> args = sCommandToExec.getOtherArgs();
        ActionInfo aInfo = sCommandToExec.getActionInfo();
        mobAroundExecution(block.getLocation(), null, false, args, aInfo);

    }
}
