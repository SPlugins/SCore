package com.ssomar.score.screen;

/**
 * How a screen is shown.
 * <ul>
 *     <li>AUTO: PINNED when the player can have it, else FLOATING, else CHEST.</li>
 *     <li>PINNED: panel fixed to the camera, cursor moved by the head (needs the SCore Screens resource pack).</li>
 *     <li>FLOATING: panel fixed in front of the player, the crosshair is the cursor (1.19.4+).</li>
 *     <li>CHEST: a classic chest menu built from the same layout (every version).</li>
 * </ul>
 */
public enum ScreenMode {
    AUTO, PINNED, FLOATING, CHEST;

    public static ScreenMode fromString(String s, ScreenMode def) {
        if (s == null) return def;
        try {
            return valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return def;
        }
    }
}
