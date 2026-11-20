package com.ssomar.score.screen.internal;

import com.ssomar.score.screen.CloseReason;
import com.ssomar.score.screen.ScreenInput;
import com.ssomar.score.screen.Screens;
import com.ssomar.score.utils.backward_compatibility.SnapshotUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;

/** Chest screens: clicks, drags and close. */
public final class ScreenListener implements Listener {

    private static ChestScreenGui gui(org.bukkit.inventory.Inventory inv) {
        if (inv == null) return null;
        InventoryHolder h = SnapshotUtils.getHolderNoSnapshot(inv);
        return h instanceof ChestScreenGui ? (ChestScreenGui) h : null;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent e) {
        ChestScreenGui gui = gui(e.getView().getTopInventory());
        if (gui == null) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getClickedInventory() == null || e.getClickedInventory() != e.getView().getTopInventory()) return;
        String widget = gui.widgetAt(e.getSlot());
        if (widget == null) return;
        ChestScreenSession s = gui.getSession();
        if (!s.isOpen()) return;
        s.dispatch(new ScreenInput.Click(widget, e.isRightClick(), e.isShiftClick()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDrag(InventoryDragEvent e) {
        if (gui(e.getView().getTopInventory()) != null) e.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        ChestScreenGui gui = gui(e.getInventory());
        if (gui == null) return;
        ChestScreenSession s = gui.getSession();
        if (s.closingByCode) return;
        if (Screens.get((Player) e.getPlayer()) == s) Screens.close((Player) e.getPlayer(), CloseReason.PLAYER);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Screens.close(e.getPlayer(), CloseReason.QUIT);
    }
}
