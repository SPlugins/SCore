package com.ssomar.score.screen.internal;

import com.ssomar.score.SCore;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

/** Small memory of SCore Screens (SCore/screens-data.yml): cursor test answers, map ids reused by the image tiles. */
public final class ScreenData {

    private static YamlConfiguration data;

    private ScreenData() {
    }

    private static File file() {
        return new File(SCore.dataFolder, "screens-data.yml");
    }

    private static synchronized YamlConfiguration data() {
        if (data == null) {
            File f = file();
            data = f.exists() ? YamlConfiguration.loadConfiguration(f) : new YamlConfiguration();
        }
        return data;
    }

    private static synchronized void save() {
        final String dump = data().saveToString();
        SCore.schedulerHook.runAsyncTask(() -> {
            try {
                Files.write(file().toPath(), dump.getBytes(StandardCharsets.UTF_8));
            } catch (Throwable ignored) {
            }
        }, 0);
    }

    /** null = not asked yet, true = the player sees the cursor, false = the shader is not active for this player */
    public static synchronized Boolean cursorSeen(UUID player) {
        String k = "cursor." + player;
        return data().contains(k) ? data().getBoolean(k) : null;
    }

    public static synchronized void setCursorSeen(UUID player, Boolean seen) {
        data().set("cursor." + player, seen);
        save();
    }

    public static synchronized List<Integer> mapIds() {
        return data().getIntegerList("map-ids");
    }

    public static synchronized void setMapIds(List<Integer> ids) {
        data().set("map-ids", ids);
        save();
    }
}
