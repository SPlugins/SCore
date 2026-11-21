package com.ssomar.score.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An answer of the assistant split for Minecraft: plain paragraphs (markdown removed), the code blocks (configs to
 * copy) and the links (buttons). Pure, unit tested.
 */
public final class AiAnswer {

    public static final class Code {
        public final String language, text;

        Code(String language, String text) {
            this.language = language;
            this.text = text;
        }
    }

    public static final class Link {
        public final String label, url;

        Link(String label, String url) {
            this.label = label;
            this.url = url;
        }
    }

    public final List<String> paragraphs = new ArrayList<>();
    public final List<Code> codes = new ArrayList<>();
    public final List<Link> links = new ArrayList<>();

    private static final Pattern FENCE = Pattern.compile("```([a-zA-Z0-9_-]*)\\s*\\n(.*?)```", Pattern.DOTALL);
    private static final Pattern MD_LINK = Pattern.compile("\\[([^\\]]{1,80})\\]\\((https?://[^\\s)]+)\\)");
    private static final Pattern URL = Pattern.compile("(?<![(\\[])https?://[^\\s)\\]>\"']+");

    public static AiAnswer parse(String markdown) {
        AiAnswer a = new AiAnswer();
        if (markdown == null) return a;
        StringBuilder rest = new StringBuilder();
        Matcher m = FENCE.matcher(markdown);
        int last = 0, n = 0;
        while (m.find()) {
            rest.append(markdown, last, m.start());
            n++;
            a.codes.add(new Code(m.group(1).isEmpty() ? "text" : m.group(1).toLowerCase(), m.group(2).replaceAll("\\s+$", "")));
            rest.append("\n[config ").append(n).append("]\n");
            last = m.end();
        }
        rest.append(markdown.substring(last));
        String text = rest.toString();

        Matcher l = MD_LINK.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (l.find()) {
            a.addLink(l.group(1), l.group(2));
            l.appendReplacement(sb, Matcher.quoteReplacement(l.group(1)));
        }
        l.appendTail(sb);
        text = sb.toString();
        Matcher u = URL.matcher(text);
        while (u.find()) {
            String url = u.group().replaceAll("[.,;:]+$", "");
            a.addLink(shortLabel(url), url);
        }

        for (String block : text.split("\\n\\s*\\n")) {
            String p = clean(block);
            if (!p.isEmpty()) a.paragraphs.add(p);
        }
        return a;
    }

    private void addLink(String label, String url) {
        for (Link k : links) if (k.url.equals(url)) return;
        links.add(new Link(label, url));
    }

    static String shortLabel(String url) {
        String s = url.replaceFirst("^https?://(www\\.)?", "");
        int q = s.indexOf('?');
        if (q > 0) s = s.substring(0, q);
        return s.length() > 40 ? s.substring(0, 39) + "…" : s;
    }

    /** markdown to plain text: headings, emphasis, inline code, list bullets */
    static String clean(String block) {
        StringBuilder out = new StringBuilder();
        for (String line : block.split("\\n")) {
            String t = line.replaceAll("\\s+$", "");
            if (t.trim().isEmpty()) continue;
            t = t.replaceFirst("^\\s{0,3}#{1,6}\\s+", "");
            t = t.replaceFirst("^(\\s*)[-*+]\\s+", "$1• ");
            t = t.replaceAll("\\*\\*(.+?)\\*\\*", "$1").replaceAll("__(.+?)__", "$1");
            t = t.replaceAll("(?<![*\\w])\\*(?!\\s)(.+?)(?<!\\s)\\*(?![*\\w])", "$1");
            t = t.replaceAll("`([^`]+)`", "$1");
            if (out.length() > 0) out.append('\n');
            out.append(t);
        }
        return out.toString().trim();
    }
}
