package com.ssomar.score.screen;

public enum WidgetKind {
    /** text only, never clickable */
    LABEL,
    /** clickable box with a text */
    BUTTON,
    /** clickable icon with a caption under it (and a lore shown on hover / in the chest item) */
    TILE,
    /** an item icon, never clickable */
    ICON,
    /** a plain coloured rectangle */
    FILL,
    /** a picture (map tiles in PINNED mode, its caption otherwise) */
    IMAGE;

    public boolean isClickable() {
        return this == BUTTON || this == TILE || this == IMAGE;
    }
}
