package com.ssomar.score.usedapi;

import com.ssomar.score.SCore;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Queue;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MyCoreProtectAPI {

    public static void logRemoval(String user, Location location, Material type, BlockData blockData){
        if (SCore.hasCoreProtect) {
            Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("CoreProtect");

            if (!plugin.isEnabled()) return;

            // Check that CoreProtect is loaded
            if (!(plugin instanceof CoreProtect)) {
                return;
            }

            CoreProtectAPI CoreProtect = ((CoreProtect) plugin).getAPI();
            if (!CoreProtect.isEnabled()) {
                return;
            }

            // Check that a compatible version of the API is loaded
            if (CoreProtect.APIVersion() < 9) {
                return;
            }

            CoreProtect.logRemoval(user, location, type, blockData);
        }
    }

    public static boolean isNaturalBlock(Block block) {
        if (SCore.hasCoreProtect) {
            Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("CoreProtect");

            if (!plugin.isEnabled()) return false;

            // Check that CoreProtect is loaded
            if (!(plugin instanceof CoreProtect)) {
                return false;
            }

            CoreProtectAPI CoreProtect = ((CoreProtect) plugin).getAPI();
            if (!CoreProtect.isEnabled()) {
                return false;
            }

            // Check that a compatible version of the API is loaded
            if (CoreProtect.APIVersion() < 9) {
                return false;
            }

            List<String[]> list = CoreProtect.blockLookup(block, 2592000);

            return list.size() == 0;
        }

        return false;
    }

    public void addPickup(Location location, ItemStack itemStack, Player player) {
        /* Logging uses CoreProtect internals (Queue, ConfigHandler) that change between CoreProtect versions. The item is already in the
         * player's inventory when this runs: if it threw, the pickup was cut short and the item stayed on the ground too (dupe,
         * Discord 1558753195352789063). Logging must never break the pickup. */
        if (pickupLoggingBroken) return;
        try {
            addPickupUnsafe(location, itemStack, player);
        } catch (Throwable e) {
            pickupLoggingBroken = true;
            SCore.plugin.getLogger().warning("CoreProtect pickup logging disabled: this CoreProtect version is not compatible (" + e + ")");
        }
    }

    private static boolean pickupLoggingBroken = false;

    private void addPickupUnsafe(Location location, ItemStack itemStack, Player player) {
        if (SCore.hasCoreProtect) {
            Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("CoreProtect");

            if (plugin != null && !plugin.isEnabled()) return;

            // Check that CoreProtect is loaded
            if (!(plugin instanceof CoreProtect)) {
                return;
            }

            CoreProtectAPI CoreProtect = ((CoreProtect) plugin).getAPI();
            if (!CoreProtect.isEnabled()) {
                return;
            }

            // Check that a compatible version of the API is loaded
            if (CoreProtect.APIVersion() < 9) {
                return;
            }

            if (!(Config.getConfig(location.getWorld())).ITEM_PICKUPS)
                return;

            if (itemStack == null)
                return;
            String loggingItemId = player.getName().toLowerCase(Locale.ROOT) + "." + location.getBlockX() + "." + location.getBlockY() + "." + location.getBlockZ();
            int itemId = new Register().getItem(loggingItemId);
            List<ItemStack> list = (List<ItemStack>) ConfigHandler.itemsPickup.getOrDefault(loggingItemId, new ArrayList());
            list.add(itemStack.clone());
            ConfigHandler.itemsPickup.put(loggingItemId, list);
            int time = (int) (System.currentTimeMillis() / 1000L) + 1;
            new Register().addItemTransaction(player, location.clone(), time, itemId);
        }

    }

    static class Register extends Queue {
        public Register() {

        }

        public static int getItem(String loggingItemId) {
            return getItemId(loggingItemId);
        }

        public void addItemTransaction(Player player, Location location, int time, int itemId) {
            Queue.queueItemTransaction(player.getName(), location.clone(), time, 0, itemId);
        }
    }

}
