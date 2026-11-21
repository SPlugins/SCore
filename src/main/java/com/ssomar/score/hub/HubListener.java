package com.ssomar.score.hub;

import com.ssomar.score.SCore;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.languages.messages.TM;
import com.ssomar.score.languages.messages.Text;
import com.ssomar.score.utils.strings.StringConverter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** Opt-in messages to the admins (score.hub) when they join: Hub reminder and, once, a review request. */
public final class HubListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        GeneralConfig c = GeneralConfig.getInstance();
        if (!c.isHubJoinNotification() && !c.isHubReviewReminder()) return;
        if (!p.hasPermission("score.hub")) return;
        SCore.schedulerHook.runEntityTask(() -> {
            if (!p.isOnline()) return;
            if (c.isHubJoinNotification()) sendRunLine(p, TM.g(Text.HUB_JOIN));
            if (c.isHubReviewReminder() && HubData.takeReviewReminder(c.getHubReviewReminderDays())) sendRunLine(p, TM.g(Text.HUB_REVIEW_REMINDER));
        }, null, p, 60);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        SPluginsHub.forget(e.getPlayer());
    }

    private static void sendRunLine(Player p, String line) {
        String text = StringConverter.coloredString(line);
        try {
            TextComponent tc = new TextComponent(TextComponent.fromLegacyText(text));
            tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/score hub"));
            p.spigot().sendMessage(tc);
        } catch (Throwable t) {
            p.sendMessage(text);
        }
    }
}
