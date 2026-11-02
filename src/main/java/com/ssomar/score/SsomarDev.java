package com.ssomar.score;

import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Set;

public class SsomarDev {

    /* This setting is turned to false when building the plugin for production */
    private static boolean enableDebug = true;


    // version 1 of the testmsg
    public static void testMsg(String message, boolean isActiveDebug) {
       testMsg(message, isActiveDebug,null);
    }

    /**
     * Currently used by Special70. Do whatever you want as its mainly used for checking code flows.
     * @param message
     * @param groupType
     */
    public static void testMsg(String message, boolean isActiveDebug, DebugMsgGroups groupType) {
        if (enableDebug) {
            if(isActiveDebug){
                try {
                    Bukkit.getPlayer("Ssomar").sendMessage(message);
                } catch (Exception ignored) {}

                try {
                    Bukkit.getPlayer("Moccains").sendMessage(message);
                } catch (Exception ignored) {}
            }

            if(groupType != null) {
                if (!forceGroupWhitelist().contains(groupType)) return;
                testMsg(message, true, null);
            }
        }
    }

    // Used to force enable specific debug groups - let empty on GitHub
    public static Set<DebugMsgGroups> forceGroupWhitelist(){
        Set<DebugMsgGroups> whitelist = new HashSet<>();
        //whitelist.add(DebugMsgGroups._1);
        return whitelist;
    }

    /**
     * Used to group up specific debug messages to allow the dev to enable/disable at will.
     * <br/>
     * Details for each enum is written in SCore Documentation. Contact Special70 for further details
     */
    public enum DebugMsgGroups {
        _1
    }

}
