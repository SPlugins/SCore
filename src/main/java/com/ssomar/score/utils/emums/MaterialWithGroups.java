package com.ssomar.score.utils.emums;

import com.ssomar.score.SCore;
import com.ssomar.score.utils.FixedMaterial;
import org.bukkit.Material;

import java.util.*;

public enum MaterialWithGroups {
    ALL_CHESTS(FixedMaterial.getMaterials(Arrays.asList("CHEST", "TRAPPED_CHEST", "ENDER_CHEST"))),
    ALL_FURNACES(FixedMaterial.getMaterials(Arrays.asList("FURNACE", "FURNACE_MINECART", "BLAST_FURNACE"))),
    ALL_PLANKS(FixedMaterial.getMaterials(Arrays.asList("MANGROVE_PLANKS", "WARPED_PLANKS", "OAK_PLANKS", "SPRUCE_PLANKS", "BIRCH_PLANKS", "JUNGLE_PLANKS", "ACACIA_PLANKS", "DARK_OAK_PLANKS", "CRIMSON_PLANKS", "CHERRY_PLANKS", "PALE_OAK_PLANKS"))),
    ALL_LOGS(FixedMaterial.getMaterials(Arrays.asList("WARPED_STEM", "CRIMSON_STEM", "MANGROVE_LOG", "WARPED_LOG", "OAK_LOG", "SPRUCE_LOG", "BIRCH_LOG", "JUNGLE_LOG", "ACACIA_LOG", "DARK_OAK_LOG", "CRIMSON_LOG", "CHERRY_LOG", "PALE_OAK_LOG"))),
    ALL_WOODS(FixedMaterial.getMaterials(Arrays.asList("MANGROVE_WOOD", "WARPED_WOOD", "OAK_WOOD", "SPRUCE_WOOD", "BIRCH_WOOD", "JUNGLE_WOOD", "ACACIA_WOOD", "DARK_OAK_WOOD", "CRIMSON_WOOD", "CHERY_WOOD", "PALE_OAK_WOOD"))),
    ALL_STRIPPED_LOGS(FixedMaterial.getMaterials(Arrays.asList("STRIPPED_OAK_LOG", "STRIPPED_SPRUCE_LOG", "STRIPPED_BIRCH_LOG", "STRIPPED_JUNGLE_LOG", "STRIPPED_ACACIA_LOG", "STRIPPED_DARK_OAK_LOG", "STRIPPED_CRIMSON_LOG", "STRIPPED_WARPED_LOG", "STRIPPED_PALE_OAK_LOG"))),
    ALL_STRIPPED_WOODS(FixedMaterial.getMaterials(Arrays.asList("STRIPPED_OAK_WOOD", "STRIPPED_SPRUCE_WOOD", "STRIPPED_BIRCH_WOOD", "STRIPPED_JUNGLE_WOOD", "STRIPPED_ACACIA_WOOD", "STRIPPED_DARK_OAK_WOOD", "STRIPPED_CRIMSON_WOOD", "STRIPPED_WARPED_WOOD", "STRIPPED_PALE_OAK_WOOD"))),
    ALL_ORES(FixedMaterial.getMaterials(Arrays.asList("COAL_ORE", "DEEPSLATE_COAL_ORE", "IRON_ORE", "DEEPSLATE_IRON_ORE", "COPPER_ORE", "DEEPSLATE_COPPER_ORE",
            "GOLD_ORE", "DEEPSLATE_GOLD_ORE", "REDSTONE_ORE", "DEEPSLATE_REDSTONE_ORE", "EMERALD_ORE", "DEEPSLATE_EMERALD_ORE", "LAPIS_ORE", "DEEPSLATE_LAPIS_ORE"
            , "DIAMOND_ORE", "DEEPSLATE_DIAMOND_ORE", "NETHER_GOLD_ORE", "NETHER_QUARTZ_ORE"))),
    ALL_WOOLS(FixedMaterial.getMaterials(Arrays.asList("WHITE_WOOL", "ORANGE_WOOL", "MAGENTA_WOOL", "LIGHT_BLUE_WOOL", "YELLOW_WOOL", "LIME_WOOL", "PINK_WOOL", "GRAY_WOOL", "LIGHT_GRAY_WOOL", "CYAN_WOOL", "PURPLE_WOOL", "BLUE_WOOL", "BROWN_WOOL", "GREEN_WOOL", "RED_WOOL", "BLACK_WOOL"))),
    ALL_SLABS(FixedMaterial.getMaterialsContains("SLAB")),
    ALL_STAIRS(FixedMaterial.getMaterialsContains("STAIRS")),
    ALL_FENCES(FixedMaterial.getMaterialsContains("FENCE")),
    ALL_SAPLINGS(FixedMaterial.getMaterialsContains("SAPLING")),
    ALL_CROPS(FixedMaterial.getMaterials(Arrays.asList("CROPS", "NETHER_WARTS", "POTATO", "CARROT", "BEETROOT_BLOCK", "WHEAT", "CARROTS", "BEETROOTS", "POTATOES", "NETHER_WART"))),
    ALL_DOORS(FixedMaterial.getMaterialsContains("DOOR", "TRAPDOOR")),
    ALL_TRAPDOORS(FixedMaterial.getMaterialsContains("TRAPDOOR")),
    ALL_BEDS(FixedMaterial.getMaterialsContains("BED")),
    ALL_TERRACOTTA(FixedMaterial.getMaterialsContains("TERRACOTTA")),
    ALL_NORMAL_TERRACOTTA(FixedMaterial.getMaterialsContains("TERRACOTTA", "GLAZED")),
    ALL_GLAZED_TERRACOTTA(FixedMaterial.getMaterialsContains("GLAZED_TERRACOTTA")),
    ALL_CONCRETE(FixedMaterial.getMaterialsContains("CONCRETE")),
    ALL_CONCRETE_POWDERS(FixedMaterial.getMaterialsContains("CONCRETE_POWDER")),
    ALL_GLASS(FixedMaterial.getMaterialsContains("GLASS")),
    ALL_STAINED_GLASS(FixedMaterial.getMaterialsContains("STAINED_GLASS")),
    ALL_SHULKER_BOXES(FixedMaterial.getMaterialsContains("SHULKER_BOX")),
    ALL_LEAVES(FixedMaterial.getMaterialsContains("LEAVES")),
    ALL_CARPETS(FixedMaterial.getMaterialsContains("CARPET"));

    private List<Material> materials;

    MaterialWithGroups(List<Material> materials) {
        this.materials = materials;
    }

    /** The materials of this group (unmodifiable view). */
    public List<Material> getMaterials() {
        return Collections.unmodifiableList(materials);
    }

    /** Group lookup by name (case-insensitive), empty when the name is a plain material or unknown. */
    public static Optional<MaterialWithGroups> getGroup(String name) {
        if (name == null) return Optional.empty();
        name = name.toUpperCase();
        for (MaterialWithGroups materialGroup : MaterialWithGroups.values()) {
            if (materialGroup.name().equals(name)) return Optional.of(materialGroup);
        }
        return Optional.empty();
    }

    public static boolean verif(Material material, String name) {
        name = name.toUpperCase();
        MaterialWithGroups materialWithGroups = null;
        for (MaterialWithGroups materialGroup : MaterialWithGroups.values()) {
            if (materialGroup.name().equals(name)) {
                materialWithGroups = materialGroup;
            }
        }
        if (materialWithGroups == null) {
            try {
                return material.equals(Material.valueOf(name));
            } catch (Exception e) {
                return false;
            }
        } else {
            return materialWithGroups.materials.contains(material);
        }
    }

    public static Optional<String> getMaterialWithGroups(String name) {
        name = name.toUpperCase();
        for (MaterialWithGroups materialGroup : MaterialWithGroups.values()) {
            if (materialGroup.name().equals(name)) {
                return Optional.of(materialGroup.name());
            }
        }

        for (Material material : Material.values()) {
            if (material.name().equals(name)) {
                return Optional.of(material.name());
            }
        }
        return Optional.empty();
    }

    public static List<String> getMaterialWithGroupsList(boolean acceptAir, boolean acceptItem, boolean acceptBlock) {
        SortedMap<String, String> map = new TreeMap<String, String>();
        for (MaterialWithGroups materialGroup : MaterialWithGroups.values()) {
            map.put(materialGroup.name(), materialGroup.name());
        }
        for (Material material : Material.values()) {
            if (
                    (!SCore.is1v12Less() && material.isAir())
                            || (SCore.is1v12Less() && material.equals(Material.AIR))
            ) {
                if (acceptAir) {
                    map.put(material.name(), material.name());
                } else {
                    continue;
                }
            }
            if (acceptItem && material.isItem()) map.put(material.name(), material.name());
            else if (acceptBlock && material.isBlock()) map.put(material.name(), material.name());
        }
        return new ArrayList<>(map.values());
    }

    public static Material getMaterial(String name) {
        name = name.toUpperCase();
        MaterialWithGroups materialWithGroups = null;
        for (MaterialWithGroups materialGroup : MaterialWithGroups.values()) {
            if (materialGroup.name().equals(name)) {
                materialWithGroups = materialGroup;
            }
        }
        if (materialWithGroups == null) {
            for (Material material : Material.values()) {
                if (material.name().equals(name)) {
                    if (SCore.is1v12Less() && material.equals(Material.AIR)) return Material.BARRIER;
                    else if (!SCore.is1v12Less() && material.isAir()) return Material.BARRIER;
                    else if (!material.isItem()) return Material.BARRIER;
                    return material;
                }
            }
        } else {
            switch (materialWithGroups) {
                case ALL_CHESTS:
                    return FixedMaterial.getMaterial(Collections.singletonList("CHEST"));
                case ALL_FURNACES:
                    return FixedMaterial.getMaterial(Collections.singletonList("FURNACE"));
                case ALL_PLANKS:
                    return FixedMaterial.getMaterial(Collections.singletonList("OAK_PLANKS"));
                case ALL_LOGS:
                    return FixedMaterial.getMaterial(Collections.singletonList("OAK_LOG"));
                case ALL_WOODS:
                    return FixedMaterial.getMaterial(Collections.singletonList("OAK_WOOD"));
                case ALL_ORES:
                    return FixedMaterial.getMaterial(Collections.singletonList("COAL_ORE"));
                case ALL_WOOLS:
                    return FixedMaterial.getMaterial(Collections.singletonList("LIME_WOOL"));
                case ALL_SLABS:
                    return FixedMaterial.getMaterial(Collections.singletonList("STONE_SLAB"));
                case ALL_STAIRS:
                    return FixedMaterial.getMaterial(Collections.singletonList("STONE_STAIRS"));
                case ALL_FENCES:
                    return FixedMaterial.getMaterial(Collections.singletonList("OAK_FENCE"));
                case ALL_SAPLINGS:
                    return FixedMaterial.getMaterial(Collections.singletonList("OAK_SAPLING"));
                case ALL_CROPS:
                    return FixedMaterial.getMaterial(Arrays.asList("WHEAT", "CROPS"));
                case ALL_DOORS:
                    return FixedMaterial.getMaterial(Collections.singletonList("OAK_DOOR"));
                case ALL_TRAPDOORS:
                    return FixedMaterial.getMaterial(Collections.singletonList("OAK_TRAPDOOR"));
                case ALL_BEDS:
                    return FixedMaterial.getMaterial(Collections.singletonList("OAK_BED"));
                case ALL_TERRACOTTA:
                    return FixedMaterial.getMaterial(Collections.singletonList("TERRACOTTA"));
                case ALL_NORMAL_TERRACOTTA:
                    return FixedMaterial.getMaterial(Collections.singletonList("LIME_TERRACOTTA"));
                case ALL_GLAZED_TERRACOTTA:
                    return FixedMaterial.getMaterial(Collections.singletonList("LIME_GLAZED_TERRACOTTA"));
                case ALL_CONCRETE:
                    return FixedMaterial.getMaterial(Collections.singletonList("LIME_CONCRETE"));
                case ALL_GLASS:
                    return FixedMaterial.getMaterial(Collections.singletonList("GLASS"));
                case ALL_STAINED_GLASS:
                    return FixedMaterial.getMaterial(Collections.singletonList("LIME_STAINED_GLASS"));
                case ALL_SHULKER_BOXES:
                    return FixedMaterial.getMaterial(Collections.singletonList("LIME_SHULKER_BOX"));
                case ALL_LEAVES:
                    return FixedMaterial.getMaterial(Collections.singletonList("OAK_LEAVES"));
                case ALL_CARPETS:
                    return FixedMaterial.getMaterial(Arrays.asList("WHITE_CARPET", "CARPET"));
            }
        }
        return Material.STONE;
    }
}
