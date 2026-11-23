package com.ssomar.particles.commands;

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 Crypto Morin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import com.google.common.base.Enums;
import com.ssomar.score.SCore;
import com.ssomar.score.utils.scheduler.ScheduledTask;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;


public final class XParticle {

    public static final double PII = 2 * Math.PI;

    private XParticle() {}

    /**
     * An optimized and stable way of getting particles for cross-version support.
     *
     * @param particle the particle name.
     *
     * @return a particle that matches the specified name.
     * @since 1.0.0
     */
    public static Particle getParticle(String particle) {
        return Enums.getIfPresent(Particle.class, particle).orNull();
    }

    /**
     * Get a random particle from a list of particle names.
     *
     * @param particles the particles name.
     *
     * @return a random particle from the list.
     * @since 1.0.0
     */
    public static Particle randomParticle(String... particles) {
        int rand = randInt(0, particles.length - 1);
        return getParticle(particles[rand]);
    }

    /**
     * A thread safe way to get a random double in a range.
     *
     * @param min the minimum number.
     * @param max the maximum number.
     *
     * @return a random number.
     * @see #randInt(int, int)
     * @since 1.0.0
     */
    public static double random(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    /**
     * A thread safe way to get a random integer in a range.
     *
     * @param min the minimum number.
     * @param max the maximum number.
     *
     * @return a random number.
     * @see #random(double, double)
     * @since 1.0.0
     */
    public static int randInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * Generate a random RGB color for particles.
     *
     * @return a random color.
     * @since 1.0.0
     */
    public static Color randomColor() {
        ThreadLocalRandom gen = ThreadLocalRandom.current();
        int randR = gen.nextInt(0, 256);
        int randG = gen.nextInt(0, 256);
        int randB = gen.nextInt(0, 256);

        return Color.fromRGB(randR, randG, randB);
    }

    /**
     * Generate a random colorized dust with a random size.
     *
     * @return a REDSTONE colored dust.
     * @since 1.0.0
     */
    public static Particle.DustOptions randomDust() {
        float size = randInt(5, 10) / 10f;
        return new Particle.DustOptions(randomColor(), size);
    }

    /**
     * Spawns an atom with orbits and a nucleus.
     *
     * @param orbits the number of atom orbits.
     * @param radius the radius of orbits.
     * @param rate   the rate of orbit and nucleus points.
     *
     * /score particles shape:atom color:BLUE,YELLOW orbits:4 radius:5.0 rate:100 offsetY:1
     * /score particles shape:atom particle:CLOUD orbits:10 radius:20.5 rate:100 offsetY:1
     *
     * @since 1.0.0
     */
    public static void atom(int orbits, double radius, double rate, ParticleDisplay display) {
        ParticleDisplay orbit = display;
        ParticleDisplay nucleus = display;
        double dist = Math.PI / orbits;
        for (double angle = 0; orbits > 0; angle += dist) {
            orbit.setRotation(new Vector(0, 0, angle));
            circle(radius, rate,0,"","", orbit);
            orbits--;
        }

        sphere(radius / 3, rate / 2, nucleus);
    }

    /**
     * Spawns multiple animated atomic-like circles rotating around in their orbit.
     *
     * @param orbits the orbits of the atom.
     * @param radius the radius of the atom orbits.
     * @param rate   the rate of orbit points.
     * @param time   the time in ticks to display the atom.
     *
     * @since 1.0.0
     */
    public static ScheduledTask atomic(int orbits, double radius, double rate, double time, ParticleDisplay display) {

        final AtomicReference<ScheduledTask> task = new AtomicReference<>();

        Runnable runnable = new Runnable() {
            final double rateDiv = Math.PI / rate;
            final double dist = Math.PI / orbits;
            double theta = 0;
            double repeat = 0;

            @Override
            public void run() {
                int orbital = orbits;
                theta += rateDiv;

                double x = radius * Math.cos(theta);
                double z = radius * Math.sin(theta);

                for (double angle = 0; orbital > 0; angle += dist) {
                    display.setRotation(new Vector(0, 0, angle));
                    display.spawn(x, 0, z);
                    orbital--;
                }
                if (++repeat > time) task.get().cancel();
            }
        };
        task.set(SCore.schedulerHook.runAsyncRepeatingTask(runnable, 0L, 1L));
        return task.get();
    }

    /**
     * Creates a blacksun-like increasing circles.
     *
     * @param radius     the radius of the biggest circle.
     * @param radiusRate the radius rate change of circles.
     * @param rate       the rate of the biggest circle points.
     * @param rateChange the rate change of circle points.
     *
     * @since 1.0.0
     */
    public static void blackSun(double radius, double radiusRate, double rate, double rateChange, ParticleDisplay display) {
        double j = 0;
        if(radiusRate < 0) radiusRate = -radiusRate;
        else if(radiusRate == 0) radiusRate = 1;
        for (double i = 10; i > 0; i -= radiusRate) {
            j += rateChange;
            circle(radius + i, Math.max(5,rate - j), 0,"","ring", display);
        }
    }

    /**
     * Spawn a blackhole.
     *
     * @param points the points of the blackhole pulls.
     * @param radius the radius of the blackhole circle.
     * @param rate   the rate of the blackhole circle points.
     * @param mode   blackhole mode. There are 5 modes.
     * @param time   the amount of ticks to keep the blackhole.
     *
     * @since 3.0.0
     */
    public static ScheduledTask blackhole(int points, double radius, double rate, int mode, int time, ParticleDisplay display) {
        display.directional();
        display.extra = 0.1;

        final AtomicReference<ScheduledTask> task = new AtomicReference<>();

        Runnable runnable = new Runnable() {
            final double rateDiv = Math.PI / rate;
            int timer = time;
            double theta = 0;

            @Override
            public void run() {
                for (int i = 0; i < points; i++) {
                    // Spawn a circle.
                    double angle = PII * ((double) i / points);
                    double x = radius * Math.cos(theta + angle);
                    double z = radius * Math.sin(theta + angle);

                    // Set the angle of the circle point as its degree.
                    double phi = Math.atan2(z, x);
                    double xDirection = -Math.cos(phi);
                    double zDirection = -Math.sin(phi);

                    display.offset(xDirection, 0, zDirection);
                    display.spawn(x, 0, z);

                    // The modes are done by random math methods that are
                    // just randomly tested to give a different shape.
                    if (mode > 1) {
                        x = radius * Math.cos(-theta + angle);
                        z = radius * Math.sin(-theta + angle);

                        // Eye shaped blackhole
                        if (mode == 2) phi = Math.atan2(z, x);
                        else if (mode == 3) phi = Math.atan2(x, z);
                        else if (mode == 4) Math.atan2(Math.log(x), Math.log(z));

                        xDirection = -Math.cos(phi);
                        zDirection = -Math.sin(phi);

                        display.offset(xDirection, 0, zDirection);
                        display.spawn(x, 0, z);
                    }
                }

                theta += rateDiv;
                if (--timer <= 0) task.get().cancel();
            }
        };
        task.set(SCore.schedulerHook.runAsyncRepeatingTask(runnable, 0L, 1L));
        return task.get();
    }

    /**
     * Spawn a cone.
     *
     * @param height     the height of the cone.
     * @param radius     the radius of the cone circle.
     * @param rate       the rate of the cone circles.
     * @param circleRate the rate of the cone circle points.
     * @param fillMode   the fill mode of the cone. Can be "disk", "ring" or "spiral".
     *
     * @since 1.0.0
     */
    public static void cone(double height, double radius, double rate, double circleRate, String fillMode, ParticleDisplay display) {
        // Our biggest radius / amount of loop times = the amount to subtract from the biggest radius so it wouldn't be negative.
        double radiusDiv = radius / (height / rate);
        //if rate == 0 -> infinite loop
        if(rate < 0) rate = 1;
        if(circleRate < 0) circleRate = 1;
        // We're going spawn circles with different radiuses and rates to make a cone.
        display.setDirectionPitch(90);
        for (double i = 0; i < height; i += rate) {
            radius -= radiusDiv;
            // The remainder of radiusDiv division might be not 0
            // This will happen to the last loop only.
            //SsomarDev.testMsg("circle radius: " + radius, true);
            if (radius < 0) break;
            circle(radius, circleRate, 0, "", fillMode,display.cloneWithLocation(0, i, 0));
        }
    }

    /**
     * Spawns a crescent pointing in the direction defined by yaw and pitch.
     *
     * @param radius         Radius of the main crescent arc.
     * @param rate           Number of segments in the arc.
     * @param display        Particle display object to spawn particles.
     */
    public static void crescent(double radius, double rate, ParticleDisplay display) {
        double rateDiv = Math.PI / rate;
        double end = Math.toRadians(325);
        double start = Math.toRadians(45);

        // Compute direction vector from yaw and pitch
        double yawRad = Math.toRadians(display.getDirectionYaw());
        double pitchRad = Math.toRadians(0);
        double dx = -Math.cos(pitchRad) * Math.sin(yawRad);
        double dy = -Math.sin(pitchRad);
        double dz = Math.cos(pitchRad) * Math.cos(yawRad);
        Vector targetDir = new Vector(dx, dy, dz).normalize();

        // Default direction of the crescent (facing +X)
        Vector from = new Vector(1, 0, 0);

        // Compute rotation axis and angle
        Vector axis = from.clone().crossProduct(targetDir);
        double angle = Math.acos(from.dot(targetDir));
        boolean rotate = axis.lengthSquared() > 0.0001;

        for (double theta = start; theta <= end; theta += rateDiv) {
            double x = Math.cos(theta);
            double z = Math.sin(theta);

            // Outer crescent circle point
            Vector outer = new Vector(radius * x, 0, radius * z);
            if (rotate) outer = rotateVector(outer, axis, angle);
            display.spawn(outer.getX(), outer.getY(), outer.getZ());

            // Inner crescent (shifted smaller circle)
            double smallerRadius = radius / 1.3;
            Vector inner = new Vector(smallerRadius * x + 0.8, 0, smallerRadius * z);
            if (rotate) inner = rotateVector(inner, axis, angle);
            display.spawn(inner.getX(), inner.getY(), inner.getZ());
        }
    }

    /**
     * Rotates a vector around an axis by a given angle using Rodrigues' rotation formula.
     */
    private static Vector rotateVector(Vector v, Vector axis, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return v.clone().multiply(cos)
                .add(axis.clone().crossProduct(v).multiply(sin))
                .add(axis.clone().multiply(axis.dot(v) * (1 - cos)));
    }

    public static ScheduledTask circle(double radius, double density, int time, String drawMode, String fillMode, ParticleDisplay display) {
        return baseCircle(radius, density, 0, time, drawMode, fillMode, display);
    }

    public static ScheduledTask baseCircle(double radius, double density, double height, int time, String drawMode, String fillMode, ParticleDisplay display) {
        List<Vector> points = new ArrayList<>();
        double totalAngle = 2 * Math.PI;
        double step = totalAngle / density;

        if(height <= 0) height = 1;
        if(density <= 0) density = 1;

        Vector direction = calculDirection(display.getDirectionYaw(), display.getDirectionPitch());
        Vector axis1 = calculDirection(display.getDirectionYaw() + 90, 0).normalize();  // perpendicular horizontal axis
        Vector axis2 = axis1.clone().getCrossProduct(direction).normalize(); // vertical axis

        double verticalStep = 1.0 / (density / 2); // control density vertically

        switch (fillMode.toLowerCase()) {
            case "cylinder":
                for (double y = 0; y <= height; y += verticalStep) {
                    Vector layerOffset = direction.clone().normalize().multiply(y);
                    addCircleLayer(radius, density, step, "ring", axis1, axis2, direction.clone().add(layerOffset), points);
                }
                break;
            case "disk":
            case "spiral":
            case "ring":
            default:
                // Only one layer, like a flat shape
                addCircleLayer(radius, density, step, fillMode, axis1, axis2, direction, points);
                break;
        }

        // Apply draw order
        switch (drawMode.toLowerCase()) {
            case "counterclockwise":
                Collections.reverse(points);
                break;
            case "random":
                Collections.shuffle(points);
                break;
        }

        // Instant display
        if (time <= 0) {
            for (Vector p : points) display.spawn(p);
            return null;
        }

        int totalPoints = points.size();
        int perTick = Math.max(1, totalPoints / time);
        AtomicReference<ScheduledTask> task = new AtomicReference<>();

        Runnable runnable = new Runnable() {
            int index = 0;

            @Override
            public void run() {
                for (int i = 0; i < perTick && index < totalPoints; i++, index++) {
                    display.spawn(points.get(index));
                }
                if (index >= totalPoints) task.get().cancel();
            }
        };

        ScheduledTask scheduled = SCore.schedulerHook.runAsyncRepeatingTask(runnable, 0L, 1L);
        task.set(scheduled);
        return scheduled;
    }


    private static void addCircleLayer(double radius, double density, double step, String fillMode,
                                       Vector axis1, Vector axis2, Vector offset, List<Vector> points) {

        switch (fillMode.toLowerCase()) {

            case "spiral":
                double spiralDensity = density * 2;
                double spiralStep = 2 * Math.PI / spiralDensity;
                for (double t = 0; t <= 2 * Math.PI * (radius / 0.1); t += spiralStep) {
                    double r = (t / (2 * Math.PI)) * (radius / (density / 10));
                    Vector vec = rotatedCirclePoint(r, t % (2 * Math.PI), axis1, axis2).add(offset);
                    points.add(vec);
                }
                break;

            case "ring":
                for (double theta = 0; theta <= 2 * Math.PI; theta += step) {
                    Vector vec = rotatedCirclePoint(radius, theta, axis1, axis2).add(offset);
                    points.add(vec);
                }
                break;
            case "disk":
            default:
                for (double r = 0; r <= radius; r += radius / (density / 10)) {
                    for (double theta = 0; theta <= 2 * Math.PI; theta += step) {
                        Vector vec = rotatedCirclePoint(r, theta, axis1, axis2).add(offset);
                        points.add(vec);
                    }
                }
                break;
        }
    }


    private static Vector rotatedCirclePoint(double r, double theta, Vector axis1, Vector axis2) {
        double x = r * Math.cos(theta);
        double y = r * Math.sin(theta);
        return axis1.clone().multiply(x).add(axis2.clone().multiply(y));
    }

    /**
     * Spawns connected 3D ellipses.
     *
     * @param maxRadius  the maximum radius for the ellipses.
     * @param rate       the rate of the 3D ellipses circle points.
     * @param radiusRate the rate of the circle radius change.
     * @param extend     the extension for each ellipse.
     * @param time       the duration of the beam in ticks
     *
     * @return the animation handler.
     * @since 3.0.0
     */
    public static ScheduledTask circularBeam(double maxRadius, double rate, double radiusRate, double extend, double time, ParticleDisplay display) {

        final AtomicReference<ScheduledTask> task = new AtomicReference<>();

        Runnable runnable = new Runnable() {
            final double rateDiv = Math.PI / rate;
            final double radiusDiv = Math.PI / radiusRate;
            final Vector dir = display.getLocation().getDirection().normalize().multiply(extend);
            double dynamicRadius = 0;
            long repeat = 0;

            @Override
            public void run() {
                // If we wanted to use actual numbers as the radius then the curve for
                // each loop wouldn't be smooth.
                double radius = maxRadius * Math.sin(dynamicRadius);
                // Spawn normal circles.
                for (double theta = 0; theta < PII; theta += rateDiv) {
                    double x = radius * Math.sin(theta);
                    double z = radius * Math.cos(theta);
                    display.spawn(x, 0, z);
                }

                dynamicRadius += radiusDiv;
                if (dynamicRadius > Math.PI) dynamicRadius = 0;
                // Next circle center location. (need to clone otherwise it'll change the original)
                display.getLocation().clone().add(dir);
                if (++repeat > time){
                    task.get().cancel();
                    return;
                }
            }
        };

        task.set(SCore.schedulerHook.runAsyncRepeatingTask(runnable, 0L, 1L));

        return task.get();
    }

    public static ScheduledTask cylinder(double radius, double density, double height, int time, String drawMode, ParticleDisplay display) {
        return baseCircle(radius, density, height, time, drawMode, "cylinder", display);
    }

    /**
     * Spawns a diamond-shaped rhombus.
     *
     * @param radiusRate the radius of the diamond. Lower means longer radius.
     * @param rate       the rate of the diamond points.
     * @param height     the height of the diamond.
     *
     * @since 4.0.0
     */
    public static void diamond(double radiusRate, double rate, double height, ParticleDisplay display) {
        double count = 0;
        for (double y = 0; y < height * 2; y += rate) {
            // We're going to increase our x particles as we get closer to the center
            // and decrease as we move away. If the radius is equal to rate it'll form a rotated square.
            if (y < height) count += radiusRate;
            else count -= radiusRate;

            // Now we can make an arrow or a right triangle if let x be equal to 0
            // But we want both sides to have particle.
            for (double x = -count; x < count; x += rate) display.spawn(x, y, 0);
        }
    }

    /**
     * Spawn a DNA double helix string with nucleotides.
     *
     * @param radius              the radius of two DNA string circles.
     * @param rate                the rate of DNA strings and hydrogen bond points.
     * @param height              the height of the DNA strings.
     * @param hydrogenBondDist    the distance between each hydrogen bond (read inside method). This distance is also affected by rate.
     * @param display             display for strings.
     *
     * @since 1.0.0
     */
    public static void dna(double radius, double rate, double extension, int height, int hydrogenBondDist, ParticleDisplay display) {
        ParticleDisplay hydrogenBondDisplay = display;
        // The distance between each hydrogen bond from the previous bond.
        // All the nucleotides in DNA will form a bond but this will indicate the
        // distance between the phosphodiester bonds.
        int nucleotideDist = 0;

        // Move the helix upwards by forming phosphodiester bonds between two nucleotides on the same string.
        for (double y = 0; y <= height; y += rate) {
            nucleotideDist++;

            // The helix string is generated in a circle tunnel.
            double x = radius * Math.cos(extension * y);
            double z = radius * Math.sin(extension * y);

            // The two nucleotides on each DNA string.
            // Should be exactly facing each other with the same Y pos.
            Location nucleotide1 = display.getLocation().clone().add(x, y, z);
            display.spawn(x, y, z);
            Location nucleotide2 = display.getLocation().clone().subtract(x, -y, z);
            display.spawn(-x, y, -z);

            // If it's the appropriate distance for two nucleotides to form a hydrogen bond.
            // We don't care about the type of nucleotide. It's going to be one bond only.
            if (nucleotideDist >= hydrogenBondDist) {
                nucleotideDist = 0;
                line(nucleotide1, nucleotide2, rate * 2, hydrogenBondDisplay);
            }
        }
    }

    /**
     * Spawn an animated DNA replication with colored bonds.
     *
     * @param radius           the radius of DNA helix circle.
     * @param rate             the rate of DNA points.
     * @param speed            the number of points to build in a single tick. Recommended is 5.
     * @param extension        the extension of the DNA helix sin/cos waves.
     * @param height           the height of the DNA strings.
     * @param hydrogenBondDist the distance between two DNA string helix points in a single string for each hydrogen bond to be formed.
     *
     * @return the timer handling the animation.
     * @since 3.0.0
     */
    public static ScheduledTask dnaReplication(double radius, double rate, double speed, double extension,
                                               int height, int hydrogenBondDist, ParticleDisplay display) {
        // We'll use the common nucleotide colors.
        ParticleDisplay adenine = ParticleDisplay.colored(null, java.awt.Color.BLUE, 1); // Blue
        ParticleDisplay thymine = ParticleDisplay.colored(null, java.awt.Color.YELLOW, 1); // Yellow
        ParticleDisplay guanine = ParticleDisplay.colored(null, java.awt.Color.GREEN, 1); // Green
        ParticleDisplay cytosine = ParticleDisplay.colored(null, java.awt.Color.RED, 1); // Red

        final int speedFinal = (int) Math.max(1, speed);

        final AtomicReference<ScheduledTask> task = new AtomicReference<>();

        Runnable runnable = new Runnable() {
            double y = 0;
            int nucleotideDist = 0;

            @Override
            public void run() {
                int repeat = speedFinal;
                /* the location comes from the target (entity, block...): when it is gone, stop the animation instead of throwing every tick
                 * (Folia, Discord 1421543760637988936) */
                if (display.getLocation() == null) {
                    /* async with no delay: the first tick can run before task.set(...), it is then cancelled on the next one */
                    ScheduledTask t = task.get();
                    if (t != null) t.cancel();
                    return;
                }
                while (repeat-- != 0) {
                    y += rate;
                    nucleotideDist++;

                    double x = radius * Math.cos(extension * y);
                    double z = radius * Math.sin(extension * y);
                    Location nucleotide1 = display.getLocation().clone().add(x, y, z);
                    //display.spawn(x, y, z);
                    circle(1, 10,0,"","", display.cloneWithLocation(x, y, z));
                    Location nucleotide2 = display.getLocation().clone().subtract(x, -y, z);
                    circle(1, 10, 0,"","",display.cloneWithLocation(-x, y, -z));
                    //display.spawn(-x, y, -z);

                    // We're going to find the midpoint of the two nucleotides so we can
                    // form our hydrogen bond.
                    // We'll convert locations to vectors since the midpoint method is only
                    // available for Vectors. Yes, we can still calculate the midpoint from locations too.
                    // Xm = (x1 + x2) / 2, Ym = (y1 + y2) / 2, Zm = (z1 + z2) / 2
                    Location midPointBond = nucleotide1.toVector().midpoint(nucleotide2.toVector()).toLocation(nucleotide1.getWorld());

                    if (nucleotideDist >= hydrogenBondDist) {
                        nucleotideDist = 0;

                        // Adenine - Thymine
                        if (randInt(0, 1) == 1) {
                            line(nucleotide1, midPointBond, rate - 0.1, adenine);
                            line(nucleotide2, midPointBond, rate - 0.1, thymine);
                        }
                        // Guanine - Cytosine
                        else {
                            line(nucleotide1, midPointBond, rate - 0.1, cytosine);
                            line(nucleotide2, midPointBond, rate - 0.1, guanine);
                        }
                    }

                    if (y >= height) {
                        task.get().cancel();
                        return;
                    }
                }
            }
        };
        task.set(SCore.schedulerHook.runAsyncRepeatingTask(runnable, 0L, 1L));
        return task.get();
    }

    /**
     * Spawn an ellipse.

     * @since 2.0.0
     */
    public static void ellipse(double start, double end, double rate, double radius, double otherRadius, ParticleDisplay display) {
        // The only difference between circles and ellipses are that
        // ellipses use a different radius for one of their axis.
        rate = Math.max(0.1, rate);
        for (double theta = start; theta <= end; theta += rate) {
            double x = radius * Math.cos(theta);
            double y = otherRadius * Math.sin(theta);
            display.spawn(x, y, 0);
        }
    }

    /**
     * A sin/cos based smoothly animated explosion wave.
     * Source: https://www.youtube.com/watch?v=n8W7RxW5KB4
     *
     * @param rate   the distance between each cos/sin lines.
     * @param start  the initial distance from the center to start the wave.
     * @param height the vertical amplitude of the wave.
     *
     * @since 1.0.0
     */
    public static ScheduledTask explosionWave(double rate, double start, double height, ParticleDisplay display) {
        ParticleDisplay secDisplay = display;

        final double heightFinal = height <= 0 ? -height : height;

        final AtomicReference<ScheduledTask> task = new AtomicReference<>();

        Runnable runnable = new Runnable() {
            final double addition = Math.PI * 0.1;
            final double rateDiv = Math.PI / rate;
            double times = start;

            public void run() {
                times += addition;
                for (double theta = 0; theta <= PII; theta += rateDiv) {
                    double x = times * Math.cos(theta);
                    double y = heightFinal * Math.exp(-0.1 * times) * Math.sin(times) + 1.5;
                    double z = times * Math.sin(theta);
                    display.spawn(x, y, z);

                    theta = theta + Math.PI / 64;
                    x = times * Math.cos(theta);
                    // y remains the same
                    z = times * Math.sin(theta);
                    secDisplay.spawn(x, y, z);
                }

                if (times > 20) {
                    task.get().cancel();
                    return;
                }
            }
        };
        task.set(SCore.schedulerHook.runAsyncRepeatingTask(runnable, 0L, 1L));
        return task.get();
    }

    /**
     * Spawns an eye-shaped figure in the direction given by yaw and pitch.
     *
     * @param radius         the vertical radius of the eye.
     * @param radius2        the mirrored vertical radius (usually same as radius).
     * @param rate           the number of segments in the eye curve.
     * @param extension      how sharp or wide the eye shape is (recommended ~0.2).
     * @param display        the ParticleDisplay to spawn particles with.
     */
    public static void eye(double radius, double radius2, double rate, double extension, ParticleDisplay display) {
        if (rate == 0) rate = 1;
        if (extension == 0) extension = 1;

        double rateDiv = Math.PI / rate;
        double limit = Math.PI / extension;

        // Direction vector from yaw/pitch
        double yawRad = Math.toRadians(display.getDirectionYaw());
        double pitchRad = Math.toRadians(display.getDirectionPitch());
        double dx = -Math.cos(pitchRad) * Math.sin(yawRad);
        double dy = -Math.sin(pitchRad);
        double dz = Math.cos(pitchRad) * Math.cos(yawRad);
        Vector targetDir = new Vector(dx, dy, dz).normalize();

        // Default direction (along +X)
        Vector from = new Vector(1, 0, 0);

        // Compute rotation axis and angle
        Vector axis = from.clone().crossProduct(targetDir);
        double angle = Math.acos(from.dot(targetDir));
        boolean rotate = axis.lengthSquared() > 0.0001;

        double x = 0;
        for (double i = 0; i < limit; i += rateDiv) {
            double y = radius * Math.sin(extension * i);
            double y2 = radius2 * Math.sin(extension * -i);

            Vector point1 = new Vector(x, y, 0);
            Vector point2 = new Vector(x, y2, 0);
            if (rotate) {
                point1 = rotateVector(point1, axis, angle);
                point2 = rotateVector(point2, axis, angle);
            }

            display.spawn(point1.getX(), point1.getY(), point1.getZ());
            display.spawn(point2.getX(), point2.getY(), point2.getZ());

            x += 0.1;
        }
    }

    /**
     * Spawns a heart-shaped figure pointing in a specific direction.
     *
     * @param cut             number of paired ovals (2 for a classic heart).
     * @param cutAngle        compression for the two ovals (4 for a classic heart).
     * @param depth           depth of the inner spike of the heart.
     * @param compressHeight  compression factor on the Y axis.
     * @param rate            density of points (higher = smoother shape).
     * @param display         the ParticleDisplay used to spawn the particles.
     */
    public static void heart(double cut, double cutAngle, double depth, double compressHeight, double rate, ParticleDisplay display) {
        // Compute target direction vector from yaw and pitch
        double yawRad = Math.toRadians(display.getDirectionYaw()+90);
        double pitchRad = Math.toRadians(display.getDirectionPitch());
        double dx = -Math.cos(pitchRad) * Math.sin(yawRad);
        double dy = -Math.sin(pitchRad);
        double dz = Math.cos(pitchRad) * Math.cos(yawRad);
        Vector targetDir = new Vector(dx, dy, dz).normalize();

        // Default direction (along Z+ axis)
        Vector from = new Vector(0, 0, 1);

        // Compute rotation axis and angle
        Vector axis = from.clone().crossProduct(targetDir);
        double angle = Math.acos(from.dot(targetDir));
        boolean rotate = axis.lengthSquared() > 0.0001;

        for (double theta = 0; theta <= Math.PI * 2; theta += Math.PI / rate) {
            double phi = theta / cut;
            double cos = Math.cos(phi);
            double sin = Math.sin(phi);
            double omega = Math.pow(Math.abs(Math.sin(2 * cutAngle * phi)) + depth * Math.abs(Math.sin(cutAngle * phi)),
                    1 / compressHeight);

            double y = omega * (sin + cos);
            double z = omega * (cos - sin);

            Vector point = new Vector(0, y, z);
            if (rotate) point = rotateVector(point, axis, angle);

            display.spawn(point.getX(), point.getY(), point.getZ());
        }
    }

    /**
     * Spawns animated helix shapes.
     *
     * @param strings   the amount of helix strings. The rotation angle will split equally for each.
     * @param radius    the radius of the helix.
     * @param rate      the rate of helix points.
     * @param extension the helix circle extension.
     * @param height    the height of the helix.
     * @param speed     the speed of the rate builder in each animation tick.
     * @param fadeUp    helix radius will decrease to zero as it gets closer to the top.
     * @param fadeDown  helix radius will increase to the original radius as it gets closer to the center.
     *
     * @return the animation task.
     * @since 3.0.0
     */
    public static ScheduledTask helix(int strings, double radius, double rate, double extension, int height, int speed,
                                      boolean fadeUp, boolean fadeDown, ParticleDisplay display) {

        final AtomicReference<ScheduledTask> task = new AtomicReference<>();

        Runnable runnable = new Runnable() {
            // If we look at a helix string from above, we'll see a circle tunnel.
            // To make this tunnel we're going to generate circles while moving
            // upwards to get a curvy tunnel.
            // Since we're generating this string infinitely we don't need
            // to use radians or degrees.
            final double dist = PII / strings;
            final double radiusDiv = radius / (height / rate);
            final double radiusDiv2 = fadeUp && fadeDown ? radiusDiv * 2 : radiusDiv;
            double dynamicRadius = fadeDown ? 0 : radius;
            boolean center = !fadeDown;
            double y = 0;

            @Override
            public void run() {
                int repeat = speed;
                while (repeat-- > 0) {
                    y += rate;

                    // 2D cirlce points.
                    double x = dynamicRadius * Math.cos(extension * y);
                    double z = dynamicRadius * Math.sin(extension * y);

                    if (!center) {
                        dynamicRadius += radiusDiv2;
                        if (dynamicRadius >= radius) center = true;
                    } else if (fadeUp) dynamicRadius -= radiusDiv2;

                    // Now we're going to copy our points and rotate them.
                    int tempString = strings;
                    for (double angle = 0; tempString > 0; angle += dist) {
                        display.rotate(0, angle, 0);
                        display.spawn(x, y, z);
                        display.rotate(0, -angle, 0);
                        tempString--;
                    }

                    if (y > height) {
                        task.get().cancel();
                        return;
                    }
                }
            }
        };
        task.set(SCore.schedulerHook.runAsyncRepeatingTask(runnable, 0L, 1L));
        return task.get();
    }

    /**
     * Spawns an illuminati shape.
     *
     * @param size      the size of the illuminati shape.
     * @param extension the extension of the illuminati eye.
     *
     * @since 4.0.0
     */
    public static void illuminati(double size, double extension, ParticleDisplay display) {
        polygon(3, 1, size, 1 / (size * 30), 0, display);
        // It'd be really hard to automatically adjust the extension based on the size.
        eye(size / 4, size / 4, 30, extension, display.cloneWithLocation(0.3, 0, size / 1.8).rotate(Math.PI / 2, Math.PI / 2, 0));
        display.setDirectionPitch(90);
        circle(size / 5, size * 5, 0,"","",display.cloneWithLocation(0.3, 0, 0));
    }

    /**
     * This is supposed to be something similar to this: https://www.deviantart.com/pwincessstar/art/701840646
     * The numbers on this shape are really sensitive. Changing a single one can result
     * in a totally different shape.
     *
     * @param size the shape of the explosion circle. Recommended value is 6
     *
     * @see #polygon(int, int, double, double, double, ParticleDisplay)
     * @since 1.0.0
     */
    public static void meguminExplosion(double size, ParticleDisplay display) {
        polygon(10, 4, size, 0.02, 0.3, display);
        polygon(10, 3, size / (size - 1), 0.5, 0, display);
        circle(size, 40,0,"","", display);
        spread(30, 2, display.getLocation(), display.getLocation().clone().add(0, 10, 0), 5, 5, 5, display);
    }

    /**
     * Spawns circles increasing their radius.
     *
     * @param radius     the radius for the first circle.
     * @param rate       the rate of circle points.
     * @param radiusRate the circle radius change rate.
     * @param distance   the distance between each circle.
     *
     * @return the animation handler.
     * @see #circularBeam(Plugin, double, double, double, double, double, ParticleDisplay)
     * @since 3.0.0
     */
    public static ScheduledTask magicCircles(double radius, double rate, double radiusRate, double distance, int time, ParticleDisplay display) {

        final AtomicReference<ScheduledTask> task = new AtomicReference<>();

        Runnable runnable = new Runnable() {
            final double radiusDiv = Math.PI / radiusRate;
            final Vector dir = display.getLocation().getDirection().normalize().multiply(distance);
            double dynamicRadius = radius;
            int timer = time;

            @Override
            public void run() {
                double rateDiv = Math.PI / (rate * dynamicRadius);
                for (double theta = 0; theta < PII; theta += rateDiv) {
                    double x = dynamicRadius * Math.sin(theta);
                    double z = dynamicRadius * Math.cos(theta);
                    display.spawn(x, 0, z);
                }

                // We're going to use normal numbers since the circle radius will be always changing
                // in one axis.
                dynamicRadius += radiusDiv;
                display.getLocation().add(dir);

                if(timer-- <= 0) task.get().cancel();
            }
        };
        task.set(SCore.schedulerHook.runAsyncRepeatingTask(runnable, 0L, 1L));
        return task.get();
    }

    /**
     * Spawns a connected 2D polygon.
     * Tutorial: https://www.spigotmc.org/threads/158678/
     *
     * @param points     the number of polygon points.
     * @param connection the connection level of two points.
     * @param size       the size of the shape.
     * @param rate       the rate of connection points.
     * @param extend     extends the shape, connecting unrelated points together.
     *
     * @since 1.0.0
     */
    public static void polygon(int points, int connection, double size, double rate, double extend, ParticleDisplay display) {
        for (int point = 0; point < points; point++) {
            // Generate our points in a circle shaped area.
            double angle = Math.toRadians(360D / points * point);
            // Our next point to connect to the previous one.
            // So if you don't want them to connect you can just skip the rest.
            double nextAngle = Math.toRadians(360D / points * (point + connection));

            // Size is basically the circle's radius.
            // Get our X and Z position based on the angle of the point.
            double x = Math.cos(angle) * size;
            double z = Math.sin(angle) * size;

            double x2 = Math.cos(nextAngle) * size;
            double z2 = Math.sin(nextAngle) * size;

            // The distance between one point to another.
            double deltaX = x2 - x;
            double deltaZ = z2 - z;

            // Connect the points.
            // Extend value is a little complicated Idk how to explain it.
            // Might be related: https://en.wikipedia.org/wiki/Hypercube
            for (double pos = 0; pos < 1 + extend; pos += rate) {
                display.spawn(x + (deltaX * pos), 0, z + (deltaZ * pos));
            }
        }
    }

    public static ScheduledTask ring(double radius, double density, int time, String drawMode, ParticleDisplay display) {
        return circle(radius, density, time, drawMode, "ring", display);
    }

    /**
     * Spawn a sphere.
     * Tutorial: https://www.spigotmc.org/threads/146338/
     * Also uses its own unique directional pattern.
     *
     * @param radius the circle radius.
     * @param rate   the rate of cirlce points/particles.
     *
     * @since 1.0.0
     */
    public static void sphere(double radius, double rate, ParticleDisplay display) {
        // Cache
        double rateDiv = Math.PI / rate;

        // To make a sphere we're going to generate multiple circles
        // next to each other.
        for (double phi = 0; phi <= Math.PI; phi += rateDiv) {
            // Cache
            double y1 = radius * Math.cos(phi);
            double y2 = radius * Math.sin(phi);

            for (double theta = 0; theta <= PII; theta += rateDiv) {
                double x = Math.cos(theta) * y2;
                double z = Math.sin(theta) * y2;

                if (display.isDirectional()) {
                    // We're going to do the same thing from spreading circle.
                    // Since this is a 3D shape we'll need to get the y value as well.
                    // I'm not sure if this is the right way to do it.
                    double omega = Math.atan2(z, x);
                    double directionX = Math.cos(omega);
                    double directionY = Math.sin(Math.atan2(y2, y1));
                    double directionZ = Math.sin(omega);

                    display.offset(directionX, directionY, directionZ);
                }

                display.spawn(x, y1, z);
            }
        }
    }

    /**
     * Spawns a sphere with spikes coming out from the center.
     * The sphere points will not be visible.
     *
     * @param radius            the radius of the sphere.
     * @param rate              the rate of sphere spike points.
     * @param chance            the chance to grow a spike randomly.
     * @param minRandomDistance he minimum distance of spikes from sphere.
     * @param maxRandomDistance the maximum distance of spikes from sphere.
     *
     * @see #sphere(double, double, ParticleDisplay)
     * @since 1.0.0
     */
    public static void spikeSphere(double radius, double rate, int chance, double minRandomDistance, double maxRandomDistance, ParticleDisplay display) {
        double rateDiv = Math.PI / rate;

        // Generate normal circle points.
        for (double phi = 0; phi <= Math.PI; phi += rateDiv) {
            double y = radius * Math.cos(phi);
            double sinPhi = radius * Math.sin(phi);

            for (double theta = 0; theta <= PII; theta += rateDiv) {
                double x = Math.cos(theta) * sinPhi;
                double z = Math.sin(theta) * sinPhi;

                if (chance == 0 || randInt(0, chance) == 1) {
                    Location start = display.cloneLocation(x, y, z);
                    // We want to get the direction of our center location and the circle point
                    // so we cant spawn spikes on the opposite direction.
                    Vector endVect = start.clone().subtract(display.getLocation()).toVector().multiply(random(minRandomDistance, maxRandomDistance));
                    Location end = start.clone().add(endVect);
                    line(start, end, 0.1, display);
                }
            }
        }
    }

    /**
     * Spawns a 2D wall of particles based on pitch and yaw, supporting full control over height, length, and width.
     *
     * @param height           height in blocks along vertical axis.
     * @param length           length in blocks along direction vector.
     * @param width            width in blocks perpendicular to the wall direction.
     * @param density          number of particles per block (higher = more dense).
     * @param time    time in ticks to animate the full display.
     * @param drawMode         "vertical" or "horizontal" — controls iteration order.
     * @param verticalOrder    "up" or "down" — order along height.
     * @param horizontalOrder  "near" or "far" — order along length.
     * @param display          the particle display object.
     */
    public static ScheduledTask square(double height, double length, double width, double density, int time,
                                       String drawMode, String verticalOrder, String horizontalOrder, ParticleDisplay display) {
        List<Vector> grid = new ArrayList<>();
        double spacing = 1.0 / density;

        Vector direction = calculDirection(display.getDirectionYaw(), display.getDirectionPitch());

        // Compute two axes for the plane: vertical and horizontal
        Vector axis1 = calculDirection(display.getDirectionYaw() + 90, 0).normalize(); // horizontal across wall
        Vector axis2 = axis1.clone().getCrossProduct(direction).normalize();           // vertical (up)
        Vector axis3 = direction.clone().normalize();                                  // length direction

        boolean verticalUp = verticalOrder.equalsIgnoreCase("up");
        boolean horizontalNear = horizontalOrder.equalsIgnoreCase("near");

        List<Double> hList = new ArrayList<>();
        for (double h = (verticalUp ? 0 : height);
             verticalUp ? h <= height : h >= 0;
             h += (verticalUp ? spacing : -spacing)) {
            hList.add(h);
        }

        List<Double> lList = new ArrayList<>();
        for (double l = (horizontalNear ? 0 : length);
             horizontalNear ? l <= length : l >= 0;
             l += (horizontalNear ? spacing : -spacing)) {
            lList.add(l);
        }

        List<Double> wList = new ArrayList<>();
        for (double w = -width / 2; w <= width / 2; w += spacing) {
            wList.add(w);
        }


        // Depending on draw mode: iterate order
        if (drawMode.equalsIgnoreCase("vertical")) {
            for (double l : lList) {
                for (double w : wList) {
                    for (double h : hList) {
                        Vector point = axis1.clone().multiply(w)
                                .add(axis2.clone().multiply(h))
                                .add(axis3.clone().multiply(l));
                        grid.add(point);
                    }
                }
            }
        } else {
            for (double h : hList) {
                for (double w : wList) {
                    for (double l : lList) {
                        Vector point = axis1.clone().multiply(w)
                                .add(axis2.clone().multiply(h))
                                .add(axis3.clone().multiply(l));
                        grid.add(point);
                    }
                }
            }
        }

        if (time <= 0) {
            for (Vector point : grid) display.spawn(point);
            return null;
        }

        int total = grid.size();
        int perTick = Math.max(1, total / time);
        AtomicReference<ScheduledTask> task = new AtomicReference<>();

        Runnable runnable = new Runnable() {
            int index = 0;

            @Override
            public void run() {
                for (int i = 0; i < perTick && index < total; i++, index++) {
                    display.spawn(grid.get(index));
                }
                if (index >= total) task.get().cancel();
            }
        };

        ScheduledTask scheduled = SCore.schedulerHook.runAsyncRepeatingTask(runnable, 0L, 1L);
        task.set(scheduled);
        return scheduled;
    }


































    public static ScheduledTask spiral(double radius, double density, int time, String drawMode, ParticleDisplay display) {
        return circle(radius, density, time, drawMode, "spiral", display);
    }

    public static ScheduledTask filledCircle(double radius, double density, int time, String drawMode, ParticleDisplay display) {
        return circle(radius, density, time, drawMode, "disk", display);
    }





    /**
     * Spawns a double pendulum with chaotic movement.
     * Note that if this runs for too long it'll stop working due to
     * the limit of doubles resulting in a {@link Double#NaN}
     * <p>
     * <a href="https://en.wikipedia.org/wiki/Double_pendulum">Double pendulum</a>
     * is a way to show <a href="https://en.wikipedia.org/wiki/Chaos_theory">Chaos motion</a>.
     * The particles display are showing the path where the second
     * pendulum is going from.
     * <p>
     * Changing the mass or length to a lower value can make the
     * shape stop producing new paths since it reaches the doubles limit.
     * Source: https://www.myphysicslab.com/pendulum/double-pendulum-en.html
     *
     * @param plugin     the timer handler.
     * @param radius     the radius of the pendulum. Yes this doesn't depend on length since the length needs to be a really
     *                   high value and this won't work with Minecraft's xyz.
     * @param gravity    the gravity of the enviroment. Recommended is -1 positive numbers will mean gravity towards space.
     * @param length     the length of the first pendulum. Recommended is 200
     * @param length2    the length of the second pendulum. Recommended is 200
     * @param mass1      the mass of the first pendulum. Recommended is 50
     * @param mass2      the mass of the second pendulum. Recommended is 50
     * @param dimension3 if it should enter 3D mode.
     * @param speed      the speed of the animation.
     *
     * @return the animation handler.
     * @since 4.0.0
     */
    public static ScheduledTask chaoticDoublePendulum(Plugin plugin, double radius, double gravity, double length, double length2, double mass1, double mass2, boolean dimension3, int speed, int time, ParticleDisplay display) {
        // If you want the particles to stay. But it's gonna lag a lot.
        //Map<Vector, Vector> locs = new HashMap<>();

        if(speed <= 0) speed = 1;
        final int speedFinal = speed;
        final AtomicReference<ScheduledTask> task = new AtomicReference<>();

        Runnable runnable =  new Runnable() {
            final Vector rotation = new Vector(Math.PI / 33, Math.PI / 44, Math.PI / 55);
            double theta = Math.PI / 2;
            double theta2 = Math.PI / 2;
            double thetaPrime = 0;
            double thetaPrime2 = 0;
            int timer = time;

            @Override
            public void run() {
                int repeat = speedFinal;
                while (repeat-- != 0) {
                    if (dimension3) display.rotate(rotation);
                    double totalMass = mass1 + mass2;
                    double totalMassDouble = 2 * totalMass;
                    double deltaTheta = theta - theta2;

                    double lenLunar = (totalMassDouble - mass2 * Math.cos(2 * theta - 2 * theta2));
                    double deltaCosTheta = Math.cos(deltaTheta);
                    double deltaSinTheta = Math.sin(deltaTheta);
                    double phi = thetaPrime * thetaPrime * length;
                    double phi2 = thetaPrime2 * thetaPrime2 * length2;

                    // Don't expect me to explain these... Read the website.
                    double num1 = -gravity * totalMassDouble * Math.sin(theta);
                    double num2 = -mass2 * gravity * Math.sin(theta - 2 * theta2);
                    double num3 = -2 * deltaSinTheta * mass2;
                    double num4 = phi2 + phi * deltaCosTheta;
                    double len = length * lenLunar;
                    double thetaDoublePrime = (num1 + num2 + num3 * num4) / len;

                    num1 = 2 * deltaSinTheta;
                    num2 = phi * totalMass;
                    num3 = gravity * totalMass * Math.cos(theta);
                    num4 = phi2 * mass2 * deltaCosTheta;
                    len = length2 * lenLunar;
                    double thetaDoublePrime2 = (num1 * (num2 + num3 + num4)) / len;

                    thetaPrime += thetaDoublePrime;
                    thetaPrime2 += thetaDoublePrime2;
                    theta += thetaPrime;
                    theta2 += thetaPrime2;

                    double x = radius * Math.sin(theta);
                    double y = radius * Math.cos(theta);
                    double x2 = x + radius * Math.sin(theta2);
                    double y2 = y + radius * Math.cos(theta2);

                    display.spawn(x2, y2, 0);

//                locs.forEach((v, v2) -> {
//                    ParticleDisplay dis = display.clone();
//                    dis.rotation = v2;
//                    dis.spawn(v.getX(), v.getY(), v.getZ());
//                });
//                locs.put(new Vector(x2, y2, 0), display.rotation.clone());
                }
                if(--timer == 0) task.get().cancel();
            }
        };
        task.set(SCore.schedulerHook.runAsyncRepeatingTask(runnable, 0L, 1L));
        return task.get();
    }


    /**
     * Spawn a 3D infinity sign.
     *
     * @param radius the radius of the infinity circles.
     * @param rate   the rate of the sign points.
     *
     * @since 3.0.0
     */
    public static void infinity(double radius, double rate, ParticleDisplay display) {
        double rateDiv = Math.PI / rate;
        for (double i = 0; i < PII; i += rateDiv) {
            double x = Math.sin(i);
            double smooth = Math.pow(x, 2) + 1;
            double curve = radius * Math.cos(i);

            double z = curve / smooth;
            double y = (curve * x) / smooth;

            // If you remove x the infinity symbol will be 2D
            circle(1, rate, 0,"","", display.cloneWithLocation(x, y, z));
        }
    }

    /**
     * Spawns a rainbow.
     *
     * @param radius  the radius of the smallest circle.
     * @param rate    the rate of the rainbow points.
     * @param curve   the curve the the rainbow circles.
     * @param layers  the layers of each rainbow color.
     * @param compact the distance between each circles.
     *
     * @since 2.0.0
     */
    public static void rainbow(double radius, double rate, double curve, double layers, double compact, ParticleDisplay display) {
        int[][] rainbow = {
                {128, 0, 128}, // Violet
                {75, 0, 130}, // Indigo
                {0, 0, 255}, // Blue
                {0, 255, 0}, // Green
                {255, 255, 0}, // Yellow
                {255, 140, 0}, // Orange
                {255, 0, 0} // Red
        };
        double secondRadius = radius * curve;

        // Rainbows have 7 colors.
        // Refer to RAINBOW constant for the color order.
        for (int i = 0; i < 7; i++) {
            // Get the rainbow color in order.
            int[] rgb = rainbow[i];
            display = ParticleDisplay.colored(display.getLocation(), rgb[0], rgb[1], rgb[2], 1);

            // Display the same color multiple times.
            for (int layer = 0; layer < layers; layer++) {
                double rateDiv = Math.PI / (rate * (i + 2));

                // We're going to create our rainbow layer from half circles.
                for (double theta = 0; theta <= Math.PI; theta += rateDiv) {
                    double x = radius * Math.cos(theta);
                    double y = secondRadius * Math.sin(theta);
                    display.spawn(x, y, 0);
                }

                radius += compact;
            }
        }
    }


    /**
     * Spawns a galaxy-like vortex.
     * Note that the speed of the particle is important.
     * Speed 0 will spawn static lines.
     *
     * @param points the points of the vortex.
     * @param rate   the speed of the vortex.
     * @param time   the duration of the vortex in ticks
     *
     * @return the task handling the animation.
     * @since 2.0.0
     */
    public static ScheduledTask vortex(int points, double rate, double time, ParticleDisplay display) {
        double rateDiv = Math.PI / rate;
        display.directional();

       final AtomicReference<ScheduledTask> task = new AtomicReference<>();

        Runnable runnable =  new Runnable() {
            double theta = 0;
            long repeat = 0;

            @Override
            public void run() {
                theta += rateDiv;

                for (int i = 0; i < points; i++) {
                    // Calculate our starting point in a circle radius.
                    double multiplier = (PII * ((double) i / points));
                    double x = Math.cos(theta + multiplier);
                    double z = Math.sin(theta + multiplier);

                    // Calculate our direction of the spreading particles based on their angle.
                    double angle = Math.atan2(z, x);
                    double xDirection = Math.cos(angle);
                    double zDirection = Math.sin(angle);

                    display.offset(xDirection, 0, zDirection);
                    display.spawn(x, 0, z);
                }
                if (++repeat > time){
                    task.get().cancel();
                    return;
                }
                //System.out.println("Vortex: >> "+repeat);
            }
        };
        task.set(SCore.schedulerHook.runAsyncRepeatingTask(runnable, 0L, 1L));
        return task.get();
    }

    /**
     * This will move the shape around in an area randomly while rotating them.
     * The position of the shape will be randomized positively and negatively by the offset parameters on each axis.
     *
     * @param update   the timer period in ticks.
     * @param rate     the distance between each location. Recommended value is 5.
     * @param runnable the particles to spawn.
     * @param displays the display references used to spawn particles in the runnable.
     *
     * @return the async task handling the movement.
     * @see #rotateAround(Plugin, long, double, double, double, double, Runnable, ParticleDisplay...)
     * @since 1.0.0
     */
    public static ScheduledTask moveRotatingAround(long update, double rate, double offsetx, double offsety, double offsetz,
                                                Runnable runnable, ParticleDisplay... displays) {
        Runnable runnable1 =  new Runnable() {
            double rotation = 180;

            @Override
            public void run() {
                rotation += rate;
                // Generate random radians.
                double x = Math.toRadians(90 + rotation);
                double y = Math.toRadians(60 + rotation);
                double z = Math.toRadians(30 + rotation);

                Vector vector = new Vector(offsetx * Math.PI, offsety * Math.PI, offsetz * Math.PI);
                if (offsetx != 0) ParticleDisplay.rotateAround(vector, ParticleDisplay.Axis.X, x);
                if (offsety != 0) ParticleDisplay.rotateAround(vector, ParticleDisplay.Axis.Y, y);
                if (offsetz != 0) ParticleDisplay.rotateAround(vector, ParticleDisplay.Axis.Z, z);

                for (ParticleDisplay display : displays) display.getLocation().add(vector);
                runnable.run();
                for (ParticleDisplay display : displays) display.getLocation().subtract(vector);
            }
        };
        return SCore.schedulerHook.runAsyncRepeatingTask(runnable1, 0L, update);
    }

    /**
     * This will move the particle around in an area randomly.
     * The position of the shape will be randomized positively and negatively by the offset parameters on each axis.
     *
     * @param plugin   the schedule handler.
     * @param update   the timer period in ticks.
     * @param rate     the distance between each location. Recommended value is 5.
     * @param runnable the particles to spawn.
     * @param displays the display references used to spawn particles in the runnable.
     *
     * @return the async task handling the movement.
     * @see #rotateAround(Plugin, long, double, double, double, double, Runnable, ParticleDisplay...)
     * @since 1.0.0
     */
    public static ScheduledTask moveAround(long update, double rate, double endRate, double offsetx, double offsety, double offsetz,
                                        Runnable runnable, ParticleDisplay... displays) {
        Runnable runnable1 = new Runnable() {
            double multiplier = 0;
            boolean opposite = false;

            @Override
            public void run() {
                if (opposite) multiplier -= rate;
                else multiplier += rate;

                double x = multiplier * offsetx;
                double y = multiplier * offsety;
                double z = multiplier * offsetz;

                for (ParticleDisplay display : displays) display.getLocation().add(x, y, z);
                runnable.run();
                for (ParticleDisplay display : displays) display.getLocation().subtract(x, y, z);

                if (opposite) {
                    if (multiplier <= 0) opposite = false;
                } else {
                    if (multiplier >= endRate) opposite = true;
                }
            }
        };
        return SCore.schedulerHook.runAsyncRepeatingTask(runnable1, 0L, update);
    }

    /**
     * A simple test method to spawn a shape repeatedly for diagnosis.
     *
     * @param runnable the shape(s) to display.
     *
     * @return the timer task handling the displays.
     * @since 1.0.0
     */
    public static ScheduledTask testDisplay(Runnable runnable) {
        return SCore.schedulerHook.runAsyncRepeatingTask(runnable, 0L, 1L);
    }

    /**
     * This will rotate the shape around in an area randomly.
     * The position of the shape will be randomized positively and negatively by the offset parameters on each axis.
     *
     * @param update   the timer period in ticks.
     * @param rate     the distance between each location. Recommended value is 5.
     * @param runnable the particles to spawn.
     * @param displays the displays references used to spawn particles in the runnable.
     *
     * @return the async task handling the movement.
     * @since 1.0.0
     */
    public static ScheduledTask rotateAround(long update, double rate, double offsetx, double offsety, double offsetz,
                                          Runnable runnable, ParticleDisplay... displays) {
        Runnable runnable1 = new Runnable() {
            double rotation = 180;

            @Override
            public void run() {
                rotation += rate;
                double x = Math.toRadians((90 + rotation) * offsetx);
                double y = Math.toRadians((60 + rotation) * offsety);
                double z = Math.toRadians((30 + rotation) * offsetz);

                Vector vector = new Vector(x, y, z);
                for (ParticleDisplay display : displays) display.rotate(vector);
                runnable.run();
            }
        };
        return SCore.schedulerHook.runAsyncRepeatingTask(runnable1, 0L, update);
    }




    /**
     * Spawns animated spikes randomly spreading at the end location.
     *
     * @param amount    the amount of spikes to spawn.
     * @param rate      rate of spike line points.
     * @param start     start location of spikes.
     * @param originEnd end location of spikes.
     *
     * @since 1.0.0
     */
    public static ScheduledTask spread(int amount, int rate, Location start, Location originEnd,
                                    double offsetx, double offsety, double offsetz, ParticleDisplay display) {

        final AtomicReference<ScheduledTask> task = new AtomicReference<>();

        Runnable runnable = new Runnable() {
            int count = amount;

            @Override
            public void run() {
                int frame = rate;

                while (frame-- != 0) {
                    double x = random(-offsetx, offsetx);
                    double y = random(-offsety, offsety);
                    double z = random(-offsetz, offsetz);

                    Location end = originEnd.clone().add(x, y, z);
                    line(start, end, 0.1, display);
                }

                if (count-- == 0) task.get().cancel();
            }
        };
        task.set(SCore.schedulerHook.runAsyncRepeatingTask(runnable, 0L, 1L));

        return task.get();
    }



    /**
     * Spawns a broken line that creates more and extended branches
     * as it gets closer to the end length.
     * This method doesn't support rotations. Use the direction instead.
     *
     * @param start      the starting point of the new branch. For the first call it's the same location as the displays location.
     * @param direction  the direction of the lightning. A simple direction would be {@code entity.getLocation().getDirection().normalize()}
     *                   For a simple end point direction would be {@code endLocation.toVector().subtract(start.toVector()).normalize()}
     * @param entries    the number of entries for the main lightning branch. Recommended is 20
     * @param branches   the maximum number of branches each entry can have. Recommended is 200
     * @param radius     the radius of the lightning branches. Recommended is 0.5
     * @param offset     the offset of the lightning branches. Recommended is 2
     * @param offsetRate the offset change rate of the lightning branches. Recommended is 1
     * @param length     the length of the lightning branch. Recommended is 1.5
     * @param lengthRate the length change rate of the lightning branch. Recommended is 1
     * @param branch     the chance of creating a new branch. Recommended is 0.1
     * @param branchRate the chance change of creating a new branch. Recommended is 1
     *
     * @since 3.0.0
     */
    public static void lightning(Location start, Vector direction, int entries, int branches, double radius,
                                 double offset, double offsetRate,
                                 double length, double lengthRate,
                                 double branch, double branchRate, ParticleDisplay display) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (entries <= 0) return;
        boolean inRange = true;

        // Check if we can create new branches or the current branch
        // length is already in range.
        while (random.nextDouble() < branch || inRange) {
            // Break our straight line randomly.
            Vector randomizer = new Vector(
                    random.nextDouble(-radius, radius), random.nextDouble(-radius, radius), random.nextDouble(-radius, radius))
                    .normalize().multiply((random.nextDouble(-radius, radius)) * offset);
            Vector endVector = start.clone().toVector().add(direction.clone().multiply(length)).add(randomizer);
            Location end = endVector.toLocation(start.getWorld());

            // Check if the broken line length is in our max length range.
            if (end.distance(start) <= length) {
                inRange = true;
                continue;
            } else inRange = false;

            // Create particle points in our broken straight line.
            int rate = (int) (start.distance(end) / 0.1); // distance * (distance / 10)
            Vector rateDir = endVector.clone().subtract(start.toVector()).normalize().multiply(0.1);
            for (int i = 0; i < rate; i++) {
                Location loc = start.clone().add(rateDir.clone().multiply(i));
                display.spawn(loc);
            }

            // Create new entries if possible.
            lightning(end.clone(), direction, entries - 1, branches - 1, radius, offset * offsetRate, offsetRate,
                    length * lengthRate, lengthRate,
                    branch * branchRate, branchRate, display);
            // Check if the maximum number of branches has already been used for this entry.
            if (branches <= 0) break;
        }
    }

    /**
     * Draws a line from the player's looking direction.
     *
     * @param player the player to draw the line from.
     * @param length the length of the line.
     * @param rate   the rate of points of the line.
     *
     * @see #line(Location, Location, double, ParticleDisplay)
     * @since 1.0.0
     */
    public static void drawLine(Player player, double length, double rate, ParticleDisplay display) {
        Location eye = player.getEyeLocation();
        line(eye, eye.clone().add(eye.getDirection().multiply(length)), rate, display);
    }

    /**
     * A simple method to spawn animated clouds effect.
     *
     * @param plugin the timer handler.
     * @param cloud  recommended particle is {@link Particle#CLOUD} or {@link Particle #SMOKE_LARGE} and the offset xyz should be higher than 2
     * @param rain   recommended particle is {@link Particle #WATER_DROP} or {@link Particle#FALLING_LAVA} and the offset xyz should be the same as cloud.
     *
     * @return the timer task handling the animation.
     * @since 1.0.0
     */
    public static ScheduledTask cloud(Plugin plugin, ParticleDisplay cloud, ParticleDisplay rain) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                cloud.spawn();
                rain.spawn();
            }
        };
        return SCore.schedulerHook.runAsyncRepeatingTask(runnable, 0L, 1L);
    }

    /**
     * Spawns a line from a location to another.
     * Tutorial: https://www.spigotmc.org/threads/176695/
     * This method is a modified version to get the best performance.
     *
     * @param start the starting point of the line.
     * @param end   the ending point of the line.
     * @param rate  the rate of points of the line.
     *
     * @see #drawLine(Player, double, double, ParticleDisplay)
     * @since 1.0.0
     */
    public static void line(Location start, Location end, double rate, ParticleDisplay display) {
        rate = Math.abs(rate);
        double x = end.getX() - start.getX();
        double y = end.getY() - start.getY();
        double z = end.getZ() - start.getZ();
        double length = Math.sqrt(NumberConversions.square(x) + NumberConversions.square(y) + NumberConversions.square(z));

        x /= length;
        y /= length;
        z /= length;

        ParticleDisplay clone = display.clone();
        clone.withLocation(start);
        for (double i = 0; i < length; i += rate) {
            // Since the rate can be any number it's possible to get a higher number than
            // the length in the last loop.
            clone.spawn(x * i, y * i, z * i);
        }
    }




    /**
     * Animated 4D tesseract using matrix motion.
     * Since this is a 4D shape the usage should be highly limited.
     * A failed prototype: https://imgur.com/eziNk7x
     * Final Version: https://imgur.com/Vb2HDQN
     * <p>
     * https://en.wikipedia.org/wiki/Tesseract
     * https://en.wikipedia.org/wiki/Rotation_matrix
     *
     * @param size   the size of the tesseract. Recommended is 4
     * @param rate   the rate of the tesseract points. Recommended is 0.3
     * @param speed  the speed of the tesseract matrix motion. Recommended is 0.01
     * @param time  the amount of ticks to keep the animation.
     *
     * @since 4.0.0
     */
    public static ScheduledTask tesseract(double size, double rate, double speed, double time, ParticleDisplay display) {
        // We can multiply these later to change the size.
        // This array doesn't really need to be a constant as it's initialized once.
        double[][] positions = {
                {-1, -1, -1, 1}, {1, -1, -1, 1},
                {1, 1, -1, 1}, {-1, 1, -1, 1},
                {-1, -1, 1, 1}, {1, -1, 1, 1},
                {1, 1, 1, 1}, {-1, 1, 1, 1},

                {-1, -1, -1, -1}, {1, -1, -1, -1},
                {1, 1, -1, -1}, {-1, 1, -1, -1},
                {-1, -1, 1, -1}, {1, -1, 1, -1},
                {1, 1, 1, -1}, {-1, 1, 1, -1},
        };

//        BiFunction<Double, Double, Double> reverseMatrix = (a, b) -> {
//            if (a < 0) a -= b;
//            else a += b;
//            return -a;
//        };
//
//        List<double[]> original = new ArrayList<>(Arrays.asList(positions));
//        List<double[]> points = new ArrayList<>(original);
//        List<double[]> rev = new ArrayList<>(original);
        List<int[]> connections = new ArrayList<>();
//
//        double dist = 0;
//        Collections.reverse(rev);
//        List<double[]> reversed = new ArrayList<>();
//        for (int i = 0; i < 4; i += 2) {
//            reversed.add(rev.get(i + 1));
//            reversed.add(rev.get(i));
//        }
//        reversed.forEach(x -> points.add(new double[]{
//                reverseMatrix.apply(x[0], dist), reverseMatrix.apply(x[1], dist),
//                reverseMatrix.apply(x[2], dist), reverseMatrix.apply(x[3], dist)}));

        // Connect the generated 4D points together.
        // This can later be modified to support multi-dimension hypercubes.
        int level = 1;
        for (int h = 0; h <= level; h++) {
            int start = 8 * h;
            for (int i = start; i < start + 4; i++) {
                connections.add(new int[]{i, ((i + 1) % 4) + start});
                connections.add(new int[]{i + 4, (((i + 1) % 4) + 4) + start});
                connections.add(new int[]{i, i + 4});
            }
        }
        for (int i = 0; i < (level + 1) * 4; i++) connections.add(new int[]{i, i + 8});

        final AtomicReference<ScheduledTask> task = new AtomicReference<>();

        Runnable runnable = new Runnable() {
            double angle = 0;
            long repeat = 0;

            @Override
            public void run() {
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);

                // https://en.wikipedia.org/wiki/Rotation_matrix
                double[][] rotationXY = {
                        {cos, -sin, 0, 0},
                        {sin, cos, 0, 0},
                        {0, 0, 1, 0},
                        {0, 0, 0, 1}
                };

                // What does it mean to rotate a shape in the w (4th) axis?
                double[][] rotationZW = {
                        {1, 0, 0, 0},
                        {0, 1, 0, 0},
                        {0, 0, cos, -sin},
                        {0, 0, sin, cos}
                };

                double[][] projected3D = new double[positions.length][4];
                for (int i = 0; i < positions.length; i++) {
                    // To get the prototype version simply rotate the
                    // cube by using the display.rotate method in one of the axis.
                    double[] point = positions[i];
                    double[] rotated = matrix(rotationXY, point);
                    rotated = matrix(rotationZW, rotated);

                    int distance = 2;
                    double w = 1 / (distance - rotated[3]);
                    double[][] projection = {
                            {w, 0, 0, 0},
                            {0, w, 0, 0},
                            {0, 0, w, 0}
                    };

                    double[] projected = matrix(projection, rotated);
                    for (int proj = 0; proj < projected.length; proj++) projected[proj] *= size;
                    projected3D[i] = projected;

                    display.spawn(projected[0], projected[1], projected[2]);
                }

                for (int[] connection : connections) {
                    // Get the points of our tesseract and connect the two points using our line method.
                    double[] pointA = projected3D[connection[0]];
                    double[] pointB = projected3D[connection[1]];
                    Location start = display.cloneLocation(pointA[0], pointA[1], pointA[2]);
                    Location end = display.cloneLocation(pointB[0], pointB[1], pointB[2]);
                    line(start, end, rate, display);
                }

                if (++repeat > time) {
                    task.get().cancel();
                    return;
                }
                else angle += speed;
            }
        };
        task.set(SCore.schedulerHook.runAsyncRepeatingTask(runnable, 0L, 1L));
        return task.get();
    }

    /**
     * A method to translate matrix motion for 4D.
     * https://en.wikipedia.org/wiki/Rotation_matrix
     *
     * @since 4.0.0
     */
    private static double[] matrix(double[][] a, double[] m) {
        double[][] b = new double[4][1];
        b[0][0] = m[0];
        b[1][0] = m[1];
        b[2][0] = m[2];
        b[3][0] = m[3];

        int colsA = a[0].length;
        int rowsA = a.length;
        int colsB = b[0].length;
        int rowsB = b.length;

        double[][] result = new double[rowsA][rowsB];
        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                float sum = 0;
                for (int k = 0; k < colsA; k++) {
                    sum += a[i][k] * b[k][j];
                }
                result[i][j] = sum;
            }
        }

        double[] v = new double[4];
        v[0] = result[0][0];
        v[1] = result[1][0];
        v[2] = result[2][0];
        if (result.length > 3) v[3] = result[3][0];
        return v;
    }


    /**
     * Spawn 3D spiked circles.
     * Note that the animation is intended to be used with prototype mode enabled.
     * Animations without prototype doesn't really look good. You might want to increase the speed.
     *
     * @param plugin      the timer handler.
     * @param points      the number of circle sides with spikes.
     * @param spikes      the number of spikes on each side.
     * @param rate        the rate of star points.
     * @param spikeLength the length of each spike.
     * @param coreRadius  the radius of the center circle.
     * @param neuron      the neuron level. Neuron level is affected by the prototype mode.
     *                    Normal value is 1 if prototype mode is disabled. If the value goes higher than 1 it'll form a neuron-like body cell shape.
     *                    The value is used in small ranges for when prototype mode is enabled. Usually between 0.01 and 0.1
     * @param prototype   if the spikes of the star should use helix instead of a random generator.
     * @param speed       the speed of animation. Smoothest/slowest is 1
     *
     * @see #spikeSphere(double, double, int, double, double, ParticleDisplay)
     * @since 3.0.0
     */
    public static void star(int points, int spikes, double rate, double spikeLength, double coreRadius,
                            double neuron, boolean prototype, int speed, ParticleDisplay display) {
        double pointsRate = PII / points;
        double rateDiv = Math.PI / rate;
        ThreadLocalRandom random = prototype ? null : ThreadLocalRandom.current();

        for (int i = 0; i < spikes * 2; i++) {
            double spikeAngle = i * Math.PI / spikes;

            final AtomicReference<ScheduledTask> task = new AtomicReference<>();

            Runnable runnable = new Runnable() {
                double vein = 0;
                double theta = 0;

                @Override
                public void run() {
                    int repeat = speed;
                    while (repeat-- != 0) {
                        theta += rateDiv;

                        // We're going to spawn little circles to create our spikes.
                        // Spawning them with a random radius.
                        double height = (prototype ? vein : random.nextDouble(0, Math.min(0.1,neuron))) * spikeLength;
                        if (prototype) vein += neuron;
                        Vector vector = new Vector(Math.cos(theta), 0, Math.sin(theta));

                        // We don't want to fill the inside circle.
                        vector.multiply((spikeLength - height) * coreRadius / spikeLength);
                        vector.setY(coreRadius + height);

                        // Rotate the vector for the next spike.
                        ParticleDisplay.rotateAround(vector, ParticleDisplay.Axis.X, spikeAngle);
                        for (int j = 0; j < points; j++) {
                            // Rotate the spikes to copy them with equal angles.
                            ParticleDisplay.rotateAround(vector, ParticleDisplay.Axis.Y, pointsRate);
                            display.spawn(vector);
                        }

                        if (theta >= PII) {
                            task.get().cancel();
                            return;
                        }
                    }
                }
            };
            task.set(SCore.schedulerHook.runAsyncRepeatingTask(runnable, 0L, 1L));
        }
    }


    /**
     * https://upload.wikimedia.org/wikipedia/commons/thumb/9/9a/Pentagram_within_circle.svg/800px-Pentagram_within_circle.svg.png
     *
     * @see #polygon(int, int, double, double, double, ParticleDisplay)
     * @since 1.0.0
     */
    public static void neopaganPentagram(double size, double rate, double extend, ParticleDisplay star, ParticleDisplay circle) {
        polygon(5, 2, size, rate, extend, star);
        circle(size + 0.5, rate * 1000, 0,"","",circle);
    }






    /**
     * Converts yaw and pitch into a direction vector.
     *
     * @param yaw   Yaw in degrees (0 = South, 90 = West, 180 = North, -90 = East)
     * @param pitch Pitch in degrees (positive looks downward)
     * @return Normalized direction vector
     */
    public static Vector calculDirection(double yaw, double pitch) {
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);

        double x = -Math.cos(pitchRad) * Math.sin(yawRad);
        double y = -Math.sin(pitchRad);
        double z = Math.cos(pitchRad) * Math.cos(yawRad);

        return new Vector(x, y, z).normalize();
    }

    /**
     * Reads an Image from the given path.
     *
     * @param path the path of the image.
     *
     * @return a buffered image.
     * @since 1.0.0
     */
    private static BufferedImage getImage(Path path) {
        if (!Files.exists(path)) return null;
        try {
            return ImageIO.read(Files.newInputStream(path, StandardOpenOption.READ));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Resizes an image maintaining aspect ratio (kinda).
     *
     * @param path   the path of the image.
     * @param width  the new width.
     * @param height the new height.
     *
     * @return the resized image.
     * @since 1.0.0
     */
    private static CompletableFuture<BufferedImage> getScaledImage(Path path, int width, int height) {
        return CompletableFuture.supplyAsync(() -> {
            BufferedImage image = getImage(path);
            if (image == null) return null;
            int finalHeight = height;
            int finalWidth = width;

            if (image.getWidth() > image.getHeight()) {
                finalHeight = width * image.getHeight() / image.getWidth();
            } else {
                finalWidth = height * image.getWidth() / image.getHeight();
            }

            BufferedImage resizedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = resizedImg.createGraphics();

            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.drawImage(image, 0, 0, finalWidth, finalHeight, null);
            graphics.dispose();
            return resizedImg;
        });
    }

    /**
     * Renders a resized image.
     *
     * @param path          the path of the image.
     * @param resizedWidth  the resizing width.
     * @param resizedHeight the resizing height.
     * @param compact       the pixel compact of the image.
     *
     * @return the rendered particle locations.
     * @since 1.0.0
     */
    public static CompletableFuture<Map<double[], Color>> renderImage(Path path, int resizedWidth, int resizedHeight, double compact) {
        return getScaledImage(path, resizedWidth, resizedHeight).thenCompose((image) -> renderImage(image, resizedWidth, resizedHeight, compact));
    }

    /**
     * Renders every pixel of the image and saves the location and
     * the particle colors to a map.
     *
     * @param image         the image to render.
     * @param resizedWidth  the new image width.
     * @param resizedHeight the new image height.
     * @param compact       particles compact value. Should be lower than 0.5 and higher than 0.1 The recommended value is 0.2
     *
     * @return a rendered map of an image.
     * @since 1.0.0
     */
    @SuppressWarnings("unused")
    public static CompletableFuture<Map<double[], Color>> renderImage(BufferedImage image, int resizedWidth, int resizedHeight, double compact) {
        return CompletableFuture.supplyAsync(() -> {
            if (image == null) return null;

            int width = image.getWidth();
            int height = image.getHeight();
            double centerX = width / 2D;
            double centerY = height / 2D;

            Map<double[], Color> rendered = new HashMap<>();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = image.getRGB(x, y);

                    // Transparency
                    if ((pixel >> 24) == 0x0) continue;
                    // 0 - 255
                    //if ((pixel & 0xff000000) >>> 24 == 0) continue;
                    // 0.0 - 1.0
                    //if (pixel == java.awt.Color.TRANSLUCENT) continue;

                    java.awt.Color color = new java.awt.Color(pixel);
                    int r = color.getRed();
                    int g = color.getGreen();
                    int b = color.getBlue();

                    double[] coords = {(x - centerX) * compact, (y - centerY) * compact};
                    Color bukkitColor = Color.fromRGB(r, g, b);
                    rendered.put(coords, bukkitColor);
                }
            }
            return rendered;
        });
    }

    /**
     * Display a rendered image repeatedly.
     *
     * @param plugin   the scheduler handler.
     * @param render   the rendered image map.
     * @param location the dynamic location to display the image at.
     * @param repeat   amount of times to repeat displaying the image.
     * @param period   the perioud between each repeats.
     * @param quality  the quality of the image is exactly the number of particles display for each pixel. Recommended value is 1
     * @param speed    the speed is exactly the same value as the speed of particles. Recommended amount is 0
     * @param size     the size of the particle. Recommended amount is 0.8
     *
     * @return the async bukkit task displaying the image.
     * @since 1.0.0
     */
    public static ScheduledTask displayRenderedImage(Plugin plugin, Map<double[], Color> render, Callable<Location> location,
                                                  int repeat, long period, int quality, int speed, float size) {

        final AtomicReference<ScheduledTask> task = new AtomicReference<>();

        Runnable runnable = new Runnable() {
            int times = repeat;

            @Override
            public void run() {
                try {
                    displayRenderedImage(render, location.call(), quality, speed, size);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (times-- < 1) {
                    task.get().cancel();
                }
            }
        };
        task.set(SCore.schedulerHook.runAsyncRepeatingTask(runnable, 0L, period));
        return task.get();
    }

    /**
     * Display a rendered image repeatedly.
     *
     * @param render   the rendered image map.
     * @param location the dynamic location to display the image at.
     * @param quality  the quality of the image is exactly the number of particles display for each pixel. Recommended value is 1
     * @param speed    the speed is exactly the same value as the speed of particles. Recommended amount is 0
     * @param size     the size of the particle. Recommended amount is 0.8
     *
     * @since 1.0.0
     */
    public static void displayRenderedImage(Map<double[], Color> render, Location location, int quality, int speed, float size) {
        World world = location.getWorld();
        for (Map.Entry<double[], Color> pixel : render.entrySet()) {
            Particle.DustOptions data = new Particle.DustOptions(pixel.getValue(), size);
            double[] pixelLoc = pixel.getKey();

            Location loc = new Location(world, location.getX() - pixelLoc[0], location.getY() - pixelLoc[1], location.getZ());
            Particle particle = SCore.is1v20v5Plus() ? Particle.DUST_COLOR_TRANSITION : Particle.valueOf("REDSTONE");
            world.spawnParticle(particle, loc, quality, 0, 0, 0, speed, data);
        }
    }

    /**
     * A simple method used to save images. Useful to cache text generated images.
     *
     * @param image the buffered image to save.
     * @param path  the path to save the image to.
     *
     * @see #stringToImage(Font, java.awt.Color, String)
     * @since 1.0.0
     */
    public static void saveImage(BufferedImage image, Path path) {
        try {
            ImageIO.write(image, "png", Files.newOutputStream(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts a string to an image which can be used to display as a particle.
     *
     * @param font  the font to generate the text with.
     * @param color the color of text.
     * @param str   the string to generate the image.
     *
     * @return the buffered image.
     * @see #saveImage(BufferedImage, Path)
     * @since 1.0.0
     */
    public static CompletableFuture<BufferedImage> stringToImage(Font font, java.awt.Color color, String str) {
        return CompletableFuture.supplyAsync(() -> {
            BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setFont(font);

            FontRenderContext context = graphics.getFontMetrics().getFontRenderContext();
            Rectangle2D frame = font.getStringBounds(str, context);
            graphics.dispose();

            image = new BufferedImage((int) Math.ceil(frame.getWidth()), (int) Math.ceil(frame.getHeight()), BufferedImage.TYPE_INT_ARGB);
            graphics = image.createGraphics();
            graphics.setColor(color);
            graphics.setFont(font);

            graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            FontMetrics metrics = graphics.getFontMetrics();
            graphics.drawString(str, 0, metrics.getAscent());
            graphics.dispose();

            return image;
        });
    }
}

