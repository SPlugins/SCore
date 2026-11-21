package com.ssomar.score.screen;

/**
 * Everything needed to open a screen. Built with {@link #builder(String)}.
 */
public final class ScreenSpec {

    private final String id;
    private final ScreenLayout layout;
    private final ScreenMode mode;
    private final ScreenInputHandler handler;
    private final int backdrop;

    private ScreenSpec(Builder b) {
        this.id = b.id;
        this.layout = b.layout;
        this.mode = b.mode;
        this.handler = b.handler;
        this.backdrop = b.backdrop;
    }

    /** @param id namespaced id, e.g. "splugins:hub"; can be switched to chest by the config list screens.off */
    public static Builder builder(String id) {
        return new Builder(id);
    }

    public String getId() { return id; }
    public ScreenLayout getLayout() { return layout; }
    public ScreenMode getMode() { return mode; }
    public ScreenInputHandler getHandler() { return handler; }
    /** ARGB colour behind the panel in PINNED mode (0 = none) */
    public int getBackdrop() { return backdrop; }

    public ScreenSpec withLayout(ScreenLayout layout) {
        Builder b = new Builder(id);
        b.layout = layout;
        b.mode = mode;
        b.handler = handler;
        b.backdrop = backdrop;
        return b.build();
    }

    public static final class Builder {
        private final String id;
        private ScreenLayout layout;
        private ScreenMode mode = ScreenMode.AUTO;
        private ScreenInputHandler handler;
        private int backdrop = 0xF0080A10;

        private Builder(String id) {
            this.id = id;
        }

        public Builder layout(ScreenLayout layout) { this.layout = layout; return this; }
        public Builder mode(ScreenMode mode) { this.mode = mode; return this; }
        public Builder onInput(ScreenInputHandler handler) { this.handler = handler; return this; }
        public Builder backdrop(int argb) { this.backdrop = argb; return this; }

        public ScreenSpec build() {
            if (layout == null) throw new IllegalStateException("screen " + id + " has no layout");
            if (handler == null) handler = (s, i) -> { };
            return new ScreenSpec(this);
        }
    }
}
