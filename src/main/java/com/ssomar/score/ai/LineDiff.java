package com.ssomar.score.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Line diff of two YAML files, shown to the admin before a change of the assistant is applied: removed lines,
 * added lines, a little context around them, and the unchanged runs folded.
 */
public final class LineDiff {

    /** ' ' unchanged, '-' removed, '+' added, '…' folded (text = how many lines) */
    public static final class Line {
        public final char kind;
        public final String text;

        Line(char kind, String text) {
            this.kind = kind;
            this.text = text;
        }

        @Override
        public String toString() {
            return kind + text;
        }
    }

    public final List<Line> lines;
    public final int added, removed;

    private LineDiff(List<Line> lines, int added, int removed) {
        this.lines = lines;
        this.added = added;
        this.removed = removed;
    }

    static final long MAX_CELLS = 4_000_000L;

    static String[] split(String s) {
        if (s == null || s.isEmpty()) return new String[0];
        String n = s.replace("\r\n", "\n");
        if (n.endsWith("\n")) n = n.substring(0, n.length() - 1);
        return n.split("\n", -1);
    }

    /**
     * @param context  unchanged lines kept around a change
     * @param maxLines lines shown at most (the rest becomes one "…" line)
     */
    public static LineDiff of(String before, String after, int context, int maxLines) {
        String[] a = split(before), b = split(after);
        List<Line> raw = new ArrayList<>();
        if ((long) a.length * b.length > MAX_CELLS) {
            for (String s : a) raw.add(new Line('-', s));
            for (String s : b) raw.add(new Line('+', s));
        } else {
            // longest common subsequence, from the end so the walk below goes forward
            int[][] l = new int[a.length + 1][b.length + 1];
            for (int i = a.length - 1; i >= 0; i--)
                for (int j = b.length - 1; j >= 0; j--)
                    l[i][j] = a[i].equals(b[j]) ? l[i + 1][j + 1] + 1 : Math.max(l[i + 1][j], l[i][j + 1]);
            int i = 0, j = 0;
            while (i < a.length || j < b.length) {
                if (i < a.length && j < b.length && a[i].equals(b[j])) {
                    raw.add(new Line(' ', a[i]));
                    i++;
                    j++;
                } else if (i < a.length && (j >= b.length || l[i + 1][j] >= l[i][j + 1])) {
                    raw.add(new Line('-', a[i++])); // removed lines first, like a usual diff
                } else {
                    raw.add(new Line('+', b[j++]));
                }
            }
        }
        int added = 0, removed = 0;
        boolean[] keep = new boolean[raw.size()];
        for (int k = 0; k < raw.size(); k++) {
            char c = raw.get(k).kind;
            if (c == ' ') continue;
            if (c == '+') added++;
            else removed++;
            for (int d = Math.max(0, k - context); d <= Math.min(raw.size() - 1, k + context); d++) keep[d] = true;
        }
        List<Line> out = new ArrayList<>();
        int folded = 0;
        for (int k = 0; k < raw.size(); k++) {
            if (keep[k]) {
                if (folded > 0) out.add(new Line('…', String.valueOf(folded)));
                folded = 0;
                out.add(raw.get(k));
            } else folded++;
        }
        if (folded > 0 && !out.isEmpty()) out.add(new Line('…', String.valueOf(folded)));
        if (out.size() > maxLines) {
            int hidden = out.size() - (maxLines - 1);
            out = new ArrayList<>(out.subList(0, maxLines - 1));
            out.add(new Line('…', String.valueOf(hidden)));
        }
        return new LineDiff(Collections.unmodifiableList(out), added, removed);
    }
}
