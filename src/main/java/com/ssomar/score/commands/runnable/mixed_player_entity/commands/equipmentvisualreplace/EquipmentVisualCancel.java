package com.ssomar.score.commands.runnable.mixed_player_entity.commands.equipmentvisualreplace;

import com.ssomar.score.commands.runnable.ArgumentChecker;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.mixed_player_entity.MixedCommand;
import com.ssomar.score.utils.emums.BetterEquipmentSlot;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/* TOTEM_ANIMATION */
public class EquipmentVisualCancel extends MixedCommand {

    @Override
    public void run(Player p, Entity receiver, SCommandToExec sCommandToExec) {
        List<String> args = sCommandToExec.getOtherArgs();
        EquipmentSlot slot = BetterEquipmentSlot.getEquipmentSlot(args.get(0)).orElse(EquipmentSlot.HAND);
        EquipmentVisualManager.getInstance().cancel(receiver, slot);
    }

    @Override
    public Optional<String> verify(List<String> args, boolean isFinalVerification) {
        if (args.size() < 1) return Optional.of(notEnoughArgs + getTemplate());

        ArgumentChecker ac = checkEquipmentSlot(args.get(0), isFinalVerification, getTemplate());
        if (!ac.isValid()) return Optional.of(ac.getError());

        return Optional.empty();
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("EQUIPMENT_VISUAL_CANCEL");
        return names;
    }

    @Override
    public String getTemplate() {
        return "EQUIPMENT_VISUAL_CANCEL {EquipmentSlot}";
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
