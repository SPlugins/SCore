package com.ssomar.score.utils;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/** %block_item_material%: block-only materials are mapped to the item that places them. */
class ToolsListMaterialItemCandidatesTest {

    @Test
    void wallVariantsLoseTheWallPrefix() {
        assertEquals(Collections.singletonList("HORN_CORAL_FAN"), ToolsListMaterial.itemCandidatesOfBlock("HORN_CORAL_WALL_FAN"));
        assertEquals(Collections.singletonList("TORCH"), ToolsListMaterial.itemCandidatesOfBlock("WALL_TORCH"));
        assertEquals(Collections.singletonList("OAK_SIGN"), ToolsListMaterial.itemCandidatesOfBlock("OAK_WALL_SIGN"));
        assertEquals(Collections.singletonList("OAK_HANGING_SIGN"), ToolsListMaterial.itemCandidatesOfBlock("OAK_WALL_HANGING_SIGN"));
        assertEquals(Collections.singletonList("RED_BANNER"), ToolsListMaterial.itemCandidatesOfBlock("RED_WALL_BANNER"));
        assertEquals(Collections.singletonList("PLAYER_HEAD"), ToolsListMaterial.itemCandidatesOfBlock("PLAYER_WALL_HEAD"));
    }

    @Test
    void pottedPlantsAndCrops() {
        assertEquals(Collections.singletonList("DANDELION"), ToolsListMaterial.itemCandidatesOfBlock("POTTED_DANDELION"));
        assertEquals(Collections.singletonList("MELON_SEEDS"), ToolsListMaterial.itemCandidatesOfBlock("ATTACHED_MELON_STEM"));
        assertEquals(Arrays.asList("KELP"), ToolsListMaterial.itemCandidatesOfBlock("KELP_PLANT"));
        assertEquals(Arrays.asList("CAVE_VINES", "GLOW_BERRIES"), ToolsListMaterial.itemCandidatesOfBlock("CAVE_VINES_PLANT"));
    }

    @Test
    void unknownShapesGiveNothing() {
        assertTrue(ToolsListMaterial.itemCandidatesOfBlock("STONE").isEmpty());
        assertTrue(ToolsListMaterial.itemCandidatesOfBlock(null).isEmpty());
    }
}
