package com.ssomar.score.commands.score.clear;

import com.ssomar.particles.commands.ShapesManager;
import com.ssomar.score.actionbar.ActionbarHandler;
import com.ssomar.score.commands.runnable.CommandsHandler;
import com.ssomar.score.commands.runnable.player.commands.Bossbar;
import com.ssomar.score.commands.runnable.player.commands.While;
import com.ssomar.score.features.custom.cooldowns.CooldownsManager;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.strings.StringConverter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClearCommand {

    public static void clearCmd(SPlugin sPlugin, CommandSender sender, String[] args) {
        UUID pUUID = null;

        /* Target of the clear */
        if (sender instanceof Player) {
            if (args.length >= 1) {
                boolean isUUID = false;
                try {
                    pUUID = UUID.fromString(args[0]);
                    isUUID = true;
                } catch (Exception e) {
                    isUUID = false;
                }
                if (isUUID) {
                } else if (Bukkit.getPlayer(args[0]) == null) {
                    sender.sendMessage(StringConverter.coloredString("&4" + sPlugin.getNameDesign() + " &cInvalid playername."));
                    return;
                } else pUUID = Bukkit.getPlayer(args[0]).getUniqueId();
            } else pUUID = ((Player) sender).getUniqueId();
        } else {
            if (args.length < 1) {
                sender.sendMessage(StringConverter.coloredString("&4" + sPlugin.getNameDesign() + " &cERROR &6/" + sPlugin.getShortName().toLowerCase() + " clear {UUID or playername}."));
                return;
            }
            boolean isUUID = false;
            try {
                pUUID = UUID.fromString(args[0]);
                isUUID = true;
            } catch (Exception e) {
                isUUID = false;
            }
            if (isUUID) {
            } else if (Bukkit.getPlayer(args[0]) == null) {
                sender.sendMessage(StringConverter.coloredString("&4" + sPlugin.getNameDesign() + " &cInvalid playername."));
                return;
            } else pUUID = Bukkit.getPlayer(args[0]).getUniqueId();
        }

        ClearType clearType = ClearType.ALL;
        if (args.length >= 2) {
            try {
                clearType = ClearType.valueOf(args[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String name = "";
        Player player = Bukkit.getPlayer(pUUID);
        if (player != null) name = player.getName();
        else name = pUUID.toString();

        switch (clearType) {

            case ALL:
                CommandsHandler.getInstance().removeAllDelayedCommands(pUUID);
                CooldownsManager.getInstance().removeCooldownsOf(pUUID);
                if (player != null) {
                    ActionbarHandler.getInstance().removeActionbars(player);
                    Bossbar.getInstance().clearTasks(player);
                    ShapesManager.getInstance().clearRunningShapes(player);
                }
                sender.sendMessage(StringConverter.coloredString("&2" + sPlugin.getNameDesign() + " &aSuccessfully clear the user/entity: &e" + name + " &7&o(all)"));
                break;
            case DELAYED_COMMANDS:
                CommandsHandler.getInstance().removeAllDelayedCommands(pUUID);
                sender.sendMessage(StringConverter.coloredString("&2" + sPlugin.getNameDesign() + " &aSuccessfully clear the user/entity: &e" + name + " &7&o(delayed commands)"));
                break;
            case COOLDOWNS:
                CooldownsManager.getInstance().removeCooldownsOf(pUUID);
                sender.sendMessage(StringConverter.coloredString("&2" + sPlugin.getNameDesign() + " &aSuccessfully clear the user/entity: &e" + name + " &7&o(cooldowns)"));
                break;
            case ACTIONBARS:
                if (player != null) ActionbarHandler.getInstance().removeActionbars(player);
                sender.sendMessage(StringConverter.coloredString("&2" + sPlugin.getNameDesign() + " &aSuccessfully clear the user/entity: &e" + name + " &7&o(actionbars)"));
                break;
            case WHILE:
                While.getInstance().removeWhile(pUUID);
                sender.sendMessage(StringConverter.coloredString("&2" + sPlugin.getNameDesign() + " &aSuccessfully clear the user/entity: &e" + name + " &7&o(while)"));
                break;
            case BOSSBARS:
                if (player != null) Bossbar.getInstance().clearTasks(player);
                sender.sendMessage(StringConverter.coloredString("&2" + sPlugin.getNameDesign() + " &aSuccessfully clear the user/entity: &e" + name + " &7&o(bossbars)"));
                break;
            case PARTICLES:
                if (player != null) ShapesManager.getInstance().clearRunningShapes(player);
                sender.sendMessage(StringConverter.coloredString("&2" + sPlugin.getNameDesign() + " &aSuccessfully clear the user/entity: &e" + name + " &7&o(particles)"));
                break;

        }


    }
}
