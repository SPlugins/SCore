package com.ssomar.score.commands.runnable.player.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.api.executableitems.ExecutableItemsAPI;
import com.ssomar.score.api.executableitems.config.ExecutableItemInterface;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.player.PlayerCommand;
import com.ssomar.score.features.custom.activators.activator.SActivator;
import com.ssomar.score.features.custom.cooldowns.CooldownsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * EICOOLDOWN_RESET {PLAYER} {ID} [optional activator]
 * Removes the cooldown of an ExecutableItem (all its activators, or only the given one) for a player.
 * EICOOLDOWN can only add a cooldown: the longest one always wins, so it can't shorten or remove one.
 */
public class EICooldownReset extends PlayerCommand {

    @Override
    public void run(Player p, Player receiver, SCommandToExec sCommandToExec) {
        List<String> args = sCommandToExec.getOtherArgs();
        Player player = Bukkit.getPlayerExact(args.get(0));
        if (player == null) return;
        String id = args.get(1);
        String activatorId = args.size() >= 3 ? args.get(2) : null;

        List<ExecutableItemInterface> eiAffected = new ArrayList<>();
        if (id.equalsIgnoreCase("all")) {
            eiAffected.addAll(ExecutableItemsAPI.getExecutableItemsManager().getAllExecutableItems());
        } else {
            ExecutableItemsAPI.getExecutableItemsManager().getExecutableItem(id).ifPresent(eiAffected::add);
        }

        for (ExecutableItemInterface ei : eiAffected) {
            boolean cleared = false;
            for (SActivator activator : ei.getActivators().getActivators().values()) {
                if (activatorId != null && !activatorId.equalsIgnoreCase(activator.getId())) continue;
                CooldownsManager.getInstance().clearCooldown("EI:" + ei.getId() + ":" + activator.getId(), player.getUniqueId());
                cleared = true;
            }
            /* Also remove the vanilla visual cooldown (enableVisualCooldown) */
            if (cleared && SCore.is1v21v2Plus()) {
                try {
                    ItemStack item = ei.buildItem(1, Optional.empty());
                    if (item != null) player.setCooldown(item, 0);
                } catch (Throwable ignored) {
                }
            }
        }
    }

    @Override
    public Optional<String> verify(List<String> args, boolean isFinalVerification) {
        if (args.size() < 2) return Optional.of(notEnoughArgs + getTemplate());
        return Optional.empty();
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("EICOOLDOWN_RESET");
        return names;
    }

    @Override
    public String getTemplate() {
        return "EICOOLDOWN_RESET {PLAYER} {ID} [optional activator]";
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
