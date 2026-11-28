package com.ssomar.score.hardness;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.ssomar.score.SCore;
import com.ssomar.score.hardness.hardness.manager.HardnessesManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class HardnessesHandler {

    //public static final List<HardnessModifier> MODIFIERS = new ArrayList<>();
    // filled from the Netty thread (packet listener), read by the scheduler tasks
    private final Map<Location, BukkitScheduler> breakerPerLocation = new ConcurrentHashMap<>();
    private final ProtocolManager protocolManager;
    private final PacketAdapter listener = new PacketAdapter(SCore.plugin, ListenerPriority.LOW, PacketType.Play.Client.BLOCK_DIG) {
        @Override
        public void onPacketReceiving(final PacketEvent event) {
            final PacketContainer packet = event.getPacket();
            final Player player = event.getPlayer();
            ItemStack itemNotFinal = null;
            if (SCore.is1v12Less()) itemNotFinal = player.getInventory().getItemInHand();
            else itemNotFinal = player.getInventory().getItemInMainHand();
            final ItemStack item = itemNotFinal;
            if (player.getGameMode() == GameMode.CREATIVE) return;

            final StructureModifier<BlockPosition> dataTemp = packet.getBlockPositionModifier();
            final StructureModifier<EnumWrappers.Direction> dataDirection = packet.getDirections();
            final StructureModifier<EnumWrappers.PlayerDigType> data = packet
                    .getEnumModifier(EnumWrappers.PlayerDigType.class, 2);
            EnumWrappers.PlayerDigType type;

            try {
                type = data.getValues().get(0);
            } catch (IllegalArgumentException exception) {
                type = EnumWrappers.PlayerDigType.SWAP_HELD_ITEMS;
            }

            /*
             * BLOCK_DIG also carries drop item / drop stack / swap hands / release use item, with the position 0,0,0. Reading that
             * block from the Netty thread loads its chunk when it is not loaded (1.21.9+: spawn chunks are no longer kept), the
             * Netty thread waits for the main thread, which can wait for Netty: the server freezes (qvazzar, 28/11). Only the
             * digging types are about a block, and a block is only read if its chunk is already loaded.
             */
            if (type != EnumWrappers.PlayerDigType.START_DESTROY_BLOCK && type != EnumWrappers.PlayerDigType.ABORT_DESTROY_BLOCK
                    && type != EnumWrappers.PlayerDigType.STOP_DESTROY_BLOCK) return;

            final BlockPosition pos = dataTemp.getValues().get(0);
            final World world = player.getWorld();
            if (!world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) return;
            final Block block = world.getBlockAt(pos.getX(), pos.getY(), pos.getZ());
            final BlockFace blockFace = dataDirection.size() > 0 ? BlockFace.valueOf(dataDirection.read(0).name()) : BlockFace.UP;

            HardnessModifier triggeredModifier = null;
            for (final HardnessModifier modifier : HardnessesManager.getInstance().getAllObjects())
                if (modifier.isEnabled() && modifier.isTriggered(player, block, item)) {
                    triggeredModifier = modifier;
                    break;
                }
            if (triggeredModifier == null) return;
            long period = triggeredModifier.getPeriod(player, block, item);
            //if (period == 0) return;

            Enchantment enchantment = SCore.is1v20v5Plus() ? Enchantment.EFFICIENCY : Enchantment.getByName("DIG_SPEED");
            switch (item.getEnchantmentLevel(enchantment)){
                case 0:
                    break;
                case 1:
                    period = (long) (period*0.75);
                    break;
                case 2:
                    period = (long) (period*0.70);
                    break;
                case 3:
                    period = (long) (period*0.65);
                    break;
                case 4:
                    period = (long) (period*0.60);
                    break;
                case 5:
                    period = (long) (period*0.55);
                    break;
                default:
                    period = (long) (period*0.55);
                    break;
            }

            final long finalPeriod = period;
            final Location location = block.getLocation();
            final HardnessModifier modifier = triggeredModifier;

            //SsomarDev.testMsg("period: " + period + "block tyep "+block.getType(), true);
            event.setCancelled(true);
            if (finalPeriod <= 0){
                Bukkit.getScheduler().runTask(SCore.plugin, () -> breakBlock(location, world, block, player, item, modifier, null));
            }

            int delay = 20;
            if(modifier.isPeriodInTicks()) delay = 1;
            final int delayFinal = delay;

            if (type == EnumWrappers.PlayerDigType.START_DESTROY_BLOCK) {

                //SsomarDev.testMsg("START_DESTROY_BLOCK", true);
                Bukkit.getScheduler().runTask(SCore.plugin, () -> player.addPotionEffect(new PotionEffect(getSlowType(), (int) (finalPeriod * delayFinal)+1, Integer.MAX_VALUE, false, false, false)));
                if (breakerPerLocation.containsKey(location))
                    breakerPerLocation.get(location).cancelTasks(SCore.plugin);

                final BukkitScheduler scheduler = Bukkit.getScheduler();
                // Cancellation state is being ignored.
                // However still needs to be called for plugin support.
                final PlayerInteractEvent playerInteractEvent = new PlayerInteractEvent(player, Action.LEFT_CLICK_BLOCK, player.getInventory().getItemInMainHand(), block, blockFace, EquipmentSlot.HAND);
                scheduler.runTask(SCore.plugin, () -> Bukkit.getPluginManager().callEvent(playerInteractEvent));

                // If the relevant damage event is cancelled, return
                if (blockDamageEventCancelled(block, player)) return;

                breakerPerLocation.put(location, scheduler);

                //SsomarDev.testMsg("before runTaskTimer", true);
                scheduler.runTaskTimer(SCore.plugin, new Consumer<BukkitTask>() {
                    int value = 0;

                    @Override
                    public void accept(final BukkitTask bukkitTask) {
                         //SsomarDev.testMsg("accept runTaskTimer > "+value+" time >"+System.currentTimeMillis(), true);

                        if (!breakerPerLocation.containsKey(location)) {
                            bukkitTask.cancel();
                            return;
                        }

                        for (final Entity entity : world.getNearbyEntities(location, 16, 16, 16))
                            if (entity instanceof Player) {
                                Player viewer = (Player) entity;
                                int stage = (int) (value*10/finalPeriod);
                                //SsomarDev.testMsg("stage > "+stage, true);
                                sendBlockBreak(viewer, location, stage);
                            }

                        if (value++ < finalPeriod) return;

                        breakBlock(location, world, block, player, item, modifier, bukkitTask);
                    }
                }, 0, delayFinal);
            } else {
                Bukkit.getScheduler().runTask(SCore.plugin, () -> {
                    player.removePotionEffect(getSlowType());
                    /* if (!ProtectionLib.canBreak(player, block.getLocation()))
                        player.sendBlockChange(block.getLocation(), block.getBlockData()); */

                    for (final Entity entity : world.getNearbyEntities(location, 16, 16, 16))
                        if (entity instanceof Player) {
                            Player viewer = (Player) entity;
                            sendBlockBreak(viewer, location, 10);
                        }
                    breakerPerLocation.remove(location);
                });
            }
        }
    };

    public PotionEffectType getSlowType(){
        return SCore.is1v20v5Plus() ? PotionEffectType.MINING_FATIGUE : PotionEffectType.getByName("SLOW_DIGGING");
    }

    public void breakBlock(Location location, World world, Block block, Player player, ItemStack item, HardnessModifier modifier, BukkitTask bukkitTask){
        final BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        Bukkit.getPluginManager().callEvent(blockBreakEvent);

        if (!blockBreakEvent.isCancelled() /* && ProtectionLib.canBreak(player, block.getLocation()) */) {
            modifier.breakBlock(player, block, item);
            PlayerItemDamageEvent playerItemDamageEvent = new PlayerItemDamageEvent(player, item, 1);
            Bukkit.getPluginManager().callEvent(playerItemDamageEvent);
        }

        Bukkit.getScheduler().runTask(SCore.plugin, () ->
                player.removePotionEffect(getSlowType()));
        breakerPerLocation.remove(location);
        for (final Entity entity : world.getNearbyEntities(location, 16, 16, 16))
            if (entity instanceof Player) {
                Player viewer = (Player) entity;
                sendBlockBreak(viewer, location, 10);
            }
        if(bukkitTask != null) bukkitTask.cancel();
    }

    public HardnessesHandler() {
        protocolManager = SCore.protocolManager;
        //MODIFIERS.add(new RealHardnessModifier());
    }

    private boolean blockDamageEventCancelled(Block block, Player player) {
        return false;
    }

    private void sendBlockBreak(final Player player, final Location location, final int stage) {
        Block block = location.getBlock();
        final PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
        packet.getIntegers().write(0, location.hashCode()).write(1, stage);
        packet.getBlockPositionModifier().write(0, new BlockPosition(location.toVector()));

        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerListener() {
        protocolManager.addPacketListener(listener);
    }

    private String getSound(Block block) {
        return block.getBlockData().getSoundGroup().getHitSound().getKey().toString();
    }
}
