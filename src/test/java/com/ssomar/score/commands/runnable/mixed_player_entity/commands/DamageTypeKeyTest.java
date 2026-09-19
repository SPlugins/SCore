package com.ssomar.score.commands.runnable.mixed_player_entity.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DAMAGE type: the default keeps its historical behaviour (player attack when a player launched the
 * command), explicit vanilla ids work whatever their case.
 */
class DamageTypeKeyTest {

    @Test
    void defaultIsThePlayerAttackWhenAPlayerLaunchedTheCommand() {
        assertEquals("player_attack", Damage.resolveDamageTypeKey(Damage.AUTO_DAMAGE_TYPE, true));
        assertEquals("indirect_magic", Damage.resolveDamageTypeKey(Damage.AUTO_DAMAGE_TYPE, false));
        assertEquals("player_attack", Damage.resolveDamageTypeKey(null, true));
        assertEquals("player_attack", Damage.resolveDamageTypeKey("  ", true));
    }

    @Test
    void previousUppercaseDefaultKeepsBehavingLikeAuto() {
        // "INDIRECT_MAGIC" was rejected by NamespacedKey, so it has always meant "auto"
        assertEquals("player_attack", Damage.resolveDamageTypeKey("INDIRECT_MAGIC", true));
        assertEquals("indirect_magic", Damage.resolveDamageTypeKey("INDIRECT_MAGIC", false));
    }

    @Test
    void explicitTypesAreCaseInsensitive() {
        assertEquals("in_fire", Damage.resolveDamageTypeKey("IN_FIRE", true));
        assertEquals("in_fire", Damage.resolveDamageTypeKey("in_fire", true));
        assertEquals("fall", Damage.resolveDamageTypeKey("minecraft:FALL", false));
        assertEquals("indirect_magic", Damage.resolveDamageTypeKey("indirect_magic", true));
    }
}
