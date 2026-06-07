package com.ssomar.score.commands.score;

import com.ssomar.executableitems.configs.Message;
import com.ssomar.score.api.executableitems.events.AddItemInPlayerInventoryEvent;
import com.ssomar.score.commands.runnable.ActionInfo;
import com.ssomar.score.commands.runnable.CommandsHandler;
import com.ssomar.score.commands.runnable.player.PlayerRunCommand;
import com.ssomar.score.configs.messages.MessageMain;
import com.ssomar.score.sobject.SObject;
import com.ssomar.score.sobject.SObjectBuildable;
import com.ssomar.score.sobject.SObjectManager;
import com.ssomar.score.sobject.manager.ManagerWithBuildable;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.numbers.NTools;
import com.ssomar.score.utils.placeholders.StringPlaceholder;
import com.ssomar.score.utils.strings.StringConverter;
import com.ssomar.score.utils.strings.StringSetting;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

// must have in the config
// pickupLimit: 1000
// giveLimit: 1000
// silentGive: false


public class GiveCommand<X extends SPlugin, Y extends SObjectManager<Z>, Z extends SObject & SObjectBuildable> extends CustomCommandWithManagerAbstract<X, Y, Z> {


    public GiveCommand(X sPlugin, Y sObjectManager) {
        super(sPlugin, sObjectManager);
    }

    /**
     * Give an ExecutableItem to a player
     *
     * @param sender                        The sender of the command
     * @param objectId
     * @param amount
     * @param player                        , if empty it will give the item to all players on the server
     * @param settings
     * @param giveOfflinePlayer             if the player is offline
     *                                      if true, the item will be given to the player when he will connect
     *                                      if false, the item will not be given to the player
     * @param commandToRunIfPlayerIsOffline the command to run if the player is offline (the give command)
     */
    public void give(CommandSender sender, String objectId, String amount, String player, Map<String, Object> settings, String world, boolean giveOfflinePlayer, String commandToRunIfPlayerIsOffline) {
        Optional<Z> objectOpt = checkSObject(sender, objectId);
        if (!objectOpt.isPresent()) return;
        Optional<Integer> amountOpt = checkAmount(sender, amount);
        if (!amountOpt.isPresent()) return;
        Optional<Optional<Player>> playerOpt = checkPlayer(sender, player, giveOfflinePlayer);
        Optional<Optional<World>> worldOpt = checkWorld(sender, world);

        if (!worldOpt.isPresent()) {
            return;

        } else if (!playerOpt.isPresent()) {
            runOfflineCommand(player, giveOfflinePlayer, commandToRunIfPlayerIsOffline);
            return;
        }

        give(playerOpt.get(), objectOpt.get(), amountOpt.get(), settings, worldOpt.get());

        if (!getSPlugin().getPluginConfig().getBooleanSetting("silentGive"))
            getSm().sendMessage(sender, getSPlugin().getNameDesign() + " &a" + getSObjectManager().getObjectName() + " &e" + objectId + " &agiven to &e" + player + " &a(&7" + amount + "&a)");
    }

    public static void runOfflineCommand(String player, boolean giveOfflinePlayer, String commandToRunIfPlayerIsOffline) {
        //System.out.println("runOfflineCommand >> "+player+" "+giveOfflinePlayer+" "+commandToRunIfPlayerIsOffline);
        OfflinePlayer p = Bukkit.getOfflinePlayer(player);
        if (giveOfflinePlayer) {
            ActionInfo aInfo = new ActionInfo("", new StringPlaceholder());
            aInfo.setReceiverUUID(p.getUniqueId());
            PlayerRunCommand command = new PlayerRunCommand(commandToRunIfPlayerIsOffline, 0, aInfo);
            CommandsHandler handler = CommandsHandler.getInstance();
            if (handler.getDelayedCommandsSaved().containsKey(p.getUniqueId())) {
                //System.out.println("runOfflineCommand >> pUUID"+p.getUniqueId()+" ADDDD "+command);
                handler.getDelayedCommandsSaved().get(p.getUniqueId()).add(command);
            } else{
                //System.out.println("runOfflineCommand >> pUUID"+p.getUniqueId()+" NEW "+command);
                handler.getDelayedCommandsSaved().put(p.getUniqueId(), new ArrayList<>(Arrays.asList(command)));
            }
        }
    }

    public void give(Optional<Player> playerOpt, Z object, int amount, Map<String, Object> settings, Optional<World> worldOpt) {
        if (playerOpt.isPresent()) {
            multipleGive(playerOpt.get(), object, amount, settings);
        } else {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (worldOpt.isPresent()) {
                    if (p.getWorld().equals(worldOpt.get())) {
                        multipleGive(p, object, amount, settings);
                    }
                } else {
                    multipleGive(p, object, amount, settings);
                }
            }
        }
    }


    /**
     * Give an ExecutableItem to a player
     *
     * @param sender         The sender of the command
     * @param executableItem
     * @param amount
     * @param player         , if empty it will give the item to all players on the server
     * @param settings
     */
    public void giveSlot(CommandSender sender, String executableItem, String amount, String player, Map<String, Object> settings, String slot, String overrideStr, boolean giveOfflinePlayer, String commandToRunIfPlayerIsOffline) {
        Optional<Z> objectOpt = checkSObject(sender, executableItem);
        if (!objectOpt.isPresent()) return;
        Optional<Integer> amountOpt = checkAmount(sender, amount);
        if (!amountOpt.isPresent()) return;
        Optional<Integer> slotOpt = checkSlot(sender, slot);
        if (!slotOpt.isPresent()) return;
        Optional<Optional<Player>> playerOpt = checkPlayer(sender, player, giveOfflinePlayer);
        boolean override = Boolean.parseBoolean(overrideStr);
        //SsomarDev.testMsg("OVER: "+override+ " str: "+overrideStr);

        if (!playerOpt.isPresent()) {
            runOfflineCommand(player, giveOfflinePlayer, commandToRunIfPlayerIsOffline);
            return;
        }

        if (playerOpt.get().isPresent()) {
            Player p = playerOpt.get().get();
            PlayerInventory inventory = p.getInventory();
            boolean callAddInInventoryEvent = false;
            int slotInt = slotOpt.get();
            if(slotInt == -1) slotInt = inventory.getHeldItemSlot();
            if (inventory.getItem(slotInt) == null) {
                inventory.setItem(slotInt, objectOpt.get().buildItem(amountOpt.get(), playerOpt.get(), settings));
                callAddInInventoryEvent = true;
            } else {
                ItemStack item = inventory.getItem(slotInt).clone();
                ItemStack toCompare = item.clone();
                toCompare.setAmount(1);

                if (toCompare.isSimilar(objectOpt.get().buildItem(1, playerOpt.get(), settings)) && objectOpt.get().canBeStacked()) {
                    inventory.getItem(slotInt).setAmount(inventory.getItem(slotInt).getAmount() + amountOpt.get());
                    callAddInInventoryEvent = true;
                } else if (override) {
                    inventory.setItem(slotInt, objectOpt.get().buildItem(amountOpt.get(), playerOpt.get(), settings));
                    callAddInInventoryEvent = true;

                    /* Move the item that is override to not lose it */
                    for (ItemStack toDrop : inventory.addItem(item).values()) {
                        p.getWorld().dropItem(p.getLocation(), toDrop);
                    }
                }
            }
            if (callAddInInventoryEvent && getSPlugin().getShortName().equals("EI")) {
                /* Call add event */
                AddItemInPlayerInventoryEvent eventToCall = new AddItemInPlayerInventoryEvent(p, inventory.getItem(slotInt), slotInt);
                Bukkit.getPluginManager().callEvent(eventToCall);
            }
            if (!getSPlugin().getPluginConfig().getBooleanSetting("silentGive"))
                getSm().sendMessage(sender, getSPlugin().getNameDesign() + " &a" + getSObjectManager().getObjectName() + " &e" + executableItem + " &agiven to &e" + player + " &a(&7" + amount + "&a) &ain slot &e" + slot);
        }
    }

    public int give(Player player, Z sObject, Map<String, Object> settings, int quantity) {

        Optional<Player> pOptional = Optional.ofNullable(player);

        int firstEmptySlot = player.getInventory().firstEmpty();
        //SsomarDev.testMsg("firstEmptySlot: " + firstEmptySlot, true);
        ItemStack itemStack = sObject.buildItem(quantity, pOptional, settings);
        Map<Integer, ItemStack> over = player.getInventory().addItem(itemStack);
        //player.updateInventory();
        if (over.size() > 0) {
            //SsomarDev.testMsg("over: " + over.size(), true);
            int out = 0;
            for (Integer index : over.keySet()) {
                player.getWorld().dropItem(player.getLocation(), over.get(index));
                out = out + over.get(index).getAmount();
            }
            return out;
        } else if (getSPlugin().getShortName().equals("EI")) {

            AddItemInPlayerInventoryEvent eventToCall = new AddItemInPlayerInventoryEvent(player, itemStack, firstEmptySlot);
            /* */
            Bukkit.getPluginManager().callEvent(eventToCall);
        }

        return 0;
    }

    public int simpleGive(Player player, Z sObject, Map<String, Object> settings) {
        return give(player, sObject, settings, 1);
    }

    public void multipleGive(Player player, Z object, int quantity, Map<String, Object> settings) {

        Optional<Player> pOptional = Optional.ofNullable(player);

        int limit = getSPlugin().getPluginConfig().getIntSetting("pickupLimit", 100000);
        if (limit <= -1) limit = 100000;
        int cpt = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && getSObjectManager() instanceof ManagerWithBuildable && ((ManagerWithBuildable) getSObjectManager()).getObject(item).isPresent())
                cpt = cpt + item.getAmount();
        }

        int quantityAbleToGive = limit - cpt;
        if (quantityAbleToGive < 0) quantityAbleToGive = 0;
        int notAbbleToGive = quantity - quantityAbleToGive;
        if (notAbbleToGive < 0) notAbbleToGive = 0;
        quantity = quantity - notAbbleToGive;
        ItemStack item = object.buildItem(1, pOptional, settings);
        int maxStack = item.getMaxStackSize();

        //SsomarDev.testMsg("quantity : " + quantity, true);
        //SsomarDev.testMsg("maxStack : " + maxStack, true);
        //SsomarDev.testMsg("quantityAbleToGive : " + quantityAbleToGive, true);
        //SsomarDev.testMsg("notAbbleToGive : " + notAbbleToGive, true);
        int inInventory = quantity;
        int outInventory = 0;
        while (quantity > 0) {
            outInventory = outInventory + give(player, object, settings, Math.min(maxStack, quantity));
            quantity = quantity - maxStack;
        }
        if (outInventory > 0) {
            getSm().sendMessage(player, StringConverter.replaceVariable(MessageMain.getInstance().getMessage(getSPlugin().getPlugin(), Message.FULL_INVENTORY), player.getName(), StringConverter.coloredString(object.getItemName()), outInventory + "", 0));
            inInventory = inInventory - outInventory;
        }

        while (notAbbleToGive > 0) {
            player.getWorld().dropItem(player.getLocation(), object.buildItem(Math.min(maxStack, quantity), pOptional, settings));
            notAbbleToGive = notAbbleToGive - maxStack;
            getSm().sendMessage(player, StringConverter.coloredString(MessageMain.getInstance().getMessage(getSPlugin().getPlugin(), Message.PICKUP_LIMIT_MESSAGE)).replaceAll("%limit%", "" + getSPlugin().getPluginConfig().getIntSetting("pickupLimit", 100000)));
        }

        if (!getSPlugin().getPluginConfig().getBooleanSetting("silentGive"))
            getSm().sendMessage(player, StringConverter.replaceVariable(MessageMain.getInstance().getMessage(getSPlugin().getPlugin(), Message.RECEIVE_ITEM), player.getName(), StringConverter.coloredString(object.getItemName()), inInventory + "", 0));
    }


    public List<String> getCommands() {
        List<String> commands = new ArrayList<>();
        commands.add("give");
        commands.add("giveall");
        commands.add("giveslot");
        return commands;
    }

    public void run(CommandSender sender, String command, String[] args, String typedCommand) {
        switch (command) {
            case "give":
                if (args.length >= 2) {

                    List<String> arguments = new ArrayList<>(Arrays.asList(args));
                    Map<String, Object> settings = StringSetting.extractSettingsAndRebuildCorrectly(arguments, 1, new ArrayList<>());

                    /** OLD METHODS SHOULD BE REMOVED DURING JUNE 2024 **/
                    Map<String, String> variablesMap = checkVariables(sender, typedCommand).orElse(new HashMap<>());
                    if (!variablesMap.isEmpty()) {
                        settings.put("Variables", variablesMap);
                        getSm().sendMessage(sender, "&cWARNING : &6VAR() &cin the command &6/" + getSPlugin().getShortName().toLowerCase() + " give &cis deprecated, please use the new method to set variables in the command /ei give Ssomar &6xyz{Variables:{test:5,test2:\"Edit please\"}}");
                        getSm().sendMessage(sender, "&cCommand : &7" + typedCommand);
                        getSm().sendMessage(sender, "&cThis method will be removed during June 2024");
                    }
                    Optional<Integer> usage = checkUsage(sender, typedCommand).orElse(Optional.empty());
                    usage.ifPresent(integer -> {
                        settings.put("Usage", String.valueOf(integer));
                        getSm().sendMessage(sender, "&cWARNING : &6USAGE() &cin the command &6/" + getSPlugin().getShortName().toLowerCase() + " give &cis deprecated, please use the new method to set usage in the command /ei give Ssomar &6xyz{Usage:5}");
                        getSm().sendMessage(sender, "&cCommand : &7" + typedCommand);
                        getSm().sendMessage(sender, "&cThis method will be removed during June 2024");
                    });
                    /** END  OLD METHODS SHOULD BE REMOVED DURING JUNE 2024 END **/

                    //for (String s : arguments) SsomarDev.testMsg("arguments: " + s, true);
                    //for (String s : args) SsomarDev.testMsg("args: " + s, true);
                    String pName = arguments.get(0);
                    String eiID = arguments.get(1);
                    String quantity = "1";
                    if (arguments.size() >= 3) {
                        quantity = arguments.get(2);
                    }
                    boolean giveOfflinePlayer = true;
                    if (arguments.size() >= 4) {
                        giveOfflinePlayer = Boolean.parseBoolean(arguments.get(3));
                    }
                    //SsomarDev.testMsg("typedCommand: " + typedCommand, true);
                    new GiveCommand(getSPlugin(), getSObjectManager()).give(sender, eiID, quantity, pName, settings, "", giveOfflinePlayer, typedCommand);
                } else
                    getSm().sendMessage(sender, ChatColor.RED + getSPlugin().getNameWithBrackets() + " &cNot enough args /" + getSPlugin().getShortName().toLowerCase() + " give {playername} {id} {quantity} [giveOfflinePlayer]");
                break;

            case "giveall":
                if (args.length >= 1) {
                    List<String> arguments = new ArrayList<>(Arrays.asList(args));
                    Map<String, Object> settings = StringSetting.extractSettingsAndRebuildCorrectly(arguments, 1, new ArrayList<>());
                    String eiID = arguments.get(0);
                    String quantity = "1";
                    String world = "";
                    if (arguments.size() >= 2) {
                        quantity = arguments.get(1);
                    }
                    boolean giveOfflinePlayer = false;
                    if (arguments.size() >= 3) {
                        String arg = arguments.get(2);
                        if (arg.equalsIgnoreCase("true") || arg.equalsIgnoreCase("false")) {
                            giveOfflinePlayer = Boolean.parseBoolean(arg);
                        } else {
                            world = arguments.get(2);
                        }
                    }
                    if (arguments.size() >= 4) {
                        giveOfflinePlayer = Boolean.parseBoolean(arguments.get(3));
                    }
                    new GiveCommand(getSPlugin(), getSObjectManager()).give(sender, eiID, quantity, "all", settings, world, giveOfflinePlayer, typedCommand);
                } else
                    getSm().sendMessage(sender, ChatColor.RED + getSPlugin().getNameWithBrackets() + " &cNot enough argument /" + getSPlugin().getShortName().toLowerCase() + " giveall {id} {quantity} [world] [giveOfflinePlayer]");

                break;
            case "giveslot":
                List<String> arguments = new ArrayList<>(Arrays.asList(args));
                Map<String, Object> settings = StringSetting.extractSettingsAndRebuildCorrectly(arguments, 1, new ArrayList<>());

                if (arguments.size() < 4) {
                    getSm().sendMessage(sender, ChatColor.RED + getSPlugin().getNameWithBrackets() + " &cError not enought argument /" + getSPlugin().getShortName().toLowerCase() + " giveslot {player} {id} {quantity} {slot} [override] [giveOfflinePlayer]");
                } else {
                    String override = "false";
                    if (arguments.size() >= 5) {
                        override = arguments.get(4);
                    }
                    boolean giveOfflinePlayer = true;
                    if (arguments.size() >= 6) {
                        giveOfflinePlayer = Boolean.parseBoolean(arguments.get(5));
                    }
                    new GiveCommand(getSPlugin(), getSObjectManager()).giveSlot(sender, arguments.get(1), arguments.get(2), arguments.get(0), settings, arguments.get(3), override, giveOfflinePlayer, typedCommand);
                }
                break;
        }
    }

    public List<String> getArguments(String command, String[] args) {
        ArrayList<String> arguments = new ArrayList<String>();
        switch (args[0]) {
            case "giveall":
                for (SObject item : getSObjectManager().getLoadedObjects()) {
                    arguments.add(item.getId());
                }
                Collections.sort(arguments);
                return arguments;

            case "give":
                if (args.length == 3) {
                    return getSObjectManager().getLoadedObjectsWith(args[2]);
                } else if (args.length == 4) {
                    arguments.add("0");
                    arguments.add("1");
                    arguments.add("3");
                    arguments.add("5");
                    arguments.add("10");
                    arguments.add("25");
                    arguments.add("50");

                    return arguments;
                }
                break;

            case "giveslot":
                if (args.length == 3) {
                    return getSObjectManager().getLoadedObjectsWith(args[2]);
                } else if (args.length == 4) {
                    arguments.add("0");
                    arguments.add("1");
                    arguments.add("3");
                    arguments.add("5");
                    arguments.add("10");
                    arguments.add("25");
                    arguments.add("50");

                    return arguments;
                } else if (args.length == 5) {
                    arguments.add("0");
                    arguments.add("1");
                    arguments.add("2");
                    arguments.add("3");
                    arguments.add("4");
                    arguments.add("5");
                    arguments.add("6");
                    arguments.add("7");
                    arguments.add("8");

                    return arguments;
                }
                break;
        }
        return arguments;
    }

    @Override
    public Optional<Integer> checkAmount(CommandSender sender, String amount) {
        if (!NTools.getInteger(amount).isPresent()) {
            getSm().sendMessage(sender, ChatColor.RED + getSPlugin().getNameWithBrackets() + " Invalid amount : " + amount);
            return Optional.empty();
        }
        if (NTools.getInteger(amount).get() > getSPlugin().getPluginConfig().getIntSetting("giveLimit", 100)) {
            getSm().sendMessage(sender, ChatColor.RED + getSPlugin().getNameWithBrackets() + " Quantity > " + getSPlugin().getPluginConfig().getIntSetting("giveLimit", 100) + " is blocked for security !");
            return Optional.empty();
        }
        return NTools.getInteger(amount);
    }


    /**
     * OLD METHODS SHOULD BE REMOVED DURING JUNE 2024
     **/

    public static Optional<Map<String, String>> checkVariables(CommandSender sender, String variables) {
        if (variables == null || variables.isEmpty()) return Optional.empty();

        Map<String, String> variablesMap = new HashMap<>();
        if (variables.contains("VAR(")) {
            String[] split = variables.split("VAR\\(");
            if (split.length > 1) {
                String[] split2 = split[1].split("\\)");
                if (split2.length > 0) {
                    String allVariables = split2[0].trim();
                    String[] split3 = allVariables.split(",");
                    for (String s : split3) {
                        String[] split4 = s.split(":");
                        if (split4.length > 1) {
                            String key = split4[0].trim();
                            String value = split4[1].trim();
                            if (key.isEmpty() || value.isEmpty()) {
                                //sender.sendMessage(ChatColor.RED + "[ExecutableItems] Variable " + s + " not specified correctly !");
                                return Optional.empty();
                            } else {
                                //SsomarDev.testMsg("key : " + key + " value : " + value);
                                variablesMap.put(key, value);
                            }
                        } else {
                            //sender.sendMessage(ChatColor.RED + "[ExecutableItems] Variable " + s + " not specified correctly !");
                            return Optional.empty();
                        }
                    }
                    return Optional.of(variablesMap);
                }
            }
        }

        return Optional.of(new HashMap<>());
    }

    public static Optional<Optional<Integer>> checkUsage(CommandSender sender, String usage) {
        if (usage == null || usage.isEmpty()) return Optional.empty();
        if (NTools.getInteger(usage).isPresent()) return Optional.of(NTools.getInteger(usage));
        else {
            if (usage.contains("USAGE(")) {
                String[] split = usage.split("USAGE\\(");
                if (split.length > 1) {
                    String[] split2 = split[1].split("\\)");
                    if (split2.length > 0) {
                        String usage2 = split2[0].trim();
                        if (NTools.getInteger(usage2).isPresent()) return Optional.of(NTools.getInteger(usage2));
                        else {
                            //sender.sendMessage(ChatColor.RED + "[ExecutableItems] Usage " + usage2 + " not specified correctly !");
                            return Optional.empty();
                        }
                    }
                }
            }
        }
        return Optional.of(Optional.empty());
    }

    /** OLD METHODS SHOULD BE REMOVED DURING JUNE 2024 **/
}
