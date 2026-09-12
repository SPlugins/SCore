package com.ssomar.score.commands.runnable.block.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.ActionInfo;
import com.ssomar.score.commands.runnable.CommmandThatRunsCommand;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.block.BlockCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AllPlayers extends BlockCommand {

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("ALL_PLAYERS");
        return names;
    }

    @Override
    public String getTemplate() {
        return "ALL_PLAYERS {Your commands here}";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.LIGHT_PURPLE;
    }

    @Override
    public ChatColor getExtraColor() {
        return ChatColor.DARK_PURPLE;
    }


    @Override
    public void run(@Nullable Player p, @NotNull Block block, SCommandToExec sCommandToExec) {
        ActionInfo aInfo = sCommandToExec.getActionInfo();
        List<String> args = sCommandToExec.getOtherArgs();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                CommmandThatRunsCommand.runPlayerCommands(Bukkit.getOnlinePlayers(), args, aInfo);
            }
        };
        SCore.schedulerHook.runTask(runnable, 0);

    }
}
