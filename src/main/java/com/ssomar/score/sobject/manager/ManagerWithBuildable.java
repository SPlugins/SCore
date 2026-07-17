package com.ssomar.score.sobject.manager;

import com.ssomar.executableblocks.executableblocks.ExecutableBlockObject;
import com.ssomar.executableitems.executableitems.ExecutableItemObject;
import com.ssomar.score.SCore;
import com.ssomar.score.features.custom.variables.real.VariableReal;
import com.ssomar.score.sobject.SObject;
import com.ssomar.score.utils.strings.StringArgExtractor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public interface ManagerWithBuildable<T extends SObject> {

    Optional<SObject> getObject(ItemStack item);

    // %ei_checkamount% count all
    // %ei_checkamount_slot:0,2,3,4%
    // %ei_checkamount_id:item1,item2_slot:0,2,3,4%
    /**
     * Supports the following arg/prefixes:<br/>
     * - <code>slot</code> = ex: -1,0,1,2,3,4<br/>
     * - <code>id</code> = ex: Prem_Tornado_Blade<br/>
     * - <code>owner</code> = ex: %player%<br/>
     * - <code>owneruuid</code> = ex: %player_uuid%<br/>
     * @param instance
     * @param inv
     * @param placeholder
     * @return
     */
    static Optional<String> checkObjectAmountInInvPlaceholder(ManagerWithBuildable instance, PlayerInventory inv, String placeholder){
        if (!placeholder.startsWith("checkamount")) return Optional.empty();

        List<String> searchObject = Arrays.asList(StringArgExtractor.extractArgValue(placeholder, "id"));
        String ownername = StringArgExtractor.extractArgValue(placeholder, "owner").length > 0 ? StringArgExtractor.extractArgValue(placeholder, "owner")[0] : "";
        String owneruuid = StringArgExtractor.extractArgValue(placeholder, "owneruuid").length > 0 ? StringArgExtractor.extractArgValue(placeholder, "owneruuid")[0] : "";
        List<Integer> slotsToCheck = new ArrayList<>();
        for (String val : StringArgExtractor.extractArgValue(placeholder, "slot")) {
            slotsToCheck.add(val.equals("-1") ? inv.getHeldItemSlot() : Integer.parseInt(val));
        }

        int count = checkObjectAmountInInv(instance, inv, slotsToCheck, searchObject, ownername, owneruuid);
        return Optional.of(String.valueOf(count));
    }

    static int checkObjectAmountInInv(ManagerWithBuildable instance, PlayerInventory inv, List<Integer> slotsToCheck, List<String> searchObject, String ownername, String owneruuid){

        int count = 0;

        for (int slotToEval = 0; slotToEval < 41; slotToEval++) {
            if (!(slotsToCheck.isEmpty() || slotsToCheck.contains(slotToEval))) continue;

            ItemStack item = inv.getItem(slotToEval);
            if (item == null) continue;

            Optional<SObject> object = instance.getObject(item);
            if (!object.isPresent()) continue;

            if (!ownername.isEmpty() || !owneruuid.isEmpty()) {
                ItemMeta iM = item.getItemMeta();
                NamespacedKey key = new NamespacedKey(SCore.plugin, "ownerUUID");
                String uuidStr = iM.getPersistentDataContainer().get(key, PersistentDataType.STRING);

                if (uuidStr == null) continue;
                
                String playerUUID = "";
                if (!ownername.isEmpty()) 
                    playerUUID = Bukkit.getPlayer(ownername).getUniqueId().toString();
                else if (!owneruuid.isEmpty())
                    playerUUID = owneruuid;
                
                if (!uuidStr.equals(playerUUID)) continue;
            }
            

            if (searchObject.isEmpty() || searchObject.contains(object.get().getId()))
                count += item.getAmount();
        }

        return count;
    }

    /**
     * "var:" is required to work.<br/>
     * You won't see any usages in this project because it's used by ExecutableItems's and ExecutableBlocks's codebase<br/>
     * <br/>
     * This is a custom placeholder that returns the value of an internal variable within the ItemStack.<br/>
     * If the user provides an array of multiple slot values: <br/>
     * - If the first detected variable value is a string, the value will be returned immediately.<br/>
     * - If the rest of the detected variable value is a number, it will add them all up and return the total value.<br/>
     * - Currently does not support list variables.<br/>
     * <br/>
     * If the user passes multiple values in "var:", it will also add them up<br/>
     * in a "double" variable.<br/>
     * <br/>
     * In scenarios where the plugin user creates 2 unique EI/EB variables that possess the same variable name but not the same
     * datatype, any complications caused by the said scenario will not be considered for future fixes.<br/>
     * <br/>
     * Supports the following arg/prefixes:<br/>
     * - <code>slot</code> = ex: -1,0,1,2,3,4<br/>
     * - <code>id</code> = ex: Prem_Tornado_Blade<br/>
     * - <code>var</code> = ex: bonus<br/>
     * <br/>
     * Sample Usages:<br/>
     * - %executableitems_checkvar_id:star_man_var:defense%<br/>
     * - %executableitems_checkvar_slot:-1,40_var:atk_bonus%<br/>
     * - %executableitems_checkvar_var:defense,bonus_defense%<br/>
     * @param inv
     * @param placeholder
     * @return Value of either extracted String value of sum of all mentioned target slots
     */
    static Optional<String> getObjectVariableValueInInvPlaceholder(ManagerWithBuildable instance, PlayerInventory inv, String placeholder) {
        if (!placeholder.startsWith("checkvar")) {
            return Optional.empty();
        }

        ArrayList<String> searchVariableKey = new ArrayList<>(Arrays.asList(StringArgExtractor.extractArgValue(placeholder, "var")));
        ArrayList<String> searchObject = new ArrayList<>(Arrays.asList(StringArgExtractor.extractArgValue(placeholder, "id")));
        ArrayList<Integer> slotsToCheck = new ArrayList<>();
        for (String val : StringArgExtractor.extractArgValue(placeholder, "slot")) {
            slotsToCheck.add(val.equals("-1") ? inv.getHeldItemSlot() : Integer.parseInt(val));
        }

        // Immediately quit if there's no var key provided.
        if (searchVariableKey.isEmpty()) return Optional.empty();

        double totalNumValue = 0;

        for (int slotToEval = 0; slotToEval<41; slotToEval++) {
            if (!(slotsToCheck.isEmpty() || slotsToCheck.contains(slotToEval))) continue;

            ItemStack itemStack = inv.getItem(slotToEval);
            if (itemStack == null) continue;

            if (SCore.hasExecutableBlocks) {
                ExecutableBlockObject eb = new ExecutableBlockObject(itemStack);
                if (eb.isValid()) {
                    if (!(searchObject.isEmpty() || searchObject.contains(eb.getConfig().getId()))) continue;

                    String extractedValue = extractExecutableBlockObjectVariableDetails(eb, searchVariableKey, slotsToCheck);
                    try {
                        totalNumValue += Double.parseDouble(extractedValue);
                    } catch (Exception exception) {
                        return Optional.of(extractedValue);
                    }
                }
            }

            if (SCore.hasExecutableItems) {
                ExecutableItemObject ei = new ExecutableItemObject(itemStack);
                if (ei.isValid()) {
                    if (!(searchObject.isEmpty() || searchObject.contains(ei.getConfig().getId()))) continue;

                    String extractedValue = extractExecutableItemObjectVariableDetails(ei, searchVariableKey, slotsToCheck);
                    try {
                        totalNumValue += Double.parseDouble(extractedValue);
                    } catch (Exception exception) {
                        return Optional.of(extractedValue);
                    }
                }
            }
        }

        return Optional.of(Double.toString(totalNumValue));
    }

    /**
     * Extract variable value details from an ExecutableItem
     * @param ei
     * @param searchVariableKey
     * @param slotsToCheck
     * @return String/Double/List value in a String
     */
    static String extractExecutableItemObjectVariableDetails(ExecutableItemObject ei, ArrayList<String> searchVariableKey, ArrayList<Integer> slotsToCheck) {
        double totalNumValue = 0;
        ei.loadExecutableItemInfos();
        for (VariableReal vR : ei.getInternalData().getVariableRealsList()) {
            if (!searchVariableKey.contains(vR.getConfig().getVariableName().getValue().get())) continue;

            if (vR.getValue() instanceof Double) {
                if (slotsToCheck.size() == 1)
                    return vR.getValue().toString();
                else
                    totalNumValue += (Double) vR.getValue();
            } else
                return ((String) vR.getValue());
        }
        return Double.toString(totalNumValue);
    }

    /**
     * Extract variable value details from an ExecutableBlock
     * @param eb
     * @param searchVariableKey
     * @param slotsToCheck
     * @return String/Double/List value in a String
     */
    static String extractExecutableBlockObjectVariableDetails(ExecutableBlockObject eb, ArrayList<String> searchVariableKey, ArrayList<Integer> slotsToCheck) {
        double totalNumValue = 0;
        eb.loadExecutableBlockInfos();
        for (VariableReal vR : eb.getInternalData().getVariableRealsList()) {
            if (!searchVariableKey.contains(vR.getConfig().getVariableName().getValue().get())) continue;

            if (vR.getValue() instanceof Double) {
                if (slotsToCheck.size() == 1)
                    return vR.getValue().toString();
                else
                    totalNumValue += (Double) vR.getValue();
            } else
                return ((String) vR.getValue());
        }
        return Double.toString(totalNumValue);
    }




}
