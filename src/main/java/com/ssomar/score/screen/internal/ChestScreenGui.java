package com.ssomar.score.screen.internal;

import com.ssomar.score.SCore;
import com.ssomar.score.menu.GUI;
import com.ssomar.score.screen.ScreenLayout;
import com.ssomar.score.screen.ScreenWidget;
import com.ssomar.score.screen.WidgetKind;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

/**
 * The chest version of a screen: every widget that can be shown as an item becomes one, in its chestSlot
 * (or the next free slot). Clicks are read by {@link ScreenListener} through {@link #widgetAt(int)}.
 */
public final class ChestScreenGui extends GUI {

    private final ChestScreenSession session;
    private final Map<Integer, String> slotToWidget = new HashMap<>();

    public ChestScreenGui(ChestScreenSession session, ScreenLayout layout) {
        super(title(layout.getTitle()), layout.getChestRows() * 9);
        this.session = session;
        fill(layout);
    }

    private static String title(String t) {
        String s = t == null ? "" : t;
        // titles longer than 32 chars throw on 1.8 - 1.12
        if (SCore.is1v12Less() && s.length() > 32) s = s.substring(0, 32);
        return s;
    }

    public ChestScreenSession getSession() {
        return session;
    }

    public String widgetAt(int slot) {
        return slotToWidget.get(slot);
    }

    /** Redraw every slot from the layout (cheap: at most 54 items). */
    public void fill(ScreenLayout layout) {
        slotToWidget.clear();
        int size = getInventory().getSize();
        for (int i = 0; i < size; i++) createBackGroundItem(i);
        boolean[] used = new boolean[size];
        // first the widgets with a fixed slot, then the others in order
        for (int pass = 0; pass < 2; pass++) {
            for (ScreenWidget w : layout.getWidgets()) {
                if (!shownInChest(w)) continue;
                int wanted = w.getChestSlot();
                boolean fixed = wanted >= 0;
                if ((pass == 0) != fixed) continue;
                int slot = fixed ? wanted : nextFree(used);
                if (slot < 0 || slot >= size) continue;
                used[slot] = true;
                place(slot, w);
            }
        }
    }

    private static boolean shownInChest(ScreenWidget w) {
        if (w.getChestSlot() == -2) return false;
        return w.getKind() == WidgetKind.BUTTON || w.getKind() == WidgetKind.TILE || w.getKind() == WidgetKind.ICON
                || w.getKind() == WidgetKind.IMAGE || (w.getKind() == WidgetKind.LABEL && w.getChestSlot() >= 0);
    }

    private static int nextFree(boolean[] used) {
        for (int i = 0; i < used.length; i++) if (!used[i]) return i;
        return -1;
    }

    private void place(int slot, ScreenWidget w) {
        Material material = material(w.getIcon(), w.getKind());
        String name = w.getText().isEmpty() ? " " : w.getText().replace("\n", " ");
        String[] lore = w.getLore().toArray(new String[0]);
        createItem(material, 1, slot, (w.isEnabled() ? "" : "&8") + name, w.isGlow() || w.isSelected(), false, lore);
        if (w.isClickable()) slotToWidget.put(slot, w.getId());
    }

    static Material material(String name, WidgetKind kind) {
        if (name != null && !name.isEmpty()) {
            Material m = Material.matchMaterial(name);
            if (m != null && m != Material.AIR) return m;
        }
        if (kind == WidgetKind.LABEL) return Material.PAPER;
        if (kind == WidgetKind.IMAGE) return Material.PAINTING;
        return Material.PAPER;
    }

    public void reloadGUI() {
        fill(session.getLayout());
    }
}
