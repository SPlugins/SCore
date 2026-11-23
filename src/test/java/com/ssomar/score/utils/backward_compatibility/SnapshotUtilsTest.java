package com.ssomar.score.utils.backward_compatibility;

import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** getHolder(false) / getState(false) (no block entity snapshot) when Paper has them, classic calls otherwise. */
class SnapshotUtilsTest {

    /** Records the calls as "name(args)" and returns null. */
    @SuppressWarnings("unchecked")
    private static <T> T recorder(Class<T> clazz, List<String> calls) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, (proxy, method, args) -> {
            StringBuilder sb = new StringBuilder(method.getName()).append('(');
            if (args != null) for (int i = 0; i < args.length; i++) sb.append(i > 0 ? "," : "").append(args[i]);
            calls.add(sb.append(')').toString());
            return null;
        });
    }

    /** Spigot API: no getHolder(boolean) / getState(boolean). */
    interface SpigotLikeInventory {
        Object getHolder();
    }

    @Test
    void detectsThePaperMethodsOfTheCompiledApi() {
        // SCore compiles against paper-api: the overloads exist
        assertTrue(SnapshotUtils.isHolderNoSnapshotSupported());
        assertTrue(SnapshotUtils.isStateNoSnapshotSupported());
        assertFalse(SnapshotUtils.hasMethod(SpigotLikeInventory.class, "getHolder", boolean.class));
        assertTrue(SnapshotUtils.hasMethod(SpigotLikeInventory.class, "getHolder"));
    }

    @Test
    void holderWithoutSnapshotWhenSupported() {
        List<String> calls = new ArrayList<>();
        SnapshotUtils.getHolderNoSnapshot(recorder(Inventory.class, calls));
        assertEquals(Collections.singletonList("getHolder(false)"), calls);
    }

    @Test
    void holderFallbackWhenNotSupported() {
        List<String> calls = new ArrayList<>();
        SnapshotUtils.getHolder(recorder(Inventory.class, calls), false);
        assertEquals(Collections.singletonList("getHolder()"), calls);
    }

    @Test
    void stateWithoutSnapshotWhenSupported() {
        List<String> calls = new ArrayList<>();
        SnapshotUtils.getStateNoSnapshot(recorder(Block.class, calls));
        assertEquals(Collections.singletonList("getState(false)"), calls);
    }

    @Test
    void stateFallbackWhenNotSupported() {
        List<String> calls = new ArrayList<>();
        SnapshotUtils.getState(recorder(Block.class, calls), false);
        assertEquals(Collections.singletonList("getState()"), calls);
    }

    @Test
    void fallbackWhenTheImplementationLacksThePaperMethod() {
        List<String> calls = new ArrayList<>();
        Inventory inv = (Inventory) Proxy.newProxyInstance(Inventory.class.getClassLoader(), new Class<?>[]{Inventory.class}, (proxy, method, args) -> {
            if (args != null && args.length == 1) throw new AbstractMethodError("getHolder(Z)");
            calls.add(method.getName() + "()");
            return null;
        });
        SnapshotUtils.getHolder(inv, true);
        assertEquals(Collections.singletonList("getHolder()"), calls);
    }
}
