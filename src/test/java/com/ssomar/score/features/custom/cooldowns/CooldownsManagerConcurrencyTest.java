package com.ssomar.score.features.custom.cooldowns;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/** The async cooldown display sweep iterates the maps while other threads add, clear and remove cooldowns. */
class CooldownsManagerConcurrencyTest {

    /* Cooldown's constructors load SCore's feature settings (plugin config), unavailable headless:
     * allocate the instance without a constructor and set the fields the manager reads. */
    static Cooldown cooldown(String id, UUID entity, int seconds, boolean inTick, long time, boolean global) {
        try {
            // reflection: the module compiles with --release 8, where sun.misc is not visible
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            java.lang.reflect.Field f = unsafeClass.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Object unsafe = f.get(null);
            Cooldown cd = (Cooldown) unsafeClass.getMethod("allocateInstance", Class.class).invoke(unsafe, HeadlessCooldown.class);
            set(cd, "id", id);
            set(cd, "entityUUID", entity);
            set(cd, "cooldown", seconds);
            set(cd, "isInTick", inTick);
            set(cd, "time", time);
            set(cd, "global", global);
            return cd;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** Never constructed (allocated without constructor); toString() of Cooldown needs the plugin config. */
    static class HeadlessCooldown extends Cooldown {
        HeadlessCooldown() {
            super("never", null, 0, false, 0, false);
        }

        @Override
        public String toString() {
            return "HeadlessCooldown";
        }
    }

    static void set(Object o, String name, Object value) throws Exception {
        java.lang.reflect.Field field = Cooldown.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(o, value);
    }

    @Test
    void sweepWhileWritingDoesNotThrow() throws Exception {
        CooldownsManager manager = new CooldownsManager();
        List<UUID> players = new ArrayList<>();
        for (int i = 0; i < 20; i++) players.add(UUID.randomUUID());
        AtomicReference<Throwable> failure = new AtomicReference<>();
        ExecutorService pool = Executors.newFixedThreadPool(4);
        long end = System.currentTimeMillis() + 1500;

        Runnable writer = () -> {
            int n = 0;
            while (System.currentTimeMillis() < end && failure.get() == null) {
                try {
                    UUID p = players.get(n % players.size());
                    manager.addCooldown(cooldown("EI:item" + (n % 7) + ":act", p, 1000, false, System.currentTimeMillis(), false));
                    if (n % 5 == 0) manager.clearCooldown("EI:item" + (n % 7) + ":act", p);
                    if (n % 11 == 0) manager.removeCooldownsOf(p);
                    n++;
                } catch (Throwable t) {
                    failure.compareAndSet(null, t);
                }
            }
        };
        Runnable sweeper = () -> {
            while (System.currentTimeMillis() < end && failure.get() == null) {
                try {
                    manager.getAllCooldowns();
                    manager.cleanupExpiredCooldowns();
                    manager.getCooldownsOf(players.get(0));
                } catch (Throwable t) {
                    failure.compareAndSet(null, t);
                }
            }
        };
        List<Future<?>> futures = new ArrayList<>();
        futures.add(pool.submit(writer));
        futures.add(pool.submit(writer));
        futures.add(pool.submit(sweeper));
        futures.add(pool.submit(sweeper));
        for (Future<?> f : futures) f.get(10, TimeUnit.SECONDS);
        pool.shutdownNow();
        if (failure.get() != null) fail("concurrent access threw", failure.get());
    }

    @Test
    void removeCooldownsOfAndClearCooldownWork() {
        CooldownsManager manager = new CooldownsManager();
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        manager.addCooldown(cooldown("id1", a, 1000, false, System.currentTimeMillis(), false));
        manager.addCooldown(cooldown("id1", b, 1000, false, System.currentTimeMillis(), false));
        manager.addCooldown(cooldown("id2", a, 1000, false, System.currentTimeMillis(), false));

        manager.removeCooldownsOf(a);
        assertEquals(1, manager.getAllCooldowns().size());
        assertTrue(manager.getCooldownsOf(a).isEmpty());

        manager.clearCooldown("id1", b);
        assertTrue(manager.getAllCooldowns().isEmpty());
        assertTrue(manager.getCooldownsOf(b).isEmpty());
    }
}
