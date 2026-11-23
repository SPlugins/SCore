package com.ssomar.score.commands.runnable.util.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** A DELAYTICK / DELAY is a directive only at the start of the command or of a +++ fragment. */
class DelayTickDirectiveTest {

    @Test
    void directiveAtTheStart() {
        assertEquals("DELAYTICK 3", DelayTick.delayDirective("DELAYTICK 3", DelayTick.DELAY_TICK_NAMES));
        assertEquals(" DELAYTICK 3 ", DelayTick.delayDirective("LOCATED_LAUNCH a +++ DELAYTICK 3 +++ LOCATED_LAUNCH b", DelayTick.DELAY_TICK_NAMES));
        assertEquals("DELAY 2", DelayTick.delayDirective("DELAY 2", DelayTick.DELAY_SECONDS_NAMES));
        assertTrue(DelayTick.checkStartsWith("  DELAY_TICK 20"));
    }

    @Test
    void delayInsideAnotherCommandsPayloadIsNotADirective() {
        String line = "cooldowncommand silent %player% nightspirebowarrows 600s \"LOCATED_LAUNCH arrow ,, DELAYTICK 3 ,, LOCATED_LAUNCH arrow\"";
        assertNull(DelayTick.delayDirective(line, DelayTick.DELAY_TICK_NAMES));
        assertNull(DelayTick.delayDirective("runcommandlater 20 \"DELAY 5\"", DelayTick.DELAY_SECONDS_NAMES));
        assertFalse(DelayTick.checkStartsWith("say DELAYTICK 3"));
        // the legacy helper still matches it, other callers rely on that
        assertTrue(DelayTick.checkContains(line));
    }

    @Test
    void delayTickIsNotReadAsDelay() {
        assertNull(DelayTick.delayDirective("DELAYTICK 3", DelayTick.DELAY_SECONDS_NAMES));
    }
}
