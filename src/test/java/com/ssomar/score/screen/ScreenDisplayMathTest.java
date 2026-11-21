package com.ssomar.score.screen;

import com.ssomar.score.screen.internal.display.ClickGuard;
import com.ssomar.score.screen.internal.display.ScreenGeometry;
import com.ssomar.score.screen.internal.display.TextMetrics;
import com.ssomar.score.screen.internal.display.WidgetVisual;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ScreenDisplayMathTest {

    private static final double EPS = 1e-6;

    @Test
    void floatingPointFollowsTheCrosshair() {
        double[] centre = ScreenGeometry.floatingPoint(30, 10, 30, 10, 0.8);
        assertEquals(0, centre[0], EPS);
        assertEquals(0, centre[1], EPS);
        // turning right by 20 degrees: tan(20) on a plane at distance 1, divided by the panel unit
        double[] right = ScreenGeometry.floatingPoint(0, 0, 20, 0, 0.8);
        assertEquals(Math.tan(Math.toRadians(20)) / 0.8, right[0], EPS);
        assertEquals(0, right[1], EPS);
        // looking down gives a point below the centre
        assertTrue(ScreenGeometry.floatingPoint(0, 0, 0, 15, 1)[1] < 0);
        // looking away: no point
        assertNull(ScreenGeometry.floatingPoint(0, 0, 180, 0, 1));
    }

    @Test
    void pinnedPointMovesLikeAMouse() {
        double[] centre = ScreenGeometry.pinnedPoint(-45, 0, -45, 0);
        assertEquals(0, centre[0], EPS);
        assertEquals(0, centre[1], EPS);
        double[] right = ScreenGeometry.pinnedPoint(0, 0, 30, 0);
        assertEquals(Math.tan(Math.toRadians(30)), right[0], EPS);
        double[] down = ScreenGeometry.pinnedPoint(0, 0, 0, 20);
        assertEquals(-Math.tan(Math.toRadians(20)), down[1], EPS);
        assertNull(ScreenGeometry.pinnedPoint(0, 0, 120, 0));
    }

    @Test
    void layoutConversion() {
        double[] tl = ScreenGeometry.toLayout(-1.2, 0.675, 2.4, 1.35);
        assertEquals(0, tl[0], EPS);
        assertEquals(0, tl[1], EPS);
        double[] c = ScreenGeometry.centre(0, 0, 2.4, 1.35, 2.4, 1.35);
        assertEquals(0, c[0], EPS);
        assertEquals(0, c[1], EPS);
    }

    @Test
    void mapHeaderRoundTrip() {
        byte[] row = ScreenGeometry.mapHeader(-0.75, 0.3125, 0.21, 0.15);
        double[] back = ScreenGeometry.readMapHeader(row);
        assertNotNull(back);
        assertEquals(-0.75, back[0], 1.0 / 4096);
        assertEquals(0.3125, back[1], 1.0 / 4096);
        assertEquals(0.21, back[2], 1.0 / 4096);
        assertEquals(0.15, back[3], 1.0 / 4096);
        // signature in the first 16 pixels, only the two key colours
        for (byte b : row) assertTrue(b == ScreenGeometry.BIT_ONE || b == ScreenGeometry.BIT_ZERO);
        row[3] = 5;
        assertNull(ScreenGeometry.readMapHeader(row));
    }

    @Test
    void boxTransformCoversTheWidget() {
        double[] box = {0.4, -0.2, 0.6, 0.1};
        double[] t = WidgetVisual.boxTransform(box);
        // background of a one-space line: x from -2 px to 3 px, y from 0 to one line, scaled then translated
        double left = t[0] - 2 * TextMetrics.PX * t[2], right = t[0] + 3 * TextMetrics.PX * t[2];
        double bottom = t[1], top = t[1] + TextMetrics.LINE * t[3];
        assertEquals(0.1, left, EPS);
        assertEquals(0.7, right, EPS);
        assertEquals(-0.25, bottom, EPS);
        assertEquals(-0.15, top, EPS);
    }

    @Test
    void buttonVisual() {
        ScreenWidget b = ScreenWidget.builder("ok", WidgetKind.BUTTON).at(1.0, 0.5).size(0.3, 0.1).text("OK").build();
        WidgetVisual v = WidgetVisual.of(b, "OK", 2.4, 1.35, false, false);
        assertNotNull(v.box);
        assertEquals(WidgetVisual.BUTTON, v.boxColor);
        assertEquals(1.15 - 1.2, v.box[0], EPS);
        assertEquals(0.675 - 0.55, v.box[1], EPS);
        assertNotNull(v.text);
        // centred text: ink centre (0.5 px right of the origin) on the box centre
        assertEquals(v.box[0], v.textX + 0.5 * TextMetrics.PX * v.textScale, EPS);
        WidgetVisual hover = WidgetVisual.of(b, "OK", 2.4, 1.35, true, false);
        assertNotEquals(v.boxColor, hover.boxColor);
        WidgetVisual off = WidgetVisual.of(b.toBuilder().enabled(false).build(), "OK", 2.4, 1.35, true, false);
        assertEquals(WidgetVisual.DISABLED, off.boxColor);
        assertTrue(off.text.startsWith("§8"));
    }

    @Test
    void wideRowIsLeftAlignedAfterItsIcon() {
        ScreenWidget r = ScreenWidget.builder("row", WidgetKind.TILE).at(0.04, 0.25).size(2.32, 0.105).text("x").icon("DIAMOND").build();
        WidgetVisual v = WidgetVisual.of(r, "ExecutableItems", 2.4, 1.35, false, false);
        assertEquals("DIAMOND", v.icon);
        double left = 0.04 - 1.2;
        assertEquals(left + 0.105 * 0.5, v.iconX, EPS);
        int px = TextMetrics.width("ExecutableItems");
        double inkStart = v.textX + (1 - px / 2.0) * TextMetrics.PX * v.textScale;
        assertEquals(left + 0.105, inkStart, EPS);
    }

    @Test
    void logoReplacesTheIconOnlyWhereImagesAreDrawn() {
        ScreenWidget r = ScreenWidget.builder("row", WidgetKind.TILE).at(0.04, 0.25).size(2.32, 0.105).icon("DIAMOND_SWORD")
                .logo("https://media.ssomar.com/m/resource-icon-900").build();
        WidgetVisual pinned = WidgetVisual.of(r, "ExecutableItems", 2.4, 1.35, false, true);
        assertNull(pinned.icon);
        assertEquals("https://media.ssomar.com/m/resource-icon-900", pinned.logo);
        assertEquals(0.04 - 1.2 + 0.105 * 0.5, pinned.iconX, EPS);
        WidgetVisual floating = WidgetVisual.of(r, "ExecutableItems", 2.4, 1.35, false, false);
        assertEquals("DIAMOND_SWORD", floating.icon);
        assertNull(floating.logo);
    }

    @Test
    void longTextShrinksThenIsCut() {
        ScreenWidget b = ScreenWidget.builder("b", WidgetKind.BUTTON).at(0, 0).size(0.3, 0.09).build();
        String longText = "A very long label that never fits in a small button at all";
        WidgetVisual v = WidgetVisual.of(b, longText, 2.4, 1.35, false, false);
        double ink = TextMetrics.width(v.text) * TextMetrics.PX * v.textScale;
        assertTrue(ink <= (0.3 - 0.05) * 0.97 + EPS);
        assertTrue(v.text.endsWith("..."));
    }

    @Test
    void imageOnlyWhenAllowed() {
        ScreenWidget i = ScreenWidget.builder("img", WidgetKind.IMAGE).at(0, 0).size(0.8, 0.6).image("https://x/y.png").icon("PAPER").build();
        assertTrue(WidgetVisual.of(i, "", 2.4, 1.35, false, true).image);
        WidgetVisual placeholder = WidgetVisual.of(i, "", 2.4, 1.35, false, false);
        assertFalse(placeholder.image);
        assertEquals(WidgetVisual.IMAGE_PLACEHOLDER, placeholder.boxColor);
    }

    @Test
    void textWidths() {
        assertEquals(24, TextMetrics.width("Hello"));
        assertEquals(29, TextMetrics.width("§lHello"));
        assertEquals(24, TextMetrics.width("§aHel§llo".replace("§l", "§c")));
        assertEquals("Hello", TextMetrics.strip("§aHe§lllo"));
    }

    @Test
    void clickGuard() {
        ClickGuard g = new ClickGuard();
        UUID id = UUID.randomUUID();
        assertFalse(g.isUp(id, 0));
        g.arm(id, 1000);
        assertTrue(g.isUp(id, 1499));
        assertFalse(g.isUp(id, 1500));
        g.arm(id, 2000);
        g.extend(id, 2400); // until 2850
        assertTrue(g.isUp(id, 2800));
        g.extend(id, 2800); // until 3250
        g.extend(id, 3200); // capped at start + 1500 = 3500
        g.extend(id, 3400);
        assertTrue(g.isUp(id, 3499));
        assertFalse(g.isUp(id, 3500));
        g.arm(id, 5000);
        g.release(id, 5100);
        assertTrue(g.isUp(id, 5159));
        assertFalse(g.isUp(id, 5160));
    }
}
