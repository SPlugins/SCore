package com.ssomar.score.screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The content of a screen: a title, a panel size in panel units, and widgets in draw order (later ones on top).
 * {@code chestRows} is the height of the chest version (1 to 6 rows).
 */
public final class ScreenLayout {

    private final String title;
    private final double width;
    private final double height;
    private final List<ScreenWidget> widgets;
    private final int chestRows;

    public ScreenLayout(String title, double width, double height, List<ScreenWidget> widgets, int chestRows) {
        this.title = title == null ? "" : title;
        this.width = width;
        this.height = height;
        this.widgets = Collections.unmodifiableList(new ArrayList<>(widgets));
        this.chestRows = Math.max(1, Math.min(6, chestRows));
    }

    public String getTitle() { return title; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public List<ScreenWidget> getWidgets() { return widgets; }
    public int getChestRows() { return chestRows; }

    public ScreenWidget get(String id) {
        for (ScreenWidget w : widgets) if (w.getId().equals(id)) return w;
        return null;
    }

    /**
     * The topmost clickable widget at a panel point, or null.
     *
     * @param pu panel x from the left edge, to the right
     * @param pv panel y from the top edge, down
     */
    public ScreenWidget hit(double pu, double pv) {
        if (pu < 0 || pv < 0 || pu > width || pv > height) return null;
        for (int i = widgets.size() - 1; i >= 0; i--) {
            ScreenWidget w = widgets.get(i);
            if (w.isClickable() && w.contains(pu, pv)) return w;
        }
        return null;
    }

    /** Same layout with one widget replaced (matched by id). */
    public ScreenLayout with(ScreenWidget replacement) {
        List<ScreenWidget> list = new ArrayList<>(widgets);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(replacement.getId())) {
                list.set(i, replacement);
                return new ScreenLayout(title, width, height, list, chestRows);
            }
        }
        list.add(replacement);
        return new ScreenLayout(title, width, height, list, chestRows);
    }
}
