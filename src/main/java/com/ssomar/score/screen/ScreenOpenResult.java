package com.ssomar.score.screen;

public final class ScreenOpenResult {

    private final boolean opened;
    private final ScreenMode mode;
    private final String note;

    public ScreenOpenResult(boolean opened, ScreenMode mode, String note) {
        this.opened = opened;
        this.mode = mode;
        this.note = note;
    }

    public boolean isOpened() { return opened; }
    /** the mode used, or null if nothing opened */
    public ScreenMode getMode() { return mode; }
    /** why a richer mode was not used (e.g. "riding", "old version"), or null */
    public String getNote() { return note; }
}
