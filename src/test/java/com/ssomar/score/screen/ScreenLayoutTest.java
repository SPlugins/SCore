package com.ssomar.score.screen;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ScreenLayoutTest {

    private static ScreenLayout layout() {
        return new ScreenLayout("t", 2.4, 1.35, Arrays.asList(
                ScreenWidget.builder("bg", WidgetKind.FILL).at(0, 0).size(2.4, 1.35).build(),
                ScreenWidget.builder("a", WidgetKind.BUTTON).at(0.1, 0.1).size(0.5, 0.2).build(),
                ScreenWidget.builder("label", WidgetKind.LABEL).at(0.1, 0.1).size(0.5, 0.2).build(),
                ScreenWidget.builder("b", WidgetKind.BUTTON).at(0.4, 0.15).size(0.5, 0.2).build(),
                ScreenWidget.builder("off", WidgetKind.BUTTON).at(1.5, 1.0).size(0.3, 0.1).enabled(false).build()), 6);
    }

    @Test
    void hitReturnsTopmostClickable() {
        ScreenLayout l = layout();
        assertEquals("a", l.hit(0.2, 0.15).getId());          // only a; the label on top is not clickable
        assertEquals("b", l.hit(0.5, 0.2).getId());           // a and b overlap, b is drawn later
        assertNull(l.hit(1.0, 0.8));                          // background fill only
        assertNull(l.hit(1.6, 1.05));                         // disabled button
        assertNull(l.hit(-0.1, 0.5));                         // outside the panel
        assertNull(l.hit(2.5, 0.5));
    }

    @Test
    void withReplacesById() {
        ScreenLayout l = layout();
        ScreenLayout l2 = l.with(l.get("a").toBuilder().text("new").build());
        assertEquals("new", l2.get("a").getText());
        assertEquals(l.getWidgets().size(), l2.getWidgets().size());
        assertEquals("", l.get("a").getText()); // immutable
    }

    @Test
    void chestRowsClamped() {
        assertEquals(6, new ScreenLayout("t", 1, 1, Arrays.<ScreenWidget>asList(), 9).getChestRows());
        assertEquals(1, new ScreenLayout("t", 1, 1, Arrays.<ScreenWidget>asList(), 0).getChestRows());
    }

    @Test
    void modeFromString() {
        assertEquals(ScreenMode.FLOATING, ScreenMode.fromString(" floating ", ScreenMode.AUTO));
        assertEquals(ScreenMode.AUTO, ScreenMode.fromString("nope", ScreenMode.AUTO));
    }
}
