package com.ssomar.score.config;

import com.ssomar.score.SCore;
import com.ssomar.score.configs.messages.MessageMain;
import com.ssomar.score.features.FeatureSettingsSCore;
import com.ssomar.score.languages.messages.TM;
import com.ssomar.score.utils.logging.Utils;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class GeneralConfig extends Config {

    private static GeneralConfig instance;

    private Locale locale;

    private boolean useMySQL;

    private String dbIP;

    private int dbPort;

    private String dbName;

    private String dbUser;

    private String dbPassword;

    private String configVerbosity;

    private List<String> silenceOutputs;

    private List<String> globalSilenceOutputs;

    private boolean reduceDamageIndicatorWithProtolcolLib;

    private boolean debugCheckDamages;

    private boolean loopKillMode;

    private boolean enableDetectionEntitiesFromSpawner;

    private boolean disableCustomMetadataOnEntities;

    private boolean enableCommentsInConfig;

    private boolean editorIconPreview;

    private boolean selfHostPackDebug;

    private String selfHostPackIp;

    private String hologramsPlugin = "NONE"; // NONE, CMI, HOLOGRAPHIC_DISPLAYS, DECENT_HOLOGRAMS

    public GeneralConfig() {
        super("config.yml");
        super.setup(SCore.dataFolder, SCore.classLoader, null);
    }

    public static GeneralConfig getInstance() {
        if (instance == null) instance = new GeneralConfig();
        return instance;
    }

    public void reload() {
        super.setup(SCore.plugin);
    }

    @Override
    public boolean converter(FileConfiguration config) {
        return false;
    }

    @Override
    public void load() {
        /* Locale config (language) */
        try {
            locale = Locale.valueOf(config.getString("locale", "EN").toUpperCase());
        } catch (IllegalArgumentException e) {
            SCore.plugin.getServer().getLogger().severe("[SCore] Invalid locale name: " + config.getString("locale") + ", using default locale: EN");
            locale = Locale.EN;
        }
        Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Locale setup: &6" + locale);

        useMySQL = config.getBoolean("useMySQL", false);
        dbIP = config.getString("dbIP", "");
        dbPort = config.getInt("dbPort", 3306);
        dbName = config.getString("dbName", "");
        dbUser = config.getString("dbUser", "");
        dbPassword = config.getString("dbPassword", "");
        configVerbosity = config.getString("configVerbosity", "NORMAL");
        //SsomarDev.testMsg("configVerbosity: " + configVerbosity, true);
        reduceDamageIndicatorWithProtolcolLib = config.getBoolean("reduceDamageIndicatorWithProtolcolLib", false);
       // jetMinionsGenerateBreakActivator = config.getBoolean("jetMinionsGenerateBreakActivator", false);
        silenceOutputs = loadSilenceOutputs(config, "silenceOutputs");
        globalSilenceOutputs = loadSilenceOutputs(config, "globalSilenceOutputs");
        debugCheckDamages = config.getBoolean("debugCheckDamages", false);
        enableDetectionEntitiesFromSpawner = config.getBoolean("enableDetectionEntitiesFromSpawner", true);
        loopKillMode = config.getBoolean("loopKillMode", false);
        disableCustomMetadataOnEntities = config.getBoolean("disableCustomMetadataOnEntities", false);
        enableCommentsInConfig = config.getBoolean("enableCommentsInConfig", true) && SCore.is1v19Plus();
        editorIconPreview = config.getBoolean("editorIconPreview", true);
        selfHostPackDebug = config.getBoolean("selfHostPackDebug", false);
        selfHostPackIp = config.getString("selfHostPackIp", "");
        hologramsPlugin = config.getString("hologramsPlugin", "NONE").toUpperCase();
    }

    public boolean isVerbosityNormal() {
        return configVerbosity.equalsIgnoreCase("NORMAL");
    }

    public boolean isVerbosityMinimal() {
        return configVerbosity.toUpperCase().startsWith("MIN");
    }

    public boolean isVerbosityMaximal() {
        return configVerbosity.toUpperCase().startsWith("MAX");
    }

    public void nextLocale() {
        int next = (locale.ordinal() + 1) % Locale.values().length;
        locale = Locale.values()[next];
        config.set("locale", locale.name());
        save();
        FeatureSettingsSCore.reload();
        MessageMain.getInstance().reload();
        TM.getInstance().reload();
    }

    public void previousLocale() {
        int previous = (locale.ordinal() - 1 + Locale.values().length) % Locale.values().length;
        locale = Locale.values()[previous];
        config.set("locale", locale.name());
        save();
        FeatureSettingsSCore.reload();
        MessageMain.getInstance().reload();
        TM.getInstance().reload();
    }

    public String[] getAvailableLocales(String... strings) {
        ArrayList<String> locales = new ArrayList<>(Arrays.asList(strings));
        for (Locale locale : Locale.values()) {
            if(this.locale != locale) locales.add("&6➤ &7"+locale.name()+ " &e- &7"+locale.getName());
            else locales.add("&2➤ &7&o"+locale.name()+ " &a- &7&o"+locale.getName());
        }
        return locales.toArray(new String[0]);
    }

    /* An empty entry matches every console line: the whole console would be silenced */
    private static List<String> loadSilenceOutputs(FileConfiguration config, String key) {
        List<String> result = new ArrayList<>();
        for (String s : config.getStringList(key)) {
            if (s == null || s.trim().isEmpty()) {
                Utils.sendConsoleMsg(SCore.NAME_COLOR + " &cAn empty entry of &6" + key + " &cin config.yml is ignored (it would hide every console line).");
                continue;
            }
            result.add(s);
        }
        return result;
    }
}
