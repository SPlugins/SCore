package mockbukkitutils;

import org.mockbukkit.mockbukkit.entity.EntityMock;

/**
 * A custom EntityMock class that attempts to override so-called missing functions that are present in the real Entity class. This is to avoid errors when running tests that rely on these functions.
 */
public class TestEntity extends EntityMock {
    public TestEntity(org.mockbukkit.mockbukkit.ServerMock server, String name, java.util.UUID uuid) {
        super(server, uuid);
    }

    public double getLastDamage() {
        return 0;
    }
}
