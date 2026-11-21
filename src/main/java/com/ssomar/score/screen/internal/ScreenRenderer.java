package com.ssomar.score.screen.internal;

import com.ssomar.score.screen.ScreenMode;
import com.ssomar.score.screen.ScreenSpec;
import org.bukkit.entity.Player;

/** A way to show screens other than the chest (FLOATING, PINNED). */
public interface ScreenRenderer {

    ScreenMode getMode();

    /** null if the screen can open for this player in this mode, else a short reason ("riding", "no pack"...) */
    String refusal(Player player, ScreenSpec spec);

    /** Build and show the session. May throw: the caller falls back to the next mode. */
    AbstractScreenSession open(Player player, ScreenSpec spec);
}
