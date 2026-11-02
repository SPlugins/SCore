package com.ssomar.score.hardness.hardness.loader;

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
