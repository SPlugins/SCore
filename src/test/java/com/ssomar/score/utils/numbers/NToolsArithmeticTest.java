package com.ssomar.score.utils.numbers;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** placeholdersConditions: a part like '%player_y%-0.2' (resolved to '64.0-0.2') must compare as a number. */
class NToolsArithmeticTest {

    @Test
    void plainNumbersStillParse() {
        assertEquals(Optional.of(64.0), NTools.toNumber("64.0"));
        assertEquals(Optional.of(-3.0), NTools.toNumber(" -3 "));
        assertEquals(Optional.of(1.0E3), NTools.toNumber("1e3"));
    }

    @Test
    void arithmeticExpressions() {
        assertEquals(63.8, NTools.toNumber("64.0-0.2").get(), 1e-9);
        assertEquals(7.0, NTools.toNumber("1+2*3").get(), 1e-9);
        assertEquals(9.0, NTools.toNumber("(1+2)*3").get(), 1e-9);
        assertEquals(-5.5, NTools.toNumber("-(2+3.5)").get(), 1e-9);
        assertEquals(2.5, NTools.toNumber("10 / 4").get(), 1e-9);
        assertEquals(1.0, NTools.toNumber("3-1-1").get(), 1e-9, "left associative");
    }

    @Test
    void notANumberStaysEmpty() {
        assertFalse(NTools.toNumber("%player_y%-0.2").isPresent(), "unresolved placeholder");
        assertFalse(NTools.toNumber("abc").isPresent());
        assertFalse(NTools.toNumber("(1+2").isPresent());
        assertFalse(NTools.toNumber("1+").isPresent());
        assertFalse(NTools.toNumber("1/0").isPresent(), "infinite");
        assertFalse(NTools.toNumber("").isPresent());
        assertFalse(NTools.toNumber(null).isPresent());
    }
}
