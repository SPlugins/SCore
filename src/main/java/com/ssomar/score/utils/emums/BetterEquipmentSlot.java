package com.ssomar.score.utils.emums;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BetterEquipmentSlot {

    public static Optional<EquipmentSlot> getEquipmentSlot(String slot) {
        try{
            return Optional.of(EquipmentSlot.valueOf(slot.toUpperCase()));
        } catch (Exception e) {
            if(slot.equals("-1")) return Optional.of(EquipmentSlot.HAND);
            else if(slot.equals("40")) return Optional.of(EquipmentSlot.OFF_HAND);
            else if(slot.equals("36")) return Optional.of(EquipmentSlot.FEET);
            else if(slot.equals("37")) return Optional.of(EquipmentSlot.LEGS);
            else if(slot.equals("38")) return Optional.of(EquipmentSlot.CHEST);
            else if(slot.equals("39")) return Optional.of(EquipmentSlot.HEAD);
            else return Optional.empty();
        }
    }

    public static boolean isEquipmentSlot(String slot) {
        return getEquipmentSlot(slot).isPresent();
    }
}
