package com.ssomar.score.utils.logging;

import com.ssomar.score.SCore;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.strings.StringConverter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Utils {

    public static void sendConsoleMsg(String msg) {
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
