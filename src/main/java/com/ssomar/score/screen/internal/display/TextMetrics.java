package com.ssomar.score.screen.internal.display;

/**
 * Width of a legacy-coloured string in the default Minecraft font, in font pixels (advance included), and the
 * geometry of a TextDisplay read in the client (DisplayRenderer.TextDisplayRenderer, 26.2):
 * one font pixel = 0.025 block, a line is 10 px (0.25 block), the background quad spans x from -w/2 to w/2 + 1 px
 * and y from 0 to lines x 0.25 above the entity origin; glyphs sit 1 px above the bottom of their line.
 */
public final class TextMetrics {

    public static final double PX = 0.025;
    public static final double LINE = 10 * PX;
    /** width in px of the one-space line used for the boxes (" " advance 4) */
    public static final int BOX_TEXT_WIDTH = 4;

    private TextMetrics() {
    }

    /** advance of one character in px (glyph width + 1) */
    public static int advance(char c) {
        switch (c) {
            case 'i': case '!': case '.': case ',': case ':': case ';': case '|': case '\'':
                return 2;
            case 'l': case '`':
                return 3;
            case 'I': case 't': case '[': case ']': case ' ':
                return 4;
            case 'f': case 'k': case '(': case ')': case '{': case '}': case '<': case '>': case '"': case '*':
                return 5;
            case '@': case '~':
                return 7;
            default:
                return 6;
        }
    }

    /** width in px of a string with section-sign colour codes (bold adds 1 px per character) */
    public static int width(String s) {
        if (s == null) return 0;
        int w = 0;
        boolean bold = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '§' && i + 1 < s.length()) {
                char code = Character.toLowerCase(s.charAt(++i));
                if (code == 'l') bold = true;
                else if (code == 'r' || (code >= '0' && code <= '9') || (code >= 'a' && code <= 'f') || code == 'x') bold = false;
                continue;
            }
            if (Character.isHighSurrogate(c)) continue;
            w += advance(c) + (bold ? 1 : 0);
        }
        return w;
    }

    /** visible characters only */
    public static String strip(String s) {
        if (s == null) return "";
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '§' && i + 1 < s.length()) {
                i++;
                continue;
            }
            b.append(c);
        }
        return b.toString();
    }

    /**
     * Cut a coloured string so its width fits {@code maxPx}, adding "..." when cut.
     */
    public static String fit(String s, int maxPx) {
        if (width(s) <= maxPx) return s;
        int dots = width("...");
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '§' && i + 1 < s.length()) {
                b.append(c).append(s.charAt(++i));
                continue;
            }
            if (width(b.toString() + c) + dots > maxPx) break;
            b.append(c);
        }
        return b + "§r...";
    }
}
