package com.ssomar.score.commands.runnable.block.commands;

import com.ssomar.score.commands.runnable.CommandSetting;
import com.ssomar.score.commands.runnable.SCommand;
import com.ssomar.score.commands.runnable.SCommandToExec;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * runActivatorOnBlocks (read by ExecutableItems) is off by default and never shifts the positional arguments of the break commands.
 * triggerEvent is not a setting (a VEIN_BREAKER setting until February 2026, still written by old editor templates): it is read as before.
 */
class BreakCommandsRunActivatorOnBlocksTest {

    static SCommandToExec extract(SCommand command, String entry) {
        SCommandToExec toExec = new SCommandToExec(command);
        toExec.extractSettings(entry);
        return toExec;
    }

    @Test
    void veinBreakerRunActivatorOnBlocksIsOffByDefault() {
        SCommandToExec s = extract(new VeinBreaker(), "VEIN_BREAKER maxVeinSize:3");
        assertEquals(3, s.getSettingValue("maxVeinSize"));
        assertEquals(true, s.getSettingValue("createBBEvent"));
        assertEquals(false, s.getSettingValue("smelt"));
        assertEquals(false, s.getSettingValue("runActivatorOnBlocks"));
        assertEquals(false, extract(new VeinBreaker(), "VEIN_BREAKER").getSettingValue("runActivatorOnBlocks"));
    }

    @Test
    void veinBreakerRunActivatorOnBlocks() {
        SCommandToExec s = extract(new VeinBreaker(), "VEIN_BREAKER maxVeinSize:3 runActivatorOnBlocks:true");
        assertEquals(3, s.getSettingValue("maxVeinSize"));
        assertEquals(true, s.getSettingValue("createBBEvent"));
        assertEquals(true, s.getSettingValue("runActivatorOnBlocks"));
        assertEquals(false, extract(new VeinBreaker(), "VEIN_BREAKER maxVeinSize:3 runActivatorOnBlocks:false").getSettingValue("runActivatorOnBlocks"));
    }

    @Test
    void runActivatorOnBlocksDoesNotShiftThePositionalArguments() {
        SCommandToExec vein = extract(new VeinBreaker(), "VEIN_BREAKER 20 false runActivatorOnBlocks:true");
        assertEquals(20, vein.getSettingValue("maxVeinSize"));
        assertEquals(false, vein.getSettingValue("createBBEvent"));
        assertEquals(false, vein.getSettingValue("smelt"));
        assertEquals(true, vein.getSettingValue("runActivatorOnBlocks"));

        SCommandToExec cube = extract(new MineInCube(), "MINEINCUBE 2 true false runActivatorOnBlocks:true");
        assertEquals(2, cube.getSettingValue("radius"));
        assertEquals(true, cube.getSettingValue("droploot"));
        assertEquals(false, cube.getSettingValue("createEvent"));
        assertEquals(false, cube.getSettingValue("offsetBreak"));
        assertEquals(false, cube.getSettingValue("smelt"));
        assertEquals(true, cube.getSettingValue("runActivatorOnBlocks"));

        SCommandToExec old = extract(new MineInCube(), "MINEINCUBE 2 true true true");
        assertEquals(true, old.getSettingValue("offsetBreak"));
        assertEquals(false, old.getSettingValue("runActivatorOnBlocks"));
    }

    @Test
    void triggerEventIsReadAsBefore() {
        SCommandToExec template = extract(new VeinBreaker(), "VEIN_BREAKER maxVeinSize:10 triggerEvent:true");
        assertEquals(10, template.getSettingValue("maxVeinSize"));
        assertEquals(true, template.getSettingValue("createBBEvent"));
        assertEquals(false, template.getSettingValue("runActivatorOnBlocks"));
        assertNull(template.getSettingValue("triggerEvent"));
        // a positional slot, as before: 20 lands on createBBEvent and is not a boolean
        SCommandToExec first = extract(new VeinBreaker(), "VEIN_BREAKER triggerEvent:false 20");
        assertEquals(10, first.getSettingValue("maxVeinSize"));
        assertEquals(true, first.getSettingValue("createBBEvent"));
        assertFalse(new VeinBreaker().verifySettings("VEIN_BREAKER maxVeinSize:10 triggerEvent:yes").isPresent());

        SCommandToExec cube = extract(new MineInCube(), "MINEINCUBE 2 true triggerEvent:true");
        assertEquals(false, cube.getSettingValue("createEvent"));
        assertEquals(false, cube.getSettingValue("runActivatorOnBlocks"));

        assertEquals(Arrays.asList("2", "triggerEvent:true", "true"), FarmInCube.positionalArgs(Arrays.asList("2", "triggerEvent:true", "true")));
        assertFalse(FarmInCube.runActivatorOnBlocks(Arrays.asList("2", "true", "true", "false", "triggerEvent:true")));
    }

    @Test
    void invalidRunActivatorOnBlocksIsReported() {
        assertTrue(new VeinBreaker().verifySettings("VEIN_BREAKER runActivatorOnBlocks:yes").isPresent());
        assertFalse(new VeinBreaker().verifySettings("VEIN_BREAKER maxVeinSize:3 runActivatorOnBlocks:true").isPresent());
        assertFalse(new MineInCube().verifySettings("MINEINCUBE radius:2 runActivatorOnBlocks:true").isPresent());
    }

    @Test
    void veinBreakerTemplateOnlyShowsItsSettings() {
        VeinBreaker vein = new VeinBreaker();
        String template = vein.getTemplate();
        assertFalse(template.contains("triggerEvent"));
        for (String token : template.split(" ")) {
            if (!token.contains(":")) continue;
            String name = token.split(":")[0];
            boolean known = false;
            for (CommandSetting setting : vein.getSettings()) known |= setting.getNames().contains(name);
            assertTrue(known, token);
        }
        assertFalse(vein.verifySettings(template).isPresent());
        assertEquals(false, extract(vein, template).getSettingValue("runActivatorOnBlocks"));
        assertFalse(new MineInCube().getTemplate().contains("triggerEvent"));
        assertFalse(new FarmInCube().getTemplate().contains("triggerEvent"));
    }

    @Test
    void farmInCubeReadsRunActivatorOnBlocksOutsideItsPositionalArguments() {
        assertEquals(Arrays.asList("2", "true"), FarmInCube.positionalArgs(Arrays.asList("2", "runActivatorOnBlocks:true", "true")));
        assertEquals(Arrays.asList("2", "true", "true", "false", "true"), FarmInCube.positionalArgs(Arrays.asList("2", "true", "true", "false", "true")));
        assertTrue(FarmInCube.runActivatorOnBlocks(Arrays.asList("2", "runActivatorOnBlocks:true")));
        assertTrue(FarmInCube.runActivatorOnBlocks(Arrays.asList("2", "runActivatorOnBlocks:TRUE")));
        assertFalse(FarmInCube.runActivatorOnBlocks(Arrays.asList("2", "runActivatorOnBlocks:false")));
        assertFalse(FarmInCube.runActivatorOnBlocks(Arrays.asList("2", "true", "true", "false", "true")));
        assertFalse(FarmInCube.runActivatorOnBlocks(Collections.emptyList()));
    }
}
