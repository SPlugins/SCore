package com.ssomar.score.events;

import com.ssomar.executableitems.commands.RefreshCommand;
import com.ssomar.executableitems.commands.VariableModificationCommand;
import com.ssomar.executableitems.executableitems.activators.ActivatorEIFeature;
import com.ssomar.score.sobject.sactivator.EventInfo;
import com.ssomar.score.utils.emums.ResetSetting;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Custom Event requested by altephcomputer to allow other plugins to get details of an item refresh.
 * All the event calls are done in ExecutableItems codebase.
 */
@Getter
public class RefreshExecutableItemEvent extends Event implements Cancellable {


    private static final HandlerList HANDLERS = new HandlerList();
    /**
     * Returns the id of the refreshed executable item
     */
    private final String executableItemID;
    /**
     * The context for this is that when you refresh an ExecutableItem, you get to select what option to refresh. You can
     * turn the contents into string since ResetSetting values are enums.
     */
    private final List<ResetSetting> settings;
    /**
     * When refreshing an ExecutableItem, it requires a target player to perform item refresh tasks. This variable contains that
     * target player.
     */
    private final Player player;
    /**
     * Possible outputs: <br/>
     * - {@code REFRESH_CMD} (Done in {@link RefreshCommand#refresh(List, List, List)})<br/>
     * - {@code ACTIVATOR_AUTOUPDATE} (Done in {@link ActivatorEIFeature#run(Object, EventInfo)})
     */
    private final String refreshSource;

    /**
     * When this event is called, the caller will provide the details in the arguments. As of this writing, all calls are done
     * by ExecutableItems
     * @param executableItemID
     * @param settings
     * @param player
     * @param refreshSource
     */
    public RefreshExecutableItemEvent(String executableItemID, List<ResetSetting> settings, Player player, String refreshSource) {
        this.executableItemID = executableItemID;
        this.settings = settings;
        this.player = player;
        this.refreshSource = refreshSource;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean b) {

    }
}
