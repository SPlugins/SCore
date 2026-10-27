package com.ssomar.score.utils.particles;

import com.ssomar.score.features.custom.particles.particle.ParticleFeature;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Color;
import org.bukkit.Material;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.data.color.RegularColor;
import xyz.xenondevs.particle.data.texture.BlockTexture;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParticleBuilderXenonDev {


    public static void spawnParticle(ParticleFeature particle, float speed, float offset) {

        ParticleEffect particleEffect = ParticleEffect.valueOf(particle.getParticlesType().getValue().get().name());

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
    }
}
