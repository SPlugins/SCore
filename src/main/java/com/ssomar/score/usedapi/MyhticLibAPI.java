package com.ssomar.score.usedapi;

import com.ssomar.score.SCore;
import com.ssomar.score.utils.logging.Utils;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import io.lumine.mythic.lib.api.stat.StatInstance;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.api.stat.provider.EntityStatProvider;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.element.Element;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class MyhticLibAPI {

    /**
     * Utilizes MythicLib API to perform MMO-type damage
     * @param attacker source of the damage
     * @param entity receiver of the damage
     * @param damageInput amount of damage
     * @param damageType type of damage
     * @param element damage element
     * @param knockback if damage should knockback receiver
     * @param crit if damage source's CRITICAL_STRIKE_POWER should be added into the equation
     */
    public static void mlib_damage(@Nullable LivingEntity attacker, LivingEntity entity, double damageInput, String damageType, @Nullable String element, boolean knockback, boolean crit) {

        Element elementEnum = null;
        if (element != null){
            try {
                elementEnum = Element.valueOf(element);
            } catch (IllegalArgumentException e) {
                Utils.sendConsoleMsg(SCore.plugin, "&cInvalid Element specification in MLIB_DAMAGE command ! Element : &7"+element);
                Utils.sendConsoleMsg(SCore.plugin, "&cAvailable elements are : &7"+Element.values());
            }
        }

        // to get critical stats
        double damage = 0;

        if (crit) {
            MMOPlayerData playerData = MMOPlayerData.get((Player) attacker);
            StatMap statMap = playerData.getStatMap();
            double critStrikePower = statMap.getInstance("CRITICAL_STRIKE_POWER").getTotal() / 100;

            damage = damageInput + (damageInput * critStrikePower);
        } else damage = damageInput;

        DamageMetadata damageMetadata;
        if (elementEnum == null) damageMetadata = new DamageMetadata(damage, DamageType.valueOf(damageType));
        else damageMetadata = new DamageMetadata(damage, elementEnum, DamageType.valueOf(damageType));

        EntityStatProvider entityStatProvider = null;
        if(attacker != null) entityStatProvider = new EntityStatProvider(attacker);

        AttackMetadata attacKMetadata = new AttackMetadata(damageMetadata, entityStatProvider);
        MythicLib.inst().getDamage().damage(attacKMetadata, entity, knockback);
    }
}
