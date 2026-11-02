package com.ssomar.score.groupchecks;
import com.ssomar.score.SCore;
import mockbukkitutils.TestPlayer;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.command.ConsoleCommandSenderMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.inventory.EntityEquipmentMock;
import org.mockbukkit.mockbukkit.inventory.ItemStackMock;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.Assert.fail;

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
        try {
            MockBukkit.unmock();
        } catch (Exception ignored) {}
    }

    @Test
    public void runActions() {

        evaluateBlockIF();
        evaluateItemIF();

    }

    /**
     * When starting up SCore, it runs its startup console messages. For this JUnit test, we don't want that so we will skip the startup
     * messages and only capture the rest of the messages, which are relevant to the test.<br/>
     * <br/>
     * If needed, it can serve as a console drainer.
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

        /// should succeed
        server.dispatchCommand(console,"score run-block-command block:world,0,0,0 IF 1=1 SETBLOCK RED_WOOL");
        server.getScheduler().performTicks(5);
        Assertions.assertEquals(Material.RED_WOOL, block.getType(), "FLAG 1");

        /// should fail
        server.dispatchCommand(console,"score run-block-command block:world,0,0,0 IF 1=2 SETBLOCK ORANGE_WOOL");
        server.getScheduler().performTicks(5);
        Assertions.assertNotEquals(Material.ORANGE_WOOL, block.getType(), "FLAG 2");

        /// should succeed
        server.dispatchCommand(console,"score run-block-command block:world,0,0,0 IF 1=2||3=3 SETBLOCK YELLOW_WOOL");
        server.getScheduler().performTicks(5);
        Assertions.assertEquals(Material.YELLOW_WOOL, block.getType(), "FLAG 3");

        /// should fail
        server.dispatchCommand(console,"score run-block-command block:world,0,0,0 IF 1=2&&3=3 SETBLOCK GREEN_WOOL");
        server.getScheduler().performTicks(5);
        Assertions.assertNotEquals(Material.GREEN_WOOL, block.getType(), "FLAG 4");

        /// should succeed
        block.setType(Material.DIAMOND_BLOCK);
        server.dispatchCommand(console,"score run-block-command block:world,0,0,0 IF 1=1 CONSOLEMESSAGE BLOCK TYPE : %block%");
        server.getScheduler().performTicks(5);
        String msg = extractConsoleDetails(console).get(0).trim();
        Assertions.assertEquals("BLOCK TYPE : DIAMOND_BLOCK", msg, "FLAG 5");
    }

    /**
     * Checks various item IF executions. Relies on CONSOLEMESSAGE block cmd to report to console for effects
     */
    private void evaluateItemIF() {
        TestPlayer playerMock = new TestPlayer(server, "custom_name", UUID.randomUUID());
        server.addPlayer(playerMock);
        ItemStackMock item = new ItemStackMock(Material.STONE);
        playerMock.getInventory().setItem(0, item);

        // For val reassignments. Repeat var declaration looks ugly
        ItemStack result;

        /// should succeed
        server.dispatchCommand(console,"score run-item-command player:custom_name slot:0 IF 1=1 SET_ITEM_MATERIAL material:RED_WOOL");
        server.getScheduler().performTicks(5);
        result = playerMock.getInventory().getItem(0);
        Assertions.assertEquals(Material.RED_WOOL, result.getType(), "FLAG 1");

        /// should fail
        server.dispatchCommand(console,"score run-item-command player:custom_name slot:0 IF 1=2 SET_ITEM_MATERIAL material:ORANGE_WOOL");
        server.getScheduler().performTicks(5);
        result = playerMock.getInventory().getItem(0);
        Assertions.assertNotEquals(Material.ORANGE_WOOL, result.getType(), "FLAG 2");

        /// should succeed
        server.dispatchCommand(console,"score run-item-command player:custom_name slot:0 IF 1=2||3=3 SET_ITEM_MATERIAL material:YELLOW_WOOL");
        server.getScheduler().performTicks(5);
        result = playerMock.getInventory().getItem(0);
        Assertions.assertEquals(Material.YELLOW_WOOL, result.getType(), "FLAG 3");

        /// should fail
        server.dispatchCommand(console,"score run-item-command player:custom_name slot:0 IF 1=2&&3=3 SET_ITEM_MATERIAL material:GREEN_WOOL");
        server.getScheduler().performTicks(5);
        result = playerMock.getInventory().getItem(0);
        Assertions.assertNotEquals(Material.GREEN_WOOL, result.getType(), "FLAG 4");
    }
}
