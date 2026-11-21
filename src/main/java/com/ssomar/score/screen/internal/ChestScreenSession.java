package com.ssomar.score.screen.internal;

import com.ssomar.score.screen.CloseReason;
import com.ssomar.score.screen.ScreenMode;
import com.ssomar.score.screen.ScreenSpec;
import org.bukkit.entity.Player;

public final class ChestScreenSession extends AbstractScreenSession {

    private final ChestScreenGui gui;
    /** true while the inventory is closed by code, so the close event is not taken as the player's */
    boolean closingByCode;

    public ChestScreenSession(Player player, ScreenSpec spec) {
        super(player, spec);
        this.gui = new ChestScreenGui(this, spec.getLayout());
    }

    public void show() {
        gui.openGUISync(player);
    }

    public ChestScreenGui getGui() {
        return gui;
    }

    @Override
    public ScreenMode getMode() {
        return ScreenMode.CHEST;
    }

    @Override
    protected void redraw() {
        gui.fill(layout);
    }

    @Override
    protected void dispose(CloseReason reason) {
        if (reason == CloseReason.PLAYER || reason == CloseReason.QUIT) return; // already closed by the client
        closingByCode = true;
        if (com.ssomar.score.utils.backward_compatibility.SnapshotUtils.getHolderNoSnapshot(player.getOpenInventory().getTopInventory()) == gui)
            player.closeInventory();
    }
}
