package com.ssomar.score.sobject;

import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Caches the editor icon of each SObject: for ExecutableItems getIconItem() is a full
 * buildItem() (heads, PlaceholderAPI, OfflinePlayer lookups...), and the list editors
 * used to rebuild it for every entry on every open / page flip, freezing the main thread
 * for seconds on servers with many objects (SPlugins/SCore#224).
 * Entries are weak so objects recreated on reload are dropped automatically; callers that
 * mutate an object in place must {@link #invalidate(SObject)} it (done on editor save/delete).
 */
public final class SObjectIconCache {

    private static final Map<SObject, ItemStack> CACHE = Collections.synchronizedMap(new WeakHashMap<SObject, ItemStack>());

    private SObjectIconCache() {
    }

    /**
     * @return a clone of the cached icon (never the cached instance, the editors mutate the meta),
     * or null if getIconItem() returned null
     */
    public static ItemStack getIcon(SObject sObject) {
        ItemStack icon = CACHE.get(sObject);
        if (icon == null) {
            icon = ((SObjectEditable) sObject).getIconItem();
            if (icon == null) return null;
            CACHE.put(sObject, icon.clone());
        }
        return icon.clone();
    }

    public static void invalidate(SObject sObject) {
        if (sObject != null) CACHE.remove(sObject);
    }

    public static void clear() {
        CACHE.clear();
    }
}
