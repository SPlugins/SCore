package com.ssomar.score.utils.messages;

import com.ssomar.score.features.custom.conditions.ConditionFeature;
import com.ssomar.score.utils.placeholders.StringPlaceholder;
import com.ssomar.score.utils.strings.StringConverter;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.Serializable;

public class SendMessage implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    @Getter
    @Setter
    private StringPlaceholder sp = new StringPlaceholder();

    /** Passive activators (LOOP): the player did not try to activate anything, so the default "invalid condition"
     *  message is not sent (every loop would repeat it). A message written in the config is still sent. */
    @Getter
    @Setter
    private boolean skipDefaultConditionError = false;

    public static void sendMessageNoPlch(Player p, String s) {
        sendMessageNoPlch((CommandSender) p, s);
    }

    public static void sendMessageNoPlch(CommandSender cs, String s) {
        String prepareMsg = s;
        sendMessageFinal(cs, prepareMsg, true);
    }

    public void sendMessage(Player p, String s) {
        this.sendMessage((CommandSender) p, s, true);
    }

    public void sendMessage(Player p, String s, boolean checkUncoloredEmpty) {
        this.sendMessage((CommandSender) p, s, checkUncoloredEmpty);
    }

    public void sendMessage(CommandSender cs, String s) {
        if (skipDefaultConditionError && ConditionFeature.DEFAULT_ERROR_MESSAGE.equals(s)) return;
        String prepareMsg = s;
        prepareMsg = sp.replacePlaceholder(prepareMsg);
        sendMessageFinal(cs, prepareMsg, true);
    }

    public void sendMessage(CommandSender cs, String s, boolean checkUncoloredEmpty) {
        if (skipDefaultConditionError && ConditionFeature.DEFAULT_ERROR_MESSAGE.equals(s)) return;
        String prepareMsg = s;
        prepareMsg = sp.replacePlaceholder(prepareMsg);
        sendMessageFinal(cs, prepareMsg, checkUncoloredEmpty);
    }

    public static void sendMessageFinal(CommandSender cs, String prepareMsg, boolean checkUncoloredEmpty) {
        if(cs == null) return;
        if (!(prepareMsg.isEmpty() || (checkUncoloredEmpty && StringConverter.decoloredString(prepareMsg).isEmpty()))){
            if(prepareMsg.contains("&") || prepareMsg.contains("§") || (prepareMsg.contains("#") && !prepareMsg.contains("gradient:") && !prepareMsg.contains("color:"))) {
                prepareMsg = StringConverter.coloredString(prepareMsg);
                //SsomarDev.testMsg("send : s = " + prepareMsg, true);
                cs.sendMessage(prepareMsg);
            }
            else {
                try {
                    Audience audience = Audience.audience((Audience) cs);
                    audience.sendMessage(MiniMessage.miniMessage().deserialize(prepareMsg));
                }
                catch (Exception | Error e){
                    prepareMsg = StringConverter.coloredString(prepareMsg);
                    cs.sendMessage(prepareMsg);
                }
            }
        }
    }

    public void resetSp() {
        this.sp = new StringPlaceholder();
    }

}
