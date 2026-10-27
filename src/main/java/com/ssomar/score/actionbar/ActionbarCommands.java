package com.ssomar.score.actionbar;

import com.ssomar.score.SCore;
import com.ssomar.score.configs.messages.Message;
import com.ssomar.score.configs.messages.MessageMain;
import com.ssomar.score.utils.messages.SendMessage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ActionbarCommands {

    private static final SendMessage sm = new SendMessage();

    public static void manageCommand(Player p, String arg) {

        List<Player> hideActionbar = ActionbarHandler.getInstance().getHideActionbar();

        if (arg.equalsIgnoreCase("on")) {
            if (hideActionbar.contains(p)) {
                hideActionbar.remove(p);
                sm.sendMessage(p, MessageMain.getInstance().getMessage(SCore.plugin, Message.SET_ACTIONBAR_ON));
            } else sm.sendMessage(p, MessageMain.getInstance().getMessage(SCore.plugin, Message.HAVE_ACTIONBAR_ON));
        } else if (arg.equalsIgnoreCase("off")) {
            if (hideActionbar.contains(p))
                sm.sendMessage(p, MessageMain.getInstance().getMessage(SCore.plugin, Message.HAVE_ACTIONBAR_OFF));
            else {
                sm.sendMessage(p, MessageMain.getInstance().getMessage(SCore.plugin, Message.SET_ACTIONBAR_OFF));
                hideActionbar.add(p);
            }
        } else return;

        ActionbarHandler.getInstance().setHideActionbar(hideActionbar);
    }
}
