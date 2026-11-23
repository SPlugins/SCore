package com.ssomar.score.utils.placeholders;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** replaceCalculPlaceholder returns before parsing the value when the string doesn't contain the placeholder; the replacements don't change. */
class ReplaceCalculPlaceholderTest {

    @Test
    void absentPlaceholderReturnsTheSameString() {
        String s = "IF %block_y%<%player_y%+2 score run-block-command";
        assertSame(s, PlaceholdersInterface.replaceCalculPlaceholder(s, "%player_x%", "12.5", false));
        assertSame(s, PlaceholdersInterface.replaceCalculPlaceholder(s, "%player_direction%", "N", false));
        assertSame(s, PlaceholdersInterface.replaceCalculPlaceholder(s, "%x_int%", "abc", true));
    }

    @Test
    void presentPlaceholderIsStillReplaced() {
        assertEquals("y=121.0", PlaceholdersInterface.replaceCalculPlaceholder("y=%player_y%+1", "%player_y%", "120.0", false));
        assertEquals("y=121", PlaceholdersInterface.replaceCalculPlaceholder("y=%player_y%+1", "%player_y%", "120.0", true));
        assertEquals("y=119", PlaceholdersInterface.replaceCalculPlaceholder("y=%player_y%-1", "%player_y%", "120", true));
        assertEquals("d=N and N", PlaceholdersInterface.replaceCalculPlaceholder("d=%d% and %d%", "%d%", "N", false));
        assertEquals("cost $5", PlaceholdersInterface.replaceCalculPlaceholder("cost %price%", "%price%", "$5", false));
        assertEquals("lvl IV", PlaceholdersInterface.replaceCalculPlaceholder("lvl %l%", "%l%", "4", "", "", true, true));
    }
}
