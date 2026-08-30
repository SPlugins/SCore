package com.ssomar.score.commands.runnable.mixed_player_entity.commands.equipmentvisualreplace;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.ssomar.score.SCore;
import com.ssomar.score.utils.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import com.ssomar.score.usedapi.ProtocolLibAPI;

import java.util.*;

public class EquipmentVisualManager {

    private static EquipmentVisualManager instance;

    private Map<String, List<ScheduledTask>> tasks;

    public EquipmentVisualManager() {
        tasks = new HashMap<>();
    }

    public void addTask(UUID player, EquipmentSlot slot, List<ScheduledTask> tasks) {
        if (this.tasks.containsKey(player + String.valueOf(slot))) {
            /* add */
            this.tasks.get(player + String.valueOf(slot)).addAll(tasks);
        }
        else this.tasks.put(player + String.valueOf(slot), tasks);
    }

    public void removeTask(UUID player, EquipmentSlot slot) {
        this.tasks.remove(player + String.valueOf(slot));
    }

    public void cancelTasks(UUID player, EquipmentSlot slot) {
        if (this.tasks.containsKey(player + String.valueOf(slot))) {
            for (ScheduledTask task : this.tasks.get(player + String.valueOf(slot))) {
                task.cancel();
                undoEquipmentVisual(player, slot);
            }
            this.tasks.remove(player + String.valueOf(slot));
        }
    }

    public static EquipmentVisualManager getInstance() {
        if (instance == null) instance = new EquipmentVisualManager();
        return instance;
    }

    /**
     * Upon cancelling the task, we need to undo the visual effect and send the real equipment back to the player.
     * @param playerUUID
     * @param slot
     */
    private void undoEquipmentVisual(UUID playerUUID, EquipmentSlot slot) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) return;

        PacketContainer packet = SCore.protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        packet.getIntegers().write(0, player.getEntityId());
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> pairList = new ArrayList<>();
        EntityEquipment equipment = player.getEquipment();
        if(equipment == null) return;
        if (slot.equals(EquipmentSlot.HEAD))
            pairList.add(ProtocolLibAPI.get(EnumWrappers.ItemSlot.HEAD, equipment.getHelmet()));
        else if (slot.equals(EquipmentSlot.CHEST))
            pairList.add(ProtocolLibAPI.get(EnumWrappers.ItemSlot.CHEST, equipment.getChestplate()));
        else if (slot.equals(EquipmentSlot.LEGS))
            pairList.add(ProtocolLibAPI.get(EnumWrappers.ItemSlot.LEGS, equipment.getLeggings()));
        else if (slot.equals(EquipmentSlot.FEET))
            pairList.add(ProtocolLibAPI.get(EnumWrappers.ItemSlot.FEET, equipment.getBoots()));
        else if (slot.equals(EquipmentSlot.HAND))
            pairList.add(ProtocolLibAPI.get(EnumWrappers.ItemSlot.MAINHAND, equipment.getItemInMainHand()));
        else if (slot.equals(EquipmentSlot.OFF_HAND))
            pairList.add(ProtocolLibAPI.get(EnumWrappers.ItemSlot.OFFHAND, equipment.getItemInOffHand()));
        packet.getSlotStackPairLists().write(0, pairList);

        // To why it's iterated to all online players is to modify their perception towards the target entity's equipment.
        for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
            try {
                SCore.protocolManager.sendServerPacket(onlinePlayers, packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
