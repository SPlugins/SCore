package com.ssomar.score.screen.internal;

import com.ssomar.score.SCore;
import com.ssomar.score.screen.*;
import org.bukkit.entity.Player;

import java.util.logging.Level;

/** Common part of every session: spec, layout, input dispatch with error isolation. */
public abstract class AbstractScreenSession implements ScreenSession {

    protected final Player player;
    protected ScreenSpec spec;
    protected ScreenLayout layout;
    protected boolean open = true;

    protected AbstractScreenSession(Player player, ScreenSpec spec) {
        this.player = player;
        this.spec = spec;
        this.layout = spec.getLayout();
    }

    @Override
    public Player getPlayer() { return player; }

    @Override
    public ScreenSpec getSpec() { return spec; }

    @Override
    public ScreenLayout getLayout() { return layout; }

    @Override
    public boolean isOpen() { return open; }

    @Override
    public void setText(String widgetId, String text) {
        ScreenWidget w = layout.get(widgetId);
        if (w == null) return;
        setLayout(layout.with(w.toBuilder().text(text).build()));
    }

    @Override
    public void setLayout(ScreenLayout layout) {
        this.layout = layout;
        this.spec = spec.withLayout(layout);
        redraw();
    }

    protected abstract void redraw();

    /** Remove everything shown, without sending Closed. */
    protected abstract void dispose(CloseReason reason);

    @Override
    public void close() {
        Screens.close(player, CloseReason.PLUGIN);
    }

    /** Internal: called by {@link Screens} only (use {@link #close()}). */
    public void endSession(CloseReason reason) {
        if (!open) return;
        open = false;
        try {
            dispose(reason);
        } finally {
            dispatch(new ScreenInput.Closed(reason));
        }
    }

    public void dispatch(ScreenInput input) {
        if (input instanceof ScreenInput.Click && !open) return;
        try {
            spec.getHandler().onInput(this, input);
        } catch (Throwable t) {
            SCore.plugin.getLogger().log(Level.WARNING, "[SCore Screens] error in the handler of " + spec.getId(), t);
        }
    }
}
