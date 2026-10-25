package com.ssomar.score.configs.messages;


import com.ssomar.score.SCore;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.utils.logging.Utils;
import com.ssomar.score.utils.strings.StringConverter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageMain {

    private static MessageMain instance;

    private String fileName = "";

    private Map<Plugin, Map<MessageInterface, String>> messages = new HashMap<>();

    public static MessageMain getInstance() {
        if (instance == null) instance = new MessageMain();
        return instance;
    }

    public void load() {
        messages = new HashMap<>();
        Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Language for in-game messages setup on &6"+ GeneralConfig.getInstance().getLocale());
        fileName = "/locale/Locale_" + GeneralConfig.getInstance().getLocale() + ".yml";
    }

    public void reload() {
        Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Language for in-game messages setup on &6"+ GeneralConfig.getInstance().getLocale());
        fileName = "/locale/Locale_" + GeneralConfig.getInstance().getLocale() + ".yml";
        for(Plugin plugin : messages.keySet()) {
            Map<MessageInterface, String> formMessages = messages.get(plugin);
            loadMessagesOf(plugin, Arrays.asList(formMessages.keySet().toArray(new MessageInterface[0])));
        }
    }

    public void loadMessagesOf(Plugin plugin, List<MessageInterface> messagesEnum) {

        Map<MessageInterface, String> formMessages = new HashMap<>();

        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdir();
        File pdfile = new File(plugin.getDataFolder(), fileName);
        if (!pdfile.exists()) {
            try {
                pdfile.getParentFile().mkdir();
                pdfile.createNewFile();

                BufferedReader br = new BufferedReader(new InputStreamReader(plugin.getResource(fileName)));
                String line;

                Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pdfile), StandardCharsets.UTF_8));

                try {
                    while ((line = br.readLine()) != null) {
                        out.write(line);
                        out.write("\n");
                    }
                } finally {
                    br.close();
                    out.close();
                }

            } catch (IOException e) {
                throw new RuntimeException("Unable to create the file: " + this.fileName + " for the plugin: " + plugin.getName(), e);
            } catch (NullPointerException e) {/* locale */}
        }
        repairDoubledQuotes(pdfile);
        FileConfiguration config = YamlConfiguration.loadConfiguration(pdfile);
        /* A file that was unreadable got rewritten message by message, cut at the doubled quote ("... You"): reload them from the jar */
        for (String key : config.getKeys(false)) {
            String value = config.getString(key);
            if (value != null && (key.equals("errorUPDItem") || key.equals("errorUPDActivator")) && value.trim().endsWith("You")) config.set(key, null);
        }

        for (MessageInterface msgI : messagesEnum) {
            formMessages.put(msgI, this.loadMessage(plugin, pdfile, config, msgI.getName()));
        }
        messages.put(plugin, formMessages);
    }

    /**
     * ExecutableItems shipped Locale_EN.yml with You""ve inside a double quoted string from 2022 to 26.9.17: the whole
     * file fails to parse. Only that sequence is rewritten, the rest of the file is left as the admin wrote it.
     */
    static void repairDoubledQuotes(File pdfile) {
        try {
            if (!pdfile.exists()) return;
            String text = new String(java.nio.file.Files.readAllBytes(pdfile.toPath()), StandardCharsets.UTF_8);
            if (!text.contains("You\"\"ve")) return;
            java.nio.file.Files.write(pdfile.toPath(), text.replace("You\"\"ve", "You've").getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {
        }
    }

    public String loadMessage(Plugin plugin, File pdFile, FileConfiguration config, String message) {
        if (config.getString(message) != null) return StringConverter.coloredString(config.getString(message));
        else return StringConverter.coloredString(write(plugin, pdFile, config, message));
    }

    public String write(Plugin plugin, File pdFile, FileConfiguration config, String what) {

        String insert = "Can't load the string (" + what + ") for the plugin > " + plugin.getName() + " in language: " + GeneralConfig.getInstance().getLocale() + ", contact the developer";
        try {

            InputStream flux = plugin.getClass().getResourceAsStream("/com/ssomar/" + plugin.getName().toLowerCase() + "/configs/locale/Locale_" + GeneralConfig.getInstance().getLocale() + ".yml");
            if(flux == null) {
                Utils.sendConsoleMsg(SCore.NAME_COLOR + " &cERROR LOAD MESSAGE &6"+ what + " &cfor the plugin > &6" + plugin.getName() + " &cin language: &6" + GeneralConfig.getInstance().getLocale()+" &c(File in jar not found");
                return insert;
            }
            InputStreamReader lecture = new InputStreamReader(flux, StandardCharsets.UTF_8);
            BufferedReader buff = new BufferedReader(lecture);
            String ligne;
            boolean isNotUpdate = true;
            while ((ligne = buff.readLine()) != null && isNotUpdate) {
                if (ligne.contains(what + ":")) {
                    Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Update of &6" + what + " &7in your for the plugin > &6" + plugin.getName() + " &7in language: &6" + GeneralConfig.getInstance().getLocale());
                    String[] split = ligne.split("\"");
                    if(split.length > 1) insert = split[1];
                    else insert = "";
                    config.set(what, insert);
                    config.save(pdFile);
                    isNotUpdate = false;
                }
            }
            buff.close();
            if (isNotUpdate) {
                Utils.sendConsoleMsg(SCore.NAME_COLOR + " &cERROR LOAD MESSAGE &6" + what + " &cfor the plugin > &6" + plugin.getName() + " &cin language: &6" + GeneralConfig.getInstance().getLocale()+" (Message not found in jar)");
            }
        } catch (Exception e) {
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &cERROR LOAD MESSAGE &6"+ what + " &cfor the plugin > &6" + plugin.getName() + " &cin language: &6" + GeneralConfig.getInstance().getLocale());
            e.printStackTrace();
        }

        return insert;
    }

    public String getMessage(Plugin plugin, MessageInterface message) {
        if (messages.containsKey(plugin) && messages.get(plugin).containsKey(message)) {
            return messages.get(plugin).get(message);
        }
        return "";
    }
}
