package com.ssomar.score.events;

import com.ssomar.score.SCore;
import com.ssomar.sevents.events.player.click.CancelOffHandInteractionManager;
import com.ssomar.sevents.events.player.click.TooManyInteractionManager;
import com.ssomar.sevents.events.player.click.TransmitCancelInteractionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Self-healing cleanup for the per-player interaction markers of SEvents
 * ({@link TransmitCancelInteractionManager}, {@link CancelOffHandInteractionManager},
 * {@link TooManyInteractionManager}).
 *
 * Those static maps are used to transmit a cancellation from one native event of a click
 * to the other native events of the SAME click (PlayerInteractAtEntityEvent -&gt;
 * PlayerInteractEntityEvent, main hand -&gt; off hand, ...). They are only meaningful for a
 * few ticks, but nothing ever removed the entries when the follow-up packet never arrives
 * (armor stands consume interactAt client side so no INTERACT packet follows, empty
 * off-hand air clicks send no off-hand event, Bedrock/Geyser players never send off-hand
 * interactions, another plugin pre-cancels the event, ...).
 *
 * A stale entry then cancels a future, unrelated entity interaction of that player
 * (and can get re-seeded in a loop), which made some players unable to right click
 * entities until a full server restart, see:
 * https://discord.com/channels/701066025516531753/1516845050590400582
 * https://discord.com/channels/701066025516531753/1524291528938492105
 *
 * This class:
 * - removes the markers of a player when he disconnects (a relog now always fixes the state)
 * - purges, with a two-generation sweep, any marker that survived a full sweep period
 *   (entries live at most ~2s instead of forever), which also self-heals any stuck path
 *   we don't know about yet.
 */
public class SEventsStuckInteractionCleaner implements Listener {

    /* 20 ticks: a marker is only valid inside a single click (same tick or the next ones
     * under heavy lag), so 1-2 seconds of lifetime is already very generous. */
    private static final long SWEEP_PERIOD_TICKS = 20L;

    private final Set<UUID> transmitCancelPreviousGen = new HashSet<>();
    private final Set<UUID> cancelOffHandPreviousGen = new HashSet<>();
    private final Set<UUID> tooManyPreviousGen = new HashSet<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        TransmitCancelInteractionManager.getInstance().remove(uuid);
        CancelOffHandInteractionManager.getInstance().remove(uuid);
        TooManyInteractionManager.getInstance().remove(uuid);
        transmitCancelPreviousGen.remove(uuid);
        cancelOffHandPreviousGen.remove(uuid);
        tooManyPreviousGen.remove(uuid);
    }

    public void startSweepTask() {
        SCore.schedulerHook.runRepeatingTask(this::sweep, SWEEP_PERIOD_TICKS, SWEEP_PERIOD_TICKS);
    }

    private void sweep() {
        sweep(TransmitCancelInteractionManager.getInstance(), transmitCancelPreviousGen);
        sweep(CancelOffHandInteractionManager.getInstance(), cancelOffHandPreviousGen);
        sweep(TooManyInteractionManager.getInstance(), tooManyPreviousGen);
    }

    /**
     * Two-generation sweep: an entry still present since the previous sweep is stale
     * (older than one full period) and gets removed. Fresh entries (added since the last
     * sweep) are kept for one more period, so a click in progress is never affected.
     */
    private void sweep(Map<UUID, Integer> map, Set<UUID> previousGen) {
        if (map.isEmpty()) {
            previousGen.clear();
            return;
        }
        for (UUID uuid : new HashSet<>(map.keySet())) {
            if (previousGen.contains(uuid)) map.remove(uuid);
        }
        previousGen.clear();
        previousGen.addAll(map.keySet());
    }
}
