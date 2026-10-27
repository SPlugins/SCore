package com.ssomar.score.features.custom.nbttags;

import de.tr7zw.nbtapi.NBT;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Isolates the {@code NBT.modify()} call (and its transitive dependency on
 * {@code ReadWriteNBT}) into its own class.  Because this class is only
 * loaded when {@code SCore.hasNBTAPI} is {@code true}, servers that do not
 * ship the NBT-API plugin will never trigger a {@code NoClassDefFoundError}
 * for {@code ReadWriteNBT}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NBTTagNBTAPIApplier {

    /**
     * Applys NBTs to items using NBT API plugin
     * @param item the item that will receive the custom nbts
     * @param tags the tags
     */
    public static void applyTags(ItemStack item, List<NBTTag> tags) {
        NBT.modify(item, nbtItem -> {
            for (NBTTag nbtTag : tags) {
                nbtTag.applyTo(nbtItem, true);
            }
        });
    }
}
