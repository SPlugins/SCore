package com.ssomar.score.commands.runnable;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Argument absent : le défaut est rendu dans le type de l'argument (SPAWN_ENTITY sans offsetDistance, 2026-11-20). */
class CommandSettingDefaultTypeTest {

    @Test
    void integerDefaultOfADoubleSettingIsADouble() {
        Object v = new CommandSetting("offsetDistance", -1, Double.class, 0).getValue(null);
        assertTrue(v instanceof Double);
        assertEquals(0.0, (double) v);
        assertEquals(-1.0, (double) new CommandSetting("pitch", -1, Double.class, -1).getValue(null));
    }

    @Test
    void otherNumericTypesAreConvertedToo() {
        assertEquals(2, (int) new CommandSetting("a", -1, Integer.class, 2.7).getValue(null));
        assertEquals(1.5f, (float) new CommandSetting("b", -1, Float.class, 1.5).getValue(null));
    }

    @Test
    void nonNumericAndNullDefaultsAreUnchanged() {
        assertEquals("x", new CommandSetting("c", -1, String.class, "x").getValue(null));
        assertEquals(true, new CommandSetting("d", -1, Boolean.class, true).getValue(null));
        assertEquals(null, new CommandSetting("e", -1, Double.class, null).getValue(null));
        assertEquals(3.0, (double) new CommandSetting("f", -1, Double.class, 0).getValue("3"));
    }
}
