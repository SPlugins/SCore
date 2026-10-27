package com.ssomar.score.usedapi;

import com.ssomar.score.SCore;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.entity.Player;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MMOCoreAPI {

    public static boolean checkMana(Player player, double value) {
        if (SCore.hasMMOCore) {
            PlayerData playerData = PlayerData.get(player);
            if (playerData == null) return false;
            if (playerData.getMana() >= value) {
                return true;
            } else return false;
        }
        return true;
    }

    public static void takeMana(Player player, double value) {
        if (SCore.hasAureliumSkills) {
            PlayerData playerData = PlayerData.get(player);
            if (playerData == null) return;
            playerData.setMana(playerData.getMana() - value);
        }
    }
}
