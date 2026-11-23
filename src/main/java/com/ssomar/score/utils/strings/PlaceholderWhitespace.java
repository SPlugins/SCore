package com.ssomar.score.utils.strings;

import java.util.ArrayList;
import java.util.List;

/**
 * Whitespace handling that respects {@code %placeholder%} spans, used by the IF command:
 * the condition must be compacted, but a placeholder such as
 * {@code %checkitem_inhand:main,lorecontains:test test%} must keep its inner spaces.
 */
public final class PlaceholderWhitespace {

    private PlaceholderWhitespace() {
    }

    /**
     * Removes every whitespace located outside {@code %...%} spans, keeps the ones inside.
     * If the number of {@code %} is odd, the last one is treated as a normal character
     * (everything after it is considered outside a placeholder).
     */
    public static String stripOutsidePlaceholders(String s) {
        if (s == null || s.isEmpty()) return s;

        int count = 0;
        for (int i = 0; i < s.length(); i++) if (s.charAt(i) == '%') count++;
        int lastUnmatched = (count % 2 == 1) ? s.lastIndexOf('%') : -1;

        StringBuilder sb = new StringBuilder(s.length());
        boolean inside = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '%' && i != lastUnmatched) inside = !inside;
            if (!inside && Character.isWhitespace(c)) continue;
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * The args of a command are split on spaces, so a placeholder containing a space is scattered
     * over several args. Re-joins the first args (with spaces) until the {@code %} count of the
     * first arg is even, and returns [condition, remaining args...].
     * If the {@code %} never balance, the args are returned unchanged.
     */
    public static List<String> mergeFirstArgPlaceholders(List<String> args) {
        if (args == null || args.isEmpty()) return args;

        StringBuilder first = new StringBuilder(args.get(0));
        int consumed = 1;
        while (countPercent(first) % 2 == 1 && consumed < args.size()) {
            first.append(' ').append(args.get(consumed));
            consumed++;
        }
        if (countPercent(first) % 2 == 1) return args;

        List<String> merged = new ArrayList<>(args.size() - consumed + 1);
        merged.add(first.toString());
        merged.addAll(args.subList(consumed, args.size()));
        return merged;
    }

    private static int countPercent(CharSequence s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) if (s.charAt(i) == '%') count++;
        return count;
    }
}
