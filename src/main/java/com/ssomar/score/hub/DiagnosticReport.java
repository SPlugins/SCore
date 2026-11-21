package com.ssomar.score.hub;

import com.ssomar.score.SCore;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.*;

/** A text to paste in a support ticket: server, Java, our plugins, the other plugins. No player data. */
public final class DiagnosticReport {

    /** Plugins that often explain a support case (they change items, packs, protections or packets). */
    private static final String[] WATCH = {"ItemsAdder", "Oraxen", "Nexo", "MMOItems", "WorldGuard", "GriefPrevention", "Towny",
            "ProtocolLib", "packetevents", "ViaVersion", "ViaBackwards", "Geyser-Spigot", "floodgate", "ModelEngine", "BetterModel",
            "mcMMO", "AuraSkills", "Multiverse-Core", "CMI", "EssentialsX", "Essentials", "PlaceholderAPI", "NBTAPI", "Slimefun"};

    private DiagnosticReport() {
    }

    public static String build() {
        StringBuilder b = new StringBuilder();
        b.append("SPlugins diagnostic report\n");
        b.append("Server: ").append(Bukkit.getName()).append(" ").append(Bukkit.getVersion()).append("\n");
        b.append("Bukkit API: ").append(Bukkit.getBukkitVersion()).append("\n");
        b.append("Software: ").append(software()).append("\n");
        b.append("Java: ").append(System.getProperty("java.version")).append(" (").append(System.getProperty("java.vendor")).append(")\n");
        b.append("OS: ").append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.arch")).append("\n");
        b.append("Online players: ").append(Bukkit.getOnlinePlayers().size()).append("\n");
        b.append("\nSPlugins:\n");
        for (InstalledPlugin p : InstalledPlugin.scan()) {
            b.append("- ").append(p.getKnown().getPluginName()).append(" ").append(p.getVersion());
            if (p.getPremium() != null) b.append(p.getPremium() ? " premium" : " free");
            if (!p.isEnabled()) b.append(" DISABLED");
            if (p.getObjects() >= 0) b.append(", ").append(p.getObjects()).append(" loaded");
            if (p.getErrors() > 0) b.append(", ").append(p.getErrors()).append(" loading error(s)");
            b.append("\n");
        }
        List<String> watched = new ArrayList<>();
        List<String> others = new ArrayList<>();
        Set<String> ours = new HashSet<>();
        for (KnownPlugin k : KnownPlugin.values()) ours.add(k.getPluginName());
        for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
            if (ours.contains(p.getName())) continue;
            String s = p.getName() + " " + p.getDescription().getVersion() + (p.isEnabled() ? "" : " (disabled)");
            others.add(s);
            for (String w : WATCH) if (w.equalsIgnoreCase(p.getName())) watched.add(s);
        }
        Collections.sort(others, String.CASE_INSENSITIVE_ORDER);
        if (!watched.isEmpty()) b.append("\nTo check first: ").append(String.join(", ", watched)).append("\n");
        b.append("\nOther plugins (").append(others.size()).append("): ").append(String.join(", ", others)).append("\n");
        List<String> errors = LoadErrorCollector.all();
        if (!errors.isEmpty()) {
            b.append("\nLast loading errors:\n");
            for (int i = Math.max(0, errors.size() - 15); i < errors.size(); i++) b.append("  ").append(errors.get(i)).append("\n");
        }
        return b.toString();
    }

    static String software() {
        if (classExists("io.papermc.paper.threadedregions.RegionizedServer")) return "Folia";
        if (SCore.isPurpur()) return "Purpur";
        if (SCore.isPufferfish()) return "Pufferfish";
        if (SCore.isPaper()) return "Paper";
        if (SCore.isSpigot()) return "Spigot";
        return Bukkit.getName();
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
