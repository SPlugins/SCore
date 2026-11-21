package com.ssomar.score.hub;

import com.ssomar.score.SCore;
import com.ssomar.score.languages.messages.TM;
import com.ssomar.score.languages.messages.Text;
import com.ssomar.score.screen.*;
import com.ssomar.score.utils.logging.Utils;
import com.ssomar.score.utils.strings.StringConverter;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The SPlugins Hub: dashboard of the installed SPlugins plugins, help, discovery, news, links.
 * Admins only (permission score.hub). Shown with SCore Screens (PINNED, FLOATING or chest).
 */
public final class SPluginsHub {

    public static final String SCREEN_ID = "splugins:hub";

    /* panel geometry, in panel units (16:9, FOV 70 gives about 2.49 x 1.40) */
    static final double W = 2.4, H = 1.35;
    private static final double ROW_TOP = 0.25, ROW_H = 0.105, ROW_STEP = 0.112;
    private static final int SCREEN_ROWS = 8, CHEST_ROWS = 36;

    public enum Tab {
        HOME(Text.HUB_TAB_HOME, "NETHER_STAR"), MANAGE(Text.HUB_TAB_MANAGE, "COMPARATOR"), HELP(Text.HUB_TAB_HELP, "BOOK"),
        DISCOVER(Text.HUB_TAB_DISCOVER, "SPYGLASS"), NEWS(Text.HUB_TAB_NEWS, "BELL"), LINKS(Text.HUB_TAB_LINKS, "COMPASS"),
        SUPPORT(Text.HUB_TAB_SUPPORT, "EMERALD");

        final Text label;
        final String icon;

        Tab(Text label, String icon) {
            this.label = label;
            this.icon = icon;
        }
    }

    /** What a content row does when clicked. */
    static final class Action {
        enum Type {MANAGE, LINK, RUN, ERRORS, DIAGNOSTIC, NONE}

        final Type type;
        final String a, b;
        final boolean confirm;

        Action(Type type, String a, String b, boolean confirm) {
            this.type = type;
            this.a = a;
            this.b = b;
            this.confirm = confirm;
        }
    }

    /** One content row before pagination. */
    static final class Row {
        final String text, icon;
        final List<String> lore;
        final Action action;
        final boolean glow;
        /** picture shown in the preview of the screen modes (Discover, News), null if none */
        String image;

        Row(String text, String icon, List<String> lore, Action action, boolean glow) {
            this.text = text;
            this.icon = icon;
            this.lore = lore;
            this.action = action;
            this.glow = glow;
        }

        Row image(String url) {
            this.image = url == null || url.isEmpty() ? null : url;
            return this;
        }
    }

    static final class State {
        Tab tab = Tab.HOME;
        int page;
        String managed;
        ScreenMode mode = ScreenMode.CHEST;
        final Map<String, Action> actions = new HashMap<>();
        /** row id -> {image url, icon} for the preview */
        final Map<String, String[]> previews = new HashMap<>();
        String confirmId;
        long confirmUntil;
        String chestNote;
    }

    private static final Map<UUID, State> STATES = new ConcurrentHashMap<>();

    private SPluginsHub() {
    }

    public static void open(Player p, ScreenMode forced) {
        State st = STATES.computeIfAbsent(p.getUniqueId(), u -> new State());
        if (forced != null) Screens.setPlayerMode(p, forced);
        // asking for PINNED again runs the cursor test again (after installing a shader pack fix, for example)
        if (forced == ScreenMode.PINNED) com.ssomar.score.screen.internal.ScreenData.setCursorSeen(p.getUniqueId(), null);
        HubCatalog.refreshIfOld();
        // first build for the chest page size, rebuilt below if a screen mode opened
        st.mode = ScreenMode.CHEST;
        ScreenSpec spec = ScreenSpec.builder(SCREEN_ID).layout(build(p, st)).mode(ScreenMode.AUTO).onInput(SPluginsHub::onInput).build();
        ScreenOpenResult r = Screens.open(p, spec);
        if (!r.isOpened()) return;
        st.mode = r.getMode();
        st.chestNote = r.getMode() == ScreenMode.CHEST ? r.getNote() : null;
        if (r.getMode() != ScreenMode.CHEST || st.chestNote != null) refresh(p, st);
    }

    private static void refresh(Player p, State st) {
        ScreenSession s = Screens.get(p);
        if (s == null || !SCREEN_ID.equals(s.getSpec().getId())) return;
        s.setLayout(build(p, st));
    }

    /* ------------------------------------------------------------------ input */

    static void onInput(ScreenSession s, ScreenInput in) {
        Player p = s.getPlayer();
        State st = STATES.get(p.getUniqueId());
        if (st == null) return;
        if (in instanceof ScreenInput.Closed) return;
        if (in instanceof ScreenInput.Scroll) {
            st.page += ((ScreenInput.Scroll) in).getDelta();
            refresh(p, st);
            return;
        }
        if (in instanceof ScreenInput.Hover) {
            String id = ((ScreenInput.Hover) in).getWidgetId();
            ScreenWidget w = id == null ? null : s.getLayout().get(id);
            String hint = w == null || w.getLore().isEmpty() ? defaultHint() : String.join("  ", w.getLore());
            ScreenLayout l = s.getLayout();
            ScreenWidget hintW = l.get("hint");
            if (hintW != null) l = l.with(hintW.toBuilder().text(hint).build());
            String[] pv = id == null ? null : st.previews.get(id);
            ScreenWidget prev = l.get("preview");
            if (pv != null && prev != null && !pv[0].equals(prev.getImageUrl())) l = l.with(preview(pv));
            s.setLayout(l);
            return;
        }
        if (!(in instanceof ScreenInput.Click)) return;
        String id = ((ScreenInput.Click) in).getWidgetId();
        if (id == null) return;
        if (id.equals("close")) {
            s.close();
            return;
        }
        if (id.startsWith("tab:")) {
            st.tab = Tab.valueOf(id.substring(4));
            st.page = 0;
            if (st.tab != Tab.MANAGE) st.managed = null;
            if (st.tab == Tab.NEWS) HubData.markNewsSeen(p, HubCatalog.get());
            refresh(p, st);
            return;
        }
        if (id.equals("back")) {
            st.managed = null;
            st.page = 0;
            refresh(p, st);
            return;
        }
        if (id.equals("prev") || id.equals("next")) {
            st.page += id.equals("next") ? 1 : -1;
            refresh(p, st);
            return;
        }
        Action a = st.actions.get(id);
        if (a == null) return;
        if (a.confirm) {
            if (!id.equals(st.confirmId) || System.currentTimeMillis() > st.confirmUntil) {
                st.confirmId = id;
                st.confirmUntil = System.currentTimeMillis() + 4000;
                ScreenWidget w = s.getLayout().get(id);
                if (w != null) s.setLayout(s.getLayout().with(w.toBuilder().text(TM.g(Text.HUB_CONFIRM)).glow(true).build()));
                return;
            }
            st.confirmId = null;
        }
        run(p, s, st, a);
    }

    private static void run(Player p, ScreenSession s, State st, Action a) {
        switch (a.type) {
            case MANAGE:
                st.tab = Tab.MANAGE;
                st.managed = a.a;
                st.page = 0;
                refresh(p, st);
                break;
            case LINK:
                s.close();
                HubLinks.send(p, a.a, a.b, st.tab.name().toLowerCase(Locale.ROOT));
                break;
            case RUN:
                s.close();
                p.performCommand(a.a);
                break;
            case ERRORS:
                s.close();
                List<String> lines = LoadErrorCollector.linesOf(a.a);
                if (lines.isEmpty()) p.sendMessage(StringConverter.coloredString(TM.g(Text.HUB_MANAGE_NO_ERRORS)));
                for (int i = Math.max(0, lines.size() - 15); i < lines.size(); i++) p.sendMessage(StringConverter.coloredString("&c" + lines.get(i)));
                break;
            case DIAGNOSTIC:
                s.close();
                String report = DiagnosticReport.build();
                if (!HubLinks.sendCopy(p, TM.g(Text.HUB_HELP_DIAGNOSTIC_READY), report)) {
                    Utils.sendConsoleFlatMsg(SCore.plugin, report);
                    p.sendMessage(StringConverter.coloredString(TM.g(Text.HUB_HELP_DIAGNOSTIC_CONSOLE)));
                }
                break;
            default:
                break;
        }
    }

    /* ------------------------------------------------------------------ layout */

    static ScreenLayout build(Player p, State st) {
        HubCatalog cat = HubCatalog.get();
        List<ScreenWidget> w = new ArrayList<>();
        st.actions.clear();

        w.add(ScreenWidget.builder("title", WidgetKind.LABEL).at(0.04, 0.02).size(1.2, 0.07).text("&b&lSPlugins &f&lHub").textScale(1.2).hiddenInChest().build());
        boolean newNews = HubData.hasUnseenNews(p, cat);
        double tabW = 0.29;
        for (Tab t : Tab.values()) {
            String label = TM.g(t.label) + (t == Tab.NEWS && newNews ? TM.g(Text.HUB_TAB_NEW_BADGE) : "");
            w.add(ScreenWidget.builder("tab:" + t.name(), WidgetKind.BUTTON).at(0.04 + t.ordinal() * 0.30, 0.11).size(tabW, 0.09)
                    .text(label).icon(t.icon).selected(t == st.tab).glow(t == st.tab).chestSlot(t.ordinal()).build());
        }
        // the screen modes have no room for the word: just the cross (the chest item keeps the full name)
        String close = st.mode == ScreenMode.CHEST ? TM.g(Text.HUB_CLOSE) : "&c&l✘";
        w.add(ScreenWidget.builder("close", WidgetKind.BUTTON).at(2.17, 0.11).size(0.19, 0.09).text(close).icon("BARRIER").chestSlot(8).build());

        List<Row> rows = rows(p, st, cat);
        int perPage = st.mode == ScreenMode.CHEST ? CHEST_ROWS : SCREEN_ROWS;
        int pages = Math.max(1, (rows.size() + perPage - 1) / perPage);
        st.page = Math.max(0, Math.min(st.page, pages - 1));
        int from = st.page * perPage;
        st.previews.clear();
        boolean preview = false;
        if (st.mode != ScreenMode.CHEST)
            for (int i = from; i < Math.min(rows.size(), from + perPage); i++) if (rows.get(i).image != null) preview = true;
        double rowW = preview ? W - 0.08 - PREVIEW_W - 0.04 : W - 0.08;
        String[] first = null;
        for (int i = from; i < Math.min(rows.size(), from + perPage); i++) {
            Row r = rows.get(i);
            int idx = i - from;
            String id = "row:" + i;
            WidgetKind kind = r.action.type == Action.Type.NONE ? WidgetKind.LABEL : WidgetKind.TILE;
            ScreenWidget.Builder b = ScreenWidget.builder(id, kind).at(0.04, ROW_TOP + idx * ROW_STEP).size(rowW, ROW_H)
                    .text(r.text).icon(r.icon).lore(r.lore).glow(r.glow).chestSlot(9 + idx);
            w.add(b.build());
            if (r.action.type != Action.Type.NONE) st.actions.put(id, r.action);
            if (r.image != null) {
                String[] pv = {r.image, r.icon};
                st.previews.put(id, pv);
                if (first == null) first = pv;
            }
        }
        if (preview && first != null) w.add(preview(first));

        double barV = ROW_TOP + SCREEN_ROWS * ROW_STEP + 0.02;
        if (st.tab == Tab.MANAGE && st.managed != null)
            w.add(ScreenWidget.builder("back", WidgetKind.BUTTON).at(0.04, barV).size(0.3, 0.08).text(TM.g(Text.HUB_BACK)).icon("ARROW").chestSlot(45).build());
        if (pages > 1) {
            w.add(ScreenWidget.builder("prev", WidgetKind.BUTTON).at(0.75, barV).size(0.3, 0.08).text(TM.g(Text.HUB_PREVIOUS)).icon("ARROW").enabled(st.page > 0).chestSlot(48).build());
            w.add(ScreenWidget.builder("page", WidgetKind.LABEL).at(1.07, barV).size(0.26, 0.08)
                    .text(TM.g(Text.HUB_PAGE).replace("%page%", "" + (st.page + 1)).replace("%max%", "" + pages)).icon("PAPER").chestSlot(49).build());
            w.add(ScreenWidget.builder("next", WidgetKind.BUTTON).at(1.35, barV).size(0.3, 0.08).text(TM.g(Text.HUB_NEXT)).icon("ARROW").enabled(st.page < pages - 1).chestSlot(50).build());
        }
        List<String> info = new ArrayList<>();
        info.add(TM.g(Text.HUB_CATALOG_SOURCE).replace("%source%", cat.source));
        if (st.chestNote != null) info.add(TM.g(Text.HUB_MODE).replace("%reason%", st.chestNote));
        w.add(ScreenWidget.builder("info", WidgetKind.LABEL).at(1.9, barV).size(0.46, 0.08).text("&7SCore " + SCore.plugin.getDescription().getVersion())
                .lore(info).icon("BOOK").chestSlot(53).build());
        w.add(ScreenWidget.builder("hint", WidgetKind.LABEL).at(0.04, barV + 0.09).size(W - 0.08, 0.07).text(defaultHint()).textScale(0.8).hiddenInChest().build());

        return new ScreenLayout(TM.g(Text.HUB_TITLE) + " &8- " + TM.g(st.tab.label), W, H, w, 6);
    }

    /** picture of the hovered row (Discover, News), on the right of the rows; an icon where images can't be drawn */
    static final double PREVIEW_W = 0.84, PREVIEW_H = 0.6;

    static ScreenWidget preview(String[] pv) {
        return ScreenWidget.builder("preview", WidgetKind.IMAGE).at(W - 0.04 - PREVIEW_W, ROW_TOP).size(PREVIEW_W, PREVIEW_H)
                .image(pv[0]).icon(pv[1]).hiddenInChest().build();
    }

    static String defaultHint() {
        // dark grey is unreadable on the panel: light grey
        return (TM.g(Text.HUB_HINT) + "  " + TM.g(Text.HUB_SNEAK_HINT)).replace("&8", "&7");
    }

    static List<Row> rows(Player p, State st, HubCatalog cat) {
        switch (st.tab) {
            case HOME:
                return homeRows(cat);
            case MANAGE:
                return manageRows(st, cat);
            case HELP:
                return helpRows(cat);
            case DISCOVER:
                return discoverRows(cat);
            case NEWS:
                return newsRows(cat);
            case LINKS:
                return linkRows(cat, false);
            case SUPPORT:
                return linkRows(cat, true);
            default:
                return new ArrayList<>();
        }
    }

    private static Action none() {
        return new Action(Action.Type.NONE, null, null, false);
    }

    private static Action link(String label, String url) {
        return new Action(Action.Type.LINK, label, url, false);
    }

    private static List<Row> homeRows(HubCatalog cat) {
        List<Row> rows = new ArrayList<>();
        List<Row> upgrades = new ArrayList<>();
        for (InstalledPlugin ip : InstalledPlugin.scan()) {
            KnownPlugin k = ip.getKnown();
            List<String> lore = new ArrayList<>();
            StringBuilder title = new StringBuilder("&f&l" + k.getPluginName() + " &7" + ip.getVersion());
            if (ip.getPremium() != null) {
                String edition = ip.getPremium() ? TM.g(Text.HUB_HOME_PREMIUM) : TM.g(Text.HUB_HOME_FREE);
                title.append("  ").append(edition);
            }
            HubCatalog.Entry e = cat.pluginEntry(k, ip.getPremium());
            String status;
            if (!ip.isEnabled()) status = TM.g(Text.HUB_HOME_DISABLED);
            else if (e == null || e.latestVersion == null || e.latestVersion.isEmpty()) status = TM.g(Text.HUB_HOME_UPDATE_UNKNOWN);
            else if (HubCatalog.compareVersions(ip.getVersion(), e.latestVersion) < 0)
                status = TM.g(Text.HUB_HOME_UPDATE).replace("%version%", e.latestVersion);
            else status = TM.g(Text.HUB_HOME_UP_TO_DATE);
            title.append("  ").append(status);
            if (ip.getErrors() > 0) title.append("  ").append(TM.g(Text.HUB_HOME_ERRORS).replace("%count%", "" + ip.getErrors()));
            lore.add(TM.g(Text.HUB_HOME_VERSION).replace("%version%", ip.getVersion()));
            lore.add(status);
            if (ip.getObjects() >= 0) {
                if (ip.getFreeLimit() > 0 && ip.getFreeLimit() < 100000)
                    lore.add(TM.g(Text.HUB_HOME_FREE_LIMIT).replace("%count%", "" + ip.getObjects()).replace("%limit%", "" + ip.getFreeLimit()));
                else lore.add(TM.g(Text.HUB_HOME_OBJECTS).replace("%count%", "" + ip.getObjects()));
            }
            lore.add(ip.getErrors() > 0 ? TM.g(Text.HUB_HOME_ERRORS).replace("%count%", "" + ip.getErrors()) : TM.g(Text.HUB_HOME_NO_ERROR));
            lore.add(TM.g(Text.HUB_HOME_CLICK_MANAGE));
            rows.add(new Row(title.toString(), k.getIcon(), lore, new Action(Action.Type.MANAGE, k.getKey(), null, false), false));
            if (Boolean.FALSE.equals(ip.getPremium())) {
                HubCatalog.Entry prem = cat.pluginEntry(k, true);
                if (prem != null && prem.url != null && !prem.url.isEmpty() && "premium".equalsIgnoreCase(prem.edition)) {
                    List<String> l = new ArrayList<>();
                    l.add(TM.g(Text.HUB_HOME_UPGRADE_LORE));
                    l.add(TM.g(Text.HUB_DISCOVER_PRICE).replace("%price%", prem.priceText()));
                    l.add(TM.g(Text.HUB_CLICK_LINK));
                    upgrades.add(new Row(TM.g(Text.HUB_HOME_UPGRADE).replace("%plugin%", k.getPluginName()), "NETHER_STAR", l,
                            link(k.getPluginName() + " Premium", prem.url), true));
                }
            }
        }
        rows.addAll(upgrades);
        return rows;
    }

    private static List<Row> manageRows(State st, HubCatalog cat) {
        List<Row> rows = new ArrayList<>();
        KnownPlugin k = st.managed == null ? null : KnownPlugin.byKey(st.managed);
        if (k == null || !InstalledPlugin.isInstalled(k)) {
            st.managed = null;
            rows.add(new Row(TM.g(Text.HUB_MANAGE_CHOOSE), "PAPER", new ArrayList<String>(), none(), false));
            for (InstalledPlugin ip : InstalledPlugin.scan()) {
                List<String> l = new ArrayList<>();
                l.add(TM.g(Text.HUB_HOME_VERSION).replace("%version%", ip.getVersion()));
                l.add(TM.g(Text.HUB_HOME_CLICK_MANAGE));
                rows.add(new Row("&f&l" + ip.getKnown().getPluginName(), ip.getKnown().getIcon(), l,
                        new Action(Action.Type.MANAGE, ip.getKnown().getKey(), null, false), false));
            }
            return rows;
        }
        rows.add(new Row("&f&l" + k.getPluginName(), k.getIcon(), new ArrayList<String>(), none(), true));
        if (k.getCommand() != null) {
            rows.add(new Row(TM.g(Text.HUB_MANAGE_RELOAD) + " &8(/" + k.getCommand() + " reload)", "LIME_DYE", lore(TM.g(Text.HUB_CONFIRM)),
                    new Action(Action.Type.RUN, k.getCommand() + " reload", null, true), false));
            rows.add(new Row(TM.g(Text.HUB_MANAGE_EDITOR) + " &8(/" + k.getCommand() + " editor)", "WRITABLE_BOOK", new ArrayList<String>(),
                    new Action(Action.Type.RUN, k.getCommand() + " editor", null, false), false));
        }
        rows.add(new Row(TM.g(Text.HUB_MANAGE_WIKI), "BOOK", lore(TM.g(Text.HUB_CLICK_LINK)), link(k.getPluginName() + " wiki", k.getDocsUrl()), false));
        HubCatalog.Entry e = cat.pluginEntry(k, null);
        if (e != null && e.url != null && !e.url.isEmpty())
            rows.add(new Row(TM.g(Text.HUB_MANAGE_CHANGELOG), "KNOWLEDGE_BOOK", lore(TM.g(Text.HUB_CLICK_LINK)), link(k.getPluginName() + " updates", e.url), false));
        int errors = LoadErrorCollector.linesOf(k.getPluginName()).size();
        rows.add(new Row(TM.g(Text.HUB_MANAGE_ERRORS) + " &8(" + errors + ")", errors > 0 ? "REDSTONE" : "LIME_DYE", new ArrayList<String>(),
                new Action(Action.Type.ERRORS, k.getPluginName(), null, false), errors > 0));
        return rows;
    }

    private static List<Row> helpRows(HubCatalog cat) {
        List<Row> rows = new ArrayList<>();
        rows.add(new Row(TM.g(Text.HUB_HELP_DIAGNOSTIC), "SPYGLASS", Arrays.asList(TM.gA(Text.HUB_HELP_DIAGNOSTIC_LORE)),
                new Action(Action.Type.DIAGNOSTIC, null, null, false), true));
        rows.add(new Row(TM.g(Text.HUB_HELP_DISCORD), "BELL", lore(TM.g(Text.HUB_CLICK_LINK)), link("Discord", linkUrl(cat, "discord", "https://discord.gg/splugins")), false));
        for (InstalledPlugin ip : InstalledPlugin.scan()) {
            KnownPlugin k = ip.getKnown();
            rows.add(new Row(TM.g(Text.HUB_HELP_WIKI).replace("%plugin%", k.getPluginName()), "BOOK", lore(TM.g(Text.HUB_CLICK_LINK)),
                    link(k.getPluginName() + " wiki", k.getDocsUrl()), false));
        }
        return rows;
    }

    private static List<Row> discoverRows(HubCatalog cat) {
        List<Row> rows = new ArrayList<>();
        Set<String> done = new HashSet<>();
        for (HubCatalog.Entry e : cat.plugins) {
            KnownPlugin k = KnownPlugin.byKey(e.key);
            if (k != null && InstalledPlugin.isInstalled(k)) continue;
            if (k == KnownPlugin.SCORE) continue;
            String dedupe = (e.key == null || e.key.isEmpty() ? e.name : e.key) + "/" + e.edition;
            if (!done.add(dedupe)) continue;
            List<String> l = new ArrayList<>();
            if (e.pitch != null && !e.pitch.isEmpty()) l.add("&7" + e.pitch);
            l.add(TM.g(Text.HUB_DISCOVER_PRICE).replace("%price%", e.priceText()));
            l.add(TM.g(Text.HUB_CLICK_LINK));
            String ed = e.edition == null || e.edition.isEmpty() ? "" : " &8(" + e.edition + ")";
            rows.add(new Row("&f&l" + e.name + ed + "  &7" + e.priceText(), k != null ? k.getIcon() : "CHEST", l, link(e.name, e.url), e.salePercent > 0)
                    .image(e.banner != null && !e.banner.isEmpty() ? e.banner : e.icon));
        }
        for (HubCatalog.Entry e : cat.packs) {
            List<String> l = new ArrayList<>();
            l.add(TM.g(Text.HUB_DISCOVER_PACK));
            if (e.pitch != null && !e.pitch.isEmpty()) l.add("&7" + e.pitch);
            l.add(TM.g(Text.HUB_DISCOVER_PRICE).replace("%price%", e.priceText()));
            l.add(TM.g(Text.HUB_CLICK_LINK));
            rows.add(new Row("&d" + e.name + "  &7" + e.priceText(), "CHEST", l, link(e.name, e.url), e.salePercent > 0)
                    .image(e.banner != null && !e.banner.isEmpty() ? e.banner : e.icon));
        }
        rows.add(new Row(TM.g(Text.HUB_DISCOVER_STUDIO), "ANVIL", lore(TM.g(Text.HUB_DISCOVER_STUDIO_LORE), TM.g(Text.HUB_CLICK_LINK)),
                link("Pack Studio", linkUrl(cat, "studio", "https://splugins.net/forge")), true));
        if (rows.size() == 1) rows.add(0, new Row(TM.g(Text.HUB_DISCOVER_EMPTY), "PAPER", new ArrayList<String>(), none(), false));
        return rows;
    }

    private static List<Row> newsRows(HubCatalog cat) {
        List<Row> rows = new ArrayList<>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        for (HubCatalog.News n : cat.promotions) {
            List<String> l = new ArrayList<>();
            l.add(TM.g(Text.HUB_NEWS_PROMO) + (n.discount > 0 ? " &e-" + n.discount + "%" : ""));
            if (!n.text.isEmpty()) l.add("&7" + n.text);
            if (n.end > 0) l.add(TM.g(Text.HUB_NEWS_UNTIL).replace("%date%", df.format(new Date(n.end))));
            if (!n.url.isEmpty()) l.add(TM.g(Text.HUB_CLICK_LINK));
            rows.add(new Row("&6&l" + n.title + (n.discount > 0 ? " &e-" + n.discount + "%" : ""), "GOLD_INGOT", l,
                    n.url.isEmpty() ? none() : link(n.title, n.url), true).image(n.image));
        }
        for (HubCatalog.News n : cat.news) {
            List<String> l = new ArrayList<>();
            if (n.date > 0) l.add("&8" + df.format(new Date(n.date)));
            if (!n.text.isEmpty()) l.add("&7" + n.text);
            if (!n.url.isEmpty()) l.add(TM.g(Text.HUB_CLICK_LINK));
            rows.add(new Row("&f" + n.title, "PAPER", l, n.url.isEmpty() ? none() : link(n.title, n.url), false).image(n.image));
        }
        if (rows.isEmpty()) rows.add(new Row(TM.g(Text.HUB_NEWS_EMPTY), "PAPER", new ArrayList<String>(), none(), false));
        return rows;
    }

    private static List<Row> linkRows(HubCatalog cat, boolean support) {
        List<Row> rows = new ArrayList<>();
        if (support) {
            for (HubCatalog.Link l : links(cat)) {
                if (!l.id.startsWith("review")) continue;
                String platform = l.label;
                rows.add(new Row(TM.g(Text.HUB_SUPPORT_REVIEW).replace("%platform%", platform), "SUNFLOWER", lore(TM.g(Text.HUB_SUPPORT_REVIEW_LORE), TM.g(Text.HUB_CLICK_LINK)),
                        link(platform, l.url), true));
            }
            rows.add(new Row(TM.g(Text.HUB_SUPPORT_SUGGEST), "WRITABLE_BOOK", lore(TM.g(Text.HUB_CLICK_LINK)),
                    link("Suggestions", linkUrl(cat, "suggestions", "https://discord.gg/splugins")), false));
            rows.add(new Row(TM.g(Text.HUB_SUPPORT_DONATE), "EMERALD", lore(TM.g(Text.HUB_CLICK_LINK)),
                    link("Donate", linkUrl(cat, "donate", "https://donate.ssomar.com")), false));
            return rows;
        }
        for (HubCatalog.Link l : links(cat)) {
            if (l.id.startsWith("review") || l.id.equals("suggestions") || l.id.equals("studio")) continue;
            rows.add(new Row("&b" + l.label, l.icon == null || l.icon.isEmpty() ? "COMPASS" : l.icon, lore("&8" + l.url, TM.g(Text.HUB_CLICK_LINK)),
                    link(l.label, l.url), false));
        }
        return rows;
    }

    /** catalog links, or the built-in list when the catalog has none */
    static List<HubCatalog.Link> links(HubCatalog cat) {
        if (!cat.links.isEmpty()) return cat.links;
        List<HubCatalog.Link> list = new ArrayList<>();
        String[][] d = {
                {"website", "splugins.net", "https://splugins.net", "COMPASS"},
                {"docs", "Documentation", "https://splugins.net/docs", "BOOK"},
                {"discord", "Discord", "https://discord.gg/splugins", "BELL"},
                {"youtube", "YouTube @Ssomar", "https://www.youtube.com/@Ssomar", "PAINTING"},
                {"instagram", "Instagram @ssomar.mc", "https://www.instagram.com/ssomar.mc", "PAINTING"},
                {"donate", "donate.ssomar.com", "https://donate.ssomar.com", "EMERALD"},
                {"review-splugins", "splugins.net", "https://splugins.net/store", "SUNFLOWER"},
                {"review-spigot", "SpigotMC", "https://www.spigotmc.org/resources/authors/splugins.641604/", "SUNFLOWER"},
                {"review-builtbybit", "BuiltByBit", "https://builtbybit.com/creators/ssomar", "SUNFLOWER"},
        };
        for (String[] s : d) {
            HubCatalog.Link l = new HubCatalog.Link();
            l.id = s[0];
            l.label = s[1];
            l.url = s[2];
            l.icon = s[3];
            list.add(l);
        }
        return list;
    }

    static String linkUrl(HubCatalog cat, String id, String def) {
        for (HubCatalog.Link l : links(cat)) if (l.id.equalsIgnoreCase(id)) return l.url;
        return def;
    }

    private static List<String> lore(String... lines) {
        return new ArrayList<>(Arrays.asList(lines));
    }

    public static void forget(Player p) {
        STATES.remove(p.getUniqueId());
    }
}
