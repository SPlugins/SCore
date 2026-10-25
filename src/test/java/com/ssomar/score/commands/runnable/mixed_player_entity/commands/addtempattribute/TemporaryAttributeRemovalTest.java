package com.ssomar.score.commands.runnable.mixed_player_entity.commands.addtempattribute;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ADD_TEMPORARY_ATTRIBUTE used to stay on a player whose timer ended on the death screen
 * (isValid() is false there), until the next reconnection.
 */
class TemporaryAttributeRemovalTest {

    @SuppressWarnings("unchecked")
    private static <T extends Entity> T entity(Class<T> type, Map<String, Boolean> answers) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
            Boolean value = answers.get(method.getName());
            if (value != null) return value;
            if (method.getReturnType() == boolean.class) return false;
            return null;
        });
    }

    private static Map<String, Boolean> answers(Object... kv) {
        Map<String, Boolean> map = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) map.put((String) kv[i], (Boolean) kv[i + 1]);
        return map;
    }

    @Test
    void deadButConnectedPlayerLosesTheModifier() {
        Player onDeathScreen = entity(Player.class, answers("isOnline", true, "isValid", false, "isDead", true));
        assertTrue(AddTemporaryAttribute.shouldRemoveNow(onDeathScreen));
    }

    @Test
    void alivePlayerLosesTheModifier() {
        Player alive = entity(Player.class, answers("isOnline", true, "isValid", true, "isDead", false));
        assertTrue(AddTemporaryAttribute.shouldRemoveNow(alive));
    }

    @Test
    void offlinePlayerKeepsTheRecordForTheNextJoin() {
        Player offline = entity(Player.class, answers("isOnline", false, "isValid", false, "isDead", false));
        assertFalse(AddTemporaryAttribute.shouldRemoveNow(offline));
    }

    @Test
    void mobsKeepTheirPreviousRule() {
        assertTrue(AddTemporaryAttribute.shouldRemoveNow(entity(Zombie.class, answers("isValid", true, "isDead", false))));
        assertFalse(AddTemporaryAttribute.shouldRemoveNow(entity(Zombie.class, answers("isValid", false, "isDead", true))));
    }
}
