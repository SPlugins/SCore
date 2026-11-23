package com.ssomar.score.features.custom.drop.glowdrop;

import com.ssomar.score.SCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GlowDropManager {

    private static GlowDropManager instance;
    private final Map<ChatColor, Team> teams;

    private Map<UUID, ChatColor> links = new ConcurrentHashMap<>();


    public GlowDropManager() {
        instance = this;
        teams = new ConcurrentHashMap<>();
        links = new ConcurrentHashMap<>();
        if (!SCore.is1v11Less()) runOnScoreboardThread(this::registerTeams);
    }

    private void registerTeams() {
        ScoreboardManager scoreboardManager = SCore.plugin.getServer().getScoreboardManager();
        for (ChatColor color : ChatColor.values()) {
            if(getBannedChatColorsForGlow().contains(color)) continue; // Skip banned colors
            Team team;
            try {
                team = scoreboardManager.getMainScoreboard().registerNewTeam("team_" + color.name());
            } catch (IllegalArgumentException er) {
                team = scoreboardManager.getMainScoreboard().getTeam("team_" + color.name());
            }
            if (team != null) {
                team.setColor(color);
                teams.put(color, team);
            }
        }
    }

    public static GlowDropManager getInstance() {
        if (instance == null) {
            instance = new GlowDropManager();
        }
        return instance;
    }

    /* Folia only lets the global region thread touch the scoreboard, the drop event runs on the region of the item.
     * The tasks are queued in order, so the teams are registered before the first glow is added. */
    private static void runOnScoreboardThread(Runnable runnable) {
        if (isRegionized && !isGlobalTickThread()) SCore.schedulerHook.runTask(runnable, 0);
        else runnable.run();
    }

    private static final boolean isRegionized = SCore.hasClass("io.papermc.paper.threadedregions.RegionizedServer");

    private static boolean isGlobalTickThread() {
        try {
            return (boolean) Bukkit.class.getMethod("isGlobalTickThread").invoke(null);
        } catch (Exception e) {
            return true;
        }
    }

    public Optional<String> onRequestPlaceholder(OfflinePlayer player, String params) {
        if (params.startsWith("cmd-glow")) {
            if (links.containsKey(player.getUniqueId())) {
                return Optional.of("&" + links.get(player.getUniqueId()).getChar());
            }
            return Optional.of("");
        }
        return Optional.empty();
    }

    public static List<ChatColor> getBannedChatColorsForGlow() {
        List<ChatColor> banned = new ArrayList<>();
        banned.add(ChatColor.MAGIC);
        banned.add(ChatColor.BOLD);
        banned.add(ChatColor.STRIKETHROUGH);
        banned.add(ChatColor.UNDERLINE);
        banned.add(ChatColor.ITALIC);
        return banned;
    }

    public void addGlow(Entity entity, ChatColor color) {

        if(getBannedChatColorsForGlow().contains(color)){
            color = ChatColor.WHITE;
        }

        //SsomarDev.testMsg("addGlow : " + entity.getType() + " " + color.name(), false);
        links.put(entity.getUniqueId(), color);
        /* if(SCore.hasTAB && entity instanceof Player){
            TabPlayer tabPlayer = TabAPI.getInstance().getPlayer(entity.getUniqueId());
            TabAPI.getInstance().getTeamManager().updateTeamData(tabPlayer);
        }*/
        entity.setGlowing(true);
        ChatColor finalColor = color;
        runOnScoreboardThread(() -> {
            if (teams.containsKey(finalColor)) {
                try {
                    Team team = teams.get(finalColor);
                    team.addEntry(entity.getUniqueId().toString());
                    if (entity instanceof OfflinePlayer) team.addEntry(((OfflinePlayer) entity).getName());
                }catch (Exception e){
                    SCore.plugin.getLogger().severe("Error with glow drop, the scoreboard team is not registered ! You have probably a plugin that purges the teams, try to find it and disable the purge if you want the glow drop.");
                    //e.printStackTrace();
                }
            } //else SsomarDev.testMsg("color not found > " + finalColor.name(), false);
        });
    }

    public void removeGlow(Entity entity, ChatColor color) {
        links.remove(entity.getUniqueId());
        /* if(SCore.hasTAB && entity instanceof Player){
            TabPlayer tabPlayer = TabAPI.getInstance().getPlayer(entity.getUniqueId());
            TabAPI.getInstance().getTeamManager().resumeTeamHandling(tabPlayer);
        }*/
        if (teams.containsKey(color)) entity.setGlowing(false);
        runOnScoreboardThread(() -> {
            if (teams.containsKey(color)) {
                Team team = teams.get(color);
                if(team.hasEntry(entity.getUniqueId().toString())) team.removeEntry(entity.getUniqueId().toString());
                if (entity instanceof OfflinePlayer) {
                    if(team.hasPlayer((OfflinePlayer) entity)) team.removePlayer((OfflinePlayer) entity);
                }
            }
        });
    }
}
