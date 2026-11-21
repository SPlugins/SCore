package com.ssomar.score.screen.internal.display;

import com.ssomar.score.SCore;
import com.ssomar.score.screen.CloseReason;
import com.ssomar.score.screen.ScreenSession;
import com.ssomar.score.screen.Screens;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.EventExecutor;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Input, protection and close triggers of the FLOATING and PINNED screens (registered on 1.19.4+ only).
 * Every handler checks first whether the player has a display screen open, so it costs nothing otherwise.
 */
public final class DisplayScreenListener implements Listener {

    public static final ClickGuard GUARD = new ClickGuard();

    /** one physical left click arrives as an attack on the box and an arm swing: keep one per 150 ms */
    static final long DEDUPE_MS = 150, SWING_AFTER_RIGHT_MS = 80, COMBAT_MS = 5000;

    private static final Map<UUID, Long> LAST_CLICK = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_RIGHT = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_DAMAGE = new ConcurrentHashMap<>();

    static DisplayScreenSession session(Player p) {
        ScreenSession s = Screens.get(p);
        if (!(s instanceof DisplayScreenSession)) return null;
        DisplayScreenSession d = (DisplayScreenSession) s;
        return d.closing ? null : d;
    }

    static boolean recentlyDamaged(Player p) {
        Long t = LAST_DAMAGE.get(p.getUniqueId());
        return t != null && System.currentTimeMillis() - t < COMBAT_MS;
    }

    private static void click(Player p, DisplayScreenSession s, boolean right) {
        long now = System.currentTimeMillis();
        Long last = LAST_CLICK.get(p.getUniqueId());
        if (last != null && now - last < DEDUPE_MS) return;
        LAST_CLICK.put(p.getUniqueId(), now);
        if (right) LAST_RIGHT.put(p.getUniqueId(), now);
        s.click(right);
        // the window restarts when the handler returns (a slow handler must not let the second half through)
        LAST_CLICK.put(p.getUniqueId(), System.currentTimeMillis());
    }

    /** true (and the guard extended) if a world action of this player must be cancelled */
    private static boolean blocked(Player p) {
        if (session(p) != null) return true;
        if (Screens.get(p) instanceof DisplayScreenSession) return true;
        long now = System.currentTimeMillis();
        if (GUARD.isUp(p.getUniqueId(), now)) {
            GUARD.extend(p.getUniqueId(), now);
            return true;
        }
        return false;
    }

    /* ------------------------------------------------------------------ clicks */

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        DisplayScreenSession s = session(p);
        if (s == null) {
            if (blocked(p)) e.setCancelled(true);
            return;
        }
        e.setCancelled(true);
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getRightClicked().equals(s.getBox())) click(p, s, true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteractAtEntity(PlayerInteractAtEntityEvent e) {
        if (blocked(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAttack(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        Player p = (Player) e.getDamager();
        DisplayScreenSession s = session(p);
        if (s == null) {
            if (blocked(p)) e.setCancelled(true);
            return;
        }
        e.setCancelled(true);
        if (e.getEntity().equals(s.getBox())) click(p, s, false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSwing(PlayerAnimationEvent e) {
        Player p = e.getPlayer();
        DisplayScreenSession s = session(p);
        if (s == null || !s.built) return;
        Long right = LAST_RIGHT.get(p.getUniqueId());
        if (right != null && System.currentTimeMillis() - right < SWING_AFTER_RIGHT_MS) return;
        click(p, s, false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onScroll(PlayerItemHeldEvent e) {
        DisplayScreenSession s = session(e.getPlayer());
        if (s == null || !s.built) return;
        e.setCancelled(true);
        int d = e.getNewSlot() - e.getPreviousSlot();
        if (d > 4) d -= 9;
        if (d < -4) d += 9;
        if (d != 0) s.scroll(Integer.signum(d));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSneak(PlayerToggleSneakEvent e) {
        if (!e.isSneaking()) return;
        DisplayScreenSession s = session(e.getPlayer());
        if (s == null) return;
        e.setCancelled(true);
        Screens.close(e.getPlayer(), CloseReason.PLAYER);
    }

    /* ------------------------------------------------------------------ world protection */

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL) return;
        Player p = e.getPlayer();
        if (session(p) != null || Screens.get(p) instanceof DisplayScreenSession) {
            e.setCancelled(true);
            return;
        }
        long now = System.currentTimeMillis();
        if (!GUARD.isUp(p.getUniqueId(), now)) return;
        e.setUseInteractedBlock(Event.Result.DENY);
        e.setUseItemInHand(Event.Result.DENY);
        e.setCancelled(true);
        // a bare arm swing also gives LEFT_CLICK_AIR: not a sign that the button is held
        if (e.getAction() != Action.LEFT_CLICK_AIR) GUARD.extend(p.getUniqueId(), now);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent e) {
        if (blocked(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamageBlock(BlockDamageEvent e) {
        if (blocked(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHanging(HangingBreakByEntityEvent e) {
        if (e.getRemover() instanceof Player && blocked((Player) e.getRemover())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onVehicle(VehicleDamageEvent e) {
        if (e.getAttacker() instanceof Player && blocked((Player) e.getAttacker())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDrop(PlayerDropItemEvent e) {
        if (session(e.getPlayer()) != null) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSwap(PlayerSwapHandItemsEvent e) {
        if (session(e.getPlayer()) != null) e.setCancelled(true);
    }

    /* ------------------------------------------------------------------ close triggers */

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        LAST_DAMAGE.put(p.getUniqueId(), System.currentTimeMillis());
        if (session(p) != null) Screens.close(p, CloseReason.DAMAGE);
    }

    /** teleports done by SCore Screens itself (back to the place where the screen was opened) */
    private static final Map<UUID, org.bukkit.Location> OWN_TELEPORTS = new ConcurrentHashMap<>();

    static void expectTeleport(Player p, org.bukkit.Location to) {
        OWN_TELEPORTS.put(p.getUniqueId(), to.clone());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.getCause().name().equals("DISMOUNT")) return;
        org.bukkit.Location own = OWN_TELEPORTS.remove(e.getPlayer().getUniqueId());
        if (own != null && e.getTo() != null && own.getWorld() != null && own.getWorld().equals(e.getTo().getWorld())
                && own.distanceSquared(e.getTo()) < 0.01) return;
        if (session(e.getPlayer()) != null) Screens.close(e.getPlayer(), CloseReason.TELEPORT);
    }

    @EventHandler
    public void onWorld(PlayerChangedWorldEvent e) {
        if (session(e.getPlayer()) != null) Screens.close(e.getPlayer(), CloseReason.WORLD);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (session(e.getEntity()) != null) Screens.close(e.getEntity(), CloseReason.DEATH);
    }

    @EventHandler
    public void onGameMode(PlayerGameModeChangeEvent e) {
        if (e.getNewGameMode() == GameMode.SPECTATOR && session(e.getPlayer()) != null) Screens.close(e.getPlayer(), CloseReason.PLUGIN);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        UUID id = e.getPlayer().getUniqueId();
        GUARD.forget(id);
        LAST_CLICK.remove(id);
        LAST_RIGHT.remove(id);
        LAST_DAMAGE.remove(id);
        OWN_TELEPORTS.remove(id);
    }

    /* ------------------------------------------------------------------ registration */

    public static void register() {
        DisplayScreenListener l = new DisplayScreenListener();
        Bukkit.getPluginManager().registerEvents(l, SCore.plugin);
        registerDismount(l);
        try {
            Bukkit.getPluginManager().registerEvents(new AbortListener(), SCore.plugin);
        } catch (Throwable ignored) {
            // BlockDamageAbortEvent missing: the guard simply runs its full time
        }
        try {
            Bukkit.getPluginManager().registerEvents(new OrphanListener(), SCore.plugin);
        } catch (Throwable ignored) {
        }
        sweepOrphans();
    }

    /** EntityDismountEvent moved from org.spigotmc to org.bukkit in 1.20.5: found by reflection */
    @SuppressWarnings("unchecked")
    private static void registerDismount(Listener l) {
        for (String name : new String[]{"org.bukkit.event.entity.EntityDismountEvent", "org.spigotmc.event.entity.EntityDismountEvent"}) {
            try {
                Class<? extends Event> cls = (Class<? extends Event>) Class.forName(name);
                Method getEntity = cls.getMethod("getEntity");
                Method getDismounted = cls.getMethod("getDismounted");
                Method setCancelled = cls.getMethod("setCancelled", boolean.class);
                EventExecutor ex = (listener, event) -> {
                    if (!cls.isInstance(event)) return;
                    try {
                        Object who = getEntity.invoke(event);
                        if (!(who instanceof Player)) return;
                        DisplayScreenSession s = session((Player) who);
                        if (s != null && getDismounted.invoke(event).equals(s.getSeat())) setCancelled.invoke(event, true);
                    } catch (ReflectiveOperationException ignored) {
                    }
                };
                Bukkit.getPluginManager().registerEvent(cls, l, EventPriority.LOWEST, ex, SCore.plugin, false);
                return;
            } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            }
        }
    }

    /**
     * Screen entities are never saved, but a hard reload can leave some in a loaded chunk. Called at startup, when no
     * screen is open, so every tagged entity is an orphan.
     */
    public static void sweepOrphans() {
        if (SCore.isFolia()) return; // entities can't be read from the startup thread; they are non persistent anyway
        for (World w : Bukkit.getWorlds()) {
            try {
                for (Entity e : w.getEntities()) if (Spawn.isScreenEntity(e)) e.remove();
            } catch (Throwable ignored) {
            }
        }
    }

    public static final class AbortListener implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        public void onAbort(org.bukkit.event.block.BlockDamageAbortEvent e) {
            GUARD.release(e.getPlayer().getUniqueId(), System.currentTimeMillis());
        }
    }

    public static final class OrphanListener implements Listener {
        @EventHandler
        public void onLoad(org.bukkit.event.world.EntitiesLoadEvent e) {
            for (Entity en : e.getEntities()) if (Spawn.isScreenEntity(en)) en.remove();
        }
    }
}
