package com.ssomar.score.projectiles.features;

import com.ssomar.score.SCore;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.util.Vector;

/**
 * Speed of the fireball-like projectiles (FIREBALL, SMALL_FIREBALL, DRAGON_FIREBALL, WITHER_SKULL).
 * <p>
 * A fireball is not driven by its velocity but by its acceleration: every tick it adds the acceleration to its velocity
 * and keeps 95 % of it, so it flies at about 19 x the acceleration whatever its starting velocity.
 * {@link Fireball#setDirection(Vector)} only turns the acceleration and keeps its vanilla size (0.1), so the velocity
 * of LAUNCH and of the projectile file had no lasting effect on these projectiles (reported on Discord 1555875543671640105).
 * The starting velocity and the acceleration are now both scaled with the requested speed (a speed of 1 changes nothing). setAcceleration exists since 1.20.5; before, nothing changes.
 */
public final class FireballSpeed {

    /** Vanilla acceleration of a fireball launched with a speed of 1. */
    private static final double VANILLA_ACCELERATION = 0.1;

    private FireballSpeed() {
    }

    /**
     * @param direction the flight direction (its length is ignored)
     * @param multiplier the velocity setting of LAUNCH (1 = vanilla). Not the length of the launch vector: in a projectile launch
     *                   event that vector carries the speed of the original projectile (about 3 for a fully drawn bow), and a
     *                   fireball LAUNCHed there with velocity:1 must keep its vanilla speed as before.
     */
    public static void apply(Entity entity, Vector direction, double multiplier) {
        if (!(entity instanceof Fireball) || !SCore.is1v20v5Plus()) return;
        /* a speed of 1 keeps the vanilla flight exactly as before */
        if (Double.isNaN(multiplier) || Double.isInfinite(multiplier) || multiplier <= 0 || Math.abs(multiplier - 1) < 1e-6) return;
        if (direction.lengthSquared() == 0 || Double.isNaN(direction.lengthSquared())) return;
        Fireball fireball = (Fireball) entity;
        try {
            fireball.setAcceleration(direction.clone().normalize().multiply(VANILLA_ACCELERATION * multiplier));
            fireball.setVelocity(fireball.getVelocity().multiply(multiplier));
        } catch (NoSuchMethodError | UnsupportedOperationException ignored) {
            // older or exotic server implementation: keep the vanilla speed
        }
    }

    /**
     * Multiplies the current speed of a fireball (velocity field of a projectile file).
     */
    public static void multiply(Entity entity, double multiplier) {
        if (!(entity instanceof Fireball) || !SCore.is1v20v5Plus() || Double.isNaN(multiplier) || Double.isInfinite(multiplier) || multiplier <= 0) return;
        Fireball fireball = (Fireball) entity;
        try {
            fireball.setAcceleration(fireball.getAcceleration().multiply(multiplier));
        } catch (NoSuchMethodError | UnsupportedOperationException ignored) {
        }
    }
}
