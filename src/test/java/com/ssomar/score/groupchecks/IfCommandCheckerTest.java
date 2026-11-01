package com.ssomar.score.groupchecks;
import com.ssomar.score.SCore;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.command.ConsoleCommandSenderMock;

import java.util.ArrayList;

/**
 * Utilizes MockBukkit to simulate a Minecraft server environment for testing the "if" command checker functionality.
 */
public class IfCommandCheckerTest {
    private ServerMock server;
    private SCore plugin;
    private World world;
    private ConsoleCommandSenderMock console;

    @BeforeEach
    public void setUp() {
        // Start the mock server
        server = MockBukkit.mock();
        // Load your plugin
        plugin = MockBukkit.load(SCore.class);
        // Create a mock world
        world = server.addSimpleWorld("world");
        // Get the console command sender
        console = server.getConsoleSender();
    }

    @AfterEach
    public void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
    }

    @Test
    public void runActions() {

        ArrayList<String> consoleDetails = extractConsoleDetails(console);
    }

    /**
     * When starting up SCore, it runs its startup console messages. For this JUnit test, we don't want that so we will skip the startup
     * messages and only capture the rest of the messages, which are relevant to the test.
     * @param console
     * @return
     */
    private ArrayList<String> extractConsoleDetails(ConsoleCommandSenderMock console) {
        ArrayList<String> consoleRecords = new ArrayList<>();
        String msg;

        int markerCounter = 0;
        while ((msg = console.nextMessage()) != null) {
            if (markerCounter >= 2) {
                consoleRecords.add(msg);
            }
            if (msg.equals("§7================ §eSCore §7================")) {
                markerCounter++;
            }
        }
        return consoleRecords;
    }

    /**
     * Checks various block IF executions. Relies on CONSOLEMESSAGE block cmd to report to console for effects
     */
    private void evaluateBlockIF() {
        Block block = world.getBlockAt(0,0,0);
        block.setType(Material.BIRCH_LOG);

        /// console output should show up
        server.dispatchCommand(console,"score run-block-command block:world,0,0,0 IF 1=1 CONSOLEMESSAGE FLAG 1");
        server.getScheduler().performTicks(5);

        /// console output should not show up
        server.dispatchCommand(console,"score run-block-command block:world,0,0,0 IF 1=2 CONSOLEMESSAGE FLAG 2");
        server.getScheduler().performTicks(5);

        /// console output should show up
        server.dispatchCommand(console,"score run-block-command block:world,0,0,0 IF 1=2||3=3 CONSOLEMESSAGE FLAG 3");
        server.getScheduler().performTicks(5);

        /// console output should not show up
        server.dispatchCommand(console,"score run-block-command block:world,0,0,0 IF 1=2&&3=3 CONSOLEMESSAGE FLAG 3");
        server.getScheduler().performTicks(5);

    }
}
