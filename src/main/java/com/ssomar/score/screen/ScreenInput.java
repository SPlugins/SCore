package com.ssomar.score.screen;

/**
 * What the player did on a screen. Subclasses: {@link Click}, {@link Hover}, {@link Scroll}, {@link Closed}.
 * {@link Closed} is always the last input of a session.
 */
public abstract class ScreenInput {

    private ScreenInput() {
    }

    public static final class Click extends ScreenInput {
        private final String widgetId;
        private final boolean right;
        private final boolean shift;

        public Click(String widgetId, boolean right, boolean shift) {
            this.widgetId = widgetId;
            this.right = right;
            this.shift = shift;
        }

        /** null when the click was outside every clickable widget */
        public String getWidgetId() { return widgetId; }
        public boolean isRight() { return right; }
        public boolean isShift() { return shift; }
    }

    public static final class Hover extends ScreenInput {
        private final String widgetId;

        public Hover(String widgetId) {
            this.widgetId = widgetId;
        }

        /** null when the cursor left every clickable widget */
        public String getWidgetId() { return widgetId; }
    }

    public static final class Scroll extends ScreenInput {
        private final int delta;

        public Scroll(int delta) {
            this.delta = delta;
        }

        /** +1 = down / next, -1 = up / previous */
        public int getDelta() { return delta; }
    }

    public static final class Closed extends ScreenInput {
        private final CloseReason reason;

        public Closed(CloseReason reason) {
            this.reason = reason;
        }

        public CloseReason getReason() { return reason; }
    }
}
