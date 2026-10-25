package com.ssomar.score.utils.backward_compatibility;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.ssomar.score.SCore;
import com.ssomar.score.SsomarDev;
import com.ssomar.score.utils.MapUtil;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class AttributeUtils {

    private final static boolean DEBUG = true;

    public static Map<Object, String> getAttributes() {
        Map<Object, String> list = new HashMap<>();
        if (SCore.is1v21v2Plus()) {
            for (Keyed l : Registry.ATTRIBUTE) {
                NamespacedKey key = l.getKey();
                if (key.getNamespace().equals("minecraft")) {
                    list.put(l, l.getKey().getKey().toUpperCase());
                } else list.put(l, l.getKey().toString());
            }
        } else if (!SCore.is1v8()) {
            for (Object o : Attribute.class.getEnumConstants()) {
                // Use reflection to get .name()
                try {
                    String name = (String) o.getClass().getMethod("name").invoke(o);
                    list.put(o, name);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        // Sort the list
        return MapUtil.sortByValue(list);
    }

    /**
     * If you want to get an attribute enum value by string, perform this method call
     * and input the correct string value based on the switch case of this method.
     * @param string
     * @return
     */
    /* name -> attribute: the attribute registry is frozen once the server has started, and rebuilding + sorting it on every lookup
     * (as getAttribute did) cost 5 % of the server thread on requeim's server (max health of each block title owner, 2026-10-01) */
    private static final Map<String, Attribute> ATTRIBUTE_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    public static Attribute getAttribute(String string) {
        Attribute cached = ATTRIBUTE_CACHE.get(string);
        if (cached != null) return cached;
        Attribute found = findAttribute(string);
        if (found != null) ATTRIBUTE_CACHE.put(string, found);
        return found;
    }

    private static Attribute findAttribute(String string) {
        string = string.replace("minecraft:", "");
        SsomarDev.testMsg(ChatColor.GOLD+"[#s0019] getAttribute() is triggered: "+string, true);
        for (Map.Entry<Object, String> entry : getAttributes().entrySet()) {
            if (entry.getValue().equalsIgnoreCase(string)) {
                SsomarDev.testMsg("[#s0020] getAttributes() returns Attribute from iteration", true);
                return (Attribute) entry.getKey();
            }
        }
        SsomarDev.testMsg("[#s0021] getAttributes() checks switch case statement", true);
        try {
        switch (string.toUpperCase()) {
            case "GENERIC_MAX_HEALTH":
                return Attribute.MAX_HEALTH;
            case "GENERIC_FOLLOW_RANGE":
                return Attribute.FOLLOW_RANGE;
            case "GENERIC_KNOCKBACK_RESISTANCE":
                return Attribute.KNOCKBACK_RESISTANCE;
            case "GENERIC_MOVEMENT_SPEED":
                return Attribute.MOVEMENT_SPEED;
            case "GENERIC_FLYING_SPEED":
                return Attribute.FLYING_SPEED;
            case "GENERIC_ATTACK_DAMAGE":
                return Attribute.ATTACK_DAMAGE;
            case "GENERIC_ATTACK_KNOCKBACK":
                return Attribute.ATTACK_KNOCKBACK;
            case "GENERIC_ATTACK_SPEED":
                return Attribute.ATTACK_SPEED;
            case "GENERIC_ARMOR":
                return Attribute.ARMOR;
            case "GENERIC_ARMOR_TOUGHNESS":
                return Attribute.ARMOR_TOUGHNESS;
            case "GENERIC_LUCK":
                return Attribute.LUCK;
            case "GENERIC_JUMP_STRENGTH":
                return Attribute.JUMP_STRENGTH;
            case "GENERIC_FALL_DAMAGE_MULTIPLIER":
                return Attribute.FALL_DAMAGE_MULTIPLIER;
            case "GENERIC_MAX_ABSORPTION":
                return Attribute.MAX_ABSORPTION;
            case "GENERIC_SAFE_FALL_DISTANCE":
                return Attribute.SAFE_FALL_DISTANCE;
            case "GENERIC_SCALE":
                return Attribute.SCALE;
            case "GENERIC_STEP_HEIGHT":
                return Attribute.STEP_HEIGHT;
            case "GENERIC_GRAVITY":
                return Attribute.GRAVITY;
            case "GENERIC_BURNING_TIME":
                return Attribute.BURNING_TIME;
            case "GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE":
                return Attribute.EXPLOSION_KNOCKBACK_RESISTANCE;
            case "GENERIC_MOVEMENT_EFFICIENCY":
                return Attribute.MOVEMENT_EFFICIENCY;
            case "GENERIC_OXYGEN_BONUS":
                return Attribute.OXYGEN_BONUS;
            case "GENERIC_WATER_MOVEMENT_EFFICIENCY":
                return Attribute.WATER_MOVEMENT_EFFICIENCY;
            case "PLAYER_BLOCK_INTERACTION_RANGE":
                return Attribute.BLOCK_INTERACTION_RANGE;
            case "PLAYER_ENTITY_INTERACTION_RANGE":
                return Attribute.ENTITY_INTERACTION_RANGE;
            case "PLAYER_BLOCK_BREAK_SPEED":
                return Attribute.BLOCK_BREAK_SPEED;
            case "PLAYER_MINING_EFFICIENCY":
                return Attribute.MINING_EFFICIENCY;
            case "PLAYER_SNEAKING_SPEED":
                return Attribute.SNEAKING_SPEED;
            case "PLAYER_SUBMERGED_MINING_SPEED":
                return Attribute.SUBMERGED_MINING_SPEED;
            case "PLAYER_SWEEPING_DAMAGE_RATIO":
                return Attribute.SWEEPING_DAMAGE_RATIO;
            case "ZOMBIE_SPAWN_REINFORCEMENTS":
                return Attribute.SPAWN_REINFORCEMENTS;
            case "MAX_HEALTH":
                return Attribute.MAX_HEALTH;
            case "FOLLOW_RANGE":
                return Attribute.FOLLOW_RANGE;
            case "KNOCKBACK_RESISTANCE":
                return Attribute.KNOCKBACK_RESISTANCE;
            case "MOVEMENT_SPEED":
                return Attribute.MOVEMENT_SPEED;
            case "FLYING_SPEED":
                return Attribute.FLYING_SPEED;
            case "ATTACK_DAMAGE":
                return Attribute.ATTACK_DAMAGE;
            case "ATTACK_KNOCKBACK":
                return Attribute.ATTACK_KNOCKBACK;
            case "ATTACK_SPEED":
                return Attribute.ATTACK_SPEED;
            case "ARMOR":
                return Attribute.ARMOR;
            case "ARMOR_TOUGHNESS":
                return Attribute.ARMOR_TOUGHNESS;
            case "LUCK":
                return Attribute.LUCK;
            case "JUMP_STRENGTH":
                return Attribute.JUMP_STRENGTH;
            case "FALL_DAMAGE_MULTIPLIER":
                return Attribute.FALL_DAMAGE_MULTIPLIER;
            case "MAX_ABSORPTION":
                return Attribute.MAX_ABSORPTION;
            case "SAFE_FALL_DISTANCE":
                return Attribute.SAFE_FALL_DISTANCE;
            case "SCALE":
                return Attribute.SCALE;
            case "STEP_HEIGHT":
                return Attribute.STEP_HEIGHT;
            case "GRAVITY":
                return Attribute.GRAVITY;
            case "BURNING_TIME":
                return Attribute.BURNING_TIME;
            case "EXPLOSION_KNOCKBACK_RESISTANCE":
                return Attribute.EXPLOSION_KNOCKBACK_RESISTANCE;
            case "MOVEMENT_EFFICIENCY":
                return Attribute.MOVEMENT_EFFICIENCY;
            case "OXYGEN_BONUS":
                return Attribute.OXYGEN_BONUS;
            case "WATER_MOVEMENT_EFFICIENCY":
                return Attribute.WATER_MOVEMENT_EFFICIENCY;
            case "BLOCK_INTERACTION_RANGE":
                return Attribute.BLOCK_INTERACTION_RANGE;
            case "ENTITY_INTERACTION_RANGE":
                return Attribute.ENTITY_INTERACTION_RANGE;
            case "BLOCK_BREAK_SPEED":
                return Attribute.BLOCK_BREAK_SPEED;
            case "MINING_EFFICIENCY":
                return Attribute.MINING_EFFICIENCY;
            case "SNEAKING_SPEED":
                return Attribute.SNEAKING_SPEED;
            case "SUBMERGED_MINING_SPEED":
                return Attribute.SUBMERGED_MINING_SPEED;
            case "SWEEPING_DAMAGE_RATIO":
                return Attribute.SWEEPING_DAMAGE_RATIO;
            case "SPAWN_REINFORCEMENTS":
                return Attribute.SPAWN_REINFORCEMENTS;
        }
        return null;
        } catch (NoSuchFieldError e) {
            // The exception mentioned is triggered when attempting to return an invalid Attribute enum due to
            // the server this plugin is loaded at not having it in its api (for example, loaded Minecraft 1.8 Server attempts to grab a 1.21 attribute)
            //
            // This IF statement is made to reattempt grabbing a valid attribute by inserting "GENERIC_" at the start. If adding "GENERIC_" still would
            // not provide a valid attribute, just return null.
            if (!string.contains("GENERIC")) return getAttribute("GENERIC_"+string);
            else return null;
        }
    }


    public static void addAttributeOnItemMeta(@NotNull ItemMeta meta, Material itemType, LinkedHashMap<Attribute, AttributeModifier> attributes, boolean keepDefaultItemAttributes, boolean keepExistingAttributes, AttributeAdditionMode mode, boolean affectDefaultAttributes) {

        /* remove ExistingAttributes if needed */
        if (!keepExistingAttributes) {
            SsomarDev.testMsg("REMOVE ALL EXISTING ATTRIBUTES", DEBUG);
            Multimap<Attribute, AttributeModifier> reset = LinkedHashMultimap.create();
            meta.setAttributeModifiers(reset);
        }

        /* remove ExistingAttributes if needed */
        if (!keepDefaultItemAttributes) {
            SsomarDev.testMsg("REMOVE ALL DEFAULT ATTRIBUTES", DEBUG);
            if (!meta.hasAttributeModifiers()) {
                for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                    Multimap<Attribute, AttributeModifier> invert = itemType.getDefaultAttributeModifiers(equipmentSlot);
                    if (invert.isEmpty()) continue;
                    SsomarDev.testMsg("add blank attribute for EquipmentSlot: " + equipmentSlot, DEBUG);
                    for (Attribute col : invert.keySet()) {
                        for (AttributeModifier att : invert.get(col)) {
                            meta.addAttributeModifier(col, new AttributeModifier(att.getUniqueId(), att.getName(), 0, att.getOperation(), att.getSlot()));
                        }
                    }
                }
            }
            //Multimap<Attribute, AttributeModifier> reset = LinkedHashMultimap.create();
            //meta.setAttributeModifiers(reset);
        }

        //if (meta.hasAttributeModifiers()) SsomarDev.testMsg("GET DEFAULT DAMAGE: " + meta.getAttributeModifiers(AttributeUtils.getAttribute("GENERIC_ATTACK_DAMAGE")), DEBUG);



        /* add default attributes if needed */
        if (SCore.is1v19Plus() && keepDefaultItemAttributes) {
            if (meta.hasAttributeModifiers() || !attributes.isEmpty()) {
                for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                    SsomarDev.testMsg("add default attributes for EquipmentSlot: " + equipmentSlot, DEBUG);
                    Multimap<Attribute, AttributeModifier> defaultAttributes = itemType.getDefaultAttributeModifiers(equipmentSlot);
                    for (Attribute col : defaultAttributes.keySet()) {
                        for (AttributeModifier att : defaultAttributes.get(col)) {
                            try {
                                meta.addAttributeModifier(col, att);
                            } catch (IllegalArgumentException e) {
                                SsomarDev.testMsg("No add default attribute because already exist: " + col + " " + att, DEBUG);
                            }
                        }
                    }
                }
            }
        }

        /* override or stack  new attributes */
        for (Attribute att : attributes.keySet()) {

            AttributeModifier attModifier = attributes.get(att);

            if(mode == AttributeAdditionMode.OVERRIDE || mode == AttributeAdditionMode.STACK) {

                EquipmentSlot slot = attModifier.getSlot();
                List<AttributeModifier> toRemove = new ArrayList<>();
                // stack value if needed
                double stackValue = 0;
                NamespacedKey stackKey = null;
                // On supprime l'existant si besoin
                Multimap<Attribute, AttributeModifier> existing = meta.getAttributeModifiers(slot);
                if (existing.containsKey(att)) {
                    for (AttributeModifier modifier : existing.get(att)) {

                        if(!affectDefaultAttributes && modifier.getKey().getKey().contains("base_")) continue;

                        if (mode == AttributeAdditionMode.OVERRIDE) {
                            SsomarDev.testMsg("OVERRIDE ATTRIBUTE: " + modifier, DEBUG);
                            toRemove.add(modifier);
                        }
                        if (mode == AttributeAdditionMode.STACK && modifier.getOperation() == attModifier.getOperation() && modifier.getSlotGroup() == attModifier.getSlotGroup()) {
                            toRemove.add(modifier);
                            stackValue = modifier.getAmount();
                            stackKey = modifier.getKey();
                            SsomarDev.testMsg("STACK ATTRIBUTE found value: " + modifier.getAmount(), DEBUG);
                            break;
                        }
                    }
                }

                for (AttributeModifier remove : toRemove) {
                    meta.removeAttributeModifier(att, remove);
                    SsomarDev.testMsg("REMOVE ATTRIBUTE: " + remove, DEBUG);
                }

                // New value with stack
                if (mode == AttributeAdditionMode.STACK) {
                    if (stackKey == null)
                        stackKey = new NamespacedKey(SCore.pluginHolder, UUID.randomUUID().toString());
                    double newValue = attModifier.getAmount() + stackValue;
                    SsomarDev.testMsg("STACK ATTRIBUTE new value: " + newValue, DEBUG);
                    attModifier = new AttributeModifier(stackKey, newValue, attModifier.getOperation(), attModifier.getSlotGroup());
                }
            }

            meta.addAttributeModifier(att, attModifier);
        }

    }

    /**
     * Used to properly remove a LivingEntity entity's attribute modifier
     * @param entity_arg the LivingEntity that will have its specific attribute removed
     * @param attribute_type the attribute type
     * @param key the attribute modifier's key string (NamespacedKey string for 1.21+, UUID string for older versions)
     */
    public static void removeSpecificAttribute(LivingEntity entity_arg, String attribute_type, String key) {
        LivingEntity entity = entity_arg;

        if (entity == null) return;

        if (entity instanceof Player) {
            entity = Bukkit.getPlayer(entity.getUniqueId());
            if (entity == null) return; // player logged out
        }

        Attribute attribute = AttributeUtils.getAttribute(attribute_type);
        if (attribute == null) return;

        AttributeInstance attrInstance = entity.getAttribute(attribute);
        if (attrInstance == null) return;

        Collection<AttributeModifier> modifiers = attrInstance.getModifiers();
        if (modifiers == null || modifiers.isEmpty()) return;

        for (AttributeModifier modifier : new ArrayList<>(modifiers)) {
            if (modifier == null) continue;

            String modifierKey;

            if (SCore.is1v21Plus()) {
                if (modifier.getKey() == null) continue;
                modifierKey = modifier.getKey().toString();
            } else {
                // For versions below 1.21, use getName() which contains the UUID string
                modifierKey = modifier.getName();
            }
            if (modifierKey != null && modifierKey.equals(key)) {
                attrInstance.removeModifier(modifier);
                return;
            }
        }

    }
}
