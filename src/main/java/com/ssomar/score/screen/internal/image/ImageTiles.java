package com.ssomar.score.screen.internal.image;

import com.ssomar.score.SCore;
import com.ssomar.score.screen.ScreenLayout;
import com.ssomar.score.screen.ScreenWidget;
import com.ssomar.score.screen.internal.display.DisplayScreenSession;
import com.ssomar.score.screen.internal.display.ScreenGeometry;
import com.ssomar.score.screen.internal.display.Spawn;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * An image widget of a PINNED screen: 1 to 8 map tiles of 128 x 127 pixels. Each tile is a map in an invisible
 * fixed item frame hung in the player's own eye block, on the side where the eye is deepest inside the frame's
 * culling box, so the client always draws it; the SCore Screens shader then moves the map quad onto the panel at
 * the place written in the map's first row.
 */
public final class ImageTiles {

    private final DisplayScreenSession session;
    private final Player player;
    /** what is drawn: image URL and rectangle in centre-origin panel units */
    private String url;
    private double cx, cy, w, h;
    private final List<ItemFrame> frames = new ArrayList<>();
    private final List<Integer> slots = new ArrayList<>();
    private int cols, rows;
    private int generation;

    /** an image widget, drawn over the whole widget */
    public ImageTiles(DisplayScreenSession session, ScreenWidget widget) {
        this(session, widget.getImageUrl(), rect(session, widget));
    }

    /** an image in a rectangle {centreX, centreY, width, height} (centre-origin panel units), e.g. a plugin logo */
    public ImageTiles(DisplayScreenSession session, String url, double[] rect) {
        this.session = session;
        this.player = session.getPlayer();
        set(url, rect);
        grid();
        spawn();
        draw();
    }

    private static double[] rect(DisplayScreenSession session, ScreenWidget w) {
        ScreenLayout l = session.getLayout();
        double[] c = ScreenGeometry.centre(w.getU(), w.getV(), w.getWidth(), w.getHeight(), l.getWidth(), l.getHeight());
        return new double[]{c[0], c[1], w.getWidth(), w.getHeight()};
    }

    private void set(String url, double[] r) {
        this.url = url;
        this.cx = r[0];
        this.cy = r[1];
        this.w = r[2];
        this.h = r[3];
    }

    private void grid() {
        rows = h >= 0.45 ? 2 : 1;
        cols = (int) Math.max(1, Math.min(4, Math.round(w / h * rows * 127 / 128.0)));
    }

    /** pooled map slots used by this screen's other images, so two images never share a map */
    private int nextFreeSlot(List<Integer> taken) {
        for (int i = 0; i < ScreenImages.POOL; i++) if (!taken.contains(i) && !session.usedMapSlots().contains(i)) return i;
        return -1;
    }

    private void spawn() {
        Location eye = session.getEye();
        BlockFace face = bestFace(eye);
        for (int i = 0; i < cols * rows; i++) {
            int slot = nextFreeSlot(slots);
            if (slot < 0) break;
            MapView view = ScreenImages.map(slot, eye.getWorld());
            if (view == null) break;
            slots.add(slot);
            session.usedMapSlots().add(slot);
            ItemStack item = new ItemStack(Material.FILLED_MAP);
            MapMeta meta = (MapMeta) item.getItemMeta();
            meta.setMapView(view);
            item.setItemMeta(meta);
            // an empty first content so the frame never shows a blank map before the image arrives
            ScreenImages.show(slot, player, ScreenImages.tile(new byte[cols * 128 * rows * 127], cols * 128, i % cols, i / cols, 0, 0, 0, 0));
            Location at = eye.getBlock().getLocation().add(0.5, 0.5, 0.5);
            ItemFrame frame = Spawn.spawn(player, at, ItemFrame.class, f -> {
                f.setFacingDirection(face, true);
                f.setVisible(false);
                f.setFixed(true);
                f.setInvulnerable(true);
                f.setSilent(true);
                f.setItem(item, false);
            });
            frames.add(frame);
            session.track(frame);
        }
    }

    /** the frame face whose culling box (frame box + 0.5) holds the eye with the largest margin */
    static BlockFace bestFace(Location eye) {
        double fx = eye.getX() - Math.floor(eye.getX()), fz = eye.getZ() - Math.floor(eye.getZ());
        BlockFace best = BlockFace.SOUTH;
        double bestMargin = -1;
        Object[][] options = {{BlockFace.EAST, fx, 0.03125}, {BlockFace.WEST, fx, 0.96875}, {BlockFace.SOUTH, fz, 0.03125}, {BlockFace.NORTH, fz, 0.96875}};
        for (Object[] o : options) {
            double margin = 0.53125 - Math.abs((Double) o[1] - (Double) o[2]);
            if (margin > bestMargin) {
                bestMargin = margin;
                best = (BlockFace) o[0];
            }
        }
        return best;
    }

    public void update(ScreenWidget widget) {
        update(widget.getImageUrl(), rect(session, widget));
    }

    /** redraws only when the image or its place changed (the grid is kept) */
    public void update(String newUrl, double[] r) {
        boolean urlChanged = newUrl == null ? url != null : !newUrl.equals(url);
        boolean moved = r[0] != cx || r[1] != cy || r[2] != w || r[3] != h;
        set(newUrl, r);
        if (urlChanged || moved) draw();
    }

    private void draw() {
        final int gen = ++generation;
        final String url = this.url;
        if (url == null || url.isEmpty()) {
            paint(null);
            return;
        }
        ScreenImages.load(url).thenAccept(img -> SCore.schedulerHook.runEntityTask(() -> {
            if (gen != generation || !session.isOpen()) return;
            paint(img);
        }, null, player, 0L));
    }

    private void paint(BufferedImage img) {
        int cw = cols * 128, ch = rows * 127;
        byte[] canvas;
        try {
            canvas = ScreenImages.toMapColours(img, cw, ch);
        } catch (Throwable t) {
            canvas = new byte[cw * ch]; // a broken image shows nothing rather than failing the screen
        }
        double tw = w / cols, th = h / rows;
        double left = cx - w / 2, top = cy + h / 2;
        for (int i = 0; i < slots.size(); i++) {
            int tx = i % cols, ty = i / cols;
            double cx = left + tw * (tx + 0.5), cy = top - th * (ty + 0.5);
            ScreenImages.show(slots.get(i), player, ScreenImages.tile(canvas, cw, tx, ty, cx, cy, tw / 2, th / 2));
        }
    }

    public void remove() {
        generation++;
        for (ItemFrame f : frames) {
            try {
                f.remove();
            } catch (Throwable ignored) {
            }
        }
        frames.clear();
        for (int s : slots) {
            ScreenImages.hide(s, player);
            session.usedMapSlots().remove((Integer) s);
        }
        slots.clear();
    }
}
