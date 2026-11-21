package com.ssomar.score.screen;

import com.ssomar.score.SCore;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.screen.internal.AbstractScreenSession;
import com.ssomar.score.screen.internal.ChestScreenSession;
import com.ssomar.score.screen.internal.ScreenRenderer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Entry point of SCore Screens: open, find and close screens. One screen per player; opening a new one replaces
 * the current one. Every call must be made on the player's thread.
 */
public final class Screens {

    private static final Map<UUID, AbstractScreenSession> SESSIONS = new ConcurrentHashMap<>();
    private static final Map<ScreenMode, ScreenRenderer> RENDERERS = new EnumMap<>(ScreenMode.class);
    /** per-player forced mode (e.g. "/score hub classic"), cleared on quit */
    private static final Map<UUID, ScreenMode> PLAYER_MODE = new ConcurrentHashMap<>();

    private Screens() {
    }

    /** Used by the SCore runtime to plug the FLOATING and PINNED renderers. */
    public static void registerRenderer(ScreenRenderer renderer) {
        RENDERERS.put(renderer.getMode(), renderer);
    }

    public static void setPlayerMode(Player player, ScreenMode mode) {
        if (mode == null || mode == ScreenMode.AUTO) PLAYER_MODE.remove(player.getUniqueId());
        else PLAYER_MODE.put(player.getUniqueId(), mode);
    }

    public static ScreenOpenResult open(Player player, ScreenSpec spec) {
        AbstractScreenSession previous = SESSIONS.get(player.getUniqueId());
        String note = null;
        for (ScreenMode mode : candidates(player, spec)) {
            AbstractScreenSession session = null;
            try {
                if (mode == ScreenMode.CHEST) {
                    // a chest replaces a chest without the close event being taken as the player's
                    if (previous != null) end(previous, CloseReason.PLUGIN);
                    ChestScreenSession chest = new ChestScreenSession(player, spec);
                    SESSIONS.put(player.getUniqueId(), chest);
                    chest.show();
                    return new ScreenOpenResult(true, ScreenMode.CHEST, note);
                }
                ScreenRenderer r = RENDERERS.get(mode);
                if (r == null) continue;
                String refusal = r.refusal(player, spec);
                if (refusal != null) {
                    if (note == null) note = refusal;
                    continue;
                }
                if (previous != null) end(previous, CloseReason.PLUGIN);
                previous = null;
                session = r.open(player, spec);
                SESSIONS.put(player.getUniqueId(), session);
                return new ScreenOpenResult(true, mode, note);
            } catch (Throwable t) {
                SCore.plugin.getLogger().log(Level.WARNING, "[SCore Screens] can't open " + spec.getId() + " in " + mode + " for " + player.getName(), t);
                if (session != null) end(session, CloseReason.PLUGIN);
                SESSIONS.remove(player.getUniqueId());
                if (note == null) note = "error";
            }
        }
        return new ScreenOpenResult(false, null, note);
    }

    /** Modes to try, best first. CHEST is always last. */
    static List<ScreenMode> candidates(Player player, ScreenSpec spec) {
        ScreenMode wanted = PLAYER_MODE.get(player.getUniqueId());
        if (wanted == null) wanted = spec.getMode();
        if (wanted == ScreenMode.AUTO) wanted = GeneralConfig.getInstance().getScreensPreferMode();
        List<ScreenMode> list = new ArrayList<>();
        if (!GeneralConfig.getInstance().isScreensEnabled() || isOff(spec.getId())) wanted = ScreenMode.CHEST;
        switch (wanted) {
            case AUTO:
            case PINNED:
                list.add(ScreenMode.PINNED);
            case FLOATING:
                list.add(ScreenMode.FLOATING);
            default:
                list.add(ScreenMode.CHEST);
        }
        return list;
    }

    private static boolean isOff(String id) {
        for (String off : GeneralConfig.getInstance().getScreensOff()) {
            String o = off.trim().toLowerCase();
            String i = id.toLowerCase();
            if (i.equals(o) || i.startsWith(o + ":") || i.endsWith(":" + o)) return true;
        }
        return false;
    }

    public static ScreenSession get(Player player) {
        return SESSIONS.get(player.getUniqueId());
    }

    public static boolean isOpen(Player player) {
        return SESSIONS.containsKey(player.getUniqueId());
    }

    public static void close(Player player, CloseReason reason) {
        AbstractScreenSession s = SESSIONS.remove(player.getUniqueId());
        if (reason == CloseReason.QUIT) PLAYER_MODE.remove(player.getUniqueId());
        if (s != null) end(s, reason);
    }

    /** Close every screen (reload, disable). */
    public static void closeAll() {
        for (AbstractScreenSession s : new ArrayList<>(SESSIONS.values())) {
            SESSIONS.remove(s.getPlayer().getUniqueId());
            end(s, CloseReason.PLUGIN);
        }
    }

    private static void end(AbstractScreenSession s, CloseReason reason) {
        try {
            s.endSession(reason);
        } catch (Throwable t) {
            SCore.plugin.getLogger().log(Level.WARNING, "[SCore Screens] error while closing " + s.getSpec().getId(), t);
        }
    }
}
