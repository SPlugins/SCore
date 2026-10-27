package com.ssomar.score.usedapi;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.user.SkillsUser;
import dev.aurelium.auraskills.api.user.UserManager;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuraSkillsAPI {

    public static boolean checkMana(Player player, double value) {
        if (Dependency.AURA_SKILLS.isInstalled()) {
            AuraSkillsApi auraSkillsApi = AuraSkillsApi.get();
            UserManager userManager = auraSkillsApi.getUserManager();
            SkillsUser skillsUser = userManager.getUser(player.getUniqueId());
            if (skillsUser == null) return false;

            if (skillsUser.getMana() >= value) {
                return true;
            } else return false;
        }
        return true;
    }

    public static void takeMana(Player player, double value) {
        if (Dependency.AURA_SKILLS.isInstalled()) {
            AuraSkillsApi auraSkillsApi = AuraSkillsApi.get();
            UserManager userManager = auraSkillsApi.getUserManager();
            SkillsUser skillsUser = userManager.getUser(player.getUniqueId());
            if (skillsUser == null) return;
            skillsUser.setMana(skillsUser.getMana() - value);
        }
    }
}
