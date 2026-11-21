package com.ssomar.score.ai;

import com.ssomar.score.SCore;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The conversations of each admin with the assistant: the current one (continued by the next question) and the
 * earlier ones, kept in a history so they can be resumed later ("New conversation" archives the current one, it
 * never deletes it). Each conversation keeps its splugins.net id and its last exchanges. Stored in
 * plugins/SCore/ai-conversations/&lt;uuid&gt;.yml (a file of the first format, one conversation at the root, is read
 * as the current conversation).
 */
public final class AiHistory {

    static final int KEEP = 10, MAX_CONVERSATIONS = 20, MAX_ANSWER = 20_000, MAX_QUESTION = 4_000;

    public static final class Exchange {
        public final String question, answer;
        public final long time;

        Exchange(String question, String answer, long time) {
            this.question = question;
            this.answer = answer;
            this.time = time;
        }
    }

    /** a conversation as listed in the history */
    public static final class Summary {
        public final String key, title;
        public final long updated;
        public final int questions;
        public final boolean current;

        Summary(String key, String title, long updated, int questions, boolean current) {
            this.key = key;
            this.title = title;
            this.updated = updated;
            this.questions = questions;
            this.current = current;
        }
    }

    static final class Conversation {
        String key = UUID.randomUUID().toString().substring(0, 8);
        String id = "";
        String title = "";
        long updated;
        final List<Exchange> exchanges = new ArrayList<>();
    }

    static final class Store {
        String current;
        final List<Conversation> all = new ArrayList<>();

        Conversation current() {
            if (current == null) return null;
            for (Conversation c : all) if (c.key.equals(current)) return c;
            return null;
        }
    }

    private static final Map<UUID, Store> CACHE = new ConcurrentHashMap<>();

    private AiHistory() {
    }

    static File folder() {
        return new File(SCore.plugin.getDataFolder(), "ai-conversations");
    }

    private static File file(UUID u) {
        return new File(folder(), u + ".yml");
    }

    private static List<Exchange> readExchanges(List<Map<?, ?>> list) {
        List<Exchange> out = new ArrayList<>();
        for (Map<?, ?> m : list) {
            Object t = m.get("time");
            out.add(new Exchange(String.valueOf(m.get("question")), String.valueOf(m.get("answer")), t instanceof Number ? ((Number) t).longValue() : 0L));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static Store load(UUID u) {
        Store s = new Store();
        File f = file(u);
        if (!f.isFile()) return s;
        try {
            YamlConfiguration y = YamlConfiguration.loadConfiguration(f);
            if (y.isList("conversations")) {
                for (Map<?, ?> m : y.getMapList("conversations")) {
                    Conversation c = new Conversation();
                    if (m.get("key") != null) c.key = String.valueOf(m.get("key"));
                    c.id = m.get("conversation") == null ? "" : String.valueOf(m.get("conversation"));
                    c.title = m.get("title") == null ? "" : String.valueOf(m.get("title"));
                    Object up = m.get("updated");
                    c.updated = up instanceof Number ? ((Number) up).longValue() : 0L;
                    Object ex = m.get("exchanges");
                    if (ex instanceof List) c.exchanges.addAll(readExchanges((List<Map<?, ?>>) (List<?>) ex));
                    s.all.add(c);
                }
                s.current = y.getString("current", null);
            } else {
                // first format: one conversation at the root
                Conversation c = new Conversation();
                c.id = y.getString("conversation", "");
                c.exchanges.addAll(readExchanges(y.getMapList("exchanges")));
                if (!c.id.isEmpty() || !c.exchanges.isEmpty()) {
                    c.title = c.exchanges.isEmpty() ? "" : cut(c.exchanges.get(0).question.replace('\n', ' '), 80);
                    c.updated = c.exchanges.isEmpty() ? 0L : c.exchanges.get(c.exchanges.size() - 1).time;
                    s.all.add(c);
                    s.current = c.key;
                }
            }
        } catch (Throwable t) {
            SCore.plugin.getLogger().warning("[SCore AI] could not read the conversations of " + u + ": " + t.getMessage());
        }
        return s;
    }

    private static Store get(UUID u) {
        return CACHE.computeIfAbsent(u, AiHistory::load);
    }

    private static void save(UUID u, Store s) {
        try {
            if (s.all.isEmpty()) {
                File f = file(u);
                if (f.isFile() && !f.delete()) f.deleteOnExit();
                return;
            }
            folder().mkdirs();
            YamlConfiguration y = new YamlConfiguration();
            y.set("current", s.current);
            List<Map<String, Object>> convs = new ArrayList<>();
            for (Conversation c : s.all) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("key", c.key);
                m.put("conversation", c.id);
                m.put("title", c.title);
                m.put("updated", c.updated);
                List<Map<String, Object>> list = new ArrayList<>();
                for (Exchange e : c.exchanges) {
                    Map<String, Object> x = new LinkedHashMap<>();
                    x.put("time", e.time);
                    x.put("question", e.question);
                    x.put("answer", e.answer);
                    list.add(x);
                }
                m.put("exchanges", list);
                convs.add(m);
            }
            y.set("conversations", convs);
            y.save(file(u));
        } catch (Throwable t) {
            SCore.plugin.getLogger().warning("[SCore AI] could not save the conversations of " + u + ": " + t.getMessage());
        }
    }

    /** the splugins.net conversation to continue, or null (a new one) */
    public static String conversationId(UUID u) {
        Conversation c = get(u).current();
        return c == null || c.id == null || c.id.isEmpty() ? null : c.id;
    }

    /** there is a current conversation */
    public static boolean has(UUID u) {
        Conversation c = get(u).current();
        return c != null && (!c.exchanges.isEmpty() || !c.id.isEmpty());
    }

    /** exchanges of the current conversation, oldest first */
    public static List<Exchange> exchanges(UUID u) {
        Conversation c = get(u).current();
        return c == null ? Collections.<Exchange>emptyList() : Collections.unmodifiableList(new ArrayList<>(c.exchanges));
    }

    public static Exchange last(UUID u) {
        List<Exchange> l = exchanges(u);
        return l.isEmpty() ? null : l.get(l.size() - 1);
    }

    /** an answered question: added to the current conversation (a new one if there is none) */
    public static synchronized void add(UUID u, String conversationId, String question, String answer) {
        Store s = get(u);
        Conversation c = s.current();
        if (c == null) {
            c = new Conversation();
            c.title = cut(question.replace('\n', ' ').trim(), 80);
            s.all.add(c);
            s.current = c.key;
        }
        if (conversationId != null && !conversationId.isEmpty()) c.id = conversationId;
        c.exchanges.add(new Exchange(cut(question, MAX_QUESTION), cut(answer, MAX_ANSWER), System.currentTimeMillis()));
        c.updated = System.currentTimeMillis();
        while (c.exchanges.size() > KEEP) c.exchanges.remove(0);
        // the oldest conversations leave the history (never the current one)
        while (s.all.size() > MAX_CONVERSATIONS) {
            Conversation oldest = null;
            for (Conversation x : s.all) if (!x.key.equals(s.current) && (oldest == null || x.updated < oldest.updated)) oldest = x;
            if (oldest == null) break;
            s.all.remove(oldest);
        }
        save(u, s);
    }

    /** "New conversation": the current one goes to the history, the next question starts a new one */
    public static synchronized void startNew(UUID u) {
        Store s = get(u);
        if (s.current == null) return;
        s.current = null;
        save(u, s);
    }

    /** kept for the callers of the first version: same as {@link #startNew} */
    public static void clear(UUID u) {
        startNew(u);
    }

    /** the history, most recent first (the current conversation included, marked) */
    public static List<Summary> list(UUID u) {
        Store s = get(u);
        List<Summary> out = new ArrayList<>();
        for (Conversation c : s.all)
            if (!c.exchanges.isEmpty()) out.add(new Summary(c.key, c.title, c.updated, c.exchanges.size(), c.key.equals(s.current)));
        out.sort((a, b) -> Long.compare(b.updated, a.updated));
        return out;
    }

    /** resume a conversation of the history: it becomes the current one */
    public static synchronized boolean select(UUID u, String key) {
        Store s = get(u);
        for (Conversation c : s.all)
            if (c.key.equals(key)) {
                s.current = key;
                save(u, s);
                return true;
            }
        return false;
    }

    /** remove a conversation from the history */
    public static synchronized boolean delete(UUID u, String key) {
        Store s = get(u);
        boolean removed = s.all.removeIf(c -> c.key.equals(key));
        if (removed && key.equals(s.current)) s.current = null;
        if (removed) save(u, s);
        return removed;
    }

    /** /score reload: the conversations are read again from their files */
    public static void reload() {
        CACHE.clear();
    }

    static String cut(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
