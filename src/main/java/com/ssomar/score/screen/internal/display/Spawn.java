package com.ssomar.score.screen.internal.display;

import com.ssomar.score.SCore;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;

/**
 * Spawns the entities of a screen: configured before their spawn packet goes out (no frame where everyone sees
 * them), never saved with the chunk, visible to the viewer only, tagged {@code score:screen} for the orphan sweep.
 */
public final class Spawn {

    private static NamespacedKey key;

    private Spawn() {
    }

    public static NamespacedKey key() {
        if (key == null) key = new NamespacedKey(SCore.plugin, "screen");
        return key;
    }

    public static boolean isScreenEntity(Entity e) {
        try {
            return e.getPersistentDataContainer().has(key(), PersistentDataType.BYTE);
        } catch (Throwable t) {
            return false;
        }
    }

    public static <T extends Entity> T spawn(Player viewer, Location loc, Class<T> cls, Consumer<T> setup) {
        Consumer<T> all = e -> {
            e.setPersistent(false);
            try {
                e.setVisibleByDefault(false);
            } catch (Throwable ignored) {
                // very old API: hidden below with hideEntity
            }
            e.getPersistentDataContainer().set(key(), PersistentDataType.BYTE, (byte) 1);
            setup.accept(e);
        };
        T entity = spawnRaw(loc, cls, all);
        viewer.showEntity(SCore.plugin, entity);
        return entity;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity> T spawnRaw(Location loc, Class<T> cls, Consumer<T> setup) {
        try {
            return loc.getWorld().spawn(loc, cls, setup);
        } catch (NoSuchMethodError old) {
            // before 1.20.2 the pre-spawn callback is org.bukkit.util.Consumer
            try {
                Class<?> consumer = Class.forName("org.bukkit.util.Consumer");
                Object proxy = Proxy.newProxyInstance(consumer.getClassLoader(), new Class[]{consumer}, (p, m, args) -> {
                    if (m.getName().equals("accept")) setup.accept((T) args[0]);
                    return null;
                });
                Method spawn = loc.getWorld().getClass().getMethod("spawn", Location.class, Class.class, consumer);
                return (T) spawn.invoke(loc.getWorld(), loc, cls, proxy);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("can't spawn " + cls.getSimpleName(), e);
            }
        }
    }
}
