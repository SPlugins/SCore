package com.ssomar.score.utils.strings;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlaceholderWhitespaceTest {

    @Test
    void removesSpacesOutsidePlaceholders() {
        assertEquals("2>1&&3<4", PlaceholderWhitespace.stripOutsidePlaceholders(" 2 > 1 && 3 < 4 "));
        assertEquals("%player_level%>=10", PlaceholderWhitespace.stripOutsidePlaceholders("%player_level% >= 10"));
    }

    @Test
    void keepsSpacesInsidePlaceholders() {
        assertEquals("%checkitem_inhand:main,lorecontains:test test%=yes",
                PlaceholderWhitespace.stripOutsidePlaceholders("%checkitem_inhand:main,lorecontains:test test% = yes"));
        assertEquals("(%a b%=x||%c  d%=y)&&z=1",
                PlaceholderWhitespace.stripOutsidePlaceholders("( %a b% = x || %c  d% = y ) && z = 1"));
    }

    @Test
    void unbalancedPercentTreatsTailAsOutside() {
        // 3 '%' : the last one is not a placeholder delimiter, spaces after it are removed
        assertEquals("%a b%=50%", PlaceholderWhitespace.stripOutsidePlaceholders("%a b% = 50 %"));
        assertEquals("x=50%", PlaceholderWhitespace.stripOutsidePlaceholders("x = 50 %"));
        assertEquals("", PlaceholderWhitespace.stripOutsidePlaceholders(""));
        assertNull(PlaceholderWhitespace.stripOutsidePlaceholders(null));
    }

    @Test
    void mergesArgsUntilPlaceholderIsClosed() {
        List<String> args = Arrays.asList("%checkitem_inhand:main,lorecontains:test", "test%=yes", "say", "hi");
        assertEquals(Arrays.asList("%checkitem_inhand:main,lorecontains:test test%=yes", "say", "hi"),
                PlaceholderWhitespace.mergeFirstArgPlaceholders(args));
    }

    @Test
    void mergeLeavesBalancedOrUnbalancedArgsUnchanged() {
        List<String> balanced = Arrays.asList("%player_level%>=10", "say", "hi");
        assertEquals(balanced, PlaceholderWhitespace.mergeFirstArgPlaceholders(balanced));

        List<String> noPlaceholder = Arrays.asList("2>1", "say", "hi");
        assertEquals(noPlaceholder, PlaceholderWhitespace.mergeFirstArgPlaceholders(noPlaceholder));

        // never balances: args untouched so the commands are not swallowed
        List<String> unbalanced = Arrays.asList("%broken", "say", "hi");
        assertSame(unbalanced, PlaceholderWhitespace.mergeFirstArgPlaceholders(unbalanced));

        assertEquals(Collections.emptyList(), PlaceholderWhitespace.mergeFirstArgPlaceholders(Collections.emptyList()));
    }
}
