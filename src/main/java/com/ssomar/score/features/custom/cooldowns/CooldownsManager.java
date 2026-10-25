package com.ssomar.score.features.custom.cooldowns;

import com.ssomar.executableitems.ExecutableItems;
import com.ssomar.score.SCore;
import com.ssomar.score.SsomarDev;
import com.ssomar.score.splugin.SPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class CooldownsManager {

    private static final boolean DEBUG = true;

    private static CooldownsManager instance;

    /*
     * The maps are read by the async cooldown display sweep while the main/region threads add,
     * expire and clear cooldowns. Plain HashMap/ArrayList threw ConcurrentModificationException in
     * that sweep, so: concurrent maps, copy-on-write lists (write-light, iterate-heavy).
     * COW iterators do not support remove(): every mutation below goes through removeIf / remove(Object).
     */

    /* CD_ID Cooldown */
    private final Map<String, List<Cooldown>> cooldowns = new ConcurrentHashMap<>();

    /* Player_UUID Cooldown */
    private final Map<UUID, List<Cooldown>> cooldownsUUID = new ConcurrentHashMap<>();

    public static CooldownsManager getInstance() {
        if (instance == null) instance = new CooldownsManager();
        return instance;
    }

    public Optional<String> onRequestPlaceholder(OfflinePlayer player, String params) {
        if (params.startsWith("cooldown_")) {
            UUID uuid = player.getUniqueId();

            boolean castInt = false;
            boolean castMax = false;
            String cooldownId = "";

            // Find item slot to include below
            if (params.contains("cooldown_slot")) {
                // if (!SCore.hasNBTAPI) {
                //     return Optional.of("You need NBT API!");
                // }
                // Extract the slot number using regex
                String slotNumber = params.replaceFirst(".*cooldown_slot(\\d+).*", "$1");
                int slot = Integer.parseInt(slotNumber);

                // Retrieve equipment for the slot
                if (player.isOnline() && player.getPlayer() != null) {
                    ItemStack item = player.getPlayer().getInventory().getItem(slot);
                    if (item != null) {
                        if (item.hasItemMeta() && player != null) {
                            ItemMeta iM = item.getItemMeta();

                            NamespacedKey key = new NamespacedKey(ExecutableItems.getPluginSt(), "ei-id");
                            String eiid = iM.getPersistentDataContainer().get(key, PersistentDataType.STRING);

                            if (eiid != null) {
                                cooldownId += "EI:" + eiid + ":";
                                // get rid of the slot
                                params = params.replaceFirst("slot\\d+:", "");
                            } else {
                                return Optional.of("Not an Executable Item: " + item.getItemMeta().getDisplayName());
                            }
                        } else {
                            return Optional.of("No ItemMeta found or player is null");
                        }
                    } else {
                        return Optional.of("No Item in specified slot");
                    }
                } else {
                    return Optional.of("Player Offline");
                }

            }

            // Player requested int, toggle and remove from query.
            if (params.contains("_int")) {
                castInt = true;
                params = params.replaceFirst("_int", "");
            }
            if (params.contains("_timemax")) {
                castMax = true;
                params = params.replaceFirst("_timemax", "");
            }

            // Parse as normal, minor refactor so it's easier to read
            cooldownId += params.split("cooldown_")[1];
            SsomarDev.testMsg("CD ID " + cooldownId, DEBUG);
            Optional<Cooldown> cooldownOpt = getCooldown(cooldownId, uuid, false);
            if (cooldownOpt.isPresent()) {
                SsomarDev.testMsg("CD " + cooldownOpt.get().toString(), DEBUG);
                Cooldown cd = cooldownOpt.get();
                if (castInt && castMax) return Optional.of((int) cd.getCooldown() + "");
                else if (castMax) return Optional.of(cd.getCooldown() + "");
                else if (castInt) return Optional.of((int) cd.getTimeLeft() + "");
                else return Optional.of(cd.getTimeLeft() + "");
            }
            return Optional.of("0");
        }
        return Optional.empty();
    }

    public void addCooldown(Cooldown cd) {

        //SsomarDev.testMsg("ADD COOLDOWN " + cd.toString(), true);
        if (cd.getCooldown() == 0) return;

        String id = cd.getId();
        SsomarDev.testMsg("ADDDD " + cd.toString(), DEBUG);
        if (id == null) return;
        cooldowns.computeIfAbsent(id, k -> new CopyOnWriteArrayList<>()).add(cd);

        UUID id2 = cd.getEntityUUID();
        if (id2 == null) return;
        cooldownsUUID.computeIfAbsent(id2, k -> new CopyOnWriteArrayList<>()).add(cd);
    }

    /* FROM DB */
    public void addCooldowns(List<Cooldown> cds) {
        for (Cooldown cd : cds) {
            double timeLeft = cd.getTimeLeft();
            if (timeLeft > 0) this.addCooldown(cd);
        }
    }

    public Optional<Cooldown> getCooldown(String id, UUID uuid, boolean onlyGlobal) {
        return getCooldown(SCore.plugin, id, uuid, onlyGlobal);
    }

    public Optional<Cooldown> getCooldown(SPlugin sPlugin, String id, UUID uuid, boolean onlyGlobal) {

        //SsomarDev.testMsg("GET COOLDOWN "+id+" "+uuid, DEBUG);
        if (cooldowns.containsKey(id)) {
            double maxTimeLeft = -1;
            List<Cooldown> cds = cooldowns.get(id);
            if (cds != null && !cds.isEmpty()) {
                Cooldown cdMax = null;
                // Iterate a snapshot (COW iterator) and remove by object: index-based removal is not
                // safe when another thread changes the list in between.
                for (Cooldown cd : cds) {
                    if (cd == null) {
                        //SsomarDev.testMsg("CD NULL", true);
                        continue;
                    }
                    if (onlyGlobal && !cd.isGlobal()) {
                        //SsomarDev.testMsg("CD NOT GLOBAL", true);
                        continue;
                    }
                    if (!cd.isGlobal() && !cd.getEntityUUID().equals(uuid)) {
                        //SsomarDev.testMsg("CD NOT GLOBAL AND NOT EQUAL UUID", true);
                        continue;
                    }

                    double timeLeft = cd.getTimeLeft();

                    //SsomarDev.testMsg("CD valid >> "+timeLeft, true);
                    if (maxTimeLeft < timeLeft && timeLeft > 0) {
                        maxTimeLeft = timeLeft;
                        cdMax = cd;
                    } else {
                        cd.setNull(true);
                        cds.remove(cd);
                        UUID owner = cd.getEntityUUID() != null ? cd.getEntityUUID() : uuid;
                        if (owner != null) {
                            List<Cooldown> uuidCds = cooldownsUUID.get(owner);
                            if (uuidCds != null) uuidCds.remove(cd);
                        }
                    }
                }
                //if(cdMax != null) SsomarDev.testMsg("COOLDOWN "+cdMax.toString(), DEBUG);
                //else SsomarDev.testMsg("NO COOLDOWN ", DEBUG);
                return Optional.ofNullable(cdMax);
            }
        }
        SsomarDev.testMsg("NO COOLDOWN ", DEBUG);
        return Optional.empty();
    }

    public List<Cooldown> getCooldownsOf(UUID uuid) {
        if (cooldownsUUID.containsKey(uuid)) {
            List<Cooldown> cds = cooldownsUUID.get(uuid);
            if (cds == null) return new ArrayList<>();
            List<Cooldown> result = new ArrayList<>();
            for (Cooldown cd : cds) {
                if (cd != null && !cd.isNull()) result.add(cd);
            }
            return result;
        } else return new ArrayList<>();
    }

    public List<Cooldown> getAllCooldowns() {
        List<Cooldown> result = new ArrayList<>();

        for (String id : cooldowns.keySet()) {
            List<Cooldown> cds = cooldowns.get(id);
            if (cds == null) continue;
            for (Cooldown cd : cds) {
                if (cd != null && !cd.isNull()) result.add(cd);
            }
        }

        return result;
    }

    public void clearCooldowns() {
        cooldowns.clear();
        cooldownsUUID.clear();
    }

    /**
     * Cleans up expired cooldowns and removes empty lists from the maps.
     * This method should be called periodically to prevent memory leaks.
     */
    public void cleanupExpiredCooldowns() {
        // Clean up cooldowns map
        Iterator<Map.Entry<String, List<Cooldown>>> cooldownsIterator = cooldowns.entrySet().iterator();
        while (cooldownsIterator.hasNext()) {
            Map.Entry<String, List<Cooldown>> entry = cooldownsIterator.next();
            List<Cooldown> cds = entry.getValue();
            if (cds == null) {
                cooldownsIterator.remove();
                continue;
            }
            cds.removeIf(cd -> {
                if (cd != null && !cd.isNull() && cd.getTimeLeft() > 0) return false;
                if (cd != null) {
                    cd.setNull(true);
                    // Also remove from cooldownsUUID
                    if (cd.getEntityUUID() != null) {
                        List<Cooldown> uuidCds = cooldownsUUID.get(cd.getEntityUUID());
                        if (uuidCds != null) uuidCds.remove(cd);
                    }
                }
                return true;
            });
            // Remove empty lists from the map, atomically: a cooldown added by another thread
            // between the check and the removal must not be dropped with its list.
            if (cds.isEmpty()) {
                cooldowns.computeIfPresent(entry.getKey(), (k, v) -> v.isEmpty() ? null : v);
            }
        }

        // Clean up cooldownsUUID map - remove empty lists
        Iterator<Map.Entry<UUID, List<Cooldown>>> uuidIterator = cooldownsUUID.entrySet().iterator();
        while (uuidIterator.hasNext()) {
            Map.Entry<UUID, List<Cooldown>> entry = uuidIterator.next();
            List<Cooldown> cds = entry.getValue();
            if (cds == null) {
                uuidIterator.remove();
                continue;
            }
            // Also filter out null entries from UUID lists
            cds.removeIf(cd -> cd == null || cd.isNull());
            if (cds.isEmpty()) {
                cooldownsUUID.computeIfPresent(entry.getKey(), (k, v) -> v.isEmpty() ? null : v);
            }
        }
    }

    public void removeCooldownsOf(UUID uuid) {
        //SsomarDev.testMsg("REMOVE COOLDOWNS OF "+uuid, DEBUG);
        cooldownsUUID.remove(uuid);
        for (String s : cooldowns.keySet()) {
            List<Cooldown> cds = cooldowns.get(s);
            if (cds == null) continue;
            cds.removeIf(cd -> {
                if (cd != null && cd.getEntityUUID() != null && cd.getEntityUUID().equals(uuid)) {
                    cd.setNull(true);
                    return true;
                }
                return false;
            });
        }
    }

    public void clearCooldown(String cooldownId, @Nullable UUID uuid) {
        for (String s : cooldowns.keySet()) {
            List<Cooldown> cds = cooldowns.get(s);
            if (cds == null) continue;
            for (Cooldown cd : cds) {
                if (cd != null && cd.getId().equalsIgnoreCase(cooldownId)) {
                    if (uuid != null && (cd.getEntityUUID() == null || !cd.getEntityUUID().equals(uuid))) continue;
                    if (cd.getEntityUUID() != null) {
                        List<Cooldown> uuidCds = cooldownsUUID.get(cd.getEntityUUID());
                        if (uuidCds != null) uuidCds.remove(cd);
                    }
                    cd.setNull(true);
                    cds.remove(cd);
                    return;
                }
            }
        }
    }

    public List<String> getAllCooldownIds() {
        List<String> result = new ArrayList<>();
        result.addAll(cooldowns.keySet());
        return result;
    }


    public void printInfo() {
        System.out.println("--------^^^^^^^^^^^^^^^^^^^^-------------");


        int total = 0;
        for (String s : cooldowns.keySet()) {
            List<Cooldown> cds = cooldowns.get(s);
            total += cds.size();
        }
        System.out.println(" total : " + total);

        total = 0;
        for (UUID s : cooldownsUUID.keySet()) {
            List<Cooldown> cds = cooldownsUUID.get(s);
            total += cds.size();
        }
        System.out.println(" total : " + total);


        for (String s : cooldowns.keySet()) {
            for (Cooldown cd2 : cooldowns.get(s)) {
                if (cd2 != null) System.out.println(cd2.toString());
                else System.out.println("null");
            }
        }

        System.out.println("----------------------");

        for (UUID uuid2 : cooldownsUUID.keySet()) {
            for (Cooldown cd2 : cooldownsUUID.get(uuid2)) {
                if (cd2 != null) System.out.println(cd2.toString());
                else System.out.println("null");
            }
        }

        System.out.println("---------vvvvvvvvvvvvvvvvv-------------");
    }

}

/* Reader */
//System.out.println("--------^^^^^^^^^^^^^^^^^^^^1-------------");
//
//for(String s : cooldowns.keySet()) {
//	for(Cooldown cd2 : cooldowns.get(s)) {
//		System.out.println(cd2.toString());
//	}
//}
//
//System.out.println("----------------------");
//
//for(UUID uuid2 : cooldownsUUID.keySet()) {
//	for(Cooldown cd2 : cooldownsUUID.get(uuid2)) {
//		System.out.println(cd2.toString());
//	}
//}
//
//System.out.println("---------vvvvvvvvvvv1vvvvvv-------------");
