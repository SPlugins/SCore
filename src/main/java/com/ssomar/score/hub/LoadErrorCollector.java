package com.ssomar.score.hub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Keeps the last console lines that report a loading error ("... ERROR ..."), so the SPlugins Hub can show how many
 * errors each plugin had. Fed by {@link com.ssomar.score.utils.logging.Utils#sendConsoleMsg(String)}, which every
 * SPlugins plugin uses to print its configuration errors.
 */
public final class LoadErrorCollector {

    private static final int MAX = 300;
    private static final LinkedList<String> LINES = new LinkedList<>();

    private LoadErrorCollector() {
    }

    public static void record(String decoloredLine) {
        if (decoloredLine == null) return;
        if (!decoloredLine.contains("ERROR")) return;
        synchronized (LINES) {
            LINES.addLast(decoloredLine.trim());
            if (LINES.size() > MAX) LINES.removeFirst();
        }
    }

    /** Lines that start with (or contain in their first word) the plugin name, e.g. "ExecutableItems ERROR, ..." */
    public static List<String> linesOf(String pluginName) {
        String p = pluginName.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        synchronized (LINES) {
            for (String l : LINES) {
                String head = l.length() > 40 ? l.substring(0, 40) : l;
                if (head.toLowerCase(Locale.ROOT).contains(p)) out.add(l);
            }
        }
        return out;
    }

    public static List<String> all() {
        synchronized (LINES) {
            return Collections.unmodifiableList(new ArrayList<>(LINES));
        }
    }

    public static void clear() {
        synchronized (LINES) {
            LINES.clear();
        }
    }
}
