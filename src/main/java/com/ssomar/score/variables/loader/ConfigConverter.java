package com.ssomar.score.variables.loader;

import java.nio.charset.StandardCharsets;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

public class ConfigConverter {


    public static void update(File file) {

        FileConfiguration config;
        try {
            config = (FileConfiguration) YamlConfiguration.loadConfiguration(file);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }


        if(config.contains("visualItem") && !config.isConfigurationSection("visualItem")) {
            config.set("visualItem.material", config.getString("visualItem"));
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
