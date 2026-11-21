package com.ssomar.score.hub;

import com.ssomar.score.screen.internal.display.TextMetrics;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConversationLinesTest {

    @Test
    void wrapsWordsToTheWidth() {
        List<String> l = ConversationLines.wrap("one two three four five six seven eight nine ten", 60);
        assertTrue(l.size() > 1);
        for (String s : l) assertTrue(TextMetrics.width(s) <= 60, s);
        assertEquals("one two three four five six seven eight nine ten", String.join(" ", l));
    }

    @Test
    void cutsAWordLongerThanALine() {
        List<String> l = ConversationLines.wrap("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 40);
        assertTrue(l.size() > 1);
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.join("", l));
    }

    @Test
    void questionAnswerAndConfigInline() {
        String answer = "Your item misses a material.\n\n```yaml\nmaterial: DIAMOND_SWORD\nname: '&bSword'\n```\n\nThen reload.";
        List<ConversationLines.Line> lines = ConversationLines.of(Collections.singletonList("Why?"), Collections.singletonList(answer), "§7thinking", 400);
        assertEquals("§b» Why?", lines.get(0).text);
        assertEquals("§fYour item misses a material.", lines.get(1).text);
        assertEquals("§7material: DIAMOND_SWORD", lines.get(2).text);
        assertEquals("§7name: '&bSword'", lines.get(3).text);
        assertEquals("§fThen reload.", lines.get(4).text);
    }

    @Test
    void lastQuestionWithoutAnswerShowsThinking() {
        List<ConversationLines.Line> lines = ConversationLines.of(Arrays.asList("q1", "q2"), Collections.singletonList("a1"), "§7thinking", 400);
        assertEquals("§7thinking", lines.get(lines.size() - 1).text);
        assertEquals(1, lines.get(lines.size() - 1).exchange);
        assertEquals("", lines.get(2).text);
    }
}
