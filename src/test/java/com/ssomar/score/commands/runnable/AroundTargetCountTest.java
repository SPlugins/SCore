package com.ssomar.score.commands.runnable;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** %around_target_count%: only replaced for an AROUND / MOB_AROUND (count not null), +N / -N shorthand, nested steps kept. */
class AroundTargetCountTest {

    @Test
    void replacedWithTheCount() {
        assertEquals("heal 3", CommmandThatRunsCommand.replaceAroundTargetCount("heal %around_target_count%", 3));
        assertEquals("say 0 targets", CommmandThatRunsCommand.replaceAroundTargetCount("say %around_target_count% targets", 0));
        assertEquals("a 2 b 2", CommmandThatRunsCommand.replaceAroundTargetCount("a %around_target_count% b %around_target_count%", 2));
    }

    @Test
    void shorthandIsSupported() {
        assertEquals("heal 5", CommmandThatRunsCommand.replaceAroundTargetCount("heal %around_target_count%+2", 3));
        assertEquals("heal 2", CommmandThatRunsCommand.replaceAroundTargetCount("heal %around_target_count%-1", 3));
    }

    @Test
    void insideAPapiPlaceholderTheValueIsPlain() {
        assertEquals("heal %math_0_2*4%", CommmandThatRunsCommand.replaceAroundTargetCount("heal %math_0_2*%around_target_count%%", 4));
    }

    @Test
    void notReplacedWithoutCount() {
        String s = "say %around_target_count%";
        assertSame(s, CommmandThatRunsCommand.replaceAroundTargetCount(s, null));
        assertNull(CommmandThatRunsCommand.replaceAroundTargetCount(null, 2));
    }

    @Test
    void otherPlaceholdersAndNestedStepsUntouched() {
        String s = "say %around_target_name% %around_target_count::step1%";
        assertSame(s, CommmandThatRunsCommand.replaceAroundTargetCount(s, 2));
        // the nested level, once its step particle is replaced, gets its own count
        assertEquals("say 7", CommmandThatRunsCommand.replaceAroundTargetCount("say %around_target_count%", 7));
    }
}
