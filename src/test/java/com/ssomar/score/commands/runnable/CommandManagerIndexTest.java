package com.ssomar.score.commands.runnable;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.ssomar.score.commands.runnable.CommandNameMatchTest.named;
import static org.junit.jupiter.api.Assertions.*;

/**
 * getCommand looks the names up by their first word instead of asking every command: same command as the linear scan it
 * replaced (longest name, first registered on a tie), and the index follows changes of the commands list.
 */
class CommandManagerIndexTest {

    /** The getCommand of 2026-09-14 */
    private static SCommand linearScan(List<SCommand> commands, String entry) {
        SCommand best = null;
        int bestLen = -1;
        for (SCommand command : commands) {
            int len = command.matchedNameLength(entry);
            if (len > bestLen) {
                bestLen = len;
                best = command;
            }
        }
        return best;
    }

    private static CommandManager<SCommand> manager(List<SCommand> commands) {
        CommandManager<SCommand> m = new CommandManager<SCommand>() {};
        m.setCommands(commands);
        return m;
    }

    @Test
    void sameCommandAsTheLinearScan() {
        List<SCommand> commands = new ArrayList<>(Arrays.asList(
                named("BREAK"), named("BREAK"), named("TELEPORT"), named("TELEPORT POSITION", "TELEPORT_POSITION"),
                named("SETBLOCK"), named("SETBLOCKPOS"), named("IF"), named("SET-ITEM"), named("MOB_AROUND"), named("AROUND"),
                named("SENDMESSAGE"), named("DELAY"), named("DELAYTICK"), named("2X"), named("[OFFLINE]"), named("score run-block-command")));
        CommandManager<SCommand> m = manager(commands);
        String[] entries = {"BREAK", "break", "BREAK:1", "BREAKING", "breakinradius 3", "TELEPORT POSITION 1 2 3", "teleport 1 2",
                "TELEPORT_POSITION 1", "TELEPORT POSITIONS", "SETBLOCK stone", "SETBLOCKPOS 1 2 3", "SETBLOCK_X", "IF %a%=1 SENDMESSAGE hi", "IF",
                "SET-ITEM x", "SET x", "SET-ITEMS", "MOB_AROUND 3", "AROUND 3", "SENDMESSAGE&ahello", "DELAY 5", "DELAYTICK 5", "2X go",
                "[OFFLINE] SENDMESSAGE", "score run-block-command block:world,1,2,3 BREAK", "SCORE RUN-BLOCK-COMMAND x", "score run",
                "ıF %a%=1", "ſETBLOCK stone", "ßREAK", "éBREAK", "", " BREAK", "%player% BREAK", "minecraft:tp @s", "tp @s ~ ~ ~",
                "İF %a%=1", "DELAYTİCK 5", "TELEPORT POSİTİON 1 2 3", "KILL @e", "kill @e"};
        commands.add(named("KILL"));
        for (String entry : entries) {
            assertSame(linearScan(commands, entry), m.getCommand(entry).orElse(null), entry);
        }
        assertSame(commands.get(6), m.getCommand("İF %a%=1").orElse(null), "İ of a Turkish caps lock: IF, as with the linear scan");
        assertSame(commands.get(commands.size() - 1), m.getCommand("KILL @e").orElse(null), "Kelvin sign: KILL, as with the linear scan");
        assertSame(commands.get(0), m.getCommand("BREAK").get(), "a tie keeps the first registered command");
        assertEquals(Optional.empty(), m.getCommand(null));
    }

    /** Every name, written with every case variant regionMatches(true) accepts, gives the command of the linear scan */
    @Test
    void sameCommandForEveryCaseVariant() {
        List<SCommand> commands = new ArrayList<>(Arrays.asList(
                named("MINEINCUBE"), named("MINE"), named("KILL"), named("SETBLOCK"), named("SETBLOCKPOS"), named("IF"),
                named("TELEPORT"), named("TELEPORT POSITION"), named("DELAY"), named("DELAYTICK"), named("SENDMESSAGE")));
        CommandManager<SCommand> m = manager(commands);
        java.util.Random random = new java.util.Random(1016);
        String[] suffixes = {"", " 2", ":1", "_X", "X", "&a", " POSITION 1", "TICK 3"};
        for (SCommand command : commands) {
            String name = command.getNames().get(0);
            for (int n = 0; n < 200; n++) {
                StringBuilder entry = new StringBuilder();
                for (char c : name.toCharArray()) entry.append(variant(c, random));
                entry.append(suffixes[random.nextInt(suffixes.length)]);
                String e = entry.toString();
                assertSame(linearScan(commands, e), m.getCommand(e).orElse(null), e);
            }
        }
    }

    private static char variant(char c, java.util.Random random) {
        String options;
        switch (Character.toUpperCase(c)) {
            case 'I': options = "Iiİı"; break;
            case 'K': options = "KkK"; break;
            case 'S': options = "Ssſ"; break;
            default: options = "" + Character.toUpperCase(c) + Character.toLowerCase(c);
        }
        return options.charAt(random.nextInt(options.length()));
    }

    @Test
    void indexFollowsTheCommandsList() {
        List<SCommand> commands = new ArrayList<>(Arrays.asList(named("BREAK")));
        CommandManager<SCommand> m = manager(commands);
        assertFalse(m.getCommand("LAUNCH ARROW").isPresent());
        SCommand launch = named("LAUNCH");
        commands.add(launch);
        assertSame(launch, m.getCommand("LAUNCH ARROW").orElse(null), "command added to the list");
        SCommand other = named("LAUNCH");
        m.setCommands(new ArrayList<>(Arrays.asList(other)));
        assertSame(other, m.getCommand("LAUNCH ARROW").orElse(null), "list replaced");
    }
}
