package com.ssomar.score.commands.runnable.item.commands;

import com.ssomar.score.SsomarDev;
import com.ssomar.score.commands.runnable.ActionInfo;
import com.ssomar.score.commands.runnable.CommmandThatRunsCommand;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.item.ItemCommand;
import com.ssomar.score.utils.placeholders.StringPlaceholder;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.ssomar.score.commands.runnable.player.commands.If.evaluateCondition;

public class If extends ItemCommand {
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
    public void run(@Nullable Player launcher, ItemStack item, SCommandToExec sCommandToExec) {

        ActionInfo aInfo = sCommandToExec.getActionInfo();
        List<String> args = sCommandToExec.getOtherArgs();

        String condition = args.get(0);
        SsomarDev.testMsg("IF condition: " + condition, true);

        StringPlaceholder sp = aInfo.getSp();
        if (sp == null) sp = new StringPlaceholder();
        sp.setPlayerPlcHldr(launcher.getUniqueId(), aInfo.getSlot());
        sp.setItem(item.getItemMeta().getDisplayName());
        sp.reloadAllPlaceholders();

        List<ItemStack> targets = new ArrayList<>();
        targets.add(item);


        boolean finalResult = evaluateCondition(condition, launcher, sp);

        if (finalResult) {
            CommmandThatRunsCommand.runItemCommands(targets, args.subList(1, args.size()), aInfo, launcher);
        } else {
            SsomarDev.testMsg("IF STOPPED for condition > "+condition, true);
        }
    }
}
