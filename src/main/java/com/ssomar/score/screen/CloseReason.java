package com.ssomar.score.screen;

/** Why a screen was closed. */
public enum CloseReason {
    /** the player closed it (sneak, chest closed) */
    PLAYER,
    DAMAGE,
    TELEPORT,
    WORLD,
    QUIT,
    DEATH,
    /** closed by code (replaced by another screen, plugin disabled, reload) */
    PLUGIN
}
