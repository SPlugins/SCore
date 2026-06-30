package com.ssomar.score.commands.runnable.player.commands;

import com.ssomar.executableitems.ExecutableItems;
import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.CommandSetting;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.player.PlayerCommand;
import com.ssomar.score.utils.logging.Utils;
import de.tr7zw.nbtapi.NBT;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AddItemNBT extends PlayerCommand {
    public AddItemNBT() {
        CommandSetting slot = new CommandSetting("slot", 0, Integer.class, "0");
        CommandSetting keyValue = new CommandSetting("keyValue", 1, String.class, "foo=bar");
        CommandSetting type = new CommandSetting("type", 2, String.class, "string");
        CommandSetting mode = new CommandSetting("mode", 3, String.class, "pdc");
        List<CommandSetting> settings = getSettings();
        settings.add(slot);
        settings.add(keyValue);
        settings.add(type);
        settings.add(mode);
        setNewSettingsMode(true);
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("ADD_ITEM_NBT");
        return names;
    }

    @Override
    public String getTemplate() {
        return "ADD_ITEM_NBT slot:0 keyValue:foo=bar type:string mode:pdc";
    }

    @Override
    public ChatColor getColor() {
        return null;
    }

    @Override
    public ChatColor getExtraColor() {
        return null;
    }

    @Override
    public void run(Player p, Player receiver, SCommandToExec sCommandToExec) {

        int slot = (int) sCommandToExec.getSettingValue("slot");

        ItemStack item = p.getInventory().getItem(slot);
        if (item == null) return;

        String keyValue = ((String) sCommandToExec.getSettingValue("keyValue"));
        String[] processedKeyValue = keyValue.split("=");

        // Check input if it's valid
        if (processedKeyValue.length != 2 || processedKeyValue[0].isEmpty() || processedKeyValue[1].isEmpty()) {
            Utils.sendConsoleMsg("WARNING: Your keyValue arg value in the ADD_ITEM_NBT execution is incorrect. Reference value: "+keyValue);
            return;
        }

        String type = ((String) sCommandToExec.getSettingValue("type")).toLowerCase();
        String mode = ((String) sCommandToExec.getSettingValue("mode")).toLowerCase();
        if (mode.equals("pdc")) {
            if (SCore.is1v14() || SCore.is1v15() || SCore.is1v16Plus()) {
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                NamespacedKey nsKey = new NamespacedKey(ExecutableItems.getPluginSt(), processedKeyValue[0]);
                switch (type) {
                    case "string":
                        pdc.set(nsKey, PersistentDataType.STRING, processedKeyValue[1]);
                        break;
                    case "double":
                        try {
                            pdc.set(nsKey, PersistentDataType.DOUBLE, Double.parseDouble(processedKeyValue[1]));
                        } catch (Exception e) {
                            Utils.sendConsoleMsg("WARNING: Your keyValue arg value is not suitable for your provided mode. Reference value: "+keyValue+" | "+mode);
                        }
                        break;
                    case "int": {
                        try {
                            pdc.set(nsKey, PersistentDataType.INTEGER, Integer.parseInt(processedKeyValue[1]));
                        } catch (Exception e) {
                            Utils.sendConsoleMsg("WARNING: Your keyValue arg value is not suitable for your provided mode. Reference value: "+keyValue+" | "+mode);
                        }
                        break;
                    }
                    case "boolean":
                    case "bool":
                    {
                        try {
                            pdc.set(nsKey, PersistentDataType.BOOLEAN, Boolean.parseBoolean(processedKeyValue[1]));
                        } catch (Exception e) {
                            Utils.sendConsoleMsg("WARNING: Your keyValue arg value is not suitable for your provided mode. Reference value: "+keyValue+" | "+mode);
                        }
                        break;
                    }

                }
                item.setItemMeta(meta);
            } else {
                Utils.sendConsoleMsg("WARNING: You are trying to execute PDC mode in ADD_ITEM_NBT at the wrong version. This feature is only available at 1.14+");
            }
        } else if (mode.equals("nbtapi")) {
            if (SCore.hasNBTAPI) {
                NBT.modify(item, nbtItem -> {
                    switch (type) {
                        case "string": {
                            nbtItem.setString(processedKeyValue[0], processedKeyValue[1]);
                            return;
                        } case "double": {
                            try {
                                nbtItem.setDouble(processedKeyValue[0], Double.parseDouble(processedKeyValue[1]));
                            } catch (Exception e) {
                                Utils.sendConsoleMsg("WARNING: Your keyValue arg value is not suitable for your provided mode. Reference value: "+keyValue+" | "+mode);
                            }
                            return;
                        }
                        case "int": {
                            try {
                                nbtItem.setInteger(processedKeyValue[0], Integer.valueOf(processedKeyValue[1]));
                            } catch (Exception e) {
                                Utils.sendConsoleMsg("WARNING: Your keyValue arg value is not suitable for your provided mode. Reference value: "+keyValue+" | "+mode);
                            }
                            return;
                        }
                        case "boolean": case "bool": {
                            try {
                                nbtItem.setBoolean(processedKeyValue[0], Boolean.valueOf(processedKeyValue[1]));
                            } catch (Exception e) {
                                Utils.sendConsoleMsg("WARNING: Your keyValue arg value is not suitable for your provided mode. Reference value: "+keyValue+" | "+mode);
                            }
                            return;
                        }
                    }
                });
            } else {
                Utils.sendConsoleMsg("WARNING: You are trying to use mode \"nbtapi\" while having NBT API plugin not installed. Please install NBT API before using this mode.");
            }
        }
    }
}
