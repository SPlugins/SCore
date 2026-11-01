package com.ssomar.score.groupchecks;
import com.ssomar.score.SCore;
import lombok.var;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.plugin.PluginMock;


public class IfCommandChecker {
    private ServerMock server;
    private SCore plugin;
    private World world;

    @BeforeEach
    public void setUp() {
        // Start the mock server
        server = MockBukkit.mock();
        // Load your plugin
        plugin = MockBukkit.load(SCore.class);
        // Create a mock world
        world = server.addSimpleWorld("test");
    }

    @AfterEach
    public void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
    }

    @Test
    public void thisTestWillFail() {
        System.out.println("[TEMP] RUN TEST");
    }

}
