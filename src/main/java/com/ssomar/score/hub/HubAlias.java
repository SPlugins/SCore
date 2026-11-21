package com.ssomar.score.hub;

import com.ssomar.score.SCore;
import com.ssomar.score.screen.ScreenMode;
import com.ssomar.score.utils.logging.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Collections;

/** Adds /splugins (opens the Hub) when no other plugin already uses that command. */
public final class HubAlias {

    private static Command registered;

    private HubAlias() {
    }

    public static void register() {
        if (registered != null) return;
        try {
            CommandMap map = commandMap();
            if (map == null) return;
            if (map.getCommand("splugins") != null) {
                Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7/splugins is already used by another plugin, use &e/score hub");
                return;
            }
            Command cmd = new Command("splugins", "Opens the SPlugins Hub", "/splugins", Collections.<String>emptyList()) {
                @Override
                public boolean execute(CommandSender sender, String label, String[] args) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("The SPlugins Hub can only be opened by a player.");
                        return true;
                    }
                    Player p = (Player) sender;
                    if (!p.hasPermission("score.hub")) {
                        p.sendMessage("§cYou don't have the permission score.hub.");
                        return true;
                    }
                    SPluginsHub.open(p, args.length > 0 ? modeArg(args[0]) : null);
                    return true;
                }
            };
            cmd.setPermission("score.hub");
            map.register("score", cmd);
            registered = cmd;
        } catch (Throwable t) {
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Can't add /splugins (" + t.getClass().getSimpleName() + "), use &e/score hub");
        }
    }

    public static ScreenMode modeArgPublic(String a) {
        return modeArg(a);
    }

    static ScreenMode modeArg(String a) {
        if (a == null) return null;
        switch (a.toLowerCase()) {
            case "chest":
                return ScreenMode.CHEST;
            case "classic":
            case "floating":
                return ScreenMode.FLOATING;
            case "pinned":
                return ScreenMode.PINNED;
            case "auto":
                return ScreenMode.AUTO;
            default:
                return null;
        }
    }

    static CommandMap commandMap() {
        try {
            Method m = Bukkit.getServer().getClass().getMethod("getCommandMap");
            return (CommandMap) m.invoke(Bukkit.getServer());
        } catch (Throwable t) {
            return null;
        }
    }
}
