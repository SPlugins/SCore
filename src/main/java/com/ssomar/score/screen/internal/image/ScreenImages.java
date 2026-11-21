package com.ssomar.score.screen.internal.image;

import com.ssomar.score.SCore;
import com.ssomar.score.screen.internal.ScreenData;
import com.ssomar.score.screen.internal.display.ScreenGeometry;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Images of the PINNED screens: download and conversion to map colours (async, cached), and a small pool of maps
 * reused for every player (each map has a per-player renderer, so 8 maps serve all the open screens).
 */
public final class ScreenImages {

    public static final int POOL = 8;
    static final int MAX_BYTES = 4 * 1024 * 1024;

    private static final Map<String, CompletableFuture<BufferedImage>> CACHE = new ConcurrentHashMap<>();
    private static final List<MapView> MAPS = new ArrayList<>();
    private static final List<TileRenderer> RENDERERS = new ArrayList<>();

    private ScreenImages() {
    }

    /** the image at this URL, null if it can't be read (format not supported by ImageIO, e.g. WebP) */
    public static CompletableFuture<BufferedImage> load(String url) {
        return CACHE.computeIfAbsent(url, u -> {
            CompletableFuture<BufferedImage> f = new CompletableFuture<>();
            SCore.schedulerHook.runAsyncTask(() -> {
                try {
                    f.complete(download(u));
                } catch (Throwable t) {
                    f.complete(null);
                }
            }, 0);
            return f;
        });
    }

    private static BufferedImage download(String url) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(3000);
        c.setReadTimeout(3000);
        c.setRequestProperty("User-Agent", "SCore-Screens");
        c.setInstanceFollowRedirects(true);
        if (c.getResponseCode() != 200) return null;
        try (InputStream in = c.getInputStream()) {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                b.write(buf, 0, n);
                if (b.size() > MAX_BYTES) return null;
            }
            return ImageIO.read(new ByteArrayInputStream(b.toByteArray()));
        }
    }

    /**
     * Aspect-fit the image into a canvas of map colours, Floyd-Steinberg dithered. Transparent pixels and the
     * margins are colour 0 (transparent on a map).
     */
    @SuppressWarnings("deprecation")
    public static byte[] toMapColours(BufferedImage img, int width, int height) {
        byte[] out = new byte[width * height];
        if (img == null) return out;
        double k = Math.min(width / (double) img.getWidth(), height / (double) img.getHeight());
        int w = Math.max(1, (int) Math.round(img.getWidth() * k)), h = Math.max(1, (int) Math.round(img.getHeight() * k));
        BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, k < 1 ? RenderingHints.VALUE_INTERPOLATION_BILINEAR : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(img, 0, 0, w, h, null);
        g.dispose();
        int ox = (width - w) / 2, oy = (height - h) / 2;
        float[][] err = new float[h + 1][(w + 2) * 3];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = scaled.getRGB(x, y);
                if ((argb >>> 24) < 128) continue;
                float r = clamp(((argb >> 16) & 0xFF) + err[y][(x + 1) * 3]);
                float gr = clamp(((argb >> 8) & 0xFF) + err[y][(x + 1) * 3 + 1]);
                float b = clamp((argb & 0xFF) + err[y][(x + 1) * 3 + 2]);
                byte c = MapPalette.matchColor(new Color((int) r, (int) gr, (int) b));
                out[(oy + y) * width + ox + x] = c;
                Color got = MapPalette.getColor(c);
                float[] e = {r - got.getRed(), gr - got.getGreen(), b - got.getBlue()};
                for (int i = 0; i < 3; i++) {
                    err[y][(x + 2) * 3 + i] += e[i] * 7 / 16f;
                    err[y + 1][x * 3 + i] += e[i] * 3 / 16f;
                    err[y + 1][(x + 1) * 3 + i] += e[i] * 5 / 16f;
                    err[y + 1][(x + 2) * 3 + i] += e[i] / 16f;
                }
            }
        }
        return out;
    }

    private static float clamp(float v) {
        return Math.max(0, Math.min(255, v));
    }

    /* ------------------------------------------------------------------ map pool */

    /** the pooled map n (0 .. POOL-1), created and saved once, null if maps can't be made here */
    static synchronized MapView map(int n, World world) {
        if (n < 0 || n >= POOL) return null;
        try {
            if (MAPS.isEmpty()) {
                List<Integer> ids = new ArrayList<>(ScreenData.mapIds());
                boolean changed = false;
                for (int i = 0; i < POOL; i++) {
                    MapView v = i < ids.size() ? getMap(ids.get(i)) : null;
                    if (v == null) {
                        v = Bukkit.createMap(world);
                        if (i < ids.size()) ids.set(i, v.getId());
                        else ids.add(v.getId());
                        changed = true;
                    }
                    TileRenderer r = new TileRenderer();
                    for (MapRenderer old : new ArrayList<>(v.getRenderers())) v.removeRenderer(old);
                    v.addRenderer(r);
                    v.setTrackingPosition(false);
                    v.setUnlimitedTracking(false);
                    try {
                        v.setLocked(true);
                    } catch (Throwable ignored) {
                    }
                    MAPS.add(v);
                    RENDERERS.add(r);
                }
                if (changed) ScreenData.setMapIds(ids);
            }
            return MAPS.get(n);
        } catch (Throwable t) {
            SCore.plugin.getLogger().warning("[SCore Screens] maps for images unavailable: " + t);
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    private static MapView getMap(int id) {
        try {
            return Bukkit.getMap(id);
        } catch (Throwable t) {
            return null;
        }
    }

    /** set what map n shows to this player (128 x 128 bytes, row 0 = header) and push it now */
    static void show(int n, Player p, byte[] pixels) {
        if (n < 0 || n >= RENDERERS.size()) return;
        RENDERERS.get(n).set(p.getUniqueId(), pixels);
        try {
            p.sendMap(MAPS.get(n));
        } catch (Throwable ignored) {
        }
    }

    static void hide(int n, Player p) {
        if (n >= 0 && n < RENDERERS.size()) RENDERERS.get(n).set(p.getUniqueId(), null);
    }

    /** one tile: header in row 0, then 127 rows of the canvas */
    static byte[] tile(byte[] canvas, int canvasW, int tx, int ty, double cx, double cy, double halfW, double halfH) {
        byte[] px = new byte[128 * 128];
        System.arraycopy(ScreenGeometry.mapHeader(cx, cy, halfW, halfH), 0, px, 0, 128);
        for (int y = 0; y < 127; y++) {
            int src = (ty * 127 + y) * canvasW + tx * 128;
            System.arraycopy(canvas, src, px, (y + 1) * 128, 128);
        }
        return px;
    }

    /** per-player content of one pooled map */
    static final class TileRenderer extends MapRenderer {
        private final Map<UUID, byte[]> content = new ConcurrentHashMap<>();
        private final Set<UUID> drawn = ConcurrentHashMap.newKeySet();

        TileRenderer() {
            super(true);
        }

        void set(UUID id, byte[] px) {
            if (px == null) content.remove(id);
            else content.put(id, px);
            drawn.remove(id);
        }

        @Override
        @SuppressWarnings("deprecation")
        public void render(MapView view, MapCanvas canvas, Player player) {
            byte[] px = content.get(player.getUniqueId());
            if (px == null || !drawn.add(player.getUniqueId())) return;
            for (int y = 0; y < 128; y++)
                for (int x = 0; x < 128; x++) canvas.setPixel(x, y, px[y * 128 + x]);
        }
    }
}
