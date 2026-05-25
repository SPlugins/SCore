package com.ssomar.score.commands.runnable.block.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.SsomarDev;
import com.ssomar.score.api.executableitems.ExecutableItemsAPI;
import com.ssomar.score.api.executableitems.config.ExecutableItemInterface;
import com.ssomar.score.commands.runnable.CommandSetting;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.block.BlockCommand;
import com.ssomar.score.utils.strings.StringSetting;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DropExecutableItem extends BlockCommand {

    /**
     * Details for each argument:
     * - id : Required
     * - amount : Required
     * - owner : Optional
     * - itemdata : Optional
     */
    public DropExecutableItem() {
        CommandSetting id = new CommandSetting("id", 0, String.class, "null");
        CommandSetting amount = new CommandSetting("amount", 1, Integer.class, 1);
        CommandSetting owner = new CommandSetting("owner", 2, String.class, null);
        CommandSetting itemdata = new CommandSetting("itemdata", 3, String.class, "null");
        List<CommandSetting> settings = getSettings();
        settings.add(id);
        settings.add(amount);
        settings.add(owner);
        settings.add(itemdata);
        setNewSettingsMode(true);
    }

    @Override
    public void run(Player p, @NotNull Block block, SCommandToExec sCommandToExec) {
        String id = (String) sCommandToExec.getSettingValue("id");
        int amount = (int) sCommandToExec.getSettingValue("amount");
        String owner = (String) sCommandToExec.getSettingValue("owner");
        String itemData = (String) sCommandToExec.getSettingValue("itemdata");
        
        Map<String, Object> settings = new HashMap<>();
        if (!itemData.equals("null")) settings = StringSetting.getSettings(itemData);

        if (!(SCore.hasExecutableItems && ExecutableItemsAPI.getExecutableItemsManager().isValidID(id))) {
            SCore.plugin.getLogger().info(ChatColor.RED+"Invalid ID was provided for a DROPEXECUTABLEITEM command. Please double check your DROPEXECUTABLEITEM commands.");
            return;
        }

        Optional<Player> playerOwner = Optional.empty();
        if (owner != null) {
            try { playerOwner = Optional.ofNullable(Bukkit.getPlayer(UUID.fromString(owner))); } //  attempt by getting player details via uuid
            catch (Exception e) {}
            finally { if (!playerOwner.isPresent()) playerOwner = Optional.ofNullable(Bukkit.getPlayer(owner)); } // attempt by getting player details via ign
        }
        if (!playerOwner.isPresent() || playerOwner.get() == null)
            playerOwner = Optional.ofNullable(p); // if all fails, rely on the player details of the one who executed the cmd

        // Check if the target EI is a valid EI
        if (SCore.hasExecutableItems && ExecutableItemsAPI.getExecutableItemsManager().isValidID(id) && amount > 0) {

            if (amount > 0) {
                Optional<ExecutableItemInterface> eiOpt = ExecutableItemsAPI.getExecutableItemsManager().getExecutableItem(id);

                if (eiOpt.isPresent()) {
                    ExecutableItemInterface ei = eiOpt.get();
                    // Create a new Location object because if you try to drop the item with whole number coords, it will drop it in the corner
                    Location dropLoc = new Location(block.getWorld(), block.getX()+0.5, block.getY(), block.getZ()+0.5);

                    block.getWorld().dropItem(dropLoc, ei.buildItem(amount, playerOwner, settings));
                }
            }
        }

    }

    @Override
    public Optional<String> verify(List<String> args, boolean isFinalVerification) {
        return Optional.empty();
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("DROPEXECUTABLEITEM");
        return names;
    }

    @Override
    public String getTemplate() {
        return "DROPEXECUTABLEITEM id:{id} amount:{number} owner:{ign/uuid} itemdata:{usage/variables/durability w/o no outer side brackets}";
        // ex: DROPEXECUTABLEITEM id:drop_stick amount:1 owner:Special70 itemdata:Usage:50,Variables:{keg:deng}
    }

    @Override
    public ChatColor getColor() {
        return null;
    }

    @Override
    public ChatColor getExtraColor() {
        return null;
    }

}
