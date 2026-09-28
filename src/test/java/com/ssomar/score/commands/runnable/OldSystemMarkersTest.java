package com.ssomar.score.commands.runnable;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OldSystemMarkersTest {

    @Test
    void oldSystemMarkersAreConverted() {
        assertEquals("say %player_name%", CommmandThatRunsCommand.stripOldSystemMarkers("say %::player_name::%"));
        assertEquals("IF %player_has_potioneffect_invisibility%=no say",
                CommmandThatRunsCommand.stripOldSystemMarkers("IF %::player_has_potioneffect_invisibility::%=no say"));
    }

    @Test
    void weightedRandomSeparatorNextToAPlaceholderIsKept() {
        String weight = "weightedrandom run <%changeoutput_x_ifmatch:1700_else:2000%::> <1::crazycrates give>";
        assertEquals(weight, CommmandThatRunsCommand.stripOldSystemMarkers(weight));
        String payoff = "weightedrandom run <2000::> <1::%player_name% give>";
        assertEquals(payoff, CommmandThatRunsCommand.stripOldSystemMarkers(payoff));
    }

    @Test
    void markersOfTwoEntriesAreNeverPaired() {
        String s = "<%a%::> <1::%b%>";
        assertEquals(s, CommmandThatRunsCommand.stripOldSystemMarkers(s));
    }

    @Test
    void countsPercents() {
        assertEquals(4, CommmandThatRunsCommand.countPercent("%javascript_x_%block_lower%,a%"));
        assertEquals(0, CommmandThatRunsCommand.countPercent("say"));
    }
}
