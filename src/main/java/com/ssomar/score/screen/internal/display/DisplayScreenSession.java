package com.ssomar.score.screen.internal.display;

import com.ssomar.score.SCore;
import com.ssomar.score.screen.*;
import com.ssomar.score.screen.internal.AbstractScreenSession;
import com.ssomar.score.screen.internal.image.ImageTiles;
import com.ssomar.score.screen.internal.pack.ScreenPack;
import com.ssomar.score.utils.scheduler.ScheduledTask;
import com.ssomar.score.utils.strings.StringConverter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;
import java.util.logging.Level;

/**
 * A FLOATING or PINNED screen: the player sits on an invisible seat, an Interaction box around the eyes catches every
 * click, and the panel is made of display entities seen by this player only.
 * <ul>
 * <li>FLOATING: a panel fixed in the world in front of the player (billboard FIXED); the crosshair is the cursor.
 * Its distance adapts so that no block hides it; its angular size does not change.</li>
 * <li>PINNED: every element sits at the eye with billboard CENTER, so the panel stays glued to the screen. A pointer
 * one block ahead is mirrored by the SCore Screens shader and moves with the head like a mouse.</li>
 * </ul>
 * Layout updates are applied by difference: unchanged widgets keep their entities.
 */
public final class DisplayScreenSession extends AbstractScreenSession {

    /** FLOATING panel: angular size compared with PINNED (which fills a 16:9 FOV 70 screen) */
    static final double FLOATING_SIZE = 0.8;
    static final double FLOATING_DISTANCE = 2.5, FLOATING_MIN_DISTANCE = 0.45;
    /**
     * PINNED: the panel sits 0.25 block from the eye and its backdrop 0.3 (on screen the size is the same, everything
     * scales with the distance), so almost no block can stand in front of it. The blocks that can are sent as air
     * to this player, up to the edges of a very wide screen (32:9, high FOV).
     */
    static final double PINNED_DISTANCE = 0.25, PINNED_BACKDROP_DEPTH = 0.3;
    /** tan of the largest half angles a screen can show: 80 degrees across, 60 degrees up and down */
    static final double SCREEN_TAN_X = Math.tan(Math.toRadians(80)), SCREEN_TAN_Y = Math.tan(Math.toRadians(60));

    private final boolean pinned;
    private final Location feet;
    private final float anchorYaw, anchorPitch;
    private Location eye;
    /** distance of the panel plane, and size of one panel unit at that distance */
    private double dist, unit;

    private BlockDisplay seat;
    private Interaction box;
    private TextDisplay backdrop, plate, pointer;
    private final Map<String, Parts> parts = new LinkedHashMap<>();
    private final List<Entity> extra = new ArrayList<>();
    private final Set<Integer> mapSlots = new HashSet<>();

    boolean built;
    boolean closing;
    String hovered;
    float lastYaw = Float.NaN, lastPitch = Float.NaN;
    long lastClickAt;
    boolean inHandler;
    private ScheduledTask buildTask, pollTask;

    /* PINNED: what is changed on the player, restored on close */
    private final List<Location> hiddenBlocks = new ArrayList<>();
    private int savedSlot = -1;
    private boolean savedInvisible;
    private WeatherType savedWeather;
    private boolean handHidden;
    private static boolean imageWarned;

    private static final class Parts {
        ScreenWidget widget;
        boolean hovered;
        TextDisplay box, text;
        ItemDisplay icon;
        String iconMaterial;
        ImageTiles image;
    }

    public DisplayScreenSession(Player player, ScreenSpec spec, boolean pinned) {
        super(player, spec);
        this.pinned = pinned;
        this.feet = player.getLocation().clone();
        this.anchorYaw = feet.getYaw();
        this.anchorPitch = Math.max(-60f, Math.min(60f, feet.getPitch()));
    }

    @Override
    public ScreenMode getMode() {
        return pinned ? ScreenMode.PINNED : ScreenMode.FLOATING;
    }

    public boolean isPinned() {
        return pinned;
    }

    /* ------------------------------------------------------------------ build */

    /** Seat and click box now (so a mouse button still held from a previous screen hits the box), visuals 2 ticks later. */
    public void start() {
        Location eyeNow = player.getEyeLocation();
        seat = Spawn.spawn(player, feet.clone(), BlockDisplay.class, d -> {
            d.setBlock(Material.AIR.createBlockData());
            d.setRotation(0, 0);
        });
        if (!seat.addPassenger(player)) throw new IllegalStateException("can't seat the player");
        box = Spawn.spawn(player, eyeNow.clone().subtract(0, 1.5, 0), Interaction.class, i -> {
            i.setInteractionWidth(3f);
            i.setInteractionHeight(3f);
            i.setResponsive(false);
        });
        buildTask = SCore.schedulerHook.runEntityTask(this::build, null, player, 2L);
    }

    private void build() {
        if (!open) return;
        try {
            eye = player.getEyeLocation().clone();
            eye.setYaw(anchorYaw);
            eye.setPitch(anchorPitch);
            Location boxAt = eye.clone().subtract(0, 1.5, 0);
            if (box.getLocation().distanceSquared(boxAt) > 0.01) box.teleport(boxAt);
            if (pinned) {
                dist = PINNED_DISTANCE;
                unit = PINNED_DISTANCE;
                hideHand();
                hideNearBlocks();
            } else {
                dist = clearDistance();
                unit = FLOATING_SIZE * dist;
            }
            if (pinned && spec.getBackdrop() != 0) {
                double bw = 2 * SCREEN_TAN_X * PINNED_BACKDROP_DEPTH / unit * 1.1, bh = 2 * SCREEN_TAN_Y * PINNED_BACKDROP_DEPTH / unit * 1.1;
                backdrop = boxDisplay(new double[]{0, 0, bw, bh}, spec.getBackdrop(), PINNED_BACKDROP_DEPTH / dist);
            }
            double m = 0.03;
            plate = boxDisplay(new double[]{0, 0, layout.getWidth() + m * 2, layout.getHeight() + m * 2}, WidgetVisual.PLATE, 1.008);
            for (ScreenWidget w : layout.getWidgets()) draw(w);
            if (pinned) spawnPointer();
            built = true;
            pollTask = SCore.schedulerHook.runEntityRepeatingTask(this::poll, null, player, 1L, 1L);
            poll();
        } catch (Throwable t) {
            SCore.plugin.getLogger().log(Level.WARNING, "[SCore Screens] can't build " + spec.getId() + " for " + player.getName(), t);
            Screens.close(player, CloseReason.PLUGIN);
        }
    }

    /** FLOATING: the farthest distance (up to 2.5) where no block stands between the eye and the panel */
    private double clearDistance() {
        double best = FLOATING_DISTANCE;
        World world = eye.getWorld();
        double hw = layout.getWidth() / 2 * FLOATING_SIZE, hh = layout.getHeight() / 2 * FLOATING_SIZE;
        double[][] probes = {{0, 0}, {-hw, hh}, {hw, hh}, {-hw, -hh}, {hw, -hh}, {0, hh}, {0, -hh}, {-hw, 0}, {hw, 0}};
        double[] n = ScreenGeometry.dir(anchorYaw, anchorPitch);
        for (double[] p : probes) {
            double[] d = ScreenGeometry.towards(anchorYaw, anchorPitch, p[0], p[1]);
            double cos = d[0] * n[0] + d[1] * n[1] + d[2] * n[2];
            RayTraceResult r = world.rayTraceBlocks(eye, new Vector(d[0], d[1], d[2]), FLOATING_DISTANCE / cos + 0.2,
                    FluidCollisionMode.NEVER, true);
            if (r == null || r.getHitPosition() == null) continue;
            double along = r.getHitPosition().subtract(eye.toVector()).dot(new Vector(n[0], n[1], n[2]));
            best = Math.min(best, along - 0.12);
        }
        return Math.max(FLOATING_MIN_DISTANCE, best);
    }

    /* ------------------------------------------------------------------ entities */

    private Location origin() {
        Location l = eye.clone();
        if (!pinned) {
            l.setYaw(anchorYaw + 180);
            l.setPitch(-anchorPitch);
        }
        return l;
    }

    private void common(Display d) {
        d.setBillboard(pinned ? Display.Billboard.CENTER : Display.Billboard.FIXED);
        d.setBrightness(new Display.Brightness(15, 15));
        d.setInterpolationDuration(0);
        d.setViewRange(2f);
    }

    private static void text(TextDisplay t) {
        t.setLineWidth(100000);
        t.setAlignment(TextDisplay.TextAlignment.CENTER);
        t.setShadowed(false);
        t.setSeeThrough(false);
        t.setDefaultBackground(false);
    }

    /** a box: one-space line with a coloured background. {@code depth} is relative to the panel distance (1 = on it) */
    private TextDisplay boxDisplay(double[] rect, int argb, double depth) {
        return Spawn.spawn(player, origin(), TextDisplay.class, t -> {
            common(t);
            text(t);
            t.setText(" ");
            t.setBackgroundColor(Color.fromARGB(argb));
            t.setTransformation(boxTransformation(rect, depth));
        });
    }

    private Transformation boxTransformation(double[] rect, double depth) {
        double[] tr = WidgetVisual.boxTransform(rect);
        return new Transformation(new Vector3f((float) (tr[0] * unit), (float) (tr[1] * unit), (float) (-dist * depth)),
                new Quaternionf(), new Vector3f((float) (tr[2] * unit), (float) (tr[3] * unit), 1f), new Quaternionf());
    }

    private Transformation textTransformation(WidgetVisual v) {
        float s = (float) (v.textScale * unit);
        return new Transformation(new Vector3f((float) (v.textX * unit), (float) (v.textY * unit), (float) -dist),
                new Quaternionf(), new Vector3f(s, s, s), new Quaternionf());
    }

    private Transformation iconTransformation(WidgetVisual v) {
        float s = (float) (v.iconSize * unit);
        return new Transformation(new Vector3f((float) (v.iconX * unit), (float) (v.iconY * unit), (float) (-dist * 0.994)),
                new Quaternionf(), new Vector3f(s, s, s * 0.05f), new Quaternionf());
    }

    private void draw(ScreenWidget w) {
        Parts p = new Parts();
        parts.put(w.getId(), p);
        apply(p, w, w.getId().equals(hovered));
    }

    /** create, update or remove the entities of one widget so they match its visual */
    private void apply(Parts p, ScreenWidget w, boolean hover) {
        p.widget = w;
        p.hovered = hover;
        String txt = w.getText() == null ? "" : StringConverter.coloredString(w.getText());
        WidgetVisual v = WidgetVisual.of(w, txt, layout.getWidth(), layout.getHeight(), hover, pinned && ScreenPack.imagesAllowed(player));

        if (v.box != null) {
            if (p.box == null) p.box = boxDisplay(v.box, v.boxColor, 1.004);
            else {
                p.box.setBackgroundColor(Color.fromARGB(v.boxColor));
                p.box.setTransformation(boxTransformation(v.box, 1.004));
            }
        } else p.box = remove(p.box);

        if (v.text != null) {
            if (p.text == null) {
                final WidgetVisual fv = v;
                p.text = Spawn.spawn(player, origin(), TextDisplay.class, t -> {
                    common(t);
                    text(t);
                    t.setBackgroundColor(Color.fromARGB(0));
                    t.setText(fv.text);
                    t.setTransformation(textTransformation(fv));
                });
            } else {
                if (!v.text.equals(p.text.getText())) p.text.setText(v.text);
                p.text.setTransformation(textTransformation(v));
            }
        } else p.text = remove(p.text);

        if (v.icon != null) {
            ItemStack item = icon(v.icon);
            if (p.icon == null || !v.icon.equals(p.iconMaterial)) {
                p.icon = remove(p.icon);
                final WidgetVisual fv = v;
                p.icon = Spawn.spawn(player, origin(), ItemDisplay.class, d -> {
                    common(d);
                    d.setItemStack(item);
                    d.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GUI);
                    d.setTransformation(iconTransformation(fv));
                });
                p.iconMaterial = v.icon;
            } else p.icon.setTransformation(iconTransformation(v));
        } else {
            p.icon = remove(p.icon);
            p.iconMaterial = null;
        }

        if (v.image) {
            try {
                if (p.image == null) p.image = new ImageTiles(this, w);
                else p.image.update(w);
            } catch (Throwable t) {
                if (!imageWarned) SCore.plugin.getLogger().log(Level.WARNING, "[SCore Screens] image tiles unavailable", t);
                imageWarned = true;
            }
        } else if (p.image != null) {
            p.image.remove();
            p.image = null;
        }
    }

    private static ItemStack icon(String name) {
        Material m = Material.matchMaterial(name);
        if (m == null || !m.isItem()) m = Material.PAPER;
        return new ItemStack(m);
    }

    private void spawnPointer() {
        double[] d = ScreenGeometry.dir(anchorYaw, anchorPitch);
        Location at = eye.clone().add(d[0], d[1], d[2]);
        at.setYaw(anchorYaw + 180);
        at.setPitch(-anchorPitch);
        float s = 0.3f;
        pointer = Spawn.spawn(player, at, TextDisplay.class, t -> {
            // CENTER: the glyph always faces the camera, so it keeps its shape when the head turns
            t.setBillboard(Display.Billboard.CENTER);
            t.setBrightness(new Display.Brightness(15, 15));
            t.setViewRange(2f);
            t.setDisplayWidth(4f);
            t.setDisplayHeight(4f);
            text(t);
            t.setBackgroundColor(Color.fromARGB(0));
            t.setText(ScreenPack.POINTER_TEXT);
            // the tip (bottom-right corner of the glyph, the texture is stored upside down) sits on the entity origin
            t.setTransformation(new Transformation(new Vector3f(-0.1125f * s, -0.025f * s, 0), new Quaternionf(),
                    new Vector3f(s, s, s), new Quaternionf()));
        });
    }

    private <T extends Entity> T remove(T e) {
        if (e != null) {
            try {
                e.remove();
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    /** used by the image tiles: their item frames are removed with the screen */
    public void track(Entity e) {
        extra.add(e);
    }

    public Location getEye() {
        return eye;
    }

    /** pooled image maps used by this screen */
    public Set<Integer> usedMapSlots() {
        return mapSlots;
    }

    /* ------------------------------------------------------------------ layout changes */

    @Override
    protected void redraw() {
        if (!built || !open) return;
        Set<String> keep = new HashSet<>();
        for (ScreenWidget w : layout.getWidgets()) {
            keep.add(w.getId());
            Parts p = parts.get(w.getId());
            boolean hover = w.getId().equals(hovered);
            if (p == null) draw(w);
            else if (!p.widget.equals(w) || p.hovered != hover) apply(p, w, hover);
        }
        for (Iterator<Map.Entry<String, Parts>> it = parts.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Parts> e = it.next();
            if (keep.contains(e.getKey())) continue;
            clear(e.getValue());
            it.remove();
        }
        if (hovered != null && !keep.contains(hovered)) hovered = null;
        // a moved or new widget can now be under the cursor: recompute the hover on the next poll
        lastYaw = Float.NaN;
    }

    private void clear(Parts p) {
        p.box = remove(p.box);
        p.text = remove(p.text);
        p.icon = remove(p.icon);
        if (p.image != null) p.image.remove();
        p.image = null;
    }

    /* ------------------------------------------------------------------ input */

    /** the panel point under the cursor, in layout coordinates, or null */
    public double[] cursor() {
        Location l = player.getLocation();
        double[] pt = pinned ? ScreenGeometry.pinnedPoint(anchorYaw, anchorPitch, l.getYaw(), l.getPitch())
                : ScreenGeometry.floatingPoint(anchorYaw, anchorPitch, l.getYaw(), l.getPitch(), FLOATING_SIZE);
        if (pt == null) return null;
        return ScreenGeometry.toLayout(pt[0], pt[1], layout.getWidth(), layout.getHeight());
    }

    private String widgetUnderCursor() {
        double[] c = cursor();
        if (c == null) return null;
        ScreenWidget w = layout.hit(c[0], c[1]);
        return w == null ? null : w.getId();
    }

    /** every tick: hover from the head rotation (a seated player's rotation is not always reported by PlayerMoveEvent) */
    void poll() {
        if (!open || !built) return;
        if (!player.isOnline() || player.getVehicle() != seat) {
            if (!closing) Screens.close(player, player.isOnline() ? CloseReason.PLAYER : CloseReason.QUIT);
            return;
        }
        Location l = player.getLocation();
        if (l.getYaw() == lastYaw && l.getPitch() == lastPitch) return;
        lastYaw = l.getYaw();
        lastPitch = l.getPitch();
        String now = widgetUnderCursor();
        if (Objects.equals(now, hovered)) return;
        String old = hovered;
        hovered = now;
        refreshHover(old);
        refreshHover(now);
        dispatch(new ScreenInput.Hover(now));
    }

    private void refreshHover(String id) {
        if (id == null) return;
        Parts p = parts.get(id);
        ScreenWidget w = layout.get(id);
        if (p != null && w != null) apply(p, w, id.equals(hovered));
    }

    void click(boolean right) {
        if (!open || !built) return;
        String id = widgetUnderCursor();
        lastClickAt = System.currentTimeMillis();
        inHandler = true;
        try {
            dispatch(new ScreenInput.Click(id, right, false));
        } finally {
            inHandler = false;
            lastClickAt = System.currentTimeMillis();
        }
    }

    void scroll(int delta) {
        if (!open || !built) return;
        dispatch(new ScreenInput.Scroll(delta));
    }

    public boolean owns(Entity e) {
        if (e == null) return false;
        if (e.equals(box) || e.equals(seat)) return true;
        return extra.contains(e);
    }

    public Interaction getBox() {
        return box;
    }

    public BlockDisplay getSeat() {
        return seat;
    }

    /* ------------------------------------------------------------------ PINNED extras */

    private void hideHand() {
        try {
            savedInvisible = player.isInvisible();
            player.setInvisible(true);
            savedSlot = player.getInventory().getHeldItemSlot();
            ItemStack held = player.getInventory().getItem(savedSlot);
            if (held != null && held.getType() != Material.AIR) {
                for (int i = 0; i < 9; i++) {
                    ItemStack it = player.getInventory().getItem(i);
                    if (it == null || it.getType() == Material.AIR) {
                        player.getInventory().setHeldItemSlot(i);
                        break;
                    }
                }
            }
            savedWeather = player.getPlayerWeather();
            player.setPlayerWeather(WeatherType.CLEAR);
            handHidden = true;
        } catch (Throwable ignored) {
        }
    }

    private void showHand() {
        if (!handHidden) return;
        handHidden = false;
        try {
            player.setInvisible(savedInvisible);
            if (savedSlot >= 0 && player.getInventory().getHeldItemSlot() != savedSlot) player.getInventory().setHeldItemSlot(savedSlot);
            if (savedWeather == null) player.resetPlayerWeather();
            else player.setPlayerWeather(savedWeather);
        } catch (Throwable ignored) {
        }
    }

    /** blocks closer than the backdrop would poke through the panel: send them as air to this player only */
    private void hideNearBlocks() {
        double corner = Math.sqrt(1 + SCREEN_TAN_X * SCREEN_TAN_X + SCREEN_TAN_Y * SCREEN_TAN_Y);
        double reach = Math.min(3, PINNED_BACKDROP_DEPTH * corner + 0.3);
        int r = (int) Math.ceil(reach);
        World w = eye.getWorld();
        int ex = eye.getBlockX(), ey = eye.getBlockY(), ez = eye.getBlockZ();
        List<BlockState> fake = new ArrayList<>();
        for (int x = -r; x <= r; x++)
            for (int y = -r; y <= r; y++)
                for (int z = -r; z <= r; z++) {
                    int bx = ex + x, by = ey + y, bz = ez + z;
                    double dx = clampDist(eye.getX(), bx), dy = clampDist(eye.getY(), by), dz = clampDist(eye.getZ(), bz);
                    if (dx * dx + dy * dy + dz * dz > reach * reach) continue;
                    if (by < w.getMinHeight() || by >= w.getMaxHeight()) continue;
                    Block b = w.getBlockAt(bx, by, bz);
                    if (b.getType().isAir()) continue;
                    if (b.equals(feet.getBlock()) || b.equals(feet.clone().subtract(0, 1, 0).getBlock())) {
                        // keep the block the player stands on
                        if (by < ey) continue;
                    }
                    BlockState s = b.getState();
                    s.setType(Material.AIR);
                    fake.add(s);
                    hiddenBlocks.add(b.getLocation());
                }
        if (fake.isEmpty()) return;
        sendStates(fake);
    }

    private static double clampDist(double eye, int block) {
        if (eye < block) return block - eye;
        if (eye > block + 1) return eye - block - 1;
        return 0;
    }

    private void sendStates(List<BlockState> states) {
        try {
            player.sendBlockChanges(states);
        } catch (Throwable old) {
            for (BlockState s : states) player.sendBlockChange(s.getLocation(), s.getBlockData());
        }
    }

    private void restoreBlocks() {
        if (hiddenBlocks.isEmpty() || !player.isOnline()) {
            hiddenBlocks.clear();
            return;
        }
        List<BlockState> real = new ArrayList<>();
        for (Location l : hiddenBlocks) {
            if (l.getWorld() != null && l.getWorld().equals(player.getWorld())) real.add(l.getBlock().getState());
        }
        hiddenBlocks.clear();
        if (!real.isEmpty()) sendStates(real);
    }

    /* ------------------------------------------------------------------ close */

    @Override
    protected void dispose(CloseReason reason) {
        closing = true;
        long now = System.currentTimeMillis();
        if (reason != CloseReason.QUIT && reason != CloseReason.DEATH && (inHandler || now - lastClickAt < ClickGuard.RECENT_CLICK_MS))
            DisplayScreenListener.GUARD.arm(player.getUniqueId(), now);
        if (buildTask != null) buildTask.cancel();
        if (pollTask != null) pollTask.cancel();
        showHand();
        restoreBlocks();
        for (Parts p : parts.values()) clear(p);
        parts.clear();
        for (Entity e : extra) remove(e);
        extra.clear();
        backdrop = remove(backdrop);
        plate = remove(plate);
        pointer = remove(pointer);
        box = remove(box);
        if (seat != null) {
            try {
                seat.removePassenger(player);
            } catch (Throwable ignored) {
            }
            seat = remove(seat);
        }
        if (reason != CloseReason.QUIT && reason != CloseReason.DEATH && player.isOnline()) {
            Location back = feet.clone();
            back.setYaw(player.getLocation().getYaw());
            back.setPitch(player.getLocation().getPitch());
            if (back.getWorld() != null && back.getWorld().equals(player.getWorld())) {
                // Paper runs teleportAsync on the next tick: a screen opened meanwhile must not take it as a reason to close
                DisplayScreenListener.expectTeleport(player, back);
                if (SCore.isFolia()) player.teleportAsync(back);
                else player.teleport(back);
            }
        }
    }
}
