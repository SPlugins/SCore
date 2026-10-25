package com.ssomar.score.commands.runnable;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommmandThatRunsCommandSplitTest {

    private static final List<String> NESTING = Arrays.asList("IF", "AROUND", "MOB_AROUND", "NEAREST");

    @Test
    void detectsNestedCommandWithWordBoundary() {
        assertTrue(CommmandThatRunsCommand.startsWithCommandThatRunsCommands("IF 1<0 say a", NESTING));
        assertTrue(CommmandThatRunsCommand.startsWithCommandThatRunsCommands("  /if 1<0 say a", NESTING));
        assertTrue(CommmandThatRunsCommand.startsWithCommandThatRunsCommands("MOB_AROUND 5 say a", NESTING));
        assertFalse(CommmandThatRunsCommand.startsWithCommandThatRunsCommands("IFX say a", NESTING));
        assertFalse(CommmandThatRunsCommand.startsWithCommandThatRunsCommands("say IF a", NESTING));
        assertFalse(CommmandThatRunsCommand.startsWithCommandThatRunsCommands("", NESTING));
    }

    @Test
    void nestedIfKeepsTheWholeRemainder() {
        // IF 2>1 IF 1<0 say test1 <+> say test2  ->  the outer IF hands everything to the inner IF
        String[] outer = CommmandThatRunsCommand.splitCommands("IF 1<0 say test1 <+> say test2", 0, NESTING);
        assertArrayEquals(new String[]{"IF 1<0 say test1 <+> say test2"}, outer);

        // the inner IF (step 1) then splits on the plain separator
        String[] inner = CommmandThatRunsCommand.splitCommands("say test1 <+> say test2", 1, NESTING);
        assertArrayEquals(new String[]{"say test1 ", " say test2"}, inner);
    }

    @Test
    void flatCaseStillSplits() {
        assertArrayEquals(new String[]{"cmd1 ", " cmd2"}, CommmandThatRunsCommand.splitCommands("cmd1 <+> cmd2", 0, NESTING));
        assertArrayEquals(new String[]{"cmd1"}, CommmandThatRunsCommand.splitCommands("cmd1", 0, NESTING));
        // a nested command that is not the first command does not prevent the split
        assertArrayEquals(new String[]{"cmd0 ", " IF y cmd1"}, CommmandThatRunsCommand.splitCommands("cmd0 <+> IF y cmd1", 0, NESTING));
    }

    @Test
    void explicitStepSyntaxIsUnchanged() {
        String line = "AROUND 3 cmd1 <+::step1> cmd2 <+> cmd3";
        // step 0 splits on <+> only, even though the line starts with a nesting command
        assertArrayEquals(new String[]{"AROUND 3 cmd1 <+::step1> cmd2 ", " cmd3"}, CommmandThatRunsCommand.splitCommands(line, 0, NESTING));
        // step 1 splits on its own particle only
        assertArrayEquals(new String[]{"cmd1 ", " cmd2 <+::step2> cmd3"}, CommmandThatRunsCommand.splitCommands("cmd1 <+::step1> cmd2 <+::step2> cmd3", 1, NESTING));
        // step 1 without its particle: single command
        assertArrayEquals(new String[]{"cmd1 <+::step2> cmd2"}, CommmandThatRunsCommand.splitCommands("cmd1 <+::step2> cmd2", 1, NESTING));
    }
}
