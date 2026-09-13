package com.ssomar.score.commands.runnable.block.commands;

import com.ssomar.score.SsomarDev;
import com.ssomar.score.commands.runnable.ActionInfo;
import com.ssomar.score.commands.runnable.CommmandThatRunsCommand;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.block.BlockCommand;
import com.ssomar.score.utils.placeholders.StringPlaceholder;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.ssomar.score.commands.runnable.player.commands.If.evaluateCondition;

public class If extends BlockCommand {
    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("IF");
        return names;
    }


    @Override
    public String getTemplate() {
        return "IF {condition_without_spaces} {command1} <+> {command2} <+> ...";
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
    public void run(@Nullable Player p, @NotNull Block block, SCommandToExec sCommandToExec) {

        ActionInfo aInfo = sCommandToExec.getActionInfo();
        List<String> args = sCommandToExec.getOtherArgs();

        String condition = args.get(0);
        SsomarDev.testMsg("IF condition: " + condition, true);

        StringPlaceholder sp = aInfo.getSp();
        if (sp == null) sp = new StringPlaceholder();
        if (p != null) sp.setPlayerPlcHldr(p.getUniqueId(), aInfo.getSlot());
        sp.setBlockPlcHldr(block);
        sp.reloadAllPlaceholders();

        List<Block> targets = new ArrayList<>();
        targets.add(block);


        boolean finalResult = evaluateCondition(condition, p, sp);

        if (finalResult) {
            CommmandThatRunsCommand.runBlockCommands(targets, args.subList(1, args.size()), aInfo, p);
        } else {
            SsomarDev.testMsg("IF STOPPED for condition > "+condition, true);
        }
    }

    @Override
    public Optional<String> verify(List<String> args, boolean isFinalVerification) {

        if (args.size() < 2) return Optional.of(notEnoughArgs + getTemplate());

        return Optional.empty();
    }
}
