package com.ssomar.score.hub;

import com.ssomar.score.SCore;
import com.ssomar.score.ai.AiAnswer;
import com.ssomar.score.ai.AiAssistant;
import com.ssomar.score.ai.AiLink;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.languages.messages.TM;
import com.ssomar.score.languages.messages.Text;
import com.ssomar.score.screen.ScreenSession;
import com.ssomar.score.utils.strings.StringConverter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Assistant tab of the Hub: link the server to a splugins.net account, ask questions (with the server context
 * and the read tools), answers in a window (Paper 1.21.6+) or in the chat.
 */
public final class HubAssistant {

    private HubAssistant() {
    }

    public static boolean enabled() {
        return GeneralConfig.getInstance().isAiEnabled();
    }

    static List<SPluginsHub.Row> rows(Player p) {
        List<SPluginsHub.Row> rows = new ArrayList<>();
        SPluginsHub.Action none = SPluginsHub.action(SPluginsHub.Action.Type.NONE, null, false);
        if (!enabled()) {
            rows.add(new SPluginsHub.Row(TM.g(Text.AI_INTRO), "ENCHANTED_BOOK", new ArrayList<String>(), none, true));
            rows.add(new SPluginsHub.Row(TM.g(Text.AI_OFF), "REDSTONE_TORCH", new ArrayList<String>(), none, false));
            return rows;
        }
        if (!AiLink.isLinked()) {
            rows.add(new SPluginsHub.Row(TM.g(Text.AI_INTRO), "ENCHANTED_BOOK", new ArrayList<String>(), none, false));
            rows.add(new SPluginsHub.Row(AiLink.isLinking() ? TM.g(Text.AI_LINK_WAIT) : TM.g(Text.AI_LINK), "NAME_TAG",
                    SPluginsHub.lore(TM.g(Text.AI_LINK_LORE)), SPluginsHub.action(SPluginsHub.Action.Type.AI_LINK, null, false), true));
            return rows;
        }
        rows.add(new SPluginsHub.Row(TM.g(Text.AI_LINKED_ROW).replace("%account%", AiLink.account()), "PLAYER_HEAD", new ArrayList<String>(), none, false));
        rows.add(new SPluginsHub.Row(AiAssistant.isBusy(p) ? TM.g(Text.AI_THINKING) : TM.g(Text.AI_ASK), "WRITABLE_BOOK",
                SPluginsHub.lore(TM.g(Text.AI_ASK_LORE)), SPluginsHub.action(SPluginsHub.Action.Type.AI_ASK, null, false), true));
        if (AiAssistant.hasConversation(p))
            rows.add(new SPluginsHub.Row(TM.g(Text.AI_NEW), "BOOK", new ArrayList<String>(), SPluginsHub.action(SPluginsHub.Action.Type.AI_NEW, null, false), false));
        int errors = LoadErrorCollector.all().size();
        if (errors > 0)
            rows.add(new SPluginsHub.Row(TM.g(Text.AI_ERRORS).replace("%count%", "" + errors), "REDSTONE", SPluginsHub.lore(TM.g(Text.AI_ASK_LORE)),
                    SPluginsHub.action(SPluginsHub.Action.Type.AI_ERRORS, null, false), false));
        rows.add(new SPluginsHub.Row(TM.g(Text.AI_UNLINK), "BARRIER", SPluginsHub.lore(TM.g(Text.HUB_CONFIRM)),
                SPluginsHub.action(SPluginsHub.Action.Type.AI_UNLINK, null, true), false));
        return rows;
    }

    static void run(Player p, ScreenSession s, String type, String plugin) {
        switch (type) {
            case "AI_LINK":
                link(p, s);
                break;
            case "AI_ASK":
                if (AiAssistant.isBusy(p)) return;
                askWindow(p, s, "", null);
                break;
            case "AI_NEW":
                AiAssistant.newConversation(p);
                SPluginsHub.refreshIfOpen(p);
                break;
            case "AI_ERRORS":
                if (AiAssistant.isBusy(p)) return;
                String name = plugin == null ? "" : (KnownPlugin.byKey(plugin) == null ? plugin : KnownPlugin.byKey(plugin).getPluginName());
                String q = plugin == null ? TM.g(Text.AI_ERRORS_QUESTION_ALL) : TM.g(Text.AI_ERRORS_QUESTION).replace("%plugin%", name);
                askWindow(p, s, StringConverter.decoloredString(q), plugin);
                break;
            case "AI_UNLINK":
                AiLink.unlink(() -> SPluginsHub.refreshIfOpen(p));
                SPluginsHub.refreshIfOpen(p);
                break;
            default:
                break;
        }
    }

    /* ------------------------------------------------------------------ link */

    private static void link(Player p, ScreenSession s) {
        if (AiLink.isLinking()) return;
        AiLink.start(new AiLink.Listener() {
            @Override
            public void code(String code, String url, int expiresIn) {
                if (!p.isOnline()) return;
                String line = TM.g(Text.AI_LINK_CODE).replace("%url%", url).replace("%code%", code);
                if (HubLinks.dialogsAvailable()) {
                    s.holdClicksUntilLookAway();
                    HubDialogs.linkCode(p, line, url + "?code=" + code, () -> SPluginsHub.back(p));
                } else {
                    p.sendMessage(StringConverter.coloredString(line));
                    HubLinks.send(p, "splugins.net/link", url + "?code=" + code, "assistant");
                }
                SPluginsHub.refreshIfOpen(p);
            }

            @Override
            public void linked(String account) {
                if (!p.isOnline()) return;
                p.sendMessage(StringConverter.coloredString(TM.g(Text.AI_LINKED).replace("%account%", account)));
                SPluginsHub.refreshIfOpen(p);
            }

            @Override
            public void failed(String reason) {
                if (!p.isOnline()) return;
                p.sendMessage(StringConverter.coloredString(TM.g(Text.AI_ERROR).replace("%error%", reason)));
                SPluginsHub.refreshIfOpen(p);
            }
        });
    }

    /* ------------------------------------------------------------------ question */

    /** players who type their question in the chat (no dialogs) */
    private static final Map<UUID, String> TYPING = new ConcurrentHashMap<>();

    static void askWindow(Player p, ScreenSession s, String prefill, String focus) {
        if (HubLinks.dialogsAvailable()) {
            if (s != null) s.holdClicksUntilLookAway();
            try {
                HubDialogs.question(p, prefill, text -> ask(p, text, focus), () -> SPluginsHub.back(p));
                return;
            } catch (Throwable t) {
                SCore.plugin.getLogger().warning("[SCore AI] question window unavailable, the chat is used: " + t);
            }
        }
        if (s != null) s.close();
        if (prefill != null && !prefill.isEmpty()) {
            ask(p, prefill, focus);
            return;
        }
        TYPING.put(p.getUniqueId(), focus == null ? "" : focus);
        p.sendMessage(StringConverter.coloredString(TM.g(Text.AI_TYPE_QUESTION)));
    }

    static void ask(Player p, String question, String focus) {
        if (!AiAssistant.ask(p, question, focus == null || focus.isEmpty() ? null : focus, VIEW)) {
            if (!AiLink.isLinked()) p.sendMessage(StringConverter.coloredString(TM.g(Text.AI_ERROR).replace("%error%", "unlinked")));
        }
        SPluginsHub.refreshIfOpen(p);
    }

    static final AiAssistant.View VIEW = new AiAssistant.View() {
        @Override
        public void thinking(Player p) {
            p.sendMessage(StringConverter.coloredString(TM.g(Text.AI_THINKING)));
        }

        @Override
        public void answer(Player p, String markdown) {
            AiAnswer a = AiAnswer.parse(markdown);
            SPluginsHub.refreshIfOpen(p);
            if (HubLinks.dialogsAvailable()) {
                ScreenSession cur = com.ssomar.score.screen.Screens.get(p);
                if (cur != null) cur.holdClicksUntilLookAway();
                try {
                    HubDialogs.answer(p, a, () -> askWindow(p, null, "", null), () -> SPluginsHub.back(p));
                    return;
                } catch (Throwable t) {
                    SCore.plugin.getLogger().warning("[SCore AI] answer window unavailable, the chat is used: " + t);
                }
            }
            chatAnswer(p, a);
        }

        @Override
        public void error(Player p, String code, String message, String upgradeUrl) {
            SPluginsHub.refreshIfOpen(p);
            String line = "quota".equals(code) ? TM.g(Text.AI_QUOTA) : TM.g(Text.AI_ERROR).replace("%error%", message);
            p.sendMessage(StringConverter.coloredString(line));
            if (upgradeUrl != null && !upgradeUrl.isEmpty()) HubLinks.send(p, TM.g(Text.AI_UPGRADE), upgradeUrl, "assistant");
        }
    };

    static void chatAnswer(Player p, AiAnswer a) {
        p.sendMessage(StringConverter.coloredString("&b&l" + StringConverter.decoloredString(TM.g(Text.AI_DIALOG_TITLE))));
        for (String para : a.paragraphs) for (String line : para.split("\n")) p.sendMessage("§f" + line);
        int n = 0;
        for (AiAnswer.Code c : a.codes) {
            n++;
            if (!HubLinks.sendCopy(p, TM.g(Text.AI_COPY).replace("%n%", "" + n), c.text)) p.sendMessage(c.text);
        }
        for (AiAnswer.Link l : a.links) {
            try {
                TextComponent t = new TextComponent(TextComponent.fromLegacyText(StringConverter.coloredString("&7➜ &b&n" + l.label)));
                t.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, l.url));
                p.spigot().sendMessage(t);
            } catch (Throwable ignored) {
                p.sendMessage(l.url);
            }
        }
    }

    /** chat input (servers without dialogs) */
    public static final class ChatInput implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        @SuppressWarnings("deprecation")
        public void onChat(AsyncPlayerChatEvent e) {
            String focus = TYPING.remove(e.getPlayer().getUniqueId());
            if (focus == null) return;
            e.setCancelled(true);
            String msg = e.getMessage();
            if (msg.equalsIgnoreCase("cancel")) return;
            Player p = e.getPlayer();
            SCore.schedulerHook.runEntityTask(() -> ask(p, msg, focus), null, p, 0L);
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent e) {
            TYPING.remove(e.getPlayer().getUniqueId());
        }
    }
}
