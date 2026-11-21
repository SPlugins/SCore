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
        com.ssomar.score.ai.AiHistory.Exchange lastEx = com.ssomar.score.ai.AiHistory.last(p.getUniqueId());
        if (lastEx != null && !AiAssistant.isBusy(p)) {
            String q = lastEx.question.replace('\n', ' ');
            if (q.length() > 60) q = q.substring(0, 60) + "…";
            rows.add(new SPluginsHub.Row(TM.g(Text.AI_RESUME), "KNOWLEDGE_BOOK", SPluginsHub.lore(TM.g(Text.AI_RESUME_LORE).replace("%question%", q)),
                    SPluginsHub.action(SPluginsHub.Action.Type.AI_RESUME, null, false), true));
        }
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
            case "AI_RESUME":
                resume(p, s);
                break;
            case "AI_COPY":
                copy(p, s, plugin);
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
        // FLOATING / PINNED: the conversation in the panel, the question and "thinking..." under it
        if (!SPluginsHub.showConversation(p)) SPluginsHub.refreshIfOpen(p);
    }

    /* ------------------------------------------------------------------ conversation in the panel */

    static final double LINE_H = 0.058, LINE_STEP = 0.0625, COL_W = 0.52, BTN_H = 0.085, BTN_STEP = 0.095;

    /**
     * The conversation in the Hub panel (FLOATING / PINNED): the text on the left, page by page, the actions of the
     * last answer on the right (Reply, Copy config, links, New conversation, Back).
     *
     * @return the number of pages
     */
    static int conversation(Player p, SPluginsHub.State st, List<com.ssomar.score.screen.ScreenWidget> w) {
        List<com.ssomar.score.ai.AiHistory.Exchange> ex = com.ssomar.score.ai.AiHistory.exchanges(p.getUniqueId());
        List<String> questions = new ArrayList<>(), answers = new ArrayList<>();
        for (com.ssomar.score.ai.AiHistory.Exchange e : ex) {
            questions.add(e.question);
            answers.add(e.answer);
        }
        String pending = AiAssistant.pendingQuestion(p);
        if (pending != null) questions.add(pending);
        double textW = SPluginsHub.W - 0.08 - COL_W - 0.04;
        double scale = 2.4 * LINE_H;
        int maxPx = (int) Math.floor(textW * 0.9 / (com.ssomar.score.screen.internal.display.TextMetrics.PX * scale));
        List<ConversationLines.Line> lines = ConversationLines.of(questions, answers,
                "§7§o" + StringConverter.decoloredString(TM.g(Text.AI_THINKING)), maxPx);
        double top = SPluginsHub.rowTop(), bottom = SPluginsHub.barTop() - 0.01;
        int perPage = Math.max(4, (int) Math.floor((bottom - top) / LINE_STEP));
        int pages = Math.max(1, (lines.size() + perPage - 1) / perPage);
        if (st.aiJump) {
            st.aiJump = false;
            int lastEx = questions.size() - 1;
            int first = 0;
            for (int i = 0; i < lines.size(); i++) if (lines.get(i).exchange == lastEx) { first = i; break; }
            st.page = first / perPage;
        }
        st.page = Math.max(0, Math.min(st.page, pages - 1));
        int from = st.page * perPage;
        for (int i = from; i < Math.min(lines.size(), from + perPage); i++) {
            w.add(com.ssomar.score.screen.ScreenWidget.builder("ai:line:" + (i - from), com.ssomar.score.screen.WidgetKind.LABEL)
                    .at(0.04, top + (i - from) * LINE_STEP).size(textW, LINE_H).text(lines.get(i).text).rawText().hiddenInChest().build());
        }
        // actions of the last answer
        double x = SPluginsHub.W - 0.04 - COL_W, y = top;
        boolean busy = AiAssistant.isBusy(p);
        y = button(w, st, "ai:reply", busy ? TM.g(Text.AI_THINKING) : TM.g(Text.AI_REPLY_BUTTON), "WRITABLE_BOOK", !busy,
                SPluginsHub.action(SPluginsHub.Action.Type.AI_ASK, null, false), x, y);
        com.ssomar.score.ai.AiHistory.Exchange last = ex.isEmpty() || pending != null ? null : ex.get(ex.size() - 1);
        if (last != null) {
            AiAnswer a = AiAnswer.parse(last.answer);
            for (int n = 1; n <= Math.min(3, a.codes.size()); n++)
                y = button(w, st, "ai:copy:" + n, TM.g(Text.AI_COPY).replace("%n%", "" + n), "PAPER", true,
                        SPluginsHub.action(SPluginsHub.Action.Type.AI_COPY, "" + n, false), x, y);
            int k = 0;
            for (AiAnswer.Link l : a.links) {
                if (++k > 2) break;
                y = button(w, st, "ai:link:" + k, "&b➜ " + l.label, "COMPASS", true,
                        new SPluginsHub.Action(SPluginsHub.Action.Type.LINK, l.label, l.url, false), x, y);
            }
        }
        y = button(w, st, "ai:new", TM.g(Text.AI_NEW), "BOOK", !busy, SPluginsHub.action(SPluginsHub.Action.Type.AI_NEW, null, true), x, y);
        button(w, st, "ai:exit", TM.g(Text.HUB_BACK), "ARROW", true, SPluginsHub.action(SPluginsHub.Action.Type.AI_EXIT, null, false), x, y);
        return pages;
    }

    private static double button(List<com.ssomar.score.screen.ScreenWidget> w, SPluginsHub.State st, String id, String text, String icon, boolean enabled,
                                 SPluginsHub.Action action, double x, double y) {
        w.add(com.ssomar.score.screen.ScreenWidget.builder(id, com.ssomar.score.screen.WidgetKind.BUTTON).at(x, y).size(COL_W, BTN_H)
                .text(text).icon(icon).enabled(enabled).hiddenInChest().build());
        if (enabled) st.actions.put(id, action);
        return y + BTN_STEP;
    }

    /** "Copy config n" of the last answer: the small Copy window (or a chat line) */
    static void copy(Player p, ScreenSession s, String n) {
        com.ssomar.score.ai.AiHistory.Exchange last = com.ssomar.score.ai.AiHistory.last(p.getUniqueId());
        if (last == null) return;
        AiAnswer a = AiAnswer.parse(last.answer);
        int i;
        try {
            i = Integer.parseInt(n) - 1;
        } catch (NumberFormatException e) {
            return;
        }
        if (i < 0 || i >= a.codes.size()) return;
        if (s != null && HubLinks.dialogsAvailable()) s.holdClicksUntilLookAway();
        if (!HubLinks.sendCopy(p, TM.g(Text.AI_COPY).replace("%n%", n), a.codes.get(i).text, () -> SPluginsHub.back(p))) p.sendMessage(a.codes.get(i).text);
    }

    static final AiAssistant.View VIEW = new AiAssistant.View() {
        @Override
        public void thinking(Player p) {
            if (SPluginsHub.isScreenOpen(p)) return; // shown in the panel
            p.sendMessage(StringConverter.coloredString(TM.g(Text.AI_THINKING)));
        }

        @Override
        public void answer(Player p, String markdown) {
            AiAnswer a = AiAnswer.parse(markdown);
            if (SPluginsHub.showConversation(p)) return;
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
        public void confirm(Player p, List<AiChange> changes, Consumer<List<com.ssomar.score.ai.AiProposal.Decision>> decide) {
            String id = UUID.randomUUID().toString().substring(0, 8);
            PENDING.put(id, new Pending(changes.size(), decide));
            List<LineDiff> diffs = new ArrayList<>();
            for (AiChange c : changes) diffs.add(LineDiff.of(c.getBefore(), c.getAfter(), 2, changes.size() == 1 ? 80 : 30));
            if (HubLinks.dialogsAvailable()) {
                ScreenSession cur = com.ssomar.score.screen.Screens.get(p);
                if (cur != null) cur.holdClicksUntilLookAway();
                try {
                    HubDialogs.changes(p, changes, diffs, list -> submit(p, id, list), () -> refuseAll(p, id));
                    return;
                } catch (Throwable t) {
                    SCore.plugin.getLogger().warning("[SCore AI] change window unavailable, the chat is used: " + t);
                }
            }
            for (int i = 0; i < changes.size(); i++) chatChange(p, id, i, changes.get(i), diffs.get(i));
        }

        @Override
        public void applied(Player p, AiChange change, JSONObject result) {
            SPluginsHub.refreshIfOpen(p);
            if (result.optBoolean("expired", false)) p.sendMessage(StringConverter.coloredString(TM.g(Text.AI_EXPIRED)));
            else if (result.optBoolean("applied", false) && change.isPlainText()) {
                p.sendMessage(StringConverter.coloredString(TM.g(Text.AI_TICKET_OPENED).replace("%target%", change.getTarget())));
                String url = result.optString("url", "");
                if (!url.isEmpty()) {
                    try {
                        TextComponent t = new TextComponent(TextComponent.fromLegacyText(StringConverter.coloredString("&7➜ &b&n" + url)));
                        t.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
                        p.spigot().sendMessage(t);
                    } catch (Throwable ignored) {
                        p.sendMessage(url);
                    }
                }
            } else if (result.optBoolean("applied", false))
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

    /* ------------------------------------------------------------------ resume */

    /** the conversation shown again (window with Reply, or the chat) */
    static void resume(Player p, ScreenSession s) {
        List<com.ssomar.score.ai.AiHistory.Exchange> all = com.ssomar.score.ai.AiHistory.exchanges(p.getUniqueId());
        if (all.isEmpty()) {
            askWindow(p, s, "", null);
            return;
        }
        com.ssomar.score.ai.AiHistory.Exchange last = all.get(all.size() - 1);
        AiAnswer a = AiAnswer.parse(last.answer);
        if (HubLinks.dialogsAvailable()) {
            if (s != null) s.holdClicksUntilLookAway();
            try {
                HubDialogs.answer(p, all.subList(0, all.size() - 1), last.question, a, () -> askWindow(p, null, "", null), () -> SPluginsHub.back(p));
                return;
            } catch (Throwable t) {
                SCore.plugin.getLogger().warning("[SCore AI] answer window unavailable, the chat is used: " + t);
            }
        }
        if (s != null) s.close();
        p.sendMessage("§b» §f" + last.question);
        chatAnswer(p, a);
    }

    /* ------------------------------------------------------------------ changes */

    /** a form waiting for the admin's answer (window, or one chat line per change) */
    static final class Pending {
        final com.ssomar.score.ai.AiProposal.Decision[] decisions;
        final Consumer<List<com.ssomar.score.ai.AiProposal.Decision>> decide;

        Pending(int n, Consumer<List<com.ssomar.score.ai.AiProposal.Decision>> decide) {
            this.decisions = new com.ssomar.score.ai.AiProposal.Decision[n];
            this.decide = decide;
        }
    }

    /** forms waiting for the admin's answer, by short id (window, or /score hub ai apply|refuse <id> [n]) */
    private static final Map<String, Pending> PENDING = new ConcurrentHashMap<>();

    /** the window was validated: one decision per change */
    static void submit(Player p, String id, List<com.ssomar.score.ai.AiProposal.Decision> list) {
        Pending pd = PENDING.remove(id);
        if (pd == null) {
            p.sendMessage(StringConverter.coloredString(TM.g(Text.AI_EXPIRED)));
            return;
        }
        if (list.stream().noneMatch(d -> d.apply)) p.sendMessage(StringConverter.coloredString(TM.g(Text.AI_REFUSED)));
        pd.decide.accept(list);
        SPluginsHub.refreshIfOpen(p);
    }

    static void refuseAll(Player p, String id) {
        Pending pd = PENDING.get(id);
        if (pd == null) return;
        List<com.ssomar.score.ai.AiProposal.Decision> list = new ArrayList<>();
        for (int i = 0; i < pd.decisions.length; i++) list.add(new com.ssomar.score.ai.AiProposal.Decision(false, ""));
        submit(p, id, list);
    }

    /** chat: one change answered; the form is sent once every change has an answer */
    static void decideOne(Player p, String id, int n, boolean yes) {
        Pending pd = PENDING.get(id);
        if (pd == null || n < 0 || n >= pd.decisions.length) {
            p.sendMessage(StringConverter.coloredString(TM.g(Text.AI_EXPIRED)));
            return;
        }
        pd.decisions[n] = new com.ssomar.score.ai.AiProposal.Decision(yes, "");
        for (com.ssomar.score.ai.AiProposal.Decision d : pd.decisions) if (d == null) return;
        submit(p, id, java.util.Arrays.asList(pd.decisions));
    }

    static void chatChange(Player p, String id, int n, AiChange c, LineDiff d) {
        p.sendMessage(StringConverter.coloredString("&b&l" + StringConverter.decoloredString(TM.g(Text.AI_CHANGE_TITLE))));
        String head = c.isPlainText() ? "&e" + c.getTarget() : c.isCreation() ? TM.g(Text.AI_CHANGE_NEW).replace("%target%", c.getTarget())
                : TM.g(Text.AI_CHANGE_EDIT).replace("%target%", c.getTarget()).replace("%added%", "" + d.added).replace("%removed%", "" + d.removed);
        p.sendMessage(StringConverter.coloredString(head));
        if (!c.getSummary().isEmpty()) p.sendMessage("§f" + c.getSummary());
        if (c.isPlainText()) {
            for (String line : c.getAfter().split("\n")) p.sendMessage("§f" + line);
        } else {
        int shown = 0;
        for (LineDiff.Line l : d.lines) {
            if (++shown > 30) {
                p.sendMessage("§8  …");
                break;
            }
            p.sendMessage((l.kind == '+' ? "§a+ " : l.kind == '-' ? "§c- " : l.kind == '…' ? "§8  … " : "§7  ") + l.text);
        }
        }
        p.sendMessage(StringConverter.coloredString(TM.g(Text.AI_CHANGE_NOTE)));
        try {
            TextComponent yes = new TextComponent(TextComponent.fromLegacyText(StringConverter.coloredString("&a&l[✔ " + StringConverter.decoloredString(TM.g(Text.AI_APPLY)) + "]")));
            yes.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/score hub ai apply " + id + " " + n));
            TextComponent no = new TextComponent(TextComponent.fromLegacyText(StringConverter.coloredString("   &c&l[✘ " + StringConverter.decoloredString(TM.g(Text.AI_REFUSE)) + "]")));
            no.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/score hub ai refuse " + id + " " + n));
            yes.addExtra(no);
            p.spigot().sendMessage(yes);
        } catch (Throwable t) {
            p.sendMessage("/score hub ai apply " + id + " " + n + "   /score hub ai refuse " + id + " " + n);
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
                int n = 0;
                if (args.length > 3) try {
                    n = Integer.parseInt(args[3]);
                } catch (NumberFormatException ignored) {
                }
                decideOne(p, args[2], n, sub.equals("apply"));
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
