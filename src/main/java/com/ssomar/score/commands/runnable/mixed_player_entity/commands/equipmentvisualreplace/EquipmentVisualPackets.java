package com.ssomar.score.commands.runnable.mixed_player_entity.commands.equipmentvisualreplace;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.ssomar.score.SCore;
import com.ssomar.score.utils.scheduler.ScheduledTask;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ProtocolLib side of the equipment visuals, only loaded when ProtocolLib is enabled.
 * While a visual is active, the equipment packets of the entity are rewritten on the fly. The visual is sent again to a
 * player who starts to see the entity, and to the player himself after an inventory packet (his client applies his own
 * equipment packet to his inventory, an inventory packet resets it).
 */
class EquipmentVisualPackets implements EquipmentVisualManager.Packets {

    private static final Map<EquipmentSlot, EnumWrappers.ItemSlot> SLOTS = new EnumMap<>(EquipmentSlot.class);

    static {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            String name = slot.name().equals("HAND") ? "MAINHAND" : slot.name().equals("OFF_HAND") ? "OFFHAND" : slot.name();
            try {
                SLOTS.put(slot, EnumWrappers.ItemSlot.valueOf(name));
            } catch (IllegalArgumentException ignored) {
                /* BODY / SADDLE with an older ProtocolLib */
            }
        }
    }

    /* 1.16+: one packet carries a list of (slot, item) */
    private final boolean pairs = hasPairs();

    /* viewer entity id << 32 | entity id: one pending resend at a time */
    private final Set<Long> pendingResends = ConcurrentHashMap.newKeySet();

    private boolean listening;

    private static boolean hasPairs() {
        try {
            return SCore.protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT).getSlotStackPairLists().size() > 0;
        } catch (Throwable e) {
            return SCore.is1v16Plus();
        }
    }

    @Override
    public synchronized void listen(EquipmentVisualManager manager) {
        if (listening) return;
        listening = true;

        Set<PacketType> inventoryTypes = new HashSet<>();
        inventoryTypes.add(PacketType.Play.Server.SET_SLOT);
        inventoryTypes.add(PacketType.Play.Server.WINDOW_ITEMS);
        /* 1.21.2+, not in the ProtocolLib API SCore is compiled with */
        PacketType setPlayerInventory = serverType("SET_PLAYER_INVENTORY");
        if (setPlayerInventory != null) inventoryTypes.add(setPlayerInventory);

        List<PacketType> types = new ArrayList<>(inventoryTypes);
        types.add(PacketType.Play.Server.ENTITY_EQUIPMENT);
        types.add(PacketType.Play.Server.SPAWN_ENTITY);
        /* Living entities spawn with SPAWN_ENTITY since 1.19, players since 1.20.2 */
        for (String name : new String[]{"SPAWN_ENTITY_LIVING", "NAMED_ENTITY_SPAWN"}) {
            PacketType type = serverType(name);
            if (type != null) types.add(type);
        }

        /* LOW: the visual is in place before the listeners that translate the items of the packets. ASYNC: thread safe, no detour by the main thread */
        SCore.protocolManager.addPacketListener(new PacketAdapter(SCore.plugin, ListenerPriority.LOW, types, ListenerOptions.ASYNC) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (manager.isEmpty() || event.isPlayerTemporary()) return;
                PacketType type = event.getPacketType();
                if (PacketType.Play.Server.ENTITY_EQUIPMENT.equals(type)) {
                    rewrite(event, manager);
                } else if (inventoryTypes.contains(type)) {
                    resendLater(event.getPlayer(), event.getPlayer().getEntityId(), manager);
                } else {
                    /* The player starts to see the entity: its equipment is not sent if the real slot is empty */
                    resendLater(event.getPlayer(), event.getPacket().getIntegers().read(0), manager);
                }
            }
        });
    }

    /* null if this ProtocolLib or this version of the game does not have it */
    private static PacketType serverType(String name) {
        try {
            PacketType type = (PacketType) PacketType.Play.Server.class.getField(name).get(null);
            return type.isSupported() ? type : null;
        } catch (Throwable e) {
            return null;
        }
    }

    @Override
    public void send(int entityId, EquipmentSlot slot, ItemStack item, Collection<Player> receivers) {
        if (receivers.isEmpty()) return;
        EnumWrappers.ItemSlot itemSlot = SLOTS.get(slot);
        if (itemSlot == null) return;
        if (item == null) item = new ItemStack(Material.AIR);
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> list = new ArrayList<>();
        list.add(new Pair<>(itemSlot, item));
        PacketContainer packet = packet(entityId, list);
        for (Player player : receivers) send(player, packet);
    }

    private void rewrite(PacketEvent event, EquipmentVisualManager manager) {
        PacketContainer packet = event.getPacket();
        int entityId = packet.getIntegers().read(0);
        Map<EquipmentSlot, EquipmentVisualManager.Visual> visuals = manager.visualsOf(entityId);
        if (visuals == null) return;

        List<Pair<EnumWrappers.ItemSlot, ItemStack>> list;
        if (pairs) list = packet.getSlotStackPairLists().read(0);
        else {
            list = new ArrayList<>();
            list.add(new Pair<>(packet.getItemSlots().read(0), packet.getItemModifier().read(0)));
        }
        if (list == null) return;

        List<Pair<EnumWrappers.ItemSlot, ItemStack>> rewritten = null;
        for (int i = 0; i < list.size(); i++) {
            ItemStack visual = visualItem(visuals, list.get(i).getFirst());
            if (visual == null) continue;
            if (rewritten == null) rewritten = new ArrayList<>(list);
            rewritten.set(i, new Pair<>(list.get(i).getFirst(), visual));
        }
        /* A new packet: the server sends the same packet object to all the players who see the entity */
        if (rewritten != null) event.setPacket(packet(entityId, rewritten));
    }

    private void resendLater(Player player, int entityId, EquipmentVisualManager manager) {
        if (manager.visualsOf(entityId) == null) return;
        long key = ((long) player.getEntityId() << 32) | (entityId & 0xFFFFFFFFL);
        if (!pendingResends.add(key)) return;
        /* One tick later: after the packet being sent */
        ScheduledTask task = SCore.schedulerHook.runAsyncTask(() -> {
            pendingResends.remove(key);
            Map<EquipmentSlot, EquipmentVisualManager.Visual> visuals = manager.visualsOf(entityId);
            if (visuals == null || !player.isOnline()) return;
            List<Pair<EnumWrappers.ItemSlot, ItemStack>> list = new ArrayList<>();
            for (EquipmentVisualManager.Visual visual : visuals.values()) {
                EnumWrappers.ItemSlot itemSlot = SLOTS.get(visual.slot);
                if (itemSlot != null) list.add(new Pair<>(itemSlot, visual.item));
            }
            if (pairs) {
                if (!list.isEmpty()) send(player, packet(entityId, list));
            } else {
                for (Pair<EnumWrappers.ItemSlot, ItemStack> pair : list) {
                    List<Pair<EnumWrappers.ItemSlot, ItemStack>> one = new ArrayList<>();
                    one.add(pair);
                    send(player, packet(entityId, one));
                }
            }
        }, 1);
        if (task == null) pendingResends.remove(key);
    }

    private static ItemStack visualItem(Map<EquipmentSlot, EquipmentVisualManager.Visual> visuals, EnumWrappers.ItemSlot itemSlot) {
        for (EquipmentVisualManager.Visual visual : visuals.values()) {
            if (SLOTS.get(visual.slot) == itemSlot) return visual.item;
        }
        return null;
    }

    /* Before 1.16 the packet has a single slot: list holds one pair */
    private PacketContainer packet(int entityId, List<Pair<EnumWrappers.ItemSlot, ItemStack>> list) {
        PacketContainer packet = SCore.protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        packet.getIntegers().write(0, entityId);
        if (pairs) packet.getSlotStackPairLists().write(0, list);
        else {
            packet.getItemSlots().write(0, list.get(0).getFirst());
            packet.getItemModifier().write(0, list.get(0).getSecond());
        }
        return packet;
    }

    private static void send(Player player, PacketContainer packet) {
        try {
            SCore.protocolManager.sendServerPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
