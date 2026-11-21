package com.ssomar.score.hub;

import com.ssomar.score.ai.AiAnswer;
import com.ssomar.score.screen.internal.display.TextMetrics;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The conversation with the assistant as lines for the Hub panel (PINNED / FLOATING): the admin's questions in
 * aqua, the answers wrapped to the panel width, the configs inline in grey (one line each, cut if too long).
 * Pure: the width of a line is measured with the Minecraft font metrics.
 */
final class ConversationLines {

    static final class Line {
        final String text;
        /** index of the exchange the line belongs to (the page of the last question is found with it) */
        final int exchange;

        Line(String text, int exchange) {
            this.text = text;
            this.exchange = exchange;
        }
    }

    private static final Pattern CONFIG = Pattern.compile("^\\[config (\\d+)]$");

    private ConversationLines() {
    }

    /**
     * @param questions questions, oldest first
     * @param answers   their answers (markdown), same size, or one less while the last one is being written
     * @param thinking  text of the "thinking" line when the last question has no answer yet
     * @param maxPx     width of a line, in font pixels
     */
    static List<Line> of(List<String> questions, List<String> answers, String thinking, int maxPx) {
        List<Line> out = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            if (i > 0) out.add(new Line("", i));
            List<String> q = wrap(questions.get(i), maxPx - TextMetrics.width("» "));
            for (int k = 0; k < q.size(); k++) out.add(new Line("§b" + (k == 0 ? "» " : "  ") + q.get(k), i));
            if (i < answers.size()) {
                AiAnswer a = AiAnswer.parse(answers.get(i));
                for (String para : a.paragraphs) {
                    Matcher m = CONFIG.matcher(para.trim());
                    if (m.matches()) {
                        int n = Integer.parseInt(m.group(1));
                        if (n >= 1 && n <= a.codes.size())
                            for (String code : a.codes.get(n - 1).text.split("\n"))
                                out.add(new Line("§7" + TextMetrics.fit(code.replace('\t', ' '), maxPx - 2), i));
                        continue;
                    }
                    for (String raw : para.split("\n"))
                        for (String l : wrap(raw, maxPx)) out.add(new Line("§f" + l, i));
                }
            } else if (thinking != null) {
                out.add(new Line(thinking, i));
            }
        }
        return out;
    }

    /** word wrap of a plain line to maxPx (a word longer than a line is cut) */
    static List<String> wrap(String text, int maxPx) {
        List<String> lines = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String word : text.trim().split(" +")) {
            if (word.isEmpty()) continue;
            String next = cur.length() == 0 ? word : cur + " " + word;
            if (TextMetrics.width(next) <= maxPx) {
                cur.setLength(0);
                cur.append(next);
                continue;
            }
            if (cur.length() > 0) lines.add(cur.toString());
            cur.setLength(0);
            String w = word;
            while (TextMetrics.width(w) > maxPx && w.length() > 1) {
                int k = w.length();
                while (k > 1 && TextMetrics.width(w.substring(0, k)) > maxPx) k--;
                lines.add(w.substring(0, k));
                w = w.substring(k);
            }
            cur.append(w);
        }
        if (cur.length() > 0) lines.add(cur.toString());
        if (lines.isEmpty()) lines.add("");
        return lines;
    }
}
