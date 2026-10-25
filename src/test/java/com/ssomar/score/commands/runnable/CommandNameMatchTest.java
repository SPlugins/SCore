package com.ssomar.score.commands.runnable;

import com.ssomar.score.commands.runnable.util.commands.DelayTick;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** SCore must not claim another plugin's command that merely starts with an SCore command name. */
class CommandNameMatchTest {

    static SCommand named(String... names) {
        List<String> list = Arrays.asList(names);
        return new DelayTick() {
            @Override
            public List<String> getNames() {
                return list;
            }
        };
    }

    static CommandManager<SCommand> manager(SCommand... commands) {
        CommandManager<SCommand> m = new CommandManager<SCommand>() {};
        m.setCommands(Arrays.asList(commands));
        return m;
    }

    @Test
    void nameNeedsAWordBoundary() {
        assertTrue(SCommand.startsWithName("LAUNCH ARROW 3", "LAUNCH"));
        assertTrue(SCommand.startsWithName("launch ARROW 3", "LAUNCH"));
        assertTrue(SCommand.startsWithName("LAUNCH", "LAUNCH"));
        assertTrue(SCommand.startsWithName("SENDMESSAGE&ahello", "SENDMESSAGE"));
        assertFalse(SCommand.startsWithName("launchprojectile ARROW 3s 0 1", "LAUNCH"));
        assertFalse(SCommand.startsWithName("breakinradius 3", "BREAK"));
        assertFalse(SCommand.startsWithName("SETBLOCK_X", "SETBLOCK"));
        assertFalse(SCommand.startsWithName("LAUNC", "LAUNCH"));
    }

    @Test
    void otherPluginCommandIsNotClaimed() {
        CommandManager<SCommand> m = manager(named("LAUNCH"), named("BREAK"));
        assertEquals(Optional.empty(), m.getCommand("launchprojectile ARROW 3s 0 1"));
        assertEquals(Optional.empty(), m.getCommand("breakinradius 3 true"));
        assertTrue(m.getCommand("LAUNCH ARROW").isPresent());
    }

    @Test
    void longestNameWinsWhateverTheRegistrationOrder() {
        SCommand teleport = named("TELEPORT");
        SCommand teleportPosition = named("TELEPORT POSITION", "TELEPORT_POSITION");
        assertSame(teleportPosition, manager(teleport, teleportPosition).getCommand("TELEPORT POSITION 1 2 3").get());
        assertSame(teleportPosition, manager(teleportPosition, teleport).getCommand("TELEPORT POSITION 1 2 3").get());
        assertSame(teleport, manager(teleportPosition, teleport).getCommand("TELEPORT 1 2 3").get());
    }

    @Test
    void argsAreStrippedCaseInsensitively() {
        SCommand launch = named("LAUNCH");
        CommandManager<SCommand> m = manager(launch);
        assertEquals(Arrays.asList("ARROW", "3"), m.getArgs(launch, "launch ARROW 3"));
        assertEquals(Arrays.asList("ARROW", "3"), m.getArgs(launch, "LAUNCH ARROW 3"));
        assertEquals(Collections.emptyList(), m.getArgs(launch, "LAUNCH"));
    }
}
