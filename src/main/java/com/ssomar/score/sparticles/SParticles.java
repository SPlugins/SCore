package com.ssomar.score.sparticles;

import java.nio.charset.StandardCharsets;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.emums.CustomColor;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public class SParticles {

    private final SPlugin sPlugin;
    private final String filePath;
    private final String configurationSectionPath;
    private final List<SParticle> particles;

    public SParticles(SPlugin sPlugin, String filePath, String configurationSectionPath, ConfigurationSection section, boolean showError) {
        this.sPlugin = sPlugin;
        this.filePath = filePath;
        this.configurationSectionPath = configurationSectionPath;
        this.particles = new ArrayList<>();

        ConfigurationSection mainSection = section;

        if (!configurationSectionPath.isEmpty() && section.isConfigurationSection(configurationSectionPath))
            mainSection = section.getConfigurationSection(configurationSectionPath);

        if (mainSection.isConfigurationSection("particles")) {
            ConfigurationSection particlesSection = mainSection.getConfigurationSection("particles");
            for (String id : particlesSection.getKeys(false)) {
                Optional<SParticle> sParticleOpt = SParticle.loadSParticle(sPlugin, filePath + configurationSectionPath, particlesSection.getConfigurationSection(id), id, showError);
                sParticleOpt.ifPresent(sParticle -> particles.add(sParticle));
            }
        }
    }

    public void addParticle(SParticle sParticle) {
        particles.add(sParticle);
    }

    public void removeParticle(String id) {
        SParticle toRemove = null;
        for (SParticle sParticle : particles) {
            if (sParticle.getId().equals(id)) {
                toRemove = sParticle;
                break;
            }
        }
        if (toRemove != null) particles.remove(toRemove);
    }

    public Optional<SParticle> getParticle(String id) {
        SParticle toRemove = null;
        for (SParticle sParticle : particles) {
            if (sParticle.getId().equals(id)) {
                return Optional.ofNullable(sParticle);
            }
        }
        return Optional.ofNullable(null);
    }

    public void updateParticle(SParticle sParticle) {
        removeParticle(sParticle.getId());
        addParticle(sParticle);
    }

    public void save() {
        if (!new File(filePath).exists()) {
            sPlugin.getPlugin().getLogger().severe(sPlugin.getNameDesign() + " Error can't find the file  (" + filePath + ")");
            return;
        }
        File file = new File(filePath);
        FileConfiguration config = (FileConfiguration) YamlConfiguration.loadConfiguration(file);

        ConfigurationSection mainSection = config;

        if (!configurationSectionPath.isEmpty()) {
            if (!config.isConfigurationSection(configurationSectionPath))
                config.createSection(configurationSectionPath);
            mainSection = config.getConfigurationSection(configurationSectionPath);
        }

        mainSection.set("particles", null);

        for (SParticle sParticle : particles) {
            mainSection.set("particles." + sParticle.getId() + ".particlesType", sParticle.getParticlesType().name());
            mainSection.set("particles." + sParticle.getId() + ".particlesAmount", sParticle.getParticlesAmount());
            mainSection.set("particles." + sParticle.getId() + ".particlesOffSet", sParticle.getParticlesOffSet());
            mainSection.set("particles." + sParticle.getId() + ".particlesSpeed", sParticle.getParticlesSpeed());
            mainSection.set("particles." + sParticle.getId() + ".particlesDelay", sParticle.getParticlesDelay());
            if (sParticle.canHaveRedstoneColor())
                mainSection.set("particles." + sParticle.getId() + ".redstoneColor", CustomColor.getName(sParticle.getRedstoneColor()));
            if (sParticle.canHaveBlocktype())
                mainSection.set("particles." + sParticle.getId() + ".blockType", sParticle.getBlockType().toString());
        }


        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);

            try {
                writer.write(config.saveToString());
            } finally {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
