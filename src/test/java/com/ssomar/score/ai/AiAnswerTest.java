package com.ssomar.score.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiAnswerTest {

    @Test
    void splitsParagraphsCodesAndLinks() {
        String md = "## Fix\n\nYour item uses **displayName**, the option is `name`.\n\n"
                + "```yaml\nname: '&cFire sword'\nmaterial: DIAMOND_SWORD\n```\n\n"
                + "- see [the wiki](https://splugins.net/docs/executableitems)\n- or https://discord.gg/splugins.";
        AiAnswer a = AiAnswer.parse(md);
        assertEquals(1, a.codes.size());
        assertEquals("yaml", a.codes.get(0).language);
        assertEquals("name: '&cFire sword'\nmaterial: DIAMOND_SWORD", a.codes.get(0).text);
        assertEquals(2, a.links.size());
        assertEquals("the wiki", a.links.get(0).label);
        assertEquals("https://discord.gg/splugins", a.links.get(1).url);
        assertEquals("Fix", a.paragraphs.get(0));
        assertEquals("Your item uses displayName, the option is name.", a.paragraphs.get(1));
        assertEquals("[config 1]", a.paragraphs.get(2));
        assertTrue(a.paragraphs.get(3).startsWith("• see the wiki"));
    }

    @Test
    void emptyAndPlain() {
        assertTrue(AiAnswer.parse(null).paragraphs.isEmpty());
        AiAnswer a = AiAnswer.parse("Just text, no markdown.");
        assertEquals(1, a.paragraphs.size());
        assertTrue(a.codes.isEmpty());
        assertTrue(a.links.isEmpty());
    }
}
