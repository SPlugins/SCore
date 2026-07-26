package com.ssomar.score.commands.runnable.entity.commands;

import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.entity.EntityCommand;
import com.ssomar.score.utils.FrogTadpoleUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SetBaby extends EntityCommand {

    public SetBaby() {
        setNewSettingsMode(true);
    }

    @Override
    public void run(Player p, Entity entity, SCommandToExec sCommandToExec) {
        if (entity.isDead()) return;
        String type = entity.getType().name();
        /* Frogs have no baby form, their baby is the tadpole entity */
        if (type.equals("FROG")) {
            Entity tadpole = FrogTadpoleUtils.frogToTadpole(entity);
            if (sCommandToExec.getActionInfo() != null)
                sCommandToExec.getActionInfo().setEntityUUID(tadpole.getUniqueId());
        } else if (type.equals("TADPOLE")) {
            /* Already a baby, we just reset its growth */
            FrogTadpoleUtils.resetTadpoleAge(entity);
        } else if (entity instanceof Ageable) ((Ageable) entity).setBaby();
    }

    @Override
    public Optional<String> verify(List<String> args, boolean isFinalVerification) {
        return Optional.empty();
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("SET_BABY");
        names.add("SETBABY");
        return names;
    }

    @Override
    public String getTemplate() {
        return "SET_BABY";
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
