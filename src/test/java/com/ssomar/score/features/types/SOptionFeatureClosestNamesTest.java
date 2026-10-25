package com.ssomar.score.features.types;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** An unknown activator option is reported with the closest existing names. */
class SOptionFeatureClosestNamesTest {

    private static final List<String> OPTIONS = Arrays.asList(
            "PLAYER_ALL_CLICK", "PLAYER_BLOCK_BREAK", "PLAYER_LEFT_CLICK", "PLAYER_RIGHT_CLICK", "LOOP", "PLAYER_DEATH");

    @Test
    void typoOfBlockBreakComesFirst() {
        List<String> closest = SOptionFeature.closestNames("PLAYER_BREAK_BLOCK", OPTIONS, 3);
        assertEquals("PLAYER_BLOCK_BREAK", closest.get(0));
        assertEquals(3, closest.size());
    }

    @Test
    void caseInsensitiveAndBounded() {
        List<String> closest = SOptionFeature.closestNames("loop", OPTIONS, 1);
        assertEquals(Arrays.asList("LOOP"), closest);
        assertTrue(SOptionFeature.closestNames("x", Arrays.asList(), 3).isEmpty());
    }
}
