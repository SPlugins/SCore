package com.ssomar.score.hub;

import com.ssomar.score.SCore;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Small per-server memory of the Hub (SCore/hub-data.yml): news already seen per admin, first use, review reminder. */
public final class HubData {

    private static YamlConfiguration data;

    private HubData() {
    }

    private static File file() {
        return new File(SCore.dataFolder, "hub-data.yml");
    }

    private static synchronized YamlConfiguration data() {
        if (data == null) {
            File f = file();
            data = f.exists() ? YamlConfiguration.loadConfiguration(f) : new YamlConfiguration();
            if (!data.contains("firstUse")) {
                data.set("firstUse", System.currentTimeMillis());
                save();
            }
        }
        return data;
    }

    private static synchronized void save() {
        final String dump = data.saveToString();
        SCore.schedulerHook.runAsyncTask(() -> {
            try {
                java.nio.file.Files.write(file().toPath(), dump.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            } catch (Throwable ignored) {
            }
        }, 0);
    }

    private static Set<String> seen(Player p) {
        return new HashSet<>(data().getStringList("seen." + p.getUniqueId()));
    }

    public static boolean hasUnseenNews(Player p, HubCatalog cat) {
        Set<String> seen = seen(p);
        for (HubCatalog.News n : cat.news) if (!seen.contains(n.id)) return true;
        for (HubCatalog.News n : cat.promotions) if (!seen.contains(n.id)) return true;
        return false;
    }

    public static synchronized void markNewsSeen(Player p, HubCatalog cat) {
        List<String> ids = new ArrayList<>();
        for (HubCatalog.News n : cat.news) ids.add(n.id);
        for (HubCatalog.News n : cat.promotions) ids.add(n.id);
        data().set("seen." + p.getUniqueId(), ids);
        save();
    }

    /** true once, after the configured number of days of use, for the first admin who joins */
    public static synchronized boolean takeReviewReminder(int days) {
        YamlConfiguration d = data();
        if (d.getBoolean("reviewReminded", false)) return false;
        long first = d.getLong("firstUse", System.currentTimeMillis());
        if (System.currentTimeMillis() - first < days * 86_400_000L) return false;
        d.set("reviewReminded", true);
        save();
        return true;
    }
}
