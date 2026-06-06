package com.ssomar.score.utils.scheduler;

import com.ssomar.score.SCore;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface SchedulerHook {

    ScheduledTask runTask(Runnable runnable, long delay);

    ScheduledTask runRepeatingTask(Runnable runnable, long initDelay, long period);

    ScheduledTask runAsyncTask(Runnable runnable, long delay);

    ScheduledTask runAsyncRepeatingTask(Runnable runnable, long initDelay, long period);

    /**
     * Refer to {@link SCore#schedulerHook} on what scheduler SCore will use for your server.<br/>
     * - If it uses {@link RegionisedSchedulerHook} and the entity tied to this runnable task
     * is dead/removed, it will retire early.<br/>
     * - If it uses {@link BukkitSchedulerHook}, it will just execute a delayed task.
     * @param runnable
     * @param retired
     * @param entity
     * @param delay
     * @return ScheduledTask
     */
    ScheduledTask runEntityTask(Runnable runnable, Runnable retired, Entity entity, long delay);

    /**
     * Repeating task bound to an entity's scheduler thread (Folia) or the main thread (Bukkit).
     * On Folia, retires and cancels if the entity is removed.
     */
    ScheduledTask runEntityRepeatingTask(Runnable runnable, Runnable retired, Entity entity, long initDelay, long period);

    ScheduledTask runEntityTaskAsap(Runnable runnable, Runnable retired, Entity entity);

    ScheduledTask runLocationTask(Runnable runnable, Location location, long delay);

    ScheduledTask runLocationTaskAsap(Runnable runnable, Location location);

}
