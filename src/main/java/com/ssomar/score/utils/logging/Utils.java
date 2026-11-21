package com.ssomar.score.utils.logging;

import com.ssomar.score.SCore;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.strings.StringConverter;
import org.bukkit.Bukkit;

public class Utils {

    public static void sendConsoleMsg(String msg) {
        try {
            if (msg != null && msg.contains("ERROR")) com.ssomar.score.hub.LoadErrorCollector.record(StringConverter.decoloredString(msg));
        } catch (Throwable ignored) {
        }
        if (SCore.is1v16Plus())
            Bukkit.getConsoleSender().sendMessage(StringConverter.coloredString(msg));
        else
            Bukkit.getConsoleSender().sendMessage(StringConverter.decoloredString(msg));
    }

    public static void sendConsoleMsg(SPlugin sPlugin, String msg) {
        sendConsoleMsg(msg);
    }

    public static void sendConsoleFlatMsg(SPlugin sPlugin, String msg) {
        Bukkit.getConsoleSender().sendMessage(msg);
    }

}
