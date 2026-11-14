package com.ssomar.score.commands.runnable.player.commands.openchest;

import com.ssomar.score.commands.runnable.ArgumentChecker;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.player.PlayerCommand;
import com.ssomar.score.utils.backward_compatibility.SnapshotUtils;
import com.ssomar.score.utils.numbers.NTools;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Barrel;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.*;

public class OpenChest extends PlayerCommand {

    private static OpenChest instance;

    @Getter
    private Map<Player, Inventory> bypassChests = new HashMap<>();

    private OpenChest() {
        bypassChests = new HashMap<>();
    }

    @Override
    public void run(Player p, Player receiver, SCommandToExec sCommandToExec) {
        List<String> args = sCommandToExec.getOtherArgs();
        //OPENCHEST world x y z
        World world = Bukkit.getWorld(args.get(0));
        if (world == null) return;

        Double x = NTools.getDouble(args.get(1)).get();
        Double y = NTools.getDouble(args.get(2)).get();
        Double z = NTools.getDouble(args.get(3)).get();

        boolean bypassProtections = false;
        if (args.size() > 4) bypassProtections = Boolean.parseBoolean(args.get(4));

        Location location = new Location(world, x, y, z);
        Chunk chunk = location.getChunk();

        BlockState state = SnapshotUtils.getStateNoSnapshot(location.getBlock());

        if (state instanceof Chest) {
            Chest chest = (Chest) state;
            InventoryHolder holder = SnapshotUtils.getHolderNoSnapshot(chest.getInventory());
            Inventory chestInventory = chest.getInventory();
            if (holder instanceof DoubleChest) chestInventory = ((DoubleChest) holder).getInventory();
            if (bypassProtections) bypassChests.put(receiver, chestInventory);
            receiver.openInventory(chestInventory);
            //SsomarDev.testMsg("OPEN CHEST", true);
        } else if (state instanceof Barrel) {
            Barrel barrel = (Barrel) state;
            Inventory barrelInventory = barrel.getInventory();
            if (bypassProtections) bypassChests.put(receiver, barrelInventory);
            receiver.openInventory(barrelInventory);
            //SsomarDev.testMsg("OPEN BARREL", true);
        }
        else return;
        OpenChestManager.getInstance().addForcedChunk(chunk, receiver.getUniqueId());
    }

    @Override
    public Optional<String> verify(List<String> args, boolean isFinalVerification) {
        if (args.size() < 4) return Optional.of(notEnoughArgs + getTemplate());

        ArgumentChecker ac = checkInteger(args.get(1), isFinalVerification, getTemplate());
        if (!ac.isValid()) return Optional.of(ac.getError());

        ArgumentChecker ac2 = checkInteger(args.get(2), isFinalVerification, getTemplate());
        if (!ac2.isValid()) return Optional.of(ac.getError());

        ArgumentChecker ac3 = checkInteger(args.get(3), isFinalVerification, getTemplate());
        if (!ac3.isValid()) return Optional.of(ac.getError());

        if (args.size() > 4) {
            ArgumentChecker ac4 = checkBoolean(args.get(4), isFinalVerification, getTemplate());
            if (!ac4.isValid()) return Optional.of(ac.getError());
        }


        return Optional.empty();
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("OPEN_CHEST");
        names.add("OPENCHEST");
        return names;
    }

    @Override
    public String getTemplate() {
        return "OPENCHEST {world} {x} {y} {z} [bypassProtections]";
    }

    @Override
    public ChatColor getColor() {
        return null;
    }

    @Override
    public ChatColor getExtraColor() {
        return null;
    }

    public static OpenChest getInstance() {
        if (instance == null) instance = new OpenChest();
        return instance;
    }
}
