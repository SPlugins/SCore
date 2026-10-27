package com.ssomar.score.features.custom.particles.group;

import com.destroystokyo.paper.ParticleBuilder;
import com.ssomar.score.SCore;
import com.ssomar.score.features.custom.particles.particle.ParticleFeature;
import com.ssomar.score.utils.scheduler.ScheduledTask;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParticlesPaper1194_120 {

    public static void transformTheProjectile(Map<String, ParticleFeature> particles, Entity e, Player launcher, Material materialLaunched) {
        if (particles != null) {
            for (ParticleFeature particle : particles.values()) {

                Projectile projectile;
                if (e instanceof Projectile) projectile = (Projectile) e;
                else return;


                float speed = particle.getParticlesSpeed().getValue().get().floatValue();
                float offset =  particle.getParticlesOffSet().getValue().get().floatValue();

                final ParticleBuilder[] builder = {new ParticleBuilder(particle.getParticlesType().getValue().get()).offset(offset, offset, offset).extra(speed).count(particle.getParticlesAmount().getValue().get().intValue())};
                if (particle.canHaveRedstoneColor()) {
                    Particle.DustOptions dO = new Particle.DustOptions(Color.RED, 1);
                    if (particle.getRedstoneColor().getValue().isPresent())
                        dO = new Particle.DustOptions(particle.getRedstoneColor().getValue().get(), 1);
                    builder[0].data(dO);
                } else if (particle.canHaveBlocktype()) {
                    BlockData typeData = Material.STONE.createBlockData();
                    if (particle.getBlockType() != null)
                        typeData = particle.getBlockType().getValue().get().createBlockData();
                    if (particle.getBlockType() != null)
                        builder[0].data(typeData);
                }

                final AtomicReference<ScheduledTask> task = new AtomicReference<>();

                int delay = particle.getParticlesDelay().getValue().get();
                if(delay <= 0 ) delay = 1;

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (e.isDead() || projectile.isOnGround()) {
                            task.get().cancel();
                            return;
                        }

                        float particlesAmountForVector = 200;
                        float divide = 100;
                        /* between 1 and 200 */
                        float canlculDensity = 201 - particle.getParticlesDensity().getValue().get();

                        particlesAmountForVector = particlesAmountForVector / canlculDensity;
                        divide = divide / canlculDensity;

                        Vector vector = projectile.getVelocity();
                        for (float i = 1; i <= particlesAmountForVector; i++) {
                            float x = (float) i/divide;
                            Vector newVector = vector.clone().multiply(x);
                            try {
                                builder[0].location(projectile.getLocation().add(newVector)).spawn();
                            }
                            catch (IllegalArgumentException ex) {
                                // If the particle can't be spawned, we just ignore it
                                // This can happen if the particle is not supported by the server version
                                // we just spawn flame particles instead
                                builder[0] = new ParticleBuilder(Particle.FLAME)
                                        .offset(offset, offset, offset)
                                        .extra(speed)
                                        .count(particle.getParticlesAmount().getValue().get().intValue());
                                builder[0].location(projectile.getLocation().add(newVector)).spawn();
                            }
                        }
                    }
                };
                task.set(SCore.schedulerHook.runAsyncRepeatingTask(runnable, 0L, delay));
            }
        }
    }
}
