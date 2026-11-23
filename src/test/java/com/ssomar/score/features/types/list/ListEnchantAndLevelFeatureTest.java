package com.ssomar.score.features.types.list;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ifHasEnchant / ifHasNotEnchant: every level written for an enchantment counts (LOYALTY:1, LOYALTY:2, LOYALTY:3 used to
 * keep LOYALTY:3 only), an enchantment written without level (LOYALTY or LOYALTY:*) means all its levels.
 * Strings stand for the enchantments, the lookup plays Enchantment.getByName.
 */
class ListEnchantAndLevelFeatureTest {

    private static final List<String> KNOWN = Arrays.asList("LOYALTY", "SHARPNESS", "minecraft:loyalty", "myplugin:frost");
    private static final Function<String, String> LOOKUP = name -> KNOWN.contains(name) ? name : null;

    private final Map<String, Set<Integer>> levels = new LinkedHashMap<>();
    private final Map<String, Integer> lastLevels = new HashMap<>();

    private boolean read(String... entries) {
        boolean valid = true;
        for (String entry : entries) {
            valid &= ListEnchantAndLevelFeature.readEntry(entry, LOOKUP, levels, lastLevels);
        }
        return valid;
    }

    private static Map<String, Integer> item(Object... enchantsAndLevels) {
        Map<String, Integer> enchants = new HashMap<>();
        for (int i = 0; i < enchantsAndLevels.length; i += 2) {
            enchants.put((String) enchantsAndLevels[i], (Integer) enchantsAndLevels[i + 1]);
        }
        return enchants;
    }

    private static Set<Integer> set(Integer... values) {
        return new LinkedHashSet<>(Arrays.asList(values));
    }

    @Test
    void everyLevelOfAnEnchantmentIsKept() {
        assertTrue(read("LOYALTY:1", "LOYALTY:2", "LOYALTY:3"));
        assertEquals(set(1, 2, 3), levels.get("LOYALTY"));
        // the one-level view (getValue) is unchanged: the last level written
        assertEquals(Collections.singletonMap("LOYALTY", 3), lastLevels);
    }

    @Test
    void ifHasNotEnchantBlocksEveryLevelWritten() {
        read("LOYALTY:1", "LOYALTY:2", "LOYALTY:3");
        for (int level = 1; level <= 3; level++) {
            assertTrue(ListEnchantAndLevelFeature.hasOne(item("LOYALTY", level), levels), "Loyalty " + level);
        }
        assertFalse(ListEnchantAndLevelFeature.hasOne(item("LOYALTY", 4), levels));
        assertFalse(ListEnchantAndLevelFeature.hasOne(item(), levels));
        assertFalse(ListEnchantAndLevelFeature.hasOne(item("SHARPNESS", 1), levels));
    }

    @Test
    void ifHasEnchantAcceptsOneOfTheLevelsWrittenForEachEnchantment() {
        read("LOYALTY:1", "LOYALTY:3", "SHARPNESS:2");
        assertTrue(ListEnchantAndLevelFeature.hasAll(item("LOYALTY", 1, "SHARPNESS", 2), levels));
        assertTrue(ListEnchantAndLevelFeature.hasAll(item("LOYALTY", 3, "SHARPNESS", 2), levels));
        assertFalse(ListEnchantAndLevelFeature.hasAll(item("LOYALTY", 2, "SHARPNESS", 2), levels));
        assertFalse(ListEnchantAndLevelFeature.hasAll(item("LOYALTY", 1, "SHARPNESS", 3), levels));
        assertFalse(ListEnchantAndLevelFeature.hasAll(item("LOYALTY", 1), levels));
    }

    @Test
    void noLevelOrStarMeansAllTheLevels() {
        assertTrue(read("LOYALTY", "SHARPNESS:*"));
        assertEquals(set(), levels.get("LOYALTY"));
        assertEquals(set(), levels.get("SHARPNESS"));
        assertTrue(lastLevels.isEmpty());
        assertTrue(ListEnchantAndLevelFeature.hasOne(item("LOYALTY", 1), levels));
        assertTrue(ListEnchantAndLevelFeature.hasOne(item("LOYALTY", 5), levels));
        assertTrue(ListEnchantAndLevelFeature.hasAll(item("LOYALTY", 2, "SHARPNESS", 7), levels));
        assertFalse(ListEnchantAndLevelFeature.hasAll(item("LOYALTY", 2), levels));
    }

    @Test
    void allTheLevelsWinsOverTheLevelsWritten() {
        assertTrue(read("LOYALTY:1", "LOYALTY", "LOYALTY:2", "SHARPNESS:*", "SHARPNESS:3"));
        assertEquals(set(), levels.get("LOYALTY"));
        assertEquals(set(), levels.get("SHARPNESS"));
        assertTrue(ListEnchantAndLevelFeature.hasOne(item("LOYALTY", 4), levels));
    }

    @Test
    void namespacedEnchantments() {
        assertTrue(read("minecraft:loyalty:2", "myplugin:frost"));
        assertEquals(set(2), levels.get("minecraft:loyalty"));
        assertEquals(set(), levels.get("myplugin:frost"));
        assertTrue(read("minecraft:loyalty"));
        assertEquals(set(), levels.get("minecraft:loyalty"));
        levels.clear();
        assertTrue(read("minecraft:loyalty:*"));
        assertEquals(set(), levels.get("minecraft:loyalty"));
    }

    @Test
    void singleLevelListsBehaveAsBefore() {
        assertTrue(read("SHARPNESS:2", "LOYALTY:1"));
        assertEquals(item("SHARPNESS", 2, "LOYALTY", 1), lastLevels);
        assertTrue(ListEnchantAndLevelFeature.hasAll(item("SHARPNESS", 2, "LOYALTY", 1), levels));
        assertFalse(ListEnchantAndLevelFeature.hasAll(item("SHARPNESS", 3, "LOYALTY", 1), levels));
        assertTrue(ListEnchantAndLevelFeature.hasOne(item("LOYALTY", 1), levels));
        assertFalse(ListEnchantAndLevelFeature.hasOne(item("SHARPNESS", 3), levels));
    }

    @Test
    void invalidEntriesAreRefusedAndIgnored() {
        for (String entry : Arrays.asList("UNKNOWN", "UNKNOWN:1", "LOYALTY:x", "LOYALTY:", ":", "", "minecraft:unknown", "LOYALTY:1:2")) {
            assertFalse(ListEnchantAndLevelFeature.readEntry(entry, LOOKUP, levels, lastLevels), entry);
        }
        assertTrue(levels.isEmpty());
        assertTrue(lastLevels.isEmpty());
    }

    @Test
    void savedTextIsReadBackTheSame() {
        read("SHARPNESS:2", "LOYALTY:1", "LOYALTY:3", "myplugin:frost");
        List<String> saved = ListEnchantAndLevelFeature.toStrings(levels, Function.identity());
        assertEquals(Arrays.asList("SHARPNESS:2", "LOYALTY:1", "LOYALTY:3", "myplugin:frost"), saved);

        Map<String, Set<Integer>> reread = new LinkedHashMap<>();
        for (String s : saved) {
            assertTrue(ListEnchantAndLevelFeature.readEntry(s, LOOKUP, reread, new HashMap<>()), s);
        }
        assertEquals(levels, reread);
    }
}
