package com.ssomar.score.ai;

import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LineDiffTest {

    static String render(LineDiff d) {
        return d.lines.stream().map(Line -> Line.kind + Line.text).collect(Collectors.joining("|"));
    }

    @Test
    void oneChangedLineWithContext() {
        String a = "a\nb\nc\nd\ne\nf\ng\n";
        String b = "a\nb\nc\nD\ne\nf\ng\n";
        LineDiff d = LineDiff.of(a, b, 1, 50);
        assertEquals(1, d.added);
        assertEquals(1, d.removed);
        assertEquals("…2| c|-d|+D| e|…2", render(d));
    }

    @Test
    void creationIsAllAdded() {
        LineDiff d = LineDiff.of(null, "name: x\nmaterial: STONE", 2, 50);
        assertEquals(2, d.added);
        assertEquals(0, d.removed);
        assertEquals("+name: x|+material: STONE", render(d));
    }

    @Test
    void insertionKeepsTheRest() {
        LineDiff d = LineDiff.of("a\nb\nc", "a\nb\nx\nc", 0, 50);
        assertEquals(1, d.added);
        assertEquals(0, d.removed);
        assertEquals("…2|+x|…1", render(d));
    }

    @Test
    void sameFileHasNoLine() {
        LineDiff d = LineDiff.of("a\r\nb\r\n", "a\nb", 2, 50);
        assertEquals(0, d.added + d.removed);
        assertTrue(d.lines.isEmpty());
    }

    @Test
    void longDiffIsCut() {
        StringBuilder a = new StringBuilder(), b = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            a.append("old").append(i).append('\n');
            b.append("new").append(i).append('\n');
        }
        LineDiff d = LineDiff.of(a.toString(), b.toString(), 2, 10);
        assertEquals(10, d.lines.size());
        assertEquals('…', d.lines.get(9).kind);
        assertEquals(100, d.added);
    }
}
