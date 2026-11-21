package com.ssomar.score.screen.internal.display;

import com.ssomar.score.SCore;
import com.ssomar.score.screen.ScreenMode;
import com.ssomar.score.screen.ScreenSession;
import com.ssomar.score.screen.ScreenSpec;
import com.ssomar.score.screen.Screens;
import com.ssomar.score.screen.internal.AbstractScreenSession;
import com.ssomar.score.screen.internal.ScreenRenderer;
import com.ssomar.score.screen.internal.ScreenData;
import com.ssomar.score.screen.internal.pack.CursorTest;
import com.ssomar.score.screen.internal.pack.ScreenPack;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/** FLOATING (1.19.4+) and PINNED (1.21.4+, with the SCore Screens pack) renderers. */
public final class DisplayScreenRenderer implements ScreenRenderer {

    private final boolean pinned;

    private DisplayScreenRenderer(boolean pinned) {
        this.pinned = pinned;
    }

    /** Plug both renderers into {@link Screens} (no-op before 1.19.4: every screen opens as a chest). */
    public static void install() {
        if (!SCore.is1v19v4Plus()) return;
        Screens.registerRenderer(new DisplayScreenRenderer(false));
        Screens.registerRenderer(new DisplayScreenRenderer(true));
        DisplayScreenListener.register();
    }

    @Override
    public ScreenMode getMode() {
        return pinned ? ScreenMode.PINNED : ScreenMode.FLOATING;
    }

    @Override
    public String refusal(Player p, ScreenSpec spec) {
        if (isBedrock(p)) return "bedrock";
        if (p.getGameMode() == GameMode.SPECTATOR) return "spectator";
        // a screen replacing another one: the player still sits on the seat of the first one
        ScreenSession current = Screens.get(p);
        boolean onOurSeat = current instanceof DisplayScreenSession && p.getVehicle() != null
                && p.getVehicle().equals(((DisplayScreenSession) current).getSeat());
        if (p.isInsideVehicle() && !onOurSeat) return "riding";
        if (p.isGliding()) return "gliding";
        if (p.isSleeping()) return "sleeping";
        if (p.getFallDistance() > 1 && !isOnGround(p)) return "falling";
        if (inLiquid(p)) return "in water";
        if (DisplayScreenListener.recentlyDamaged(p)) return "combat";
        if (pinned) return ScreenPack.pinnedRefusal(p);
        return null;
    }

    @Override
    public AbstractScreenSession open(Player p, ScreenSpec spec) {
        if (pinned && !CursorTest.ID.equals(spec.getId()) && ScreenData.cursorSeen(p.getUniqueId()) == null) spec = CursorTest.wrap(spec);
        DisplayScreenSession s = new DisplayScreenSession(p, spec, pinned);
        s.start();
        return s;
    }

    /** Floodgate gives Bedrock players a UUID whose high bits are 0; Bedrock has no shaders and partial displays */
    static boolean isBedrock(Player p) {
        return p.getUniqueId().getMostSignificantBits() == 0;
    }

    @SuppressWarnings("deprecation")
    private static boolean isOnGround(Player p) {
        return p.isOnGround();
    }

    private static boolean inLiquid(Player p) {
        try {
            if (p.isInWater()) return true;
        } catch (Throwable ignored) {
        }
        Material m = p.getLocation().getBlock().getType();
        return m == Material.WATER || m == Material.LAVA;
    }
}
