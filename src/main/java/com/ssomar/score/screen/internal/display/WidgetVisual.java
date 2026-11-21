package com.ssomar.score.screen.internal.display;

import com.ssomar.score.screen.ScreenWidget;
import com.ssomar.score.screen.WidgetKind;

/**
 * What one widget looks like on a FLOATING or PINNED screen, in centre-origin panel units (pure, unit tested).
 * The session turns it into display entities: a box (TextDisplay background), a text, an icon (ItemDisplay) and,
 * for images, map tiles.
 */
public final class WidgetVisual {

    /* colours, ARGB */
    public static final int PLATE = 0xEB1B1F27;
    public static final int BUTTON = 0xF0303846;
    public static final int TILE = 0xC8232833;
    public static final int SELECTED = 0xF02F5D8C;
    public static final int GLOW = 0xF05A4A16;
    public static final int DISABLED = 0xA0202329;
    public static final int IMAGE_PLACEHOLDER = 0xC0151820;

    private static final double PAD = 0.025;

    /** box, null if none: {centreX, centreY, width, height} */
    public double[] box;
    public int boxColor;
    /** text with section-sign codes, null if none */
    public String text;
    /** text scale in panel units (a line is 0.25 x scale tall) */
    public double textScale;
    /** translation of the text display origin, panel units */
    public double textX, textY;
    /** icon material, null if none */
    public String icon;
    public double iconX, iconY, iconSize;
    /** true when the widget is an image drawn with map tiles */
    public boolean image;

    private WidgetVisual() {
    }

    /**
     * @param text     the widget text, colour codes already converted to section signs
     * @param images   true if image widgets can be drawn (PINNED with the pack, client before 26.3)
     */
    public static WidgetVisual of(ScreenWidget w, String text, double layoutW, double layoutH, boolean hovered, boolean images) {
        WidgetVisual v = new WidgetVisual();
        double[] c = ScreenGeometry.centre(w.getU(), w.getV(), w.getWidth(), w.getHeight(), layoutW, layoutH);
        double left = c[0] - w.getWidth() / 2, rightEdge = c[0] + w.getWidth() / 2, h = w.getHeight();
        WidgetKind kind = w.getKind();

        Integer color = null;
        switch (kind) {
            case FILL:
                color = w.getFill() == ScreenWidget.DEFAULT_COLOR ? TILE : w.getFill();
                break;
            case BUTTON:
            case TILE:
                if (!w.isEnabled()) color = DISABLED;
                else if (w.getFill() != ScreenWidget.DEFAULT_COLOR) color = w.getFill();
                else if (w.isSelected()) color = SELECTED;
                else if (w.isGlow()) color = GLOW;
                else color = kind == WidgetKind.BUTTON ? BUTTON : TILE;
                break;
            case LABEL:
                if (w.getFill() != ScreenWidget.DEFAULT_COLOR) color = w.getFill();
                else if (w.isSelected()) color = SELECTED;
                break;
            case IMAGE:
                if (images && w.getImageUrl() != null && !w.getImageUrl().isEmpty()) v.image = true;
                else color = IMAGE_PLACEHOLDER;
                break;
            default:
                break;
        }
        if (color != null) {
            v.box = new double[]{c[0], c[1], w.getWidth(), h};
            v.boxColor = hovered && w.isClickable() ? lighten(color) : color;
        }
        if (v.image) return v;

        double textLeft = left + PAD;
        if (w.getIcon() != null && !w.getIcon().isEmpty()) {
            if (kind == WidgetKind.ICON) {
                v.icon = w.getIcon();
                v.iconSize = Math.min(w.getWidth(), h) * 0.9;
                v.iconX = c[0];
                v.iconY = c[1];
                return v;
            }
            if (kind != WidgetKind.FILL && h >= 0.06 && w.getWidth() >= h * 5) {
                v.icon = w.getIcon();
                v.iconSize = h * 0.72;
                v.iconX = left + h * 0.5;
                v.iconY = c[1];
                textLeft = left + h;
            }
        }
        if (kind == WidgetKind.FILL || kind == WidgetKind.ICON || text == null || text.isEmpty()) return v;

        String t = w.isEnabled() ? text : "§8" + TextMetrics.strip(text);
        double area = rightEdge - PAD - textLeft;
        if (area <= 0) return v;
        double s = 2.4 * h * w.getTextScale();
        int px = Math.max(1, TextMetrics.width(t));
        double maxInk = area * 0.97;
        if (px * TextMetrics.PX * s > maxInk) {
            double fit = maxInk / (px * TextMetrics.PX);
            double min = s * 0.6;
            if (fit < min) {
                t = TextMetrics.fit(t, (int) Math.floor(maxInk / (TextMetrics.PX * min)));
                px = TextMetrics.width(t);
                fit = Math.min(min, maxInk / (Math.max(1, px) * TextMetrics.PX));
            }
            s = fit;
        }
        v.text = t;
        v.textScale = s;
        boolean leftAlign = w.getWidth() >= 0.8 || v.icon != null && w.getWidth() >= 0.5;
        if (leftAlign) v.textX = textLeft - (1 - px / 2.0) * TextMetrics.PX * s;
        else v.textX = (textLeft + rightEdge - PAD) / 2 - 0.5 * TextMetrics.PX * s;
        v.textY = c[1] - 0.135 * s;
        return v;
    }

    /** translation and scale of a box display (one-space line with a background): {tx, ty, sx, sy} */
    public static double[] boxTransform(double[] box) {
        double sx = box[2] / ((TextMetrics.BOX_TEXT_WIDTH + 1) * TextMetrics.PX);
        double sy = box[3] / TextMetrics.LINE;
        double bgCentreX = 0.5 * TextMetrics.PX * sx;
        return new double[]{box[0] - bgCentreX, box[1] - box[3] / 2, sx, sy};
    }

    static int lighten(int argb) {
        int a = argb >>> 24;
        int r = Math.min(255, ((argb >> 16) & 0xFF) + 30);
        int g = Math.min(255, ((argb >> 8) & 0xFF) + 30);
        int b = Math.min(255, (argb & 0xFF) + 34);
        return (Math.max(a, 0xF0) << 24) | (r << 16) | (g << 8) | b;
    }
}
