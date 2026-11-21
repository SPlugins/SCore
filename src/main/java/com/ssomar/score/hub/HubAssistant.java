package com.ssomar.score.hub;

import com.ssomar.score.SCore;
import com.ssomar.score.ai.AiAnswer;
import com.ssomar.score.ai.AiAssistant;
import com.ssomar.score.ai.AiBackups;
import com.ssomar.score.ai.AiChange;
import com.ssomar.score.ai.AiLink;
import com.ssomar.score.ai.LineDiff;
import org.json.JSONObject;
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
import java.util.function.Consumer;

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
        AiBackups.Entry last = p.hasPermission(AiAssistant.WRITE_PERMISSION) ? AiBackups.last() : null;
        if (last != null)
            rows.add(new SPluginsHub.Row(TM.g(Text.AI_UNDO).replace("%target%", last.target()), "CLOCK", SPluginsHub.lore(TM.g(Text.HUB_CONFIRM)),
                    SPluginsHub.action(SPluginsHub.Action.Type.AI_UNDO, null, true), false));
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
            case "AI_UNDO":
                undo(p);
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
        public void confirm(Player p, AiChange change, Consumer<Boolean> decide) {
            String id = UUID.randomUUID().toString().substring(0, 8);
            PENDING.put(id, decide);
            LineDiff d = LineDiff.of(change.getBefore(), change.getAfter(), 2, 80);
            if (HubLinks.dialogsAvailable()) {
                ScreenSession cur = com.ssomar.score.screen.Screens.get(p);
                if (cur != null) cur.holdClicksUntilLookAway();
                try {
                    HubDialogs.change(p, change, d, () -> decide(p, id, true), () -> decide(p, id, false));
                    return;
                } catch (Throwable t) {
                    SCore.plugin.getLogger().warning("[SCore AI] change window unavailable, the chat is used: " + t);
                }
            }
            chatChange(p, id, change, d);
        }

        @Override
        public void applied(Player p, AiChange change, JSONObject result) {
            SPluginsHub.refreshIfOpen(p);
            if (result.optBoolean("expired", false)) p.sendMessage(StringConverter.coloredString(TM.g(Text.AI_EXPIRED)));
            else if (result.optBoolean("applied", false))
                p.sendMessage(StringConverter.coloredString(TM.g(Text.AI_APPLIED).replace("%target%", change.getTarget())));
            else p.sendMessage(StringConverter.coloredString(TM.g(Text.AI_ERROR).replace("%error%", result.optString("reason", "?"))));
        }

        @Override
        public void error(Player p, String code, String message, String upgradeUrl) {
            SPluginsHub.refreshIfOpen(p);
            String line = "quota".equalsIgnoreCase(code) ? TM.g(Text.AI_QUOTA) : TM.g(Text.AI_ERROR).replace("%error%", message);
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

    /* ------------------------------------------------------------------ changes */

    /** changes waiting for the admin's answer, by short id (window buttons or /score hub ai apply|refuse <id>) */
    private static final Map<String, Consumer<Boolean>> PENDING = new ConcurrentHashMap<>();

    static void decide(Player p, String id, boolean yes) {
        Consumer<Boolean> d = PENDING.remove(id);
        if (d == null) {
            p.sendMessage(StringConverter.coloredString(TM.g(Text.AI_EXPIRED)));
            return;
        }
        if (!yes) p.sendMessage(StringConverter.coloredString(TM.g(Text.AI_REFUSED)));
        d.accept(yes);
        if (!yes) SPluginsHub.refreshIfOpen(p);
    }

    static void chatChange(Player p, String id, AiChange c, LineDiff d) {
        p.sendMessage(StringConverter.coloredString("&b&l" + StringConverter.decoloredString(TM.g(Text.AI_CHANGE_TITLE))));
        String head = c.isCreation() ? TM.g(Text.AI_CHANGE_NEW).replace("%target%", c.getTarget())
                : TM.g(Text.AI_CHANGE_EDIT).replace("%target%", c.getTarget()).replace("%added%", "" + d.added).replace("%removed%", "" + d.removed);
        p.sendMessage(StringConverter.coloredString(head));
        if (!c.getSummary().isEmpty()) p.sendMessage("§f" + c.getSummary());
        int n = 0;
        for (LineDiff.Line l : d.lines) {
            if (++n > 30) {
                p.sendMessage("§8  …");
                break;
            }
            p.sendMessage((l.kind == '+' ? "§a+ " : l.kind == '-' ? "§c- " : l.kind == '…' ? "§8  … " : "§7  ") + l.text);
        }
        p.sendMessage(StringConverter.coloredString(TM.g(Text.AI_CHANGE_NOTE)));
        try {
            TextComponent yes = new TextComponent(TextComponent.fromLegacyText(StringConverter.coloredString("&a&l[✔ " + StringConverter.decoloredString(TM.g(Text.AI_APPLY)) + "]")));
            yes.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/score hub ai apply " + id));
            TextComponent no = new TextComponent(TextComponent.fromLegacyText(StringConverter.coloredString("   &c&l[✘ " + StringConverter.decoloredString(TM.g(Text.AI_REFUSE)) + "]")));
            no.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/score hub ai refuse " + id));
            yes.addExtra(no);
            p.spigot().sendMessage(yes);
        } catch (Throwable t) {
            p.sendMessage("/score hub ai apply " + id + "   /score hub ai refuse " + id);
        }
    }

    static void undo(Player p) {
        if (!p.hasPermission(AiAssistant.WRITE_PERMISSION)) {
            p.sendMessage(StringConverter.coloredString(TM.g(Text.AI_NO_WRITE)));
            return;
        }
        try {
            AiBackups.Entry e = AiBackups.undoLast(p.getName());
            p.sendMessage(StringConverter.coloredString(e == null ? TM.g(Text.AI_UNDO_NONE) : TM.g(Text.AI_UNDONE).replace("%target%", e.target())));
        } catch (Exception ex) {
            p.sendMessage(StringConverter.coloredString(TM.g(Text.AI_ERROR).replace("%error%", String.valueOf(ex.getMessage()))));
        }
        SPluginsHub.refreshIfOpen(p);
    }

    /** /score hub ai apply|refuse <id>, /score hub ai undo */
    public static void command(Player p, String[] args) {
        String sub = args.length > 1 ? args[1].toLowerCase(java.util.Locale.ROOT) : "";
        switch (sub) {
            case "apply":
            case "refuse":
                if (args.length < 3) return;
                if (sub.equals("apply") && !p.hasPermission(AiAssistant.WRITE_PERMISSION)) {
                    p.sendMessage(StringConverter.coloredString(TM.g(Text.AI_NO_WRITE)));
                    return;
                }
                decide(p, args[2], sub.equals("apply"));
                break;
            case "undo":
                undo(p);
                break;
            default:
                p.sendMessage(StringConverter.coloredString("&7/score hub ai undo"));
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
