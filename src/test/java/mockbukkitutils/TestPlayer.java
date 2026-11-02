package mockbukkitutils;

import org.mockbukkit.mockbukkit.entity.PlayerMock;

/**
 * A custom PlayerMock class that attmepts to override so-called missing functions that are present in the real Player class. This is to avoid errors when running tests that rely on these functions.
 */
public class TestPlayer extends PlayerMock {
    public TestPlayer(org.mockbukkit.mockbukkit.ServerMock server, String name, java.util.UUID uuid) {
        super(server, name, uuid);
    }

    @Override
    public double getLastDamage() {
        return 0;
    }

    @Override
    public float getAttackCooldown() {
        return 0;
    }
}
