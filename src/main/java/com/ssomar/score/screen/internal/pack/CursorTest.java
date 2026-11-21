package com.ssomar.score.screen.internal.pack;

import com.ssomar.score.languages.messages.TM;
import com.ssomar.score.languages.messages.Text;
import com.ssomar.score.screen.*;
import com.ssomar.score.screen.internal.ScreenData;

import java.util.ArrayList;
import java.util.List;

/**
 * Shown once per player before the first PINNED screen: "Can you see the arrow?". Another pack (a shader pack, a
 * plugin pack loaded on top) can replace our shader, and the server can't know it. The two answers sit at mirrored
 * places: without our shader the visible arrow moves the other way, so a player who aims it at "Yes" clicks "No"
 * (the server follows the shader maths). "No" (or a mirrored click) switches this player to FLOATING for good.
 */
public final class CursorTest {

    public static final String ID = "score:cursor-test";

    private CursorTest() {
    }

    public static ScreenSpec wrap(ScreenSpec original) {
        double w = 1.6, h = 0.7, bw = 0.34, bh = 0.22, off = 0.42;
        List<ScreenWidget> list = new ArrayList<>();
        list.add(ScreenWidget.builder("title", WidgetKind.LABEL).at(0.05, 0.03).size(w - 0.1, 0.09).text(TM.g(Text.HUB_CURSOR_TITLE)).build());
        list.add(ScreenWidget.builder("hint", WidgetKind.LABEL).at(0.05, 0.13).size(w - 0.1, 0.07).text(TM.g(Text.HUB_CURSOR_HINT)).textScale(0.85).build());
        // centred on the panel middle (0.35), mirrored left / right
        list.add(ScreenWidget.builder("no", WidgetKind.BUTTON).at(w / 2 - off - bw / 2, h / 2 - bh / 2).size(bw, bh).text(TM.g(Text.HUB_CURSOR_NO)).build());
        list.add(ScreenWidget.builder("yes", WidgetKind.BUTTON).at(w / 2 + off - bw / 2, h / 2 - bh / 2).size(bw, bh).text(TM.g(Text.HUB_CURSOR_YES)).build());
        list.add(ScreenWidget.builder("sneak", WidgetKind.LABEL).at(0.05, 0.58).size(w - 0.1, 0.06).text(TM.g(Text.HUB_SNEAK_HINT)).textScale(0.85).build());
        ScreenLayout layout = new ScreenLayout(TM.g(Text.HUB_CURSOR_TITLE), w, h, list, 1);
        return ScreenSpec.builder(ID).layout(layout).mode(ScreenMode.PINNED).backdrop(original.getBackdrop())
                .onInput((s, in) -> {
                    if (!(in instanceof ScreenInput.Click)) return;
                    String id = ((ScreenInput.Click) in).getWidgetId();
                    if (!"yes".equals(id) && !"no".equals(id)) return;
                    ScreenData.setCursorSeen(s.getPlayer().getUniqueId(), "yes".equals(id));
                    Screens.open(s.getPlayer(), original);
                }).build();
    }
}
