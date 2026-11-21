package com.ssomar.score.screen;

import org.bukkit.entity.Player;

/** An open screen. All calls must be made on the player's thread. */
public interface ScreenSession {

    Player getPlayer();

    ScreenSpec getSpec();

    /** the mode actually used (never AUTO) */
    ScreenMode getMode();

    ScreenLayout getLayout();

    /** Replace the content. Only the widgets that changed are redrawn. */
    void setLayout(ScreenLayout layout);

    /** Shortcut: replace one widget's text. */
    void setText(String widgetId, String text);

    boolean isOpen();

    /** Close by code (reason PLUGIN). */
    void close();

    /**
     * Ignore clicks until the player looks elsewhere (FLOATING / PINNED; no-op in a chest). Use it before showing a
     * window over the screen (a dialog, a link confirmation): the click that leaves the window lands on the screen
     * with the cursor still on the element that opened it.
     */
    default void holdClicksUntilLookAway() {
    }
}
