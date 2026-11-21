package com.ssomar.score.hub;

import com.ssomar.score.splugin.SPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/** A snapshot of one installed SPlugins plugin. */
public final class InstalledPlugin {

    private final KnownPlugin known;
    private final String version;
    private final boolean enabled;
    /** null = unknown (not an SPlugin), true = premium, false = free */
    private final Boolean premium;
    private final int objects;
    private final int freeLimit;
    private final int errors;

    private InstalledPlugin(KnownPlugin known, String version, boolean enabled, Boolean premium, int objects, int freeLimit, int errors) {
        this.known = known;
        this.version = version;
        this.enabled = enabled;
        this.premium = premium;
        this.objects = objects;
        this.freeLimit = freeLimit;
        this.errors = errors;
    }

    public static List<InstalledPlugin> scan() {
        List<InstalledPlugin> list = new ArrayList<>();
        for (KnownPlugin k : KnownPlugin.values()) {
            Plugin p = Bukkit.getPluginManager().getPlugin(k.getPluginName());
            if (p == null) continue;
            Boolean premium = null;
            int limit = -1;
            if (p instanceof SPlugin && k != KnownPlugin.SCORE) {
                try {
                    SPlugin sp = (SPlugin) p;
                    premium = !sp.isLotOfWork();
                    limit = premium ? -1 : sp.getMaxSObjectsLimit();
                } catch (Throwable ignored) {
                }
            }
            list.add(new InstalledPlugin(k, p.getDescription().getVersion(), p.isEnabled(), premium, k.countObjects(), limit,
                    LoadErrorCollector.linesOf(k.getPluginName()).size()));
        }
        return list;
    }

    public static boolean isInstalled(KnownPlugin k) {
        return Bukkit.getPluginManager().getPlugin(k.getPluginName()) != null;
    }

    public KnownPlugin getKnown() { return known; }
    public String getVersion() { return version; }
    public boolean isEnabled() { return enabled; }
    public Boolean getPremium() { return premium; }
    public int getObjects() { return objects; }
    public int getFreeLimit() { return freeLimit; }
    public int getErrors() { return errors; }
}
