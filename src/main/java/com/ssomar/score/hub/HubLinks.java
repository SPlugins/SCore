package com.ssomar.score.hub;

import com.ssomar.score.languages.messages.TM;
import com.ssomar.score.languages.messages.Text;
import com.ssomar.score.utils.strings.StringConverter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

/** Links sent from the Hub: UTM tags for our own sites, and clickable chat lines. */
public final class HubLinks {

    private HubLinks() {
    }

    /** Adds utm_source/utm_medium/utm_campaign to splugins.net links that don't have them yet. */
    public static String tag(String url, String campaign) {
        if (url == null || url.isEmpty()) return url;
        String lower = url.toLowerCase();
        if (!(lower.contains("splugins.net") || lower.contains("ssomar.com"))) return url;
        StringBuilder b = new StringBuilder(url);
        String anchor = "";
        int hash = url.indexOf('#');
        if (hash >= 0) {
            anchor = url.substring(hash);
            b = new StringBuilder(url.substring(0, hash));
        }
        char sep = b.indexOf("?") >= 0 ? '&' : '?';
        if (!lower.contains("utm_source=")) {
            b.append(sep).append("utm_source=minecraft");
            sep = '&';
        }
        if (!lower.contains("utm_medium=")) {
            b.append(sep).append("utm_medium=splugins-hub");
            sep = '&';
        }
        if (!lower.contains("utm_campaign=") && campaign != null && !campaign.isEmpty()) b.append(sep).append("utm_campaign=").append(campaign);
        return b.append(anchor).toString();
    }

    /** Sends a clickable link line in the chat. */
    public static void send(Player p, String label, String url, String campaign) {
        send(p, label, url, campaign, null);
    }

    /** true when the link went to a dialog window (its "Back" button runs {@code back}), false for a chat line */
    public static boolean send(Player p, String label, String url, String campaign, Runnable back) {
        String tagged = tag(url, campaign);
        // Paper 1.21.6+: a native window with an "Open in Browser" button (a server can't open the browser itself)
        if (HubDialogs.available()) {
            try {
                HubDialogs.openUrl(p, label, tagged, back);
                return true;
            } catch (Throwable t) {
                warnOnce(t);
                // fall back to the chat line
            }
        }
        String text = StringConverter.coloredString(TM.g(Text.HUB_LINK_MESSAGE).replace("%label%", StringConverter.decoloredString(label)));
        try {
            TextComponent c = new TextComponent(TextComponent.fromLegacyText(text));
            c.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, tagged));
            p.spigot().sendMessage(c);
        } catch (Throwable t) {
            p.sendMessage(text + " " + tagged);
        }
        return false;
    }

    private static boolean warned;

    private static void warnOnce(Throwable t) {
        if (warned) return;
        warned = true;
        com.ssomar.score.SCore.plugin.getLogger().log(java.util.logging.Level.WARNING, "[SCore Hub] link window unavailable, the chat is used", t);
    }

    public static boolean dialogsAvailable() {
        return HubDialogs.available();
    }

    /** Sends a line that copies {@code toCopy} to the clipboard when clicked (1.15+), else returns false. */
    public static boolean sendCopy(Player p, String line, String toCopy) {
        return sendCopy(p, line, toCopy, null);
    }

    public static boolean sendCopy(Player p, String line, String toCopy, Runnable back) {
        if (HubDialogs.available()) {
            try {
                HubDialogs.copy(p, "SPlugins Hub", line, toCopy, back);
                return true;
            } catch (Throwable t) {
                warnOnce(t);
                // fall back to the chat line
            }
        }
        try {
            ClickEvent.Action copy = ClickEvent.Action.valueOf("COPY_TO_CLIPBOARD");
            TextComponent c = new TextComponent(TextComponent.fromLegacyText(StringConverter.coloredString(line)));
            c.setClickEvent(new ClickEvent(copy, toCopy));
            p.spigot().sendMessage(c);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
