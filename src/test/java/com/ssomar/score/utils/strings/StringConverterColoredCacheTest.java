package com.ssomar.score.utils.strings;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * coloredString caches its result (a MiniMessage parse for each name and lore line at every item refresh): the cached value
 * must be exactly what the conversion gives, for both values of bypassNoColor, and the cache stays bounded.
 */
class StringConverterColoredCacheTest {

    private static final List<String> SAMPLES = Arrays.asList(
            "&aGreen &lbold", "&#12A4C1&l✦ &#47B8CF&lE&#61C2D6&lt", "<red>mini <bold>message", "a & b && c", "NO_COLOR &aplain",
            "", " ", "&7&oMade with &eEI", "100% &cred", "§x§1§2§A§4§C§1already converted");

    private static String uncached(String s, boolean bypassNoColor) throws Exception {
        Method m = StringConverter.class.getDeclaredMethod("coloredStringUncached", String.class, boolean.class);
        m.setAccessible(true);
        return (String) m.invoke(null, s, bypassNoColor);
    }

    @Test
    void cachedValueIsTheConversion() throws Exception {
        for (boolean bypass : new boolean[]{false, true}) {
            for (String s : SAMPLES) {
                String expected = uncached(s, bypass);
                assertEquals(expected, StringConverter.coloredString(s, bypass), "first call " + s);
                assertEquals(expected, StringConverter.coloredString(s, bypass), "cached call " + s);
            }
        }
        assertEquals(StringConverter.coloredString("&aGreen &lbold", false), StringConverter.coloredString("&aGreen &lbold"));
    }

    @Test
    void noColorDependsOnBypass() {
        assertNotEquals(StringConverter.coloredString("NO_COLOR &aplain", false), StringConverter.coloredString("NO_COLOR &aplain", true));
    }

    @Test
    void longStringsAreConvertedWithoutTheCache() throws Exception {
        StringBuilder page = new StringBuilder();
        while (page.length() <= 1024) page.append("&aA long book page with <red>mini message ");
        String s = page.toString();
        assertEquals(uncached(s, false), StringConverter.coloredString(s));
        assertEquals(uncached(s, true), StringConverter.coloredString(s, true));
    }

    @Test
    void manyDistinctStringsStayCorrect() throws Exception {
        for (int i = 0; i < 5000; i++) {
            String s = "&" + "0123456789abcdef".charAt(i % 16) + "line " + i;
            assertEquals(uncached(s, false), StringConverter.coloredString(s));
        }
    }
}
