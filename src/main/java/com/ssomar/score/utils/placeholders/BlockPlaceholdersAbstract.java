package com.ssomar.score.utils.placeholders;

import com.ssomar.executableblocks.api.ExecutableBlocksAPI;
import com.ssomar.score.SCore;
import com.ssomar.score.api.executableblocks.config.placed.ExecutableBlockPlacedInterface;
import com.ssomar.score.usedapi.AllWorldManager;
import com.ssomar.score.usedapi.Dependency;
import com.ssomar.score.utils.ToolsListMaterial;
import com.ssomar.score.utils.backward_compatibility.BiomeUtils;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BlockPlaceholdersAbstract extends PlaceholdersInterface implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final String particle;
    /* placeholders of the block */
    private int blockX;
    private int blockY;
    private int blockZ;
    private UUID blockWorld;
    private String blockWorldName;
    private String EB_ID;

    private Biome biome;
    private String blockDimension;
    private Material fixType;

    @Getter
    private Map<String, String> placeholders;


    public BlockPlaceholdersAbstract(String particle) {
        this.particle = particle;
        this.placeholders = new HashMap<>();
    }

    public void setBlockPlcHldr(@NotNull Block block) {
        setBlockPlcHldr(block, null);
    }

    public void setBlockPlcHldr(@NotNull Block block, Material fixType) {
        Location bLoc = block.getLocation();
        this.blockX = bLoc.getBlockX();
        this.blockY = bLoc.getBlockY();
        this.blockZ = bLoc.getBlockZ();
        this.blockWorld = bLoc.getWorld().getUID();
        this.biome = block.getBiome();
        switch (block.getWorld().getEnvironment()) {
            case NETHER:
                blockDimension = "nether";
                break;
            case CUSTOM:
                blockDimension = "custom";
                break;
            case NORMAL:
                blockDimension = "normal";
                break;
            case THE_END:
                blockDimension = "end";
                break;
        }
        if (fixType != null) this.fixType = fixType;
        if(Dependency.EXECUTABLE_BLOCKS.isEnabled()){
            Optional<ExecutableBlockPlacedInterface> executableBlockPlaced = ExecutableBlocksAPI.getExecutableBlocksPlacedManager().getExecutableBlockPlaced(block);
            if(executableBlockPlaced.isPresent()){
                this.EB_ID = executableBlockPlaced.get().getEB_ID();
            }
            else this.EB_ID = "";
        }
        this.reloadBlockPlcHldr();
    }

    public void reloadBlockPlcHldr() {
        //SsomarDev.testMsg("reloadBlockPlcHldr " + particle, true);
        if (this.blockWorld != null) {
            Optional<World> worldOpt = AllWorldManager.getWorld(this.blockWorld);
            if (!worldOpt.isPresent()) return;
            World world = worldOpt.get();
            Location loc = new Location(world, blockX, blockY, blockZ);

            Block block = loc.getBlock();

            blockWorldName = world.getName();
            Material type = block.getType();

            String blockType = type.toString();
            if (fixType != null)
                blockType = fixType.toString();

            placeholders.put("%" + particle + "%", blockType);
            //SsomarDev.testMsg("Put " + particle + " " + blockType, true);
            placeholders.put("%" + particle + "_lower%", blockType.toLowerCase());
            placeholders.put("%" + particle + "_item_material%", ToolsListMaterial.getInstance().getRealMaterialOfBlock(Material.valueOf(blockType)).toString());
            placeholders.put("%" + particle + "_item_material_lower%", ToolsListMaterial.getInstance().getRealMaterialOfBlock(Material.valueOf(blockType)).toString().toLowerCase());
            placeholders.put("%" + particle + "_live%", type.toString());
            placeholders.put("%" + particle + "_live_lower%", type.toString().toLowerCase());
            placeholders.put("%" + particle + "_world%", blockWorldName);
            placeholders.put("%" + particle + "_world_lower%", blockWorldName.toLowerCase());
            placeholders.put("%" + particle + "_biome%", BiomeUtils.getBiomes().get(biome));
            placeholders.put("%" + particle + "_biome_lower%", BiomeUtils.getBiomes().get(biome).toLowerCase());
            placeholders.put("%" + particle + "_dimension%", blockDimension);
            placeholders.put("%" + particle + "_dimension_lower%", blockDimension.toLowerCase());
            placeholders.put("%" + particle + "_eb_id%", EB_ID);
            if (!SCore.is1v12Less()) placeholders.put("%" + particle + "_data%", block.getBlockData().getAsString());

            try {
                /* Don't use block.getState() here: on a block entity (chest, barrel, ...) it builds a
                 * full snapshot and serializes the whole inventory NBT. getBlockData() is equivalent
                 * for this check and free (same call as the %_data% placeholder above). */
                BlockData data = block.getBlockData();
                if (data instanceof Ageable)
                    placeholders.put("%" + particle + "_is_ageable%", "true");
                else placeholders.put("%" + particle + "_is_ageable%", "false");
            } catch (Exception | Error e) {
                placeholders.put("%" + particle + "_is_ageable%", "false");
            }

            boolean notValidSpawner = true;
            /* Cheap material guard before getState(), which is expensive on block entities.
             * Matches MOB_SPAWNER (1v12 less), SPAWNER and TRIAL_SPAWNER without a version check. */
            if (type.name().endsWith("SPAWNER")) {
                BlockState state = block.getState();
                if (state instanceof CreatureSpawner) {
                    CreatureSpawner spawner = (CreatureSpawner) state;
                    if (spawner.getSpawnedType() != null) {
                        notValidSpawner = false;
                        placeholders.put("%" + particle + "_spawnertype%", spawner.getSpawnedType().toString());
                    }
                }
            }
            if (notValidSpawner) placeholders.put("%" + particle + "_spawnertype%", "null");
        }

    }


    public String replacePlaceholder(String s) {
        String toReplace = s;
        if (blockWorld != null) {
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_x%", blockX + "", false);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_y%", blockY + "", false);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_z%", blockZ + "", false);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_x_int%", blockX + "", true);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_y_int%", blockY + "", true);
            toReplace = replaceCalculPlaceholder(toReplace, "%" + particle + "_z_int%", blockZ + "", true);
        }

        return toReplace;
    }
}
