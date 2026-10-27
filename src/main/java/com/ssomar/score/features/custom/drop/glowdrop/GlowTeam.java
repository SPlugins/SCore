package com.ssomar.score.features.custom.drop.glowdrop;


import com.ssomar.executableitems.ExecutableItems;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlowTeam {

    static Team team_AQUA;
    static Team team_BLACK;
    static Team team_BLUE;
    static Team team_DARK_AQUA;
    static Team team_DARK_BLUE;
    static Team team_DARK_GRAY;
    static Team team_DARK_GREEN;
    static Team team_DARK_PURPLE;
    static Team team_DARK_RED;
    static Team team_GOLD;
    static Team team_GRAY;
    static Team team_GREEN;
    static Team team_RED;
    static Team team_WHITE;
    static Team team_YELLOW;

    static void registerTeams() {
        ScoreboardManager scoreboardManager = ExecutableItems.plugin.getServer().getScoreboardManager();
        try {
            team_AQUA = scoreboardManager.getMainScoreboard().registerNewTeam("team_AQUA");
        } catch (IllegalArgumentException er) {
            team_AQUA = scoreboardManager.getMainScoreboard().getTeam("team_AQUA");
        }
        team_AQUA.setColor(ChatColor.AQUA);
        try {
            team_BLACK = scoreboardManager.getMainScoreboard().registerNewTeam("team_BLACK");
        } catch (IllegalArgumentException er) {
            team_BLACK = scoreboardManager.getMainScoreboard().getTeam("team_BLACK");
        }
        team_BLACK.setColor(ChatColor.BLACK);
        try {
            team_BLUE = scoreboardManager.getMainScoreboard().registerNewTeam("team_BLUE");
        } catch (IllegalArgumentException er) {
            team_BLUE = scoreboardManager.getMainScoreboard().getTeam("team_BLUE");
        }
        team_BLUE.setColor(ChatColor.BLUE);
        try {
            team_DARK_AQUA = scoreboardManager.getMainScoreboard().registerNewTeam("team_DARK_AQUA");
        } catch (IllegalArgumentException er) {
            team_DARK_AQUA = scoreboardManager.getMainScoreboard().getTeam("team_DARK_AQUA");
        }
        team_DARK_AQUA.setColor(ChatColor.DARK_AQUA);
        try {
            team_DARK_BLUE = scoreboardManager.getMainScoreboard().registerNewTeam("team_DARK_BLUE");
        } catch (IllegalArgumentException er) {
            team_DARK_BLUE = scoreboardManager.getMainScoreboard().getTeam("team_DARK_BLUE");
        }
        team_DARK_BLUE.setColor(ChatColor.DARK_BLUE);
        try {
            team_DARK_GRAY = scoreboardManager.getMainScoreboard().registerNewTeam("team_DARK_GRAY");
        } catch (IllegalArgumentException er) {
            team_DARK_GRAY = scoreboardManager.getMainScoreboard().getTeam("team_DARK_GRAY");
        }
        team_DARK_GRAY.setColor(ChatColor.DARK_GRAY);
        try {
            team_DARK_GREEN = scoreboardManager.getMainScoreboard().registerNewTeam("team_DARK_GREEN");
        } catch (IllegalArgumentException er) {
            team_DARK_GREEN = scoreboardManager.getMainScoreboard().getTeam("team_DARK_GREEN");
        }
        team_DARK_GREEN.setColor(ChatColor.DARK_GREEN);
        try {
            team_DARK_PURPLE = scoreboardManager.getMainScoreboard().registerNewTeam("team_DARK_PURPLE");
        } catch (IllegalArgumentException er) {
            team_DARK_PURPLE = scoreboardManager.getMainScoreboard().getTeam("team_DARK_PURPLE");
        }
        team_DARK_PURPLE.setColor(ChatColor.DARK_PURPLE);
        try {
            team_DARK_RED = scoreboardManager.getMainScoreboard().registerNewTeam("team_DARK_RED");
        } catch (IllegalArgumentException er) {
            team_DARK_RED = scoreboardManager.getMainScoreboard().getTeam("team_DARK_RED");
        }
        team_DARK_RED.setColor(ChatColor.DARK_RED);
        try {
            team_GOLD = scoreboardManager.getMainScoreboard().registerNewTeam("team_GOLD");
        } catch (IllegalArgumentException er) {
            team_GOLD = scoreboardManager.getMainScoreboard().getTeam("team_GOLD");
        }
        team_GOLD.setColor(ChatColor.GOLD);
        try {
            team_GRAY = scoreboardManager.getMainScoreboard().registerNewTeam("team_GRAY");
        } catch (IllegalArgumentException er) {
            team_GRAY = scoreboardManager.getMainScoreboard().getTeam("team_GRAY");
        }
        team_GRAY.setColor(ChatColor.GRAY);
        try {
            team_GREEN = scoreboardManager.getMainScoreboard().registerNewTeam("team_GREEN");
        } catch (IllegalArgumentException er) {
            team_GREEN = scoreboardManager.getMainScoreboard().getTeam("team_GREEN");
        }
        team_GREEN.setColor(ChatColor.GREEN);
        try {
            team_RED = scoreboardManager.getMainScoreboard().registerNewTeam("team_RED");
        } catch (IllegalArgumentException er) {
            team_RED = scoreboardManager.getMainScoreboard().getTeam("team_RED");
        }
        team_RED.setColor(ChatColor.RED);
        try {
            team_WHITE = scoreboardManager.getMainScoreboard().registerNewTeam("team_WHITE");
        } catch (IllegalArgumentException er) {
            team_WHITE = scoreboardManager.getMainScoreboard().getTeam("team_WHITE");
        }
        team_WHITE.setColor(ChatColor.WHITE);
        try {
            team_YELLOW = scoreboardManager.getMainScoreboard().registerNewTeam("team_YELLOW");
        } catch (IllegalArgumentException er) {
            team_YELLOW = scoreboardManager.getMainScoreboard().getTeam("team_YELLOW");
        }
        team_YELLOW.setColor(ChatColor.YELLOW);
    }
}