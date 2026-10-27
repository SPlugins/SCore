package com.ssomar.score.features.custom.particles.group;

import com.ssomar.score.SCore;
import com.ssomar.score.features.custom.particles.particle.ParticleFeature;
import com.ssomar.score.utils.scheduler.ScheduledTask;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.data.color.RegularColor;
import xyz.xenondevs.particle.data.texture.BlockTexture;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParticlesXenonDev112_1193 {

    public static void transformTheProjectile(Map<String, ParticleFeature> particles, Entity e, Player launcher, Material materialLaunched) {
        if (particles != null) {
            for (ParticleFeature particle : particles.values()) {

                Projectile projectile;
                if (e instanceof Projectile) projectile = (Projectile) e;
                else return;


                ParticleEffect particleEffect = ParticleEffect.valueOf(particle.getParticlesType().getValue().get().name());
                float speed = particle.getParticlesSpeed().getValue().get().floatValue();
                float offset =  particle.getParticlesOffSet().getValue().get().floatValue();

                ParticleBuilder builder = new ParticleBuilder(particleEffect).setOffset(offset, offset, offset).setSpeed(speed).setAmount(particle.getParticlesAmount().getValue().get().intValue());
                if (particle.canHaveRedstoneColor()) {
                    builder.setParticleData(new RegularColor(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue()));
                    if (particle.getRedstoneColor().getValue().isPresent()) {
                        Color color = particle.getRedstoneColor().getValue().get();
                        builder.setParticleData(new RegularColor(color.getRed(), color.getGreen(), color.getBlue()));
                    }
                } else if (particle.canHaveBlocktype()) {
                    builder.setParticleData(new BlockTexture(Material.STONE));
                    if (particle.getBlockType() != null)
                        builder.setParticleData(new BlockTexture(particle.getBlockType().getValue().get()));
                }

                AtomicReference<ScheduledTask> task = new AtomicReference<>();

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
                            builder.setLocation(projectile.getLocation().add(newVector)).display();
                        }
                    }
                };
                task.set(SCore.schedulerHook.runAsyncRepeatingTask(runnable, 0, delay));
            }
        }
    }
}
