package com.ssomar.score.usedapi;

import com.ssomar.score.SCore;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.coreprotect.listener.entity.EntityPickupItemListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;

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
        if (!SCore.hasCoreProtect || itemStack == null || location == null) return;
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("CoreProtect");
        if (!(plugin instanceof CoreProtect) || !plugin.isEnabled()) return;

        CoreProtectAPI coreProtect = ((CoreProtect) plugin).getAPI();
        if (!coreProtect.isEnabled() || coreProtect.APIVersion() < 9) return;

        /* CoreProtectAPI has no method to log an item pickup. EI cancels the pickup event (it adds the item itself), so
         * CoreProtect's MONITOR listener skips it: call the method that listener uses. It checks the item-pickups option,
         * adds the item to the pending list and queues the transaction; public static and unchanged from 22.x to 25.x,
         * while Queue.getItemId (used before) was removed in 25.1. */
        EntityPickupItemListener.onItemPickup(player, location.clone(), itemStack.clone());
    }

}
