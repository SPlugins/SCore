package com.ssomar.score.screen.internal.display;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * World guard after a click that closed or replaced a screen: the mouse button is still down when the click box
 * disappears, so the client would break, use or attack what is behind. While the guard is up those actions are
 * cancelled. It lasts 500 ms, each blocked action extends it by 450 ms, 1.5 s at most; releasing the button
 * (BlockDamageAbortEvent) ends it 60 ms later. Pure (the clock is passed in), unit tested.
 */
public final class ClickGuard {

    public static final long ARM_MS = 500, EXTEND_MS = 450, MAX_MS = 1500, RELEASE_MS = 60;
    /** a close this soon after a click is taken as caused by the click */
    public static final long RECENT_CLICK_MS = 250;

    private final Map<UUID, long[]> guards = new ConcurrentHashMap<>();

    public void arm(UUID id, long now) {
        guards.put(id, new long[]{now, now + ARM_MS});
    }

    public boolean isUp(UUID id, long now) {
        long[] g = guards.get(id);
        if (g == null) return false;
        if (now >= g[1]) {
            guards.remove(id);
            return false;
        }
        return true;
    }

    /** a world action was blocked: the button is still held */
    public void extend(UUID id, long now) {
        long[] g = guards.get(id);
        if (g != null) g[1] = Math.max(g[1], Math.min(g[0] + MAX_MS, now + EXTEND_MS));
    }

    /** the button was released */
    public void release(UUID id, long now) {
        long[] g = guards.get(id);
        if (g != null) g[1] = Math.min(g[1], now + RELEASE_MS);
    }

    public void forget(UUID id) {
        guards.remove(id);
    }
}
