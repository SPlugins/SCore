package com.ssomar.score.ai;

import com.ssomar.score.SCore;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The current conversation of each admin with the assistant: the splugins.net conversation id and the last
 * exchanges (question + answer), so the Hub can show it again ("Resume the conversation") after the menu was
 * closed, a reload or a restart. Kept in plugins/SCore/ai-conversations/&lt;uuid&gt;.yml.
 */
public final class AiHistory {

    static final int KEEP = 10, MAX_ANSWER = 20_000, MAX_QUESTION = 4_000;

    public static final class Exchange {
        public final String question, answer;
        public final long time;

        Exchange(String question, String answer, long time) {
            this.question = question;
            this.answer = answer;
            this.time = time;
        }
    }

    static final class Conversation {
        String id = "";
        final List<Exchange> exchanges = new ArrayList<>();
    }

    private static final Map<UUID, Conversation> CACHE = new ConcurrentHashMap<>();
    private static final Conversation NONE = new Conversation();

    private AiHistory() {
    }

    static File folder() {
        return new File(SCore.plugin.getDataFolder(), "ai-conversations");
    }

    private static File file(UUID u) {
        return new File(folder(), u + ".yml");
    }

    private static Conversation get(UUID u) {
        return CACHE.computeIfAbsent(u, k -> {
            File f = file(k);
            if (!f.isFile()) return NONE;
            try {
                YamlConfiguration y = YamlConfiguration.loadConfiguration(f);
                Conversation c = new Conversation();
                c.id = y.getString("conversation", "");
                for (Map<?, ?> m : y.getMapList("exchanges")) {
                    Object t = m.get("time");
                    c.exchanges.add(new Exchange(String.valueOf(m.get("question")), String.valueOf(m.get("answer")),
                            t instanceof Number ? ((Number) t).longValue() : 0L));
                }
                return c.id.isEmpty() && c.exchanges.isEmpty() ? NONE : c;
            } catch (Throwable t) {
                return NONE;
            }
        });
    }

    private static void save(UUID u, Conversation c) {
        try {
            folder().mkdirs();
            YamlConfiguration y = new YamlConfiguration();
            y.set("conversation", c.id);
            List<Map<String, Object>> list = new ArrayList<>();
            for (Exchange e : c.exchanges) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("time", e.time);
                m.put("question", e.question);
                m.put("answer", e.answer);
                list.add(m);
            }
            y.set("exchanges", list);
            y.save(file(u));
        } catch (Throwable t) {
            SCore.plugin.getLogger().warning("[SCore AI] could not save the conversation of " + u + ": " + t.getMessage());
        }
    }

    /** the splugins.net conversation to continue, or null */
    public static String conversationId(UUID u) {
        String id = get(u).id;
        return id == null || id.isEmpty() ? null : id;
    }

    public static boolean has(UUID u) {
        Conversation c = get(u);
        return c != NONE && (!c.exchanges.isEmpty() || !c.id.isEmpty());
    }

    /** oldest first */
    public static List<Exchange> exchanges(UUID u) {
        return Collections.unmodifiableList(new ArrayList<>(get(u).exchanges));
    }

    public static Exchange last(UUID u) {
        List<Exchange> l = get(u).exchanges;
        return l.isEmpty() ? null : l.get(l.size() - 1);
    }

    public static synchronized void add(UUID u, String conversationId, String question, String answer) {
        Conversation c = get(u);
        if (c == NONE) {
            c = new Conversation();
            CACHE.put(u, c);
        }
        if (conversationId != null && !conversationId.isEmpty()) c.id = conversationId;
        c.exchanges.add(new Exchange(cut(question, MAX_QUESTION), cut(answer, MAX_ANSWER), System.currentTimeMillis()));
        while (c.exchanges.size() > KEEP) c.exchanges.remove(0);
        save(u, c);
    }

    /** "New conversation" */
    public static synchronized void clear(UUID u) {
        CACHE.put(u, NONE);
        File f = file(u);
        if (f.isFile() && !f.delete()) f.deleteOnExit();
    }

    static String cut(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
