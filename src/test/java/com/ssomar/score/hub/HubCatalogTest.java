package com.ssomar.score.hub;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class HubCatalogTest {

    @Test
    void comparesPluginVersions() {
        assertTrue(HubCatalog.compareVersions("7.26.11.17", "7.26.11.20") < 0);
        assertTrue(HubCatalog.compareVersions("7.26.12.1", "7.26.11.20") > 0);
        assertEquals(0, HubCatalog.compareVersions("5.26.11.20", "5.26.11.20"));
        assertEquals(0, HubCatalog.compareVersions("1.2", "1.2.0"));
        assertTrue(HubCatalog.compareVersions("42.42.42", "7.26.11.20") > 0); // dev build never asks for an update
    }

    @Test
    void parsesTheSiteFormat() {
        long future = Instant.now().plusSeconds(86400).toEpochMilli();
        long past = Instant.now().minusSeconds(86400).toEpochMilli();
        String json = "{\"version\":1,\"plugins\":[{\"key\":\"ExecutableItems\",\"edition\":\"premium\",\"id\":900,\"name\":\"ExecutableItems\","
                + "\"pitch\":\"Custom items\",\"access\":\"paid\",\"price\":19.99,\"currency\":\"EUR\",\"sale\":{\"percent\":30,\"endsAt\":\""
                + Instant.ofEpochMilli(future) + "\",\"label\":\"BF\"},\"salePrice\":13.99,\"url\":\"https://splugins.net/resources/900-executableitems\","
                + "\"icon\":null,\"banner\":null,\"latestVersion\":{\"version\":\"7.26.11.20\",\"releasedAt\":\"2026-11-20T10:00:00.000Z\"}},"
                + "{\"key\":null,\"name\":\"Other\",\"access\":\"free\",\"price\":0}],"
                + "\"packs\":[{\"name\":\"Deep Dark\",\"kind\":\"premium\",\"access\":\"paid\",\"price\":12.99,\"url\":\"u\",\"sale\":null}],"
                + "\"promotions\":[{\"id\":\"hub:bf\",\"title\":\"Black Friday\",\"percent\":40,\"startsAt\":null,\"endsAt\":\"" + Instant.ofEpochMilli(future) + "\",\"url\":null},"
                + "{\"id\":\"hub:old\",\"title\":\"Old\",\"percent\":10,\"endsAt\":\"" + Instant.ofEpochMilli(past) + "\"}],"
                + "\"news\":[{\"id\":\"blog:x\",\"title\":\"Hello\",\"text\":\"t\",\"url\":null,\"date\":\"2026-11-19T00:00:00.000Z\"}],"
                + "\"links\":[{\"id\":\"discord\",\"label\":\"Discord\",\"url\":\"https://discord.gg/splugins\"},{\"id\":\"bad\",\"label\":\"x\",\"url\":\"\"}]}";
        HubCatalog c = HubCatalog.parse(json);
        assertEquals(2, c.plugins.size());
        HubCatalog.Entry ei = c.plugins.get(0);
        assertEquals("ExecutableItems", ei.key);
        assertEquals("7.26.11.20", ei.latestVersion);
        assertEquals(30, ei.salePercent);
        assertEquals("13.99€ (-30%)", ei.priceText());
        assertEquals("", ei.icon);
        assertEquals("", c.plugins.get(1).key);
        assertEquals("Free", c.plugins.get(1).priceText());
        assertEquals("12.99€", c.packs.get(0).priceText());
        assertEquals(1, c.promotions.size(), "ended promotion filtered out");
        assertEquals(40, c.promotions.get(0).discount);
        assertEquals("", c.promotions.get(0).url);
        assertEquals(1, c.news.size());
        assertTrue(c.news.get(0).date > 0);
        assertEquals(1, c.links.size(), "link without url dropped");
        assertSame(ei, c.pluginEntry(KnownPlugin.EXECUTABLE_ITEMS, true));
        assertNull(c.pluginEntry(KnownPlugin.MY_FURNITURE, null));
    }

    @Test
    void parsesErrorFallbackAndGarbageDates() {
        HubCatalog c = HubCatalog.parse("{\"version\":1,\"plugins\":[],\"packs\":[],\"bundles\":[],\"news\":[],\"promotions\":[],\"links\":[]}");
        assertTrue(c.plugins.isEmpty() && c.links.isEmpty());
        assertEquals(0, HubCatalog.time("not a date"));
        assertEquals(1700000000000L, HubCatalog.time(1700000000L));
        assertEquals(1700000000000L, HubCatalog.time(1700000000000L));
        assertTrue(HubCatalog.time("2026-11-20") > 0);
    }

    @Test
    void readsTheBundlesAndTheCommunityPacksOfTheStore() {
        HubCatalog c = HubCatalog.parse("{\"plugins\":[],\"packs\":[{\"name\":\"Deep Dark Pack\",\"kind\":\"premium\",\"official\":true,\"price\":12.99,\"url\":\"u1\"},"
                + "{\"name\":\"Member Pack\",\"kind\":\"community\",\"official\":false,\"access\":\"paid\",\"price\":5,\"url\":\"u2\"}],"
                + "\"bundles\":[{\"id\":3,\"title\":\"Starter Bundle\",\"pitch\":\"All you need\",\"discountPercent\":20,\"fullPrice\":50,\"price\":40,"
                + "\"currency\":\"EUR\",\"url\":\"https://splugins.net/bundles/3\",\"resources\":[{\"id\":900,\"name\":\"ExecutableItems\",\"icon\":\"https://x/icon.png\"},{\"id\":923,\"name\":\"Deep Dark Pack\",\"icon\":null}]},"
                + "{\"title\":\"no url\"}]}");
        assertEquals(1, c.bundles.size());
        HubCatalog.Entry b = c.bundles.get(0);
        assertEquals("Starter Bundle", b.name);
        assertEquals(Arrays.asList("ExecutableItems", "Deep Dark Pack"), b.items);
        assertEquals("https://x/icon.png", b.icon);
        assertEquals("40.00€ (-20%)", b.priceText());
        assertTrue(c.packs.get(0).official);
        assertFalse(c.packs.get(1).official);
    }
}
