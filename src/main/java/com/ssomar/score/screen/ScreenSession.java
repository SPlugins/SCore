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
}
