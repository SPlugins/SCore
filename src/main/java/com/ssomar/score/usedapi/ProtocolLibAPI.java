package com.ssomar.score.usedapi;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.ssomar.score.SCore;
import com.ssomar.score.SsomarDev;
import com.ssomar.score.config.GeneralConfig;
import com.ssomar.score.utils.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ProtocolLibAPI {

    public static List<ScheduledTask> sendEquipmentVisualReplace(Player player, EquipmentSlot slot, Material material, int amount, int time) {
        return sendEquipmentVisualReplace(player, slot, new ItemStack(material, amount), time);
    }

    public static List<ScheduledTask> sendEquipmentVisualReplace(LivingEntity entity, EquipmentSlot slot, ItemStack item, int time) {
        List<ScheduledTask> tasks = new ArrayList<>();

        if (!Dependency.PROTOCOL_LIB.isEnabled()) return tasks;

        /* Update for other */
        for (int i = 0; i < time ; i++) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    // equip packet
                    PacketContainer packet = SCore.protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
                    // write packet
                    packet.getIntegers().write(0, entity.getEntityId());
                    // make new arraylist
                    List<Pair<EnumWrappers.ItemSlot, ItemStack>> pairList = new ArrayList<>();
                    if (slot.equals(EquipmentSlot.HEAD))
                        pairList.add(new Pair<>(EnumWrappers.ItemSlot.HEAD, item));
                    else if (slot.equals(EquipmentSlot.CHEST))
                        pairList.add(new Pair<>(EnumWrappers.ItemSlot.CHEST, item));
                    else if (slot.equals(EquipmentSlot.LEGS))
                        pairList.add(new Pair<>(EnumWrappers.ItemSlot.LEGS, item));
                    else if (slot.equals(EquipmentSlot.FEET))
                        pairList.add(new Pair<>(EnumWrappers.ItemSlot.FEET, item));
                    else if (slot.equals(EquipmentSlot.HAND))
                        pairList.add(new Pair<>(EnumWrappers.ItemSlot.MAINHAND, item));
                    else if (slot.equals(EquipmentSlot.OFF_HAND))
                        pairList.add(new Pair<>(EnumWrappers.ItemSlot.OFFHAND, item));
                    packet.getSlotStackPairLists().write(0, pairList);

                    // To why it's iterated to all online players is to modify their perception towards the target entity's equipment.
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        try {
                            SCore.protocolManager.sendServerPacket(p, packet);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            tasks.add(SCore.schedulerHook.runAsyncTask(runnable, i));
        }

        // return it back to normal
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                PacketContainer packet = SCore.protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
                packet.getIntegers().write(0, entity.getEntityId());
                List<Pair<EnumWrappers.ItemSlot, ItemStack>> pairList = new ArrayList<>();
                EntityEquipment equipment = entity.getEquipment();
                if(equipment == null) return;
                if (slot.equals(EquipmentSlot.HEAD))
                    pairList.add(get(EnumWrappers.ItemSlot.HEAD, equipment.getHelmet()));
                else if (slot.equals(EquipmentSlot.CHEST))
                    pairList.add(get(EnumWrappers.ItemSlot.CHEST, equipment.getChestplate()));
                else if (slot.equals(EquipmentSlot.LEGS))
                    pairList.add(get(EnumWrappers.ItemSlot.LEGS, equipment.getLeggings()));
                else if (slot.equals(EquipmentSlot.FEET))
                    pairList.add(get(EnumWrappers.ItemSlot.FEET, equipment.getBoots()));
                else if (slot.equals(EquipmentSlot.HAND))
                    pairList.add(get(EnumWrappers.ItemSlot.MAINHAND, equipment.getItemInMainHand()));
                else if (slot.equals(EquipmentSlot.OFF_HAND))
                    pairList.add(get(EnumWrappers.ItemSlot.OFFHAND, equipment.getItemInOffHand()));
                packet.getSlotStackPairLists().write(0, pairList);

                // To why it's iterated to all online players is to modify their perception towards the target entity's equipment.
                for (Player p : Bukkit.getOnlinePlayers()) {
                    try {
                        SCore.protocolManager.sendServerPacket(p, packet);
                        SsomarDev.testMsg("send packet RESET EQUIPMENT", true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        // During tests, using longer durations may cause the clear task to run
        // before the last remaining disguise tasks could finish. To solve that,
        // 2 more ticks are added to the delay time of the clear task.
        tasks.add(SCore.schedulerHook.runTask(runnable, time+2));
        return tasks;
    }

    public static Pair<EnumWrappers.ItemSlot, ItemStack> get(EnumWrappers.ItemSlot slot, ItemStack item) {
        if (item == null) return new Pair<>(slot, new ItemStack(Material.AIR));
        return new Pair<>(slot, item);
    }


    public static void registerParticleToggleListener(java.util.Set<java.util.UUID> hiddenPlayers) {
        SCore.protocolManager.addPacketListener(
                new PacketAdapter(SCore.plugin, ListenerPriority.NORMAL, PacketType.Play.Server.WORLD_PARTICLES) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        Player recipient = event.getPlayer();
                        if (recipient != null && hiddenPlayers.contains(recipient.getUniqueId())) {
                            event.setCancelled(true);
                        }
                    }
                }
        );
    }

    public static void reduceDamageIndicator() {

        if (Dependency.PROTOCOL_LIB.isEnabled() && !SCore.is1v11Less() && GeneralConfig.getInstance().isReduceDamageIndicatorWithProtolcolLib()) {
            try {
                /* LIMIT the particles of damage */
                SCore.protocolManager.addPacketListener(
                        new PacketAdapter(SCore.plugin, ListenerPriority.NORMAL,
                                PacketType.Play.Server.WORLD_PARTICLES) {
                            @Override
                            public void onPacketReceiving(PacketEvent event) {

                            }

                            @Override
                            public void onPacketSending(PacketEvent event) {

                                //SsomarDev.testMsg("Packet sending >> " + event.getPacketType().toString());
                                if (event.getPacketType() == PacketType.Play.Server.WORLD_PARTICLES) {
                                    try {
                                        Particle type = event.getPacket().getNewParticles().read(0).getParticle();
                                        // Item packets (id: 0x29)
                                        if (type.equals(Particle.DAMAGE_INDICATOR)) {
                                            try {
                                                Field privateField = event.getPacket().getHandle().getClass().getDeclaredField("h");

                                                // Set the accessibility as true
                                                privateField.setAccessible(true);
                                                int amount = privateField.getInt(event.getPacket().getHandle());

                                                if (amount > 10) {
                                                    privateField.setInt(event.getPacket().getHandle(), 10);
                                                }
                                            } catch (NoSuchFieldException | IllegalAccessException e) {
                                                //e.printStackTrace();
                                            }

                                        }
                                    } catch (Exception e) {
                                        //Errors IllegalArgumentException can appear for no reason
                                    }
                                }
                            }
                        });
            } catch (Exception | Error ignored) {
            }
        }
    }

}
