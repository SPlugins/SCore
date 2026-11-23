package com.ssomar.score.commands.runnable.mixed_player_entity.commands.equipmentvisualreplace;

import com.ssomar.score.SCore;
import com.ssomar.score.utils.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EQUIPMENT_VISUAL_REPLACE / EQUIPMENT_VISUAL_CANCEL: at most one visual per entity and slot, nothing re-sent
 * periodically: the packet listener of EquipmentVisualPackets (ProtocolLib) rewrites the equipment packets the server
 * sends while a visual is active.
 */
public class EquipmentVisualManager implements Listener {

    /* ProtocolLib side, behind an interface so that this class never loads ProtocolLib */
    interface Packets {
        void listen(EquipmentVisualManager manager);

        void send(int entityId, EquipmentSlot slot, ItemStack item, Collection<Player> receivers);
    }

    static final class Visual {
        final LivingEntity entity;
        final int entityId;
        final EquipmentSlot slot;
        final ItemStack item;
        volatile ScheduledTask task;

        Visual(LivingEntity entity, EquipmentSlot slot, ItemStack item) {
            this.entity = entity;
            this.entityId = entity.getEntityId();
            this.slot = slot;
            this.item = item;
        }

        void cancelTask() {
            ScheduledTask t = task;
            if (t != null) t.cancel();
        }
    }

    private static final boolean HAS_TRACKED_BY = SCore.hasMethod("org.bukkit.entity.Entity", "getTrackedBy");

    private static EquipmentVisualManager instance;

    /* entity id -> slot -> visual. The inner maps are never modified once published: the packet listener reads them on the network threads */
    private final Map<Integer, Map<EquipmentSlot, Visual>> visuals = new ConcurrentHashMap<>();

    /* Created at the first REPLACE, stays null when ProtocolLib is not enabled */
    private volatile Packets packets;
    private volatile boolean listening;

    EquipmentVisualManager(Packets packets) {
        this.packets = packets;
    }

    public static synchronized EquipmentVisualManager getInstance() {
        if (instance == null) instance = new EquipmentVisualManager(null);
        return instance;
    }

    private Packets packets() {
        Packets p = packets;
        if (p == null && SCore.hasProtocolLib) {
            synchronized (this) {
                if (packets == null) packets = new EquipmentVisualPackets();
                p = packets;
            }
        }
        if (p != null && !listening) {
            listening = true;
            p.listen(this);
        }
        return p;
    }

    /**
     * Shows item in the slot of the entity for ticks, to the players who see the entity (and to the entity itself if it is a player).
     * A new visual on the same entity and slot replaces the previous one.
     */
    public void replace(LivingEntity entity, EquipmentSlot slot, ItemStack item, int ticks) {
        if (ticks <= 0) return;
        Packets packets = packets();
        if (packets == null) return;

        Visual visual = new Visual(entity, slot, item);
        Visual previous = put(visual);
        if (previous != null) previous.cancelTask();

        visual.task = SCore.schedulerHook.runEntityTask(() -> {
            if (remove(visual.entityId, slot, visual) != null) reset(visual);
        }, () -> remove(visual.entityId, slot, visual), entity, ticks);
        /* entity already removed (Folia scheduler retired) */
        if (visual.task == null) {
            remove(visual.entityId, slot, visual);
            return;
        }

        packets.send(visual.entityId, slot, item, viewers(entity));
    }

    /**
     * Ends the visual of the slot: one packet with the real item to each player who sees the entity.
     */
    public void cancel(Entity entity, EquipmentSlot slot) {
        Visual visual = remove(entity.getEntityId(), slot, null);
        if (visual == null) return;
        visual.cancelTask();
        reset(visual);
    }

    /**
     * Forgets the visuals of the entity without sending anything (quit, death, despawn).
     */
    public void clear(Entity entity) {
        if (visuals.isEmpty()) return;
        Map<EquipmentSlot, Visual> removed = visuals.remove(entity.getEntityId());
        if (removed == null) return;
        for (Visual visual : removed.values()) visual.cancelTask();
    }

    /* Plugin disable */
    public static void shutdown() {
        EquipmentVisualManager manager;
        synchronized (EquipmentVisualManager.class) {
            manager = instance;
        }
        if (manager == null) return;
        for (Integer entityId : new ArrayList<>(manager.visuals.keySet())) {
            Map<EquipmentSlot, Visual> removed = manager.visuals.remove(entityId);
            if (removed == null) continue;
            for (Visual visual : removed.values()) {
                visual.cancelTask();
                try {
                    manager.reset(visual);
                } catch (Throwable ignored) {
                }
            }
        }
    }

    public void registerListeners(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        /* Despawn (chunk unload, removal by a plugin...). Players are removed from their world when they change world: they are handled by quit and death */
        for (String eventClass : new String[]{"com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent", "org.bukkit.event.entity.EntityRemoveEvent"}) {
            Class<? extends Event> type;
            try {
                type = Class.forName(eventClass).asSubclass(Event.class);
            } catch (Throwable e) {
                continue;
            }
            try {
                plugin.getServer().getPluginManager().registerEvent(type, this, EventPriority.MONITOR, (listener, event) -> {
                    if (!(event instanceof EntityEvent)) return;
                    Entity entity = ((EntityEvent) event).getEntity();
                    if (!(entity instanceof Player)) clear(entity);
                }, plugin);
            } catch (Throwable ignored) {
            }
            break;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        clear(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent e) {
        clear(e.getEntity());
    }

    /* The fake main hand item follows the selected slot (resent after the inventory packet), the previous slot shows its real item again */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHeldItem(PlayerItemHeldEvent e) {
        Map<EquipmentSlot, Visual> slots = visuals.get(e.getPlayer().getEntityId());
        if (slots != null && slots.containsKey(EquipmentSlot.HAND)) e.getPlayer().updateInventory();
    }

    boolean isEmpty() {
        return visuals.isEmpty();
    }

    /* Read only, null if the entity has no visual */
    Map<EquipmentSlot, Visual> visualsOf(int entityId) {
        return visuals.get(entityId);
    }

    private Visual put(Visual visual) {
        Visual[] previous = new Visual[1];
        visuals.compute(visual.entityId, (id, slots) -> {
            Map<EquipmentSlot, Visual> copy = slots == null ? new EnumMap<>(EquipmentSlot.class) : new EnumMap<>(slots);
            previous[0] = copy.put(visual.slot, visual);
            return copy;
        });
        return previous[0];
    }

    /* expected == null: whatever visual the slot has */
    private Visual remove(int entityId, EquipmentSlot slot, Visual expected) {
        Visual[] removed = new Visual[1];
        visuals.computeIfPresent(entityId, (id, slots) -> {
            Visual current = slots.get(slot);
            if (current == null || (expected != null && current != expected)) return slots;
            removed[0] = current;
            if (slots.size() == 1) return null;
            Map<EquipmentSlot, Visual> copy = new EnumMap<>(slots);
            copy.remove(slot);
            return copy;
        });
        return removed[0];
    }

    private void reset(Visual visual) {
        packets.send(visual.entityId, visual.slot, realItem(visual.entity, visual.slot), viewers(visual.entity));
    }

    static ItemStack realItem(LivingEntity entity, EquipmentSlot slot) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) return null;
        switch (slot.name()) {
            case "HAND":
                return equipment.getItemInMainHand();
            case "OFF_HAND":
                return equipment.getItemInOffHand();
            case "HEAD":
                return equipment.getHelmet();
            case "CHEST":
                return equipment.getChestplate();
            case "LEGS":
                return equipment.getLeggings();
            case "FEET":
                return equipment.getBoots();
            default:
                try {
                    return equipment.getItem(slot);
                } catch (Throwable e) {
                    return null;
                }
        }
    }

    /**
     * The players who see the entity, plus the entity itself if it is a player (its own client applies the packet to its inventory).
     * getTrackedBy must run on the thread that owns the entity: commands and the end task do.
     */
    static Collection<Player> viewers(Entity entity) {
        Set<Player> viewers = new HashSet<>();
        if (entity instanceof Player) viewers.add((Player) entity);
        if (HAS_TRACKED_BY) {
            try {
                viewers.addAll(entity.getTrackedBy());
                return viewers;
            } catch (Throwable ignored) {
            }
        }
        Location location = entity.getLocation();
        double range = (Bukkit.getViewDistance() + 1) * 16;
        for (Player player : entity.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(location) <= range * range) viewers.add(player);
        }
        return viewers;
    }
}
