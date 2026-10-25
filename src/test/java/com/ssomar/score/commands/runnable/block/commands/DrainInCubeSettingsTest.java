package com.ssomar.score.commands.runnable.block.commands;

import com.ssomar.score.commands.runnable.SCommandToExec;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DRAININCUBE waterlogged:true (opt-in, false by default) also dries the waterlogged blocks. The command moved to the
 * settings mode for it: the old arguments {radius} [LAVA or WATER] must reach the command unchanged.
 */
class DrainInCubeSettingsTest {

    private static SCommandToExec extract(String entry) {
        SCommandToExec exec = new SCommandToExec(new DrainInCube());
        exec.extractSettings(entry);
        return exec;
    }

    @Test
    void oldArgumentsAreUnchangedAndWaterloggedIsOffByDefault() {
        SCommandToExec exec = extract("DRAININCUBE 3 WATER");
        assertEquals(Arrays.asList("3", "WATER"), exec.getOtherArgs());
        assertEquals(false, exec.getSettingValue("waterlogged"));

        exec = extract("DRAININCUBE *12 LAVA");
        assertEquals(Arrays.asList("*12", "LAVA"), exec.getOtherArgs());
        assertEquals(false, exec.getSettingValue("waterlogged"));

        exec = extract("DRAININCUBE 4");
        assertEquals(Collections.singletonList("4"), exec.getOtherArgs());
        assertEquals(false, exec.getSettingValue("waterlogged"));
    }

    @Test
    void waterloggedSettingIsReadWhereverItIs() {
        SCommandToExec exec = extract("DRAININCUBE 3 WATER waterlogged:true");
        assertEquals(Arrays.asList("3", "WATER"), exec.getOtherArgs());
        assertEquals(true, exec.getSettingValue("waterlogged"));

        exec = extract("DRAININCUBE waterlogged:true 2");
        assertEquals(Collections.singletonList("2"), exec.getOtherArgs());
        assertEquals(true, exec.getSettingValue("waterlogged"));
    }

    @Test
    void verification() {
        DrainInCube drain = new DrainInCube();
        assertFalse(drain.verify(Arrays.asList("3", "WATER"), false).isPresent());
        assertFalse(drain.verify(Arrays.asList("3", "WATER", "waterlogged:true"), false).isPresent());
        assertTrue(drain.verify(Arrays.asList("3", "WATER", "waterlogged:maybe"), false).isPresent());
    }
}
