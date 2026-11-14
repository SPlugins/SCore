package com.ssomar.score.config;

import java.nio.charset.StandardCharsets;
import com.ssomar.score.SCore;
import com.ssomar.score.utils.logging.Utils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public abstract class Config {
    @Getter
    protected final String fileName;
    protected File pdfile;
    @Getter
    protected FileConfiguration config;

    @Getter
    private Map<String, Object> loadedSettings;

    protected Config(String fileName) {
        this.fileName = fileName;
        this.loadedSettings = new HashMap<>();
    }

    public void setup(Plugin plugin) {
        setup(plugin.getDataFolder(), plugin.getClass().getClassLoader(), plugin);
    }

    public void setup(File dataFolder, ClassLoader classLoader, @Nullable Plugin plugin) {
        if (!dataFolder.exists()) dataFolder.mkdir();
        this.pdfile = new File(dataFolder, this.fileName);
        //System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> File: " + this.pdfile.getAbsolutePath());
        if (!this.pdfile.exists()) {
            try {
                this.pdfile.getParentFile().mkdir();
                this.pdfile.createNewFile();
                //System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> CREATE File: " + this.pdfile.getAbsolutePath());

                BufferedReader br = new BufferedReader(new InputStreamReader(getResource(classLoader, this.fileName)));
                String line;

                Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.pdfile), StandardCharsets.UTF_8));

                try {
                    while ((line = br.readLine()) != null) {
                        //System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> WRITE File: " +line);
                        out.write(line);
                        out.write("\n");
                    }
                } finally {
                    br.close();
                    out.close();
                }

            } catch (IOException e) {
                throw new RuntimeException("Unable to create the file: " + this.fileName, e);
            } catch (NullPointerException e) {/* locale */}
        }
        this.config = YamlConfiguration.loadConfiguration(this.pdfile);
        if(converter(this.config)){
            try {
                Writer writer = new OutputStreamWriter(new FileOutputStream(pdfile), StandardCharsets.UTF_8);

                try {
                    writer.write(config.saveToString());
                } finally {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(plugin != null) plugin.reloadConfig();
        load();
    }

    /**
     * Gets an embedded resource in this plugin
     *
     * @param filename Filename of the resource
     * @return File if found, otherwise null
     */
    @Nullable
    public static InputStream getResource(ClassLoader classLoader, @NotNull String filename) {
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        try {
            URL url = classLoader.getResource(filename);
            if (url == null) {
                Utils.sendConsoleMsg(SCore.NAME_COLOR+" &7Error while creating the file: &e" + filename+" &7from the plugin jar");
                return null;
            }
            Utils.sendConsoleMsg(SCore.NAME_COLOR+" &7Creating the file: &e" + filename+" &7from the plugin jar (url: &e"+url+"&7)");

            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException ex) {
            Utils.sendConsoleMsg(SCore.NAME_COLOR+" &7Error while creating the file: &e" + filename+" &7from the plugin jar");
            ex.printStackTrace();
            return null;
        }
    }

    public abstract boolean converter(FileConfiguration config);

    public abstract void load();

    public void save() {
        try {
            this.config.save(this.pdfile);
        } catch (IOException e) {
            Bukkit.getServer().getLogger().severe("Could not save " + this.fileName + "!");
        }
    }


    public boolean loadBooleanSetting(String setting, boolean defaultValue) {
        if(!config.isBoolean(setting)) {
            setBooleanSetting(setting, defaultValue);
        }
        boolean value = config.getBoolean(setting, defaultValue);
        loadedSettings.put(setting, value);
        //System.out.println("Setting: " + setting + " Value: " + value);
        return value;
    }

    public void setBooleanSetting(String setting, boolean value) {
        loadedSettings.put(setting, value);
        config.set(setting, value);
        save();
    }

    public boolean getBooleanSetting(String setting) {
        return (boolean) loadedSettings.get(setting);
    }

    public int loadIntSetting(String setting, int defaultValue) {
        if(!config.isInt(setting)) {
            setIntSetting(setting, defaultValue);
        }
        int value = config.getInt(setting, defaultValue);
        loadedSettings.put(setting, value);
        //System.out.println("Setting: " + setting + " Value: " + value);
        return value;
    }

    public void setIntSetting(String setting, int value) {
        loadedSettings.put(setting, value);
        config.set(setting, value);
        save();
    }

    public int getIntSetting(String setting, int defaultValue) {
        if (!loadedSettings.containsKey(setting)) return defaultValue;
        return (int) loadedSettings.get(setting);
    }


    public String loadStringSetting(String setting, String defaultValue) {
        if (!config.isString(setting)) {
            setStringSetting(setting, defaultValue);
        }
        String value = config.getString(setting, defaultValue);
        loadedSettings.put(setting, value);
        //System.out.println("Setting: " + setting + " Value: " + value);
        return value;
    }

    public void setStringSetting(String setting, String value) {
        loadedSettings.put(setting, value);
        config.set(setting, value);
        save();
    }

    public String getStringSetting(String setting, String defaultValue) {
        if (!loadedSettings.containsKey(setting)) return defaultValue;
        return (String) loadedSettings.get(setting);
    }

    public double loadDoubleSetting(String setting, double defaultValue) {
        if (!config.isDouble(setting)) {
            setDoubleSetting(setting, defaultValue);
        }
        double value = config.getDouble(setting, defaultValue);
        loadedSettings.put(setting, value);
        //System.out.println("Setting: " + setting + " Value: " + value);
        return value;
    }

    public void setDoubleSetting(String setting, double value) {
        loadedSettings.put(setting, value);
        config.set(setting, value);
        save();
    }

    public double getDoubleSetting(String setting, double defaultValue) {
        if (!loadedSettings.containsKey(setting)) return defaultValue;
        return (double) loadedSettings.get(setting);
    }
}

