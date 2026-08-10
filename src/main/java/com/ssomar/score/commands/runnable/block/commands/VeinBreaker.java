package com.ssomar.score.commands.runnable.block.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.ActionInfo;
import com.ssomar.score.commands.runnable.CommandSetting;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.block.BlockCommand;
import com.ssomar.score.events.BlockBreakEventExtension;
import com.ssomar.score.features.custom.detailedblocks.DetailedBlocks;
import com.ssomar.score.usedapi.AuraSkillsSmeltTracker;
import com.ssomar.score.utils.placeholders.StringPlaceholder;
import com.ssomar.score.utils.safebreak.SafeBreak;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ssomar.score.commands.runnable.block.commands.Smelt.dropItemWithFortune;

public class VeinBreaker extends BlockCommand {

    /**
     * THIS COMMAND MUST BE DELAYED OF AT LEAST 1 TICK
     * WHY ?
     * TO LET ALL ACTIVATORS OF THE ITEMS BE RUNNED CORRECLTY FIRST
     * BECAUSE THIS COMMAND CAN GENERATE ANOTHER EVENT SO IT CAN CREATE THIS SITUATION
     * X ( activator where this command is )
     * Y ( the new event generates by this command)
     * Y
     * Y
     * X
     * X
     * <p>
     * This situation is bad for the variables edition, example with the same situation and with a increment of the variable by one
     * X 1
     * Y 2
     * Y 3
     * Y 4
     * X 2
     * X 3
     * <p>
     * The delay help us to fix that and let us have:
     * X 1
     * X 2
     * X 3
     * Y 4
     * Y 5
     * Y 6
     **/

    public VeinBreaker() {
        CommandSetting maxVeinSize = new CommandSetting("maxVeinSize", 0, Integer.class, 10, true);
        CommandSetting createBBEvent = new CommandSetting("createBBEvent", 1, Boolean.class, true, true);
        CommandSetting smelt = new CommandSetting("smelt", 2, Boolean.class, false, true);
        List<CommandSetting> settings = getSettings();
        settings.add(maxVeinSize);
        settings.add(createBBEvent);
        settings.add(smelt);
        setNewSettingsMode(true);
    }

    @Override
    public void run(Player p, @NotNull Block block, SCommandToExec sCommandToExec) {
        int maxVeinSize = (int) sCommandToExec.getSettingValue("maxVeinSize");
        boolean createBBEvent = (boolean) sCommandToExec.getSettingValue("createBBEvent");
        boolean smelt = (boolean) sCommandToExec.getSettingValue("smelt");
        ActionInfo aInfo = sCommandToExec.getActionInfo();
        Material oldMaterial = aInfo.getOldBlockMaterial();

        Runnable runnable3 = new Runnable() {
            @Override
            public void run() {

                if (aInfo.isEventFromCustomBreakCommand()) return;

                DetailedBlocks whiteList;
                if ((whiteList = aInfo.getDetailedBlocks()) != null) {
                    /* I have set playerOpt on empty, otherwise if it will spam the error message if too many blocks are broken with a not valid type */
                    if (!whiteList.isValid(block, Optional.empty(), null, new StringPlaceholder(), oldMaterial, Optional.empty())) {
                        p.sendMessage(ChatColor.RED + "This block is not allowed to be broken ! : "+block.getType().name());
                        return;
                    }
                }

                List<Block> vein;
                UUID pUUID = null;
                if (p != null) pUUID = p.getUniqueId();
                if (smelt) {
                    Material blockMat = block.getType();
                    AuraSkillsSmeltTracker.recordSmelt(pUUID, block);
                    boolean safeBreakStatus = SafeBreak.breakBlockWithEvent(block, pUUID, aInfo.getSlot(), false, createBBEvent, true, BlockBreakEventExtension.BreakCause.VEIN_BREAK);
                    if (safeBreakStatus) dropItemWithFortune(block, p, blockMat);
                } else {
                    SafeBreak.breakBlockWithEvent(block, pUUID, aInfo.getSlot(), true, createBBEvent, true, BlockBreakEventExtension.BreakCause.VEIN_BREAK);
                }

                vein = getVein(block, oldMaterial, maxVeinSize);

                for (Block b : vein) {
                    ItemStack smeltItem = Smelt.getSmeltedItem(b.getType());
                    if (smelt && smeltItem != null) {
                        AuraSkillsSmeltTracker.recordSmelt(pUUID, b);
                        boolean safeBreakStatus = SafeBreak.breakBlockWithEvent(b, pUUID, aInfo.getSlot(), false, createBBEvent, true, BlockBreakEventExtension.BreakCause.MINE_IN_CUBE);
                        if (safeBreakStatus) dropItemWithFortune(b, p, smeltItem.getType());
                    } else {
                        SafeBreak.breakBlockWithEvent(b, pUUID, aInfo.getSlot(), true, createBBEvent, true, BlockBreakEventExtension.BreakCause.MINE_IN_CUBE);
                    }
                }
            }
        };
        SCore.schedulerHook.runLocationTask(runnable3, block.getLocation(), 1L);
    }

    public List<Block> getVein(Block block, Material oldMaterial, int veinSize) {
        List<Block> result = new ArrayList<>();
        this.fillVein(result, block, oldMaterial, veinSize);
        return result;
    }

    public void fillVein(List<Block> vein, Block block, Material oldMaterial, int veinSize) {

        Location loc = block.getLocation();
        List<Location> toCheck = new ArrayList<>();
        // this order is important (check direct connections first)
        toCheck.add(loc.clone().add(0, 1, 0));
        toCheck.add(loc.clone().add(0, -1, 0));
        toCheck.add(loc.clone().add(0, 0, 1));
        toCheck.add(loc.clone().add(0, 0, -1));
        toCheck.add(loc.clone().add(1, 0, 0));
        toCheck.add(loc.clone().add(-1, 0, 0));

        for (Location l : toCheck) {

            if (vein.size() >= veinSize) return;

            Block newBlock;
            if ((newBlock = l.getBlock()).getType().equals(oldMaterial)) {
                if (!vein.contains(newBlock)) {
                    vein.add(newBlock);
                    this.fillVein(vein, newBlock, oldMaterial, veinSize);
                }
            }
        }

        // this order is important (check other connections last)
        int radius = 1;
        for (int y = -radius; y < radius + 1; y++) {
            for (int x = -radius; x < radius + 1; x++) {
                for (int z = -radius; z < radius + 1; z++) {

                    if (y == 0 && z == 0 && x == 0) continue;

                    if (vein.size() >= veinSize) return;

                    Location newLoc = loc.clone();
                    newLoc.add(x, y, z);

                    if (toCheck.contains(newLoc)) continue;
                    Block newBlock;

                    if ((newBlock = newLoc.getBlock()).getType().equals(oldMaterial)) {
                        if (!vein.contains(newBlock)) {
                            vein.add(newBlock);
                            this.fillVein(vein, newBlock, oldMaterial, veinSize);
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("VEIN_BREAKER");
        return names;
    }

    @Override
    public String getTemplate() {
        return "VEIN_BREAKER maxVeinSize:10 triggerEvent:true";
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
