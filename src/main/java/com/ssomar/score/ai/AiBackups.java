package com.ssomar.score.ai;

import com.ssomar.score.SCore;
import com.ssomar.score.sobject.SObjectWithFileLoader;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Backups of the files changed by the assistant: plugins/SCore/ai-backups/, with a journal (journal.yml) of every
 * applied change (when, who, which object, the backup file). The last change not undone yet can be undone from the
 * Hub or with /score hub ai undo: the file comes back as it was (or is removed if the assistant created it) and the
 * object is reloaded.
 */
public final class AiBackups {

    static final int KEEP = 200;

    public static final class Entry {
        public final long time;
        public final String actor, plugin, type, id, file, backup;
        public final boolean created;
        boolean undone;

        Entry(long time, String actor, String plugin, String type, String id, String file, String backup, boolean created, boolean undone) {
            this.time = time;
            this.actor = actor;
            this.plugin = plugin;
            this.type = type;
            this.id = id;
            this.file = file;
            this.backup = backup;
            this.created = created;
            this.undone = undone;
        }

        public String target() {
            return plugin + " › " + type + " › " + id;
        }

        Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("time", time);
            m.put("actor", actor);
            m.put("plugin", plugin);
            m.put("type", type);
            m.put("id", id);
            m.put("file", file);
            m.put("backup", backup);
            m.put("created", created);
            m.put("undone", undone);
            return m;
        }

        static Entry of(Map<?, ?> m) {
            Object t = m.get("time");
            return new Entry(t instanceof Number ? ((Number) t).longValue() : 0L, String.valueOf(m.get("actor")), String.valueOf(m.get("plugin")),
                    String.valueOf(m.get("type")), String.valueOf(m.get("id")), String.valueOf(m.get("file")), String.valueOf(m.get("backup")),
                    Boolean.TRUE.equals(m.get("created")), Boolean.TRUE.equals(m.get("undone")));
        }
    }

    private static List<Entry> journal;

    private AiBackups() {
    }

    static File folder() {
        return new File(SCore.plugin.getDataFolder(), "ai-backups");
    }

    private static File journalFile() {
        return new File(folder(), "journal.yml");
    }

    private static synchronized List<Entry> journal() {
        if (journal == null) {
            journal = new ArrayList<>();
            File f = journalFile();
            if (f.isFile()) {
                YamlConfiguration y = YamlConfiguration.loadConfiguration(f);
                for (Map<?, ?> m : y.getMapList("changes")) journal.add(Entry.of(m));
            }
        }
        return journal;
    }

    private static synchronized void saveJournal() throws Exception {
        List<Entry> j = journal();
        while (j.size() > KEEP) {
            Entry old = j.remove(0);
            File b = new File(folder(), old.backup);
            if (b.isFile() && !b.delete()) b.deleteOnExit();
        }
        YamlConfiguration y = new YamlConfiguration();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Entry e : j) list.add(e.toMap());
        y.set("changes", list);
        folder().mkdirs();
        y.save(journalFile());
    }

    static String stamp() {
        return new SimpleDateFormat("yyyyMMdd-HHmmss-SSS", Locale.ROOT).format(new Date());
    }

    static String safe(String s) {
        return s.replaceAll("[^A-Za-z0-9_.-]", "_");
    }

    /**
     * Keep the file as it is before a change (before = null: the change creates it).
     *
     * @return the name of the backup file
     */
    public static synchronized String save(String actor, String plugin, String type, String id, File file, String before) throws Exception {
        folder().mkdirs();
        String name = stamp() + "_" + safe(plugin) + "_" + safe(id) + (before == null ? ".created" : ".yml");
        Files.write(new File(folder(), name).toPath(), (before == null ? "" : before).getBytes(StandardCharsets.UTF_8));
        journal().add(new Entry(System.currentTimeMillis(), actor, plugin, type, id, file.getAbsolutePath(), name, before == null, false));
        saveJournal();
        return name;
    }

    /** the last change not undone yet, or null */
    public static synchronized Entry last() {
        List<Entry> j = journal();
        for (int i = j.size() - 1; i >= 0; i--) if (!j.get(i).undone) return j.get(i);
        return null;
    }

    static SObjectWithFileLoader<?> loaderOf(Entry e) {
        for (SObjectWithFileLoader<?> l : SObjectWithFileLoader.getLoaders())
            if (l.getSPlugin().getName().equalsIgnoreCase(e.plugin) && l.getObjectNameLowerCase().equalsIgnoreCase(e.type)) return l;
        return null;
    }

    /**
     * Undo the last change (server thread). The file as it is now is kept too, so an undo can be undone by hand.
     *
     * @return the entry undone, or null when there is nothing to undo
     */
    public static synchronized Entry undoLast(String actor) throws Exception {
        Entry e = last();
        if (e == null) return null;
        SObjectWithFileLoader<?> l = loaderOf(e);
        if (l == null) throw new IllegalStateException(e.plugin + " is not loaded");
        File file = new File(e.file);
        if (file.isFile())
            Files.write(new File(folder(), stamp() + "_" + safe(e.plugin) + "_" + safe(e.id) + ".before-undo.yml").toPath(), Files.readAllBytes(file.toPath()));
        if (e.created) {
            if (l.isObjectLoaded(e.id)) l.deleteLoadedObject(e.id);
            if (file.isFile() && !file.delete()) throw new IllegalStateException("could not delete " + file);
        } else {
            byte[] before = Files.readAllBytes(new File(folder(), e.backup).toPath());
            Files.write(file.toPath(), before);
            l.reloadObjectFromFile(e.id);
        }
        e.undone = true;
        saveJournal();
        SCore.plugin.getLogger().info("[AI] " + actor + " undid the change of " + e.target() + " made by " + e.actor);
        return e;
    }

    /** for the tests */
    static synchronized void reset() {
        journal = null;
    }
}
