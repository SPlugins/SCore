package com.ssomar.score.commands.runnable.mixed_player_entity.commands.equipmentvisualreplace;

import com.ssomar.score.SCore;
import com.ssomar.score.utils.scheduler.ScheduledTask;
import com.ssomar.score.utils.scheduler.SchedulerHook;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Discord 1559225349173149808: EQUIPMENT_VISUAL_REPLACE scheduled one task per tick of its duration, and a CANCEL sent
 * one packet per scheduled task to every online player (36 000 x players for 30 min), even after the end.
 */
class EquipmentVisualManagerTest {

    private static final class Sent {
        final int entityId;
        final EquipmentSlot slot;
        final ItemStack item;
        final Set<Player> receivers;

        Sent(int entityId, EquipmentSlot slot, ItemStack item, Collection<Player> receivers) {
            this.entityId = entityId;
            this.slot = slot;
            this.item = item;
            this.receivers = new HashSet<>(receivers);
        }
    }

    private static final class Task implements ScheduledTask {
        final Runnable runnable;
        final Runnable retired;
        final long delay;
        boolean cancelled;

        Task(Runnable runnable, Runnable retired, long delay) {
            this.runnable = runnable;
            this.retired = retired;
            this.delay = delay;
        }

        @Override
        public void cancel() {
            cancelled = true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }
    }

    private final List<Sent> sent = new ArrayList<>();
    private final List<Task> tasks = new ArrayList<>();
    private int listens;
    private SchedulerHook previousHook;
    private EquipmentVisualManager manager;

    private final ItemStack visualItem = new ItemStack() {
    };
    private final ItemStack otherVisualItem = new ItemStack() {
    };
    private final ItemStack realOffHand = new ItemStack() {
    };
    private final ItemStack zombieHelmet = new ItemStack() {
    };

    private Player viewer;
    private Player target;
    private Zombie zombie;

    @BeforeEach
    void setUp() {
        previousHook = SCore.schedulerHook;
        SCore.schedulerHook = (SchedulerHook) Proxy.newProxyInstance(SchedulerHook.class.getClassLoader(), new Class<?>[]{SchedulerHook.class}, (proxy, method, args) -> {
            if (method.getName().equals("runEntityTask")) {
                Task task = new Task((Runnable) args[0], (Runnable) args[1], (Long) args[3]);
                tasks.add(task);
                return task;
            }
            throw new UnsupportedOperationException(method.getName());
        });
        manager = new EquipmentVisualManager(new EquipmentVisualManager.Packets() {
            @Override
            public void listen(EquipmentVisualManager m) {
                listens++;
            }

            @Override
            public void send(int entityId, EquipmentSlot slot, ItemStack item, Collection<Player> receivers) {
                sent.add(new Sent(entityId, slot, item, receivers));
            }
        });

        viewer = entity(Player.class, 2, new HashSet<>(), null);
        target = entity(Player.class, 1, new HashSet<>(Arrays.asList(viewer)), equipment(EquipmentSlot.OFF_HAND, realOffHand));
        zombie = entity(Zombie.class, 3, new HashSet<>(Arrays.asList(viewer)), equipment(EquipmentSlot.HEAD, zombieHelmet));
    }

    @AfterEach
    void tearDown() {
        SCore.schedulerHook = previousHook;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity> T entity(Class<T> type, int id, Set<Player> trackedBy, EntityEquipment equipment) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
            switch (method.getName()) {
                case "getEntityId":
                    return id;
                case "getTrackedBy":
                    return trackedBy;
                case "getEquipment":
                    return equipment;
                case "getLocation":
                    return new Location(null, 0, 0, 0);
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "equals":
                    return proxy == args[0];
                case "toString":
                    return type.getSimpleName() + "#" + id;
                default:
                    if (method.getReturnType() == boolean.class) return false;
                    return null;
            }
        });
    }

    private static EntityEquipment equipment(EquipmentSlot slot, ItemStack item) {
        Map<String, ItemStack> bySlot = new HashMap<>();
        bySlot.put(slot.name(), item);
        return (EntityEquipment) Proxy.newProxyInstance(EntityEquipment.class.getClassLoader(), new Class<?>[]{EntityEquipment.class}, (proxy, method, args) -> {
            switch (method.getName()) {
                case "getItemInMainHand":
                    return bySlot.get("HAND");
                case "getItemInOffHand":
                    return bySlot.get("OFF_HAND");
                case "getHelmet":
                    return bySlot.get("HEAD");
                case "getChestplate":
                    return bySlot.get("CHEST");
                case "getLeggings":
                    return bySlot.get("LEGS");
                case "getBoots":
                    return bySlot.get("FEET");
                default:
                    return null;
            }
        });
    }

    private long activeTasks() {
        return tasks.stream().filter(t -> !t.cancelled).count();
    }

    @Test
    void replaceSendsOnePacketToTheViewersAndTheTargetAndSchedulesOneTask() {
        manager.replace(target, EquipmentSlot.OFF_HAND, visualItem, 36000);

        assertEquals(1, sent.size());
        Sent s = sent.get(0);
        assertEquals(1, s.entityId);
        assertEquals(EquipmentSlot.OFF_HAND, s.slot);
        assertSame(visualItem, s.item);
        assertEquals(new HashSet<>(Arrays.asList(viewer, target)), s.receivers);
        assertEquals(1, tasks.size());
        assertEquals(36000, tasks.get(0).delay);
        assertEquals(1, listens);
        assertSame(visualItem, manager.visualsOf(1).get(EquipmentSlot.OFF_HAND).item);
    }

    @Test
    void secondReplaceOnTheSameSlotReplacesTheFirst() {
        manager.replace(target, EquipmentSlot.OFF_HAND, visualItem, 36000);
        manager.replace(target, EquipmentSlot.OFF_HAND, otherVisualItem, 36000);

        assertTrue(tasks.get(0).cancelled);
        assertEquals(1, activeTasks());
        assertEquals(1, manager.visualsOf(1).size());
        assertSame(otherVisualItem, manager.visualsOf(1).get(EquipmentSlot.OFF_HAND).item);

        /* the end of the first one does nothing any more */
        tasks.get(0).runnable.run();
        assertSame(otherVisualItem, manager.visualsOf(1).get(EquipmentSlot.OFF_HAND).item);
        assertEquals(2, sent.size());
    }

    @Test
    void cancelSendsOneResetWithTheRealItem() {
        manager.replace(target, EquipmentSlot.OFF_HAND, visualItem, 36000);
        sent.clear();

        manager.cancel(target, EquipmentSlot.OFF_HAND);

        assertEquals(1, sent.size());
        assertSame(realOffHand, sent.get(0).item);
        assertEquals(new HashSet<>(Arrays.asList(viewer, target)), sent.get(0).receivers);
        assertTrue(tasks.get(0).cancelled);
        assertNull(manager.visualsOf(1));
        assertTrue(manager.isEmpty());

        manager.cancel(target, EquipmentSlot.OFF_HAND);
        assertEquals(1, sent.size());
    }

    @Test
    void endOfTheDurationRestoresAndForgets() {
        manager.replace(target, EquipmentSlot.OFF_HAND, visualItem, 40);
        sent.clear();

        tasks.get(0).runnable.run();
        assertEquals(1, sent.size());
        assertSame(realOffHand, sent.get(0).item);
        assertTrue(manager.isEmpty());

        /* a CANCEL after the end sends nothing */
        manager.cancel(target, EquipmentSlot.OFF_HAND);
        assertEquals(1, sent.size());
    }

    @Test
    void cancelWorksOnAnEntityThatIsNotAPlayer() {
        manager.replace(zombie, EquipmentSlot.HEAD, visualItem, 600);
        assertEquals(new HashSet<>(Arrays.asList(viewer)), sent.get(0).receivers);
        sent.clear();

        manager.cancel(zombie, EquipmentSlot.HEAD);
        assertEquals(1, sent.size());
        assertEquals(3, sent.get(0).entityId);
        assertSame(zombieHelmet, sent.get(0).item);
        assertEquals(new HashSet<>(Arrays.asList(viewer)), sent.get(0).receivers);
    }

    @Test
    void slotsAreIndependent() {
        manager.replace(target, EquipmentSlot.OFF_HAND, visualItem, 600);
        manager.replace(target, EquipmentSlot.HEAD, otherVisualItem, 600);
        manager.cancel(target, EquipmentSlot.HEAD);

        assertEquals(1, manager.visualsOf(1).size());
        assertSame(visualItem, manager.visualsOf(1).get(EquipmentSlot.OFF_HAND).item);
        assertFalse(tasks.get(0).cancelled);
    }

    @Test
    void quitDeathAndDespawnForgetWithoutPackets() {
        manager.replace(target, EquipmentSlot.OFF_HAND, visualItem, 600);
        manager.replace(target, EquipmentSlot.HEAD, otherVisualItem, 600);
        sent.clear();

        manager.clear(target);
        assertTrue(manager.isEmpty());
        assertEquals(0, activeTasks());
        assertTrue(sent.isEmpty());
    }

    @Test
    void retiredEntityIsForgotten() {
        manager.replace(zombie, EquipmentSlot.HEAD, visualItem, 600);
        sent.clear();

        tasks.get(0).retired.run();
        assertTrue(manager.isEmpty());
        assertTrue(sent.isEmpty());
    }

    @Test
    void nothingWithoutProtocolLib() {
        EquipmentVisualManager withoutProtocolLib = new EquipmentVisualManager(null);
        withoutProtocolLib.replace(target, EquipmentSlot.OFF_HAND, visualItem, 600);
        withoutProtocolLib.cancel(target, EquipmentSlot.OFF_HAND);

        assertTrue(withoutProtocolLib.isEmpty());
        assertTrue(tasks.isEmpty());
    }

    @Test
    void zeroTicksDoesNothing() {
        manager.replace(target, EquipmentSlot.OFF_HAND, visualItem, 0);
        assertTrue(manager.isEmpty());
        assertTrue(sent.isEmpty());
        assertTrue(tasks.isEmpty());
    }
}
