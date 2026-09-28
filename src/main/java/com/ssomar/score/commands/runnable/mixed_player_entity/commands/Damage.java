package com.ssomar.score.commands.runnable.mixed_player_entity.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.ActionInfo;
import com.ssomar.score.commands.runnable.ArgumentChecker;
import com.ssomar.score.commands.runnable.CommandSetting;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.mixed_player_entity.MixedCommand;
import com.ssomar.score.usedapi.WorldGuardAPI;
import com.ssomar.score.utils.backward_compatibility.AttributeUtils;
import com.ssomar.score.utils.numbers.NTools;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
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

public class Damage extends MixedCommand {

    /** Default damage type: the launcher's attack when there is a player, magic otherwise. */
    public static final String AUTO_DAMAGE_TYPE = "auto";
    private static final java.util.Set<String> warnedUnknownTypes = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public Damage() {
        CommandSetting amount = new CommandSetting("amount", 0, String.class, "0");
        CommandSetting ifStrength = new CommandSetting("ifStr", 1, String.class, "false");
        CommandSetting ifAttribute = new CommandSetting("ifAttr", 2, String.class, "false");
        CommandSetting damageType = new CommandSetting("type", 3, String.class, AUTO_DAMAGE_TYPE);
        List<CommandSetting> settings = getSettings();
        settings.add(amount);
        settings.add(ifStrength);
        settings.add(ifAttribute);
        settings.add(damageType);
        setNewSettingsMode(true);
    }

    public void run(Player p, Entity receiver, SCommandToExec sCommandToExec) {
        List<String> args = new ArrayList<>();
        args.add((String) sCommandToExec.getSettingValue("amount"));
        args.add((String) sCommandToExec.getSettingValue("ifStr"));
        args.add((String) sCommandToExec.getSettingValue("ifAttr"));
        args.add((String) sCommandToExec.getSettingValue("type"));

        ActionInfo aInfo = sCommandToExec.getActionInfo();

        if(!(receiver instanceof LivingEntity)) return;
        LivingEntity livingReceiver = (LivingEntity) receiver;

        /* When target a NPC it can occurs */
        if (receiver == null) return;

        double damage = getDamage(p, livingReceiver, args, aInfo);

        Object damageSource = null;
        if(SCore.is1v20v5Plus()) {
            String typeName = (String) sCommandToExec.getSettingValue("type");
            String key = resolveDamageTypeKey(typeName, p != null);
            DamageType damageType = null;
            try {
                damageType = Registry.DAMAGE_TYPE.get(NamespacedKey.minecraft(key));
            } catch (IllegalArgumentException ignored) {
            }
            if (damageType == null) {
                if (warnedUnknownTypes.add(key)) {
                    SCore.plugin.getLogger().warning("[DAMAGE] Unknown damage type '" + typeName + "', the default one is used");
                }
                damageType = Registry.DAMAGE_TYPE.get(NamespacedKey.minecraft(resolveDamageTypeKey(AUTO_DAMAGE_TYPE, p != null)));
            }

            try {
                if (p != null) damageSource = DamageSource.builder(damageType).withDirectEntity(p).withCausingEntity(p).build();
                else damageSource = DamageSource.builder(damageType).build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        damage(p, livingReceiver, damage, damageSource, aInfo);
    }

    /**
     * Vanilla damage type key used by the command.
     * <ul>
     *   <li>"auto" (default): player_attack when a player launched the command (right death message,
     *   counts as that player's attack), indirect_magic otherwise;</li>
     *   <li>any other value: the vanilla id, case-insensitive (IN_FIRE and in_fire both work, the javadoc
     *   lists the names in uppercase; they used to be rejected by NamespacedKey and silently ignored).</li>
     * </ul>
     * "INDIRECT_MAGIC" in uppercase was the previous default: NamespacedKey rejected it, so it behaved
     * like "auto". It keeps that behaviour so existing configs deal the same damage.
     */
    public static String resolveDamageTypeKey(String type, boolean hasLauncher) {
        if (type == null || type.trim().isEmpty() || type.trim().equalsIgnoreCase(AUTO_DAMAGE_TYPE) || type.trim().equals("INDIRECT_MAGIC")) {
            return hasLauncher ? "player_attack" : "indirect_magic";
        }
        String key = type.trim().toLowerCase(java.util.Locale.ROOT);
        return key.startsWith("minecraft:") ? key.substring("minecraft:".length()) : key;
    }

    public static void damage(Player p, LivingEntity receiver, double damage, Object damageSource, ActionInfo actionInfo) {

        if (damage > 0 && !receiver.isDead()) {
            int maximumNoDmg = receiver.getNoDamageTicks();
            receiver.setNoDamageTicks(0);
            boolean doDamage = true;
            if (SCore.hasWorldGuard && receiver instanceof Player) doDamage = WorldGuardAPI.isInPvpZone((Player) receiver, receiver.getLocation());
            // to prevent double kill https://discord.com/channels/701066025516531753/1301172655616950294/1301172655616950294
            if(receiver.isDead()) return;
            if (doDamage) {
                if (p != null) {
                    /* To avoid looping damage */
                    if(actionInfo.isActionRelatedToDamageEvent()) p.setMetadata("cancelDamageEvent", (MetadataValue) new FixedMetadataValue((Plugin) SCore.plugin, Integer.valueOf(7772)));
                    p.setMetadata("damageFromCustomCommand", (MetadataValue) new FixedMetadataValue((Plugin) SCore.plugin, Integer.valueOf(7773)));
                    if(SCore.is1v20v5Plus() && damageSource != null) receiver.damage(damage, (DamageSource) damageSource);
                    else receiver.damage(damage, p);
                } else {
                    if(SCore.is1v20v5Plus() && damageSource != null) receiver.damage(damage, (DamageSource) damageSource);
                    else receiver.damage(damage);
                }
            }
            if(!receiver.isDead()) receiver.setNoDamageTicks(maximumNoDmg);
        }
    }

    @SuppressWarnings("deprecation")
    public static double getDamage(Player launcher, LivingEntity receiver, List<String> args, ActionInfo actionInfo) {
        double amount;
        String damage = args.get(0);

        boolean potionAmplification = false;
        if (args.size() >= 2) {
            potionAmplification = Boolean.valueOf(args.get(1));
        }
        boolean attributeAmplification = false;
        if (args.size() >= 3) {
            attributeAmplification = Boolean.valueOf(args.get(2));
        }

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

        if (launcher != null) {
            if (potionAmplification) {
                PotionEffectType incDamage = SCore.is1v20v5Plus() ? PotionEffectType.STRENGTH : PotionEffectType.getByName("INCREASE_DAMAGE");
                PotionEffect pE = launcher.getPotionEffect(incDamage);
                if (pE != null) {
                    amount = amount + (pE.getAmplifier() + 1) * 3;
                }
            }

            //SsomarDev.testMsg("boost attribute: "+ attributeAmplification);
            if (attributeAmplification) {
                Attribute att = null;
                if(SCore.is1v21v2Plus()) att = Attribute.ATTACK_DAMAGE;
                else att = AttributeUtils.getAttribute("GENERIC_ATTACK_DAMAGE");
                AttributeInstance aI = launcher.getAttribute(att);
                double bonusAmount = 0;
                if (aI != null) {
                    //SsomarDev.testMsg("damage value: "+aI.getValue());
                    for (AttributeModifier aM : aI.getModifiers()) {
                        //SsomarDev.testMsg("passe 2:  "+aM.getOperation());
                        if (aM.getOperation().equals(AttributeModifier.Operation.MULTIPLY_SCALAR_1)) {
                            //SsomarDev.testMsg("passe 3: "+(amount * aM.getAmount())+ " >> "+aM.getAmount());
                            bonusAmount = bonusAmount + amount * aM.getAmount();
                        }
                    }
                }
                //SsomarDev.testMsg("boost attribute bonus: "+ bonusAmount);
                amount = amount + bonusAmount;
            }
        }
        return amount;
    }

    @Override
    public Optional<String> verify(List<String> args, boolean isFinalVerification) {
        return Optional.empty();
    }

    public static Optional<String> staticVerif(List<String> args, boolean isFinalVerification, String template) {
        if (args.size() < 1) return Optional.of(notEnoughArgs + template);

        ArgumentChecker ac = checkDouble(args.get(0), isFinalVerification, template, true);
        if (!ac.isValid()) return Optional.of(ac.getError());

        if (args.size() >= 2) {
            String value = args.get(1);
            ArgumentChecker ac2 = checkBoolean(value, isFinalVerification, template);
            if (!ac2.isValid()) return Optional.of(ac2.getError());
        }

        if (args.size() >= 3) {
            String value = args.get(2);
            ArgumentChecker ac2 = checkBoolean(value, isFinalVerification, template);
            if (!ac2.isValid()) return Optional.of(ac2.getError());
        }

        return Optional.empty();
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("DAMAGE");
        return names;
    }

    @Override
    public String getTemplate() {
        return "DAMAGE {number} [amplified If Strength Effect, true or false] [amplified with attack attribute, true or false] [damageType]";
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