package com.ssomar.score.usedapi;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.data.PlayerData;
import com.ssomar.score.SCore;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Locale;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AureliumSkillsAPI {

    public static boolean checkMana(Player player, double value) {
        if (SCore.hasAureliumSkills) {
            AureliumSkills aureliumSkills = (AureliumSkills) Bukkit.getPluginManager().getPlugin("AureliumSkills");
            PlayerData playerData = aureliumSkills.getPlayerManager().getPlayerData(player);
            if (playerData == null) return false;
            Locale locale = playerData.getLocale();
            if (playerData.getMana() >= value) {
                return true;
            } else return false;
        }
        return true;
    }

    public static void takeMana(Player player, double value) {
        if (SCore.hasAureliumSkills) {
            AureliumSkills aureliumSkills = (AureliumSkills) Bukkit.getPluginManager().getPlugin("AureliumSkills");
            PlayerData playerData = aureliumSkills.getPlayerManager().getPlayerData(player);
            if (playerData == null) return;
            playerData.setMana(playerData.getMana() - value);
        }
    }
}
