package com.ssomar.score.commands.runnable.block.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.ActionInfo;
import com.ssomar.score.commands.runnable.CommandSetting;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.block.BlockCommand;
import com.ssomar.score.events.BlockBreakEventExtension;
import com.ssomar.score.features.custom.detailedblocks.DetailedBlocks;
import com.ssomar.score.utils.placeholders.StringPlaceholder;
import com.ssomar.score.utils.safebreak.SafeBreak;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.ssomar.score.commands.runnable.block.commands.Smelt.dropItemWithFortune;
import static org.bukkit.block.BlockFace.*;

/* MINEINCUBE {radius} {ActiveDrop true or false} */
public class InlineMineInCube extends BlockCommand {

    public InlineMineInCube() {
        CommandSetting radius = new CommandSetting("radius", 0, Integer.class, 1, true);
        CommandSetting depth = new CommandSetting("depth", 1, Integer.class, 1, true);
        CommandSetting drop = new CommandSetting("drop", 2, Boolean.class, true, true);
        CommandSetting createBBEvent = new CommandSetting("createBBEvent", 3, Boolean.class, false, true);
        CommandSetting direction = new CommandSetting("direction", 4, String.class, "", true);
        CommandSetting smelt = new CommandSetting("smelt", 5, Boolean.class, false, true);
        List<CommandSetting> settings = getSettings();
        settings.add(radius);
        settings.add(depth);
        settings.add(drop);
        settings.add(createBBEvent);
        settings.add(direction);
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
        List<String> args = sCommandToExec.getOtherArgs();
        ActionInfo aInfo = sCommandToExec.getActionInfo();

        int radius = (int) sCommandToExec.getSettingValue("radius");
        int depthArg = (int) sCommandToExec.getSettingValue("depth");
        boolean drop = (boolean) sCommandToExec.getSettingValue("drop");
        boolean createBBEvent = (boolean) sCommandToExec.getSettingValue("createBBEvent");
        String direction = (String) sCommandToExec.getSettingValue("direction");
        boolean smelt = (boolean) sCommandToExec.getSettingValue("smelt");

        //SsomarDev.testMsg("InlineMineInCube is running !", true);
        Runnable runnable3 = new Runnable() {
            @Override
            public void run() {
                /* Cancel a Loop of blockBreakEvent that MineInCbe can create */
                if (aInfo.isEventFromCustomBreakCommand()) return;

                try {

                    int depth = depthArg;
                    depth = depth - 1;

                    BlockFace directionWritten = null;
                    if(!Objects.equals(direction, "")) {
                        switch (direction) {
                            case "north":
                            case "n":
                            case "-z":
                                directionWritten = BlockFace.NORTH;
                                break;
                            case "south":
                            case "s":
                            case "+z":
                                directionWritten = BlockFace.SOUTH;
                                break;
                            case "east":
                            case "e":
                            case "+x":
                                directionWritten = BlockFace.EAST;
                                break;
                            case "west":
                            case "w":
                            case "-x":
                                directionWritten = BlockFace.WEST;
                                break;
                            case "up":
                                directionWritten = BlockFace.UP;
                                break;
                            case "down":
                                directionWritten = BlockFace.DOWN;
                                break;
                            case "auto":
                                directionWritten = getPAPIPlayerXZDirection(p);
                                break;
                        }
                    }
                   // SsomarDev.testMsg("directionWritten: " + directionWritten, true);

                    List<Material> blackList = new ArrayList<>();
                    blackList.add(Material.BEDROCK);
                    blackList.add(Material.AIR);

                    int initX = -radius;
                    int initZ = -radius;
                    int initY = -radius;
                    int maxX = radius;
                    int maxZ = radius;
                    int maxY = radius;

                    BoundingBox bb = new BoundingBox(block.getX(), block.getY(), block.getZ(), block.getX()+1, block.getY()+1, block.getZ()+1);
                    RayTraceResult rayTraceResult = bb.rayTrace(p.getEyeLocation().toVector(), p.getEyeLocation().getDirection(), 10);
                    //SsomarDev.testMsg("rayTraceResult: " + rayTraceResult.getHitBlockFace(), true);

                    BlockFace face = null;
                    if(directionWritten == null){
                        if(rayTraceResult != null &&  rayTraceResult.getHitBlockFace() != null)  face = rayTraceResult.getHitBlockFace().getOppositeFace();
                        else face = p.getFacing().getOppositeFace();
                    }else{
                        face = directionWritten;
                    }
                    //SsomarDev.testMsg("face: " + face, true);

                    int multiplier = 1;

                    if (face == NORTH) {
                        initZ = 0;
                        maxZ = depth;
                        multiplier = -1;
                    } else if (face == SOUTH) {
                        initZ = 0;
                        maxZ = depth;
                        multiplier = 1;
                    } else if (face == WEST) {
                        initX = 0;
                        maxX = depth;
                        multiplier = -1;
                    } else if (face == EAST) {
                        initX = 0;
                        maxX = depth;
                        multiplier = 1;
                    } else if (face == UP) {
                        initY = 0;
                        maxY = depth;
                        multiplier = 1;
                    } else if (face == DOWN) {
                        initY = 0;
                        maxY = depth;
                        multiplier = -1;
                    }

                    //SsomarDev.testMsg("initX: " + initX + " initY: " + initY + " initZ: " + initZ, true);

                    final boolean fDrop = drop;
                    final boolean fCreateBBEvent = createBBEvent;

                    if (radius < 10) {
                        for (int y = initY; y < maxY + 1; y++) {
                            for (int x = initX; x < maxX + 1; x++) {
                                for (int z = initZ; z < maxZ + 1; z++) {

                                   // SsomarDev.testMsg("x: " + x + " y: " + y + " z: " + z, true);

                                    Block toBreak = block.getWorld().getBlockAt(block.getX() + (x*multiplier) , block.getY() + (y*multiplier),  block.getZ() + (z*multiplier) );

                                    // Need to do that for Folia -  https://discord.com/channels/701066025516531753/1368780072252739584
                                    BukkitRunnable runnable = new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            DetailedBlocks whiteList;
                                            if ((whiteList = aInfo.getDetailedBlocks()) != null) {
                                                /* I have set playerOpt on empty, otherwise if it will spam the error message if too many blocks are broken with a not valid type */
                                                if (!whiteList.isValid(toBreak, Optional.empty(), null, new StringPlaceholder()))
                                                    return;
                                            }

                                            if (!blackList.contains(toBreak.getType())) {
                                                UUID pUUID = p.getUniqueId();
                                                ItemStack smeltItem = Smelt.getSmeltedItem(toBreak.getType());
                                                if (smelt && smeltItem != null) {
                                                    boolean safeBreakStatus = SafeBreak.breakBlockWithEvent(toBreak, pUUID, aInfo.getSlot(), false, createBBEvent, true, BlockBreakEventExtension.BreakCause.INLINE_MINEINCUBE);
                                                    if (safeBreakStatus) dropItemWithFortune(toBreak, p, smeltItem.getType());
                                                } else {
                                                    SafeBreak.breakBlockWithEvent(toBreak, pUUID, aInfo.getSlot(), true, createBBEvent, true, BlockBreakEventExtension.BreakCause.INLINE_MINEINCUBE);
                                                }
                                            }
                                        }
                                    };
                                    SCore.schedulerHook.runLocationTask(runnable, toBreak.getLocation(), 1L);
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

    /**
     * Yaw Logic sourced from: https://github.com/PlaceholderAPI/Player-Expansion/blob/master/src/main/java/com/extendedclip/papi/expansion/player/PlayerUtil.java
     * @param player
     * @return
     */
    private static BlockFace getPAPIPlayerXZDirection(Player player) {
        float pitch = player.getLocation().getPitch();
        if (pitch <= -45) {
            return BlockFace.UP;
        } else if (pitch >= 45) {
            return BlockFace.DOWN;
        }

        double rotation = player.getLocation().getYaw();
        if (rotation < 0.0D) {
            rotation += 360.0D;
        }

        if (Math.abs(rotation) <= 45 || Math.abs(rotation - 360) <= 45) {
            return BlockFace.SOUTH;
        } else if (Math.abs(rotation - 90) <= 45) {
            return BlockFace.WEST;
        } else if (Math.abs(rotation - 180) <= 45) {
            return BlockFace.NORTH;
        } else if (Math.abs(rotation - 270) <= 45) {
            return BlockFace.EAST;
        }

        return BlockFace.UP;
    }

    @Override
    public Optional<String> verify(List<String> args, boolean isFinalVerification) {
        return Optional.empty();
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("INLINE_MINEINCUBE");
        return names;
    }

    @Override
    public String getTemplate() {
        return "INLINE_MINEINCUBE {cube_radius} {depth} {ActiveDrop true or false} {create blockBreakEvent true or false} {optional direction}";
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
