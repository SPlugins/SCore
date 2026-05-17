package com.ssomar.score.commands.runnable.mixed_player_entity.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.ActionInfo;
import com.ssomar.score.commands.runnable.ArgumentChecker;
import com.ssomar.score.commands.runnable.CommandSetting;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.mixed_player_entity.MixedCommand;
import com.ssomar.score.usedapi.MyhticLibAPI;
import com.ssomar.score.usedapi.WorldGuardAPI;
import com.ssomar.score.utils.backward_compatibility.AttributeUtils;
import com.ssomar.score.utils.numbers.NTools;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MLibDamage extends MixedCommand {

    public MLibDamage() {
        CommandSetting damage = new CommandSetting("damage", 0, String.class, 10);
        CommandSetting type = new CommandSetting("type", 1, String.class, "PHYSICAL");
        CommandSetting knockback = new CommandSetting("knockback", 2, Boolean.class, false);
        CommandSetting element = new CommandSetting("element", 3, String.class, "FIRE");
        CommandSetting crit = new CommandSetting("crit", 4, Boolean.class, false);
        List<CommandSetting> settings = getSettings();
        settings.add(damage);
        settings.add(type);
        settings.add(knockback);
        settings.add(element);
        settings.add(crit);
        setNewSettingsMode(true);
    }

    public void run(Player p, Entity receiver, SCommandToExec sCommandToExec) {
        ActionInfo aInfo = sCommandToExec.getActionInfo();

        String damageArg = (String) sCommandToExec.getSettingValue("damage");
        // MAGIC, PHYSICAL, WEAPON, SKILL, PROJECTILE, UNARMED, ON_HIT, MINION, DOT;
        String damageType = (String) sCommandToExec.getSettingValue("type");
        boolean knockback = (boolean) sCommandToExec.getSettingValue("knockback");
        String element = (String) sCommandToExec.getSettingValue("element");
        boolean crit = (boolean) sCommandToExec.getSettingValue("crit");

        if(!(receiver instanceof LivingEntity)) return;
        LivingEntity livingReceiver = (LivingEntity) receiver;

        /* When target a NPC it can occurs */
        if (receiver == null) return;


        double damage = getDamage(p, livingReceiver, damageArg, aInfo);

        if (damage > 0 && !receiver.isDead()) {
            int maximumNoDmg = livingReceiver.getNoDamageTicks();
            livingReceiver.setNoDamageTicks(0);
            boolean doDamage = true;
            if (SCore.hasWorldGuard && receiver instanceof Player) doDamage = WorldGuardAPI.isInPvpZone((Player) receiver, receiver.getLocation());
            if (doDamage) {
                if (p != null) {
                    /* To avoid looping damage */
                    if(aInfo.isActionRelatedToDamageEvent()) p.setMetadata("cancelDamageEvent", (MetadataValue) new FixedMetadataValue((Plugin) SCore.plugin, Integer.valueOf(7772)));
                    p.setMetadata("damageFromCustomCommand", (MetadataValue) new FixedMetadataValue((Plugin) SCore.plugin, Integer.valueOf(7773)));
                    MyhticLibAPI.mlib_damage(p, livingReceiver, damage, damageType, element, knockback, crit);
                } else {
                    MyhticLibAPI.mlib_damage(null, livingReceiver, damage, damageType, element, knockback, crit);
                }
            }

            livingReceiver.setNoDamageTicks(maximumNoDmg);
        }
    }

    @SuppressWarnings("deprecation")
    public static double getDamage(Player launcher, LivingEntity receiver, String damage, ActionInfo actionInfo) {
        double amount;

        /* percentage damage */
        if (damage.contains("%")) {
            String[] decomp = damage.split("%");
            damage = decomp[0];
            damage = damage.trim();
            if (damage.length() == 1) {
                damage = "0" + damage;
            }

            double percentage = damage.equals("100") ? 1 : Double.parseDouble("0." + damage);
            amount = receiver.getMaxHealth() * percentage;
            amount = NTools.reduceDouble(amount, 2);
        } else amount = Double.parseDouble(damage);

        return amount;
    }

    @Override
    public Optional<String> verify(List<String> args, boolean isFinalVerification) {
        return staticVerif(args, isFinalVerification, getTemplate());
    }

    public static Optional<String> staticVerif(List<String> args, boolean isFinalVerification, String template) {
        if (args.size() < 3) return Optional.of(notEnoughArgs + template);

        ArgumentChecker ac = checkDouble(args.get(0), isFinalVerification, template, true);
        if (!ac.isValid()) return Optional.of(ac.getError());

        return Optional.empty();
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("MLIB_DAMAGE");
        return names;
    }

    @Override
    public String getTemplate() {
        return "MLIB_DAMAGE {number} {damageType} {knockback} [element]";
    }

    @Override
    public ChatColor getColor() {
        return null;
    }

    @Override
    public ChatColor getExtraColor() {
        return null;
    }
}