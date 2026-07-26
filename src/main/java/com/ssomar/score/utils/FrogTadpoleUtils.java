package com.ssomar.score.utils;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Tadpole;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

/**
 * Frogs have no baby form in vanilla, their baby is another entity: the tadpole.
 * This class converts one into the other so that SET_BABY / SET_ADULT behave as expected.
 * It must only be loaded on servers >= 1.19 (frog & tadpole classes dont exist before).
 */
public class FrogTadpoleUtils {

    private static final NamespacedKey VARIANT_KEY = new NamespacedKey("score", "frog-variant");

    public static boolean isFrog(Entity entity) {
        return entity instanceof Frog;
    }

    public static boolean isTadpole(Entity entity) {
        return entity instanceof Tadpole;
    }

    /**
     * Replaces the frog by a tadpole, the variant of the frog is saved in the tadpole
     * to restore it if the tadpole becomes an adult again.
     *
     * @return the new tadpole
     */
    public static Entity frogToTadpole(Entity entity) {
        Frog frog = (Frog) entity;
        Location loc = frog.getLocation();
        Vector velocity = frog.getVelocity();
        String variant = frog.getVariant().getKey().toString();
        String customName = frog.getCustomName();
        boolean customNameVisible = frog.isCustomNameVisible();
        boolean persistent = frog.isPersistent();
        boolean silent = frog.isSilent();
        boolean glowing = frog.isGlowing();
        boolean invulnerable = frog.isInvulnerable();

        frog.remove();

        Tadpole tadpole = loc.getWorld().spawn(loc, Tadpole.class);
        tadpole.setVelocity(velocity);
        if (customName != null) {
            tadpole.setCustomName(customName);
            tadpole.setCustomNameVisible(customNameVisible);
        }
        tadpole.setPersistent(persistent);
        tadpole.setSilent(silent);
        tadpole.setGlowing(glowing);
        tadpole.setInvulnerable(invulnerable);
        tadpole.setAge(0);
        tadpole.getPersistentDataContainer().set(VARIANT_KEY, PersistentDataType.STRING, variant);
        return tadpole;
    }

    /**
     * Replaces the tadpole by a frog, the variant is the one saved by {@link #frogToTadpole(Entity)}
     * if there is one, else it is deduced from the biome like in vanilla.
     *
     * @return the new frog
     */
    public static Entity tadpoleToFrog(Entity entity) {
        Tadpole tadpole = (Tadpole) entity;
        Location loc = tadpole.getLocation();
        Vector velocity = tadpole.getVelocity();
        String customName = tadpole.getCustomName();
        boolean customNameVisible = tadpole.isCustomNameVisible();
        boolean persistent = tadpole.isPersistent();
        boolean silent = tadpole.isSilent();
        boolean glowing = tadpole.isGlowing();
        boolean invulnerable = tadpole.isInvulnerable();
        String savedVariant = tadpole.getPersistentDataContainer().get(VARIANT_KEY, PersistentDataType.STRING);

        tadpole.remove();

        Frog frog = loc.getWorld().spawn(loc, Frog.class);
        frog.setVelocity(velocity);
        if (customName != null) {
            frog.setCustomName(customName);
            frog.setCustomNameVisible(customNameVisible);
        }
        frog.setPersistent(persistent);
        frog.setSilent(silent);
        frog.setGlowing(glowing);
        frog.setInvulnerable(invulnerable);
        frog.setAdult();
        frog.setVariant(getVariant(savedVariant, loc));
        return frog;
    }

    /**
     * Resets the growth of the tadpole, like a SET_BABY on a normal ageable entity
     */
    public static void resetTadpoleAge(Entity entity) {
        ((Tadpole) entity).setAge(0);
    }

    private static Frog.Variant getVariant(String savedVariant, Location loc) {
        if (savedVariant != null) {
            NamespacedKey key = NamespacedKey.fromString(savedVariant);
            if (key != null) {
                for (Frog.Variant variant : Frog.Variant.values()) {
                    if (variant.getKey().equals(key)) return variant;
                }
            }
        }
        /* Vanilla: the variant depends on the biome where the tadpole grew up */
        double temperature = loc.getWorld().getTemperature(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        if (temperature <= 0.15) return Frog.Variant.COLD;
        else if (temperature >= 0.9) return Frog.Variant.WARM;
        else return Frog.Variant.TEMPERATE;
    }
}
