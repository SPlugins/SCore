package com.ssomar.score.commands.runnable.block.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.ActionInfo;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.CommandSetting;
import com.ssomar.score.commands.runnable.block.BlockCommand;
import com.ssomar.score.events.BlockBreakEventExtension;
import com.ssomar.score.features.custom.detailedblocks.DetailedBlocks;
import com.ssomar.score.usedapi.AuraSkillsSmeltTracker;
import com.ssomar.score.utils.placeholders.StringPlaceholder;
import com.ssomar.score.utils.safebreak.SafeBreak;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.ssomar.score.commands.runnable.block.commands.Smelt.dropItemWithFortune;
import static org.bukkit.block.BlockFace.*;

/* MINEINCUBE {radius} {ActiveDrop true or false} */
public class MineInCube extends BlockCommand {

    public MineInCube() {
        CommandSetting radius = new CommandSetting("radius", 0, Integer.class, 0);
        CommandSetting droploot = new CommandSetting("droploot", 1, Boolean.class, true);
        CommandSetting createEvent = new CommandSetting("createEvent", 2, Boolean.class, true);
        CommandSetting offsetBreak = new CommandSetting("offsetBreak", 3, Boolean.class, false);
        CommandSetting smelt = new CommandSetting("smelt", 4, Boolean.class, false);
        List<CommandSetting> settings = getSettings();
        settings.add(radius);
        settings.add(droploot);
        settings.add(createEvent);
        settings.add(offsetBreak);
        settings.add(smelt);
        setNewSettingsMode(true);
    }

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
    @Override
    public void run(Player p, @NotNull Block block, SCommandToExec sCommandToExec) {
        ActionInfo aInfo = sCommandToExec.getActionInfo();

        //SsomarDev.testMsg("MINEINCUBE command", true);
        Runnable runnable3 = new Runnable() {
            @Override
            public void run() {
                /* Cancel a Loop of blockBreakEvent that MineInCbe can create */
                if (aInfo.isEventFromCustomBreakCommand()) return;

                try {
                    int radius = Integer.parseInt(sCommandToExec.getSettingValue("radius").toString());
                    boolean drop = Boolean.parseBoolean(sCommandToExec.getSettingValue("droploot").toString());

                    boolean createBBEvent = Boolean.parseBoolean(sCommandToExec.getSettingValue("createEvent").toString());

                    boolean offset = Boolean.parseBoolean(sCommandToExec.getSettingValue("offsetBreak").toString());
                    boolean smelt = Boolean.parseBoolean(sCommandToExec.getSettingValue("smelt").toString());

                    List<Material> blackList = new ArrayList<>();
                    blackList.add(Material.BEDROCK);
                    blackList.add(Material.AIR);

                    int offsetx = 0;
                    int offsety = 0;
                    int offsetz = 0;

                    if(offset) {
                        Set<Material> transparent = new HashSet<>();
                        transparent.add(Material.WATER);
                        transparent.add(Material.AIR);
                        if(SCore.is1v18Plus()) transparent.add(Material.CAVE_AIR);

                        List<Block> lastBlocks = p.getLastTwoTargetBlocks(transparent, 5);
                        /* for (Block b : lastBlocks) {
                            SsomarDev.testMsg("lastBlocks: " + b.getType().name(), true);
                        }*/
                        BlockFace face = null;
                        try{
                            face = lastBlocks.get(1).getFace(lastBlocks.get(0)).getOppositeFace();
                        }catch (Exception ignored){
                            // IndexOutOfBoundsException: Index 1 out of bounds for length 1 where the player has a fence or non full block in its line of view but don't break the block
                            return;
                        }

                        if (face == NORTH) {
                            offsetz = (-1 * radius);
                        } else if (face == SOUTH) {
                            offsetz = (radius);
                        } else if (face == WEST) {
                            offsetx = (-1 * radius);
                        } else if (face == EAST) {
                            offsetx = (radius);
                        } else if (face == UP) {
                            offsety = (radius);
                        } else if (face == DOWN) {
                            offsety = (-1 * radius);
                        }
                    }

                    boolean isv18plus = SCore.is1v18Plus();
                    if (radius < 10) {
                        for (int y = -radius; y < radius + 1; y++) {
                            for (int x = -radius; x < radius + 1; x++) {
                                for (int z = -radius; z < radius + 1; z++) {

                                    if(isv18plus) {
                                        if ((block.getY() + y + offsety) < -64) continue;
                                    }else{
                                        if ((block.getY() + y + offsety) < 0) continue;
                                    }

                                    //Location toBreakLoc = new Location(block.getWorld(), block.getX() + x, block.getY() + y, block.getZ() + z);
                                    Block toBreak = block.getWorld().getBlockAt(block.getX() + x+offsetx, block.getY() + y+offsety, block.getZ() + z+offsetz);

                                    DetailedBlocks whiteList;
                                    if ((whiteList = aInfo.getDetailedBlocks()) != null) {
                                        /* I have set playerOpt on empty, otherwise if it will spam the error message if too many blocks are broken with a not valid type */
                                        if (!whiteList.isValid(toBreak, Optional.empty(), null, new StringPlaceholder()))
                                            continue;
                                    }

                                    if (!blackList.contains(toBreak.getType())) {

                                        UUID pUUID = null;
                                        if (p != null) pUUID = p.getUniqueId();

                                        ItemStack smeltItem = Smelt.getSmeltedItem(toBreak.getType());
                                        if (smelt && drop && smeltItem != null) {
                                            AuraSkillsSmeltTracker.recordSmelt(pUUID, toBreak);
                                            boolean safeBreakStatus = SafeBreak.breakBlockWithEvent(toBreak, pUUID, aInfo.getSlot(), false, createBBEvent, true, BlockBreakEventExtension.BreakCause.MINE_IN_CUBE);
                                            if (safeBreakStatus) dropItemWithFortune(toBreak, p, smeltItem.getType());
                                        } else {
                                            SafeBreak.breakBlockWithEvent(toBreak, pUUID, aInfo.getSlot(), drop, createBBEvent, true, BlockBreakEventExtension.BreakCause.MINE_IN_CUBE);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }
        };
        SCore.schedulerHook.runLocationTask(runnable3, block.getLocation(), 1);
    }

    @Override
    public Optional<String> verify(List<String> args, boolean isFinalVerification) {
        return Optional.empty();
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("MINEINCUBE");
        return names;
    }

    @Override
    public String getTemplate() {
        return "MINEINCUBE {radius} {ActiveDrop true or false} {create blockBreakEvent true or false} {offset default false}";
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
