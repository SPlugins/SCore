package com.ssomar.score.hub;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HubLinksTest {

    @Test
    void tagsOnlyOurSites() {
        assertEquals("https://splugins.net/store?utm_source=minecraft&utm_medium=splugins-hub&utm_campaign=discover",
                HubLinks.tag("https://splugins.net/store", "discover"));
        assertEquals("https://discord.gg/splugins", HubLinks.tag("https://discord.gg/splugins", "help"));
    }

    @Test
    void keepsExistingTagsAndAnchor() {
        assertEquals("https://splugins.net/resources/900-ei?utm_source=minecraft&utm_medium=splugins-hub&utm_campaign=home",
                HubLinks.tag("https://splugins.net/resources/900-ei?utm_source=minecraft&utm_medium=splugins-hub", "home"));
        assertEquals("https://splugins.net/docs?utm_source=minecraft&utm_medium=splugins-hub&utm_campaign=help#top",
                HubLinks.tag("https://splugins.net/docs#top", "help"));
    }
}
