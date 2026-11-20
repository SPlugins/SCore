package com.ssomar.score.screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One element of a {@link ScreenLayout}. Immutable, built with {@link #builder(String, WidgetKind)}.
 * <p>
 * Position and size are in panel units (1 unit = 1 block seen from 1 block away), with (u, v) the top-left corner,
 * u to the right and v down from the top edge of the panel. {@code chestSlot} is where the widget goes in the chest
 * version of the screen (-1 = next free slot, -2 = not shown in the chest).
 */
public final class ScreenWidget {

    /** Sentinel: use the default colour of the widget kind. */
    public static final int DEFAULT_COLOR = 0x01000000;

    private final String id;
    private final WidgetKind kind;
    private final double u, v, width, height;
    private final String text;
    private final List<String> lore;
    private final String icon;
    private final boolean enabled;
    private final boolean selected;
    private final boolean glow;
    private final int fill;
    private final double textScale;
    private final int chestSlot;
    private final String imageUrl;

    private ScreenWidget(Builder b) {
        this.id = b.id;
        this.kind = b.kind;
        this.u = b.u;
        this.v = b.v;
        this.width = b.width;
        this.height = b.height;
        this.text = b.text;
        this.lore = Collections.unmodifiableList(new ArrayList<>(b.lore));
        this.icon = b.icon;
        this.enabled = b.enabled;
        this.selected = b.selected;
        this.glow = b.glow;
        this.fill = b.fill;
        this.textScale = b.textScale;
        this.chestSlot = b.chestSlot;
        this.imageUrl = b.imageUrl;
    }

    public static Builder builder(String id, WidgetKind kind) {
        return new Builder(id, kind);
    }

    public Builder toBuilder() {
        Builder b = new Builder(id, kind);
        b.u = u;
        b.v = v;
        b.width = width;
        b.height = height;
        b.text = text;
        b.lore = new ArrayList<>(lore);
        b.icon = icon;
        b.enabled = enabled;
        b.selected = selected;
        b.glow = glow;
        b.fill = fill;
        b.textScale = textScale;
        b.chestSlot = chestSlot;
        b.imageUrl = imageUrl;
        return b;
    }

    public boolean isClickable() {
        return enabled && kind.isClickable();
    }

    /** true if the panel point (u right, v down, from the top-left corner) is inside the widget */
    public boolean contains(double pu, double pv) {
        return pu >= u && pu <= u + width && pv >= v && pv <= v + height;
    }

    public String getId() { return id; }
    public WidgetKind getKind() { return kind; }
    public double getU() { return u; }
    public double getV() { return v; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public String getText() { return text; }
    public List<String> getLore() { return lore; }
    public String getIcon() { return icon; }
    public boolean isEnabled() { return enabled; }
    public boolean isSelected() { return selected; }
    public boolean isGlow() { return glow; }
    public int getFill() { return fill; }
    public double getTextScale() { return textScale; }
    public int getChestSlot() { return chestSlot; }
    public String getImageUrl() { return imageUrl; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScreenWidget)) return false;
        ScreenWidget w = (ScreenWidget) o;
        return id.equals(w.id) && kind == w.kind && u == w.u && v == w.v && width == w.width && height == w.height
                && eq(text, w.text) && lore.equals(w.lore) && eq(icon, w.icon) && enabled == w.enabled
                && selected == w.selected && glow == w.glow && fill == w.fill && textScale == w.textScale
                && chestSlot == w.chestSlot && eq(imageUrl, w.imageUrl);
    }

    @Override
    public int hashCode() {
        return id.hashCode() * 31 + kind.hashCode();
    }

    private static boolean eq(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }

    public static final class Builder {
        private final String id;
        private final WidgetKind kind;
        private double u, v, width = 0.3, height = 0.1;
        private String text = "";
        private List<String> lore = new ArrayList<>();
        private String icon;
        private boolean enabled = true;
        private boolean selected;
        private boolean glow;
        private int fill = DEFAULT_COLOR;
        private double textScale = 1.0;
        private int chestSlot = -1;
        private String imageUrl;

        private Builder(String id, WidgetKind kind) {
            if (id == null || id.isEmpty()) throw new IllegalArgumentException("widget id is empty");
            this.id = id;
            this.kind = kind;
        }

        public Builder at(double u, double v) { this.u = u; this.v = v; return this; }
        public Builder size(double width, double height) { this.width = width; this.height = height; return this; }
        public Builder text(String text) { this.text = text == null ? "" : text; return this; }
        public Builder lore(List<String> lore) { this.lore = lore == null ? new ArrayList<String>() : new ArrayList<>(lore); return this; }
        public Builder addLore(String line) { this.lore.add(line); return this; }
        /** a Material name; unknown names fall back to PAPER (or a stone on very old versions) */
        public Builder icon(String material) { this.icon = material; return this; }
        public Builder enabled(boolean enabled) { this.enabled = enabled; return this; }
        public Builder selected(boolean selected) { this.selected = selected; return this; }
        public Builder glow(boolean glow) { this.glow = glow; return this; }
        /** ARGB colour of the box */
        public Builder fill(int argb) { this.fill = argb; return this; }
        public Builder textScale(double scale) { this.textScale = scale; return this; }
        public Builder chestSlot(int slot) { this.chestSlot = slot; return this; }
        public Builder hiddenInChest() { this.chestSlot = -2; return this; }
        public Builder image(String url) { this.imageUrl = url; return this; }

        public ScreenWidget build() {
            return new ScreenWidget(this);
        }
    }
}
