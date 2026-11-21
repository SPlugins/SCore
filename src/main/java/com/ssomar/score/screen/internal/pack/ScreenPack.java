package com.ssomar.score.screen.internal.pack;

import com.ssomar.score.SCore;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.config.Config;
import com.ssomar.score.pack.custom.PackSettings;
import com.ssomar.score.pack.spigot.InjectSpigot;
import com.ssomar.score.screen.internal.ScreenData;
import com.ssomar.score.utils.logging.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The "SCore Screens" resource pack: text shaders (one variant per client generation, as overlays) that turn a marked
 * glyph into a mouse-like cursor and place the image tiles of PINNED screens, plus the cursor glyph. Opt-in
 * ({@code screens.pinned-pack: true}) and sent to the players with {@code score.hub} only, on top of the other
 * packs. PINNED opens only for a player whose client reported this pack as loaded.
 */
public final class ScreenPack {

    /** U+10FFF0, a supplementary private-use code point, drawn by the pack in the default font */
    public static final String POINTER_CHAR = "􏿰";
    /** colour key (254, 254, 254) read by the shader, then the cursor glyph */
    public static final String POINTER_TEXT = "§x§f§e§f§e§f§e" + POINTER_CHAR;

    public static final UUID PACK_ID = UUID.nameUUIDFromBytes("score:screens-pack".getBytes(StandardCharsets.UTF_8));

    enum Status {SENT, LOADED, FAILED}

    private static final Map<UUID, Status> STATUS = new ConcurrentHashMap<>();
    private static PackSettings settings;
    private static boolean listenerRegistered;

    private ScreenPack() {
    }

    public static boolean isActive() {
        return settings != null;
    }

    /** Build and host the pack if {@code screens.pinned-pack} is on (startup and /score reload). */
    public static synchronized void init() {
        boolean wanted = GeneralConfig.getInstance().isScreensPinnedPack() && GeneralConfig.getInstance().isScreensEnabled()
                && SCore.is1v21v4Plus();
        if (!listenerRegistered && SCore.is1v21v4Plus()) {
            Bukkit.getPluginManager().registerEvents(new PackListener(), SCore.plugin);
            listenerRegistered = true;
        }
        if (!wanted) {
            stop();
            return;
        }
        if (settings != null) return;
        try {
            File zip = build();
            settings = new PackSettings(SCore.plugin, PACK_ID, zip.getAbsolutePath(), "", false, false);
            InjectSpigot.INSTANCE.registerInjector(settings.getInjector());
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7SCore Screens pack ready (PINNED menus), sent to the players with score.hub");
            for (Player p : Bukkit.getOnlinePlayers()) if (p.hasPermission("score.hub")) send(p, 20);
        } catch (Throwable t) {
            settings = null;
            SCore.plugin.getLogger().warning("[SCore Screens] can't build the screens pack: " + t);
        }
    }

    public static synchronized void stop() {
        if (settings == null) return;
        InjectSpigot.INSTANCE.unregisterInjector(settings.getInjector());
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (STATUS.remove(p.getUniqueId()) == null) continue;
            try {
                p.removeResourcePack(PACK_ID);
            } catch (Throwable ignored) {
            }
        }
        settings = null;
    }

    /** The pack zip, rebuilt from the jar with fixed entry times so its SHA-1 only changes with SCore. */
    static File build() throws IOException {
        File dir = new File(SCore.dataFolder, "screens");
        if (!dir.exists()) dir.mkdirs();
        File zip = new File(dir, "score-screens-pack.zip");
        String list = read("screens-pack/files.txt");
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip))) {
            for (String line : list.split("\n")) {
                String name = line.trim();
                if (name.isEmpty()) continue;
                ZipEntry e = new ZipEntry(name);
                e.setTime(315532800000L);
                out.putNextEntry(e);
                try (InputStream in = Config.getResource(SCore.classLoader, "screens-pack/" + name)) {
                    if (in == null) throw new FileNotFoundException("screens-pack/" + name);
                    copy(in, out);
                }
                out.closeEntry();
            }
        }
        return zip;
    }

    private static String read(String path) throws IOException {
        try (InputStream in = Config.getResource(SCore.classLoader, path)) {
            if (in == null) throw new FileNotFoundException(path);
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            copy(in, b);
            return new String(b.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
    }

    /** send after the other server packs (EI's pack is sent at join + 0, 2, 4 s), so the shaders are on top */
    static void send(Player p, long delayTicks) {
        final PackSettings s = settings;
        if (s == null) return;
        SCore.schedulerHook.runAsyncTask(() -> {
            String url = s.getHostedPath();
            if (url == null) {
                SCore.plugin.getLogger().warning("[SCore Screens] the screens pack can't be hosted (self-hosting unreachable): PINNED menus are off");
                return;
            }
            SCore.schedulerHook.runEntityTask(() -> {
                if (!p.isOnline()) return;
                try {
                    p.addResourcePack(PACK_ID, url, s.getHash(), "", false);
                    STATUS.put(p.getUniqueId(), Status.SENT);
                } catch (Throwable t) {
                    STATUS.put(p.getUniqueId(), Status.FAILED);
                }
            }, null, p, 1L);
        }, delayTicks);
    }

    /** null if PINNED can open for this player, else a short reason */
    public static String pinnedRefusal(Player p) {
        if (!SCore.is1v21v4Plus()) return "version";
        if (settings == null) return "screens.pinned-pack is off";
        Status s = STATUS.get(p.getUniqueId());
        if (s != Status.LOADED) return s == null || s == Status.SENT ? "pack not loaded" : "pack refused";
        if (Boolean.FALSE.equals(ScreenData.cursorSeen(p.getUniqueId()))) return "cursor not visible";
        return null;
    }

    /**
     * images need the shader. 26.3 draws item frame maps before the see-through text, which hid maps under see-through
     * panels; ours are depth-tested, so the 26.3 shader has the map branch too.
     */
    public static boolean imagesAllowed(Player p) {
        return STATUS.get(p.getUniqueId()) == Status.LOADED;
    }

    public static final class PackListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR)
        public void onJoin(PlayerJoinEvent e) {
            if (settings != null && e.getPlayer().hasPermission("score.hub"))
                send(e.getPlayer(), 20 + 40L * Math.max(1, com.ssomar.score.pack.custom.PackManager.getInstance().getPacks().size()));
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent e) {
            STATUS.remove(e.getPlayer().getUniqueId());
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onStatus(PlayerResourcePackStatusEvent e) {
            try {
                if (!PACK_ID.equals(e.getID())) return;
            } catch (Throwable old) {
                return;
            }
            switch (e.getStatus().name()) {
                case "SUCCESSFULLY_LOADED":
                    STATUS.put(e.getPlayer().getUniqueId(), Status.LOADED);
                    break;
                case "ACCEPTED":
                case "DOWNLOADED":
                    break;
                default:
                    STATUS.put(e.getPlayer().getUniqueId(), Status.FAILED);
            }
        }
    }
}
