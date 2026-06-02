package com.ssomar.score.utils.scheduler;

import com.ssomar.score.SCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class BukkitSchedulerHook implements SchedulerHook {
    private final Plugin plugin;

    /**
     * Mainly used to provide the instance (itself) a pointer to the library's plugin variable
     * for relevant uses.
     * @param plugin
     */
    public BukkitSchedulerHook(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Used to convert a {@link Runnable} instance into a {@link BukkitRunnable}
     * @param runnable
     * @return
     */
    public BukkitRunnable toBukkitRunnable(Runnable runnable) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        };
    }

    /**
     * A basic non-async method for executing code at a later time
     * @param runnable
     * @param delay
     * @return
     */
    @Override
    public ScheduledTask runTask(Runnable runnable, long delay) {

        if (delay > 0) return new BukkitScheduledTask(toBukkitRunnable(runnable).runTaskLater(plugin, delay).getTaskId());
        else {
            if (Bukkit.isPrimaryThread()) {
                runnable.run();
                return new ScheduledTask() {
                    @Override
                    public void cancel() {}

                    @Override
                    public boolean isCancelled() {
                        return false;
                    }
                };
            }
            return new BukkitScheduledTask(toBukkitRunnable(runnable).runTask(plugin).getTaskId());
        }
    }

    /**
     * A basic non-async method for executing code in repeat for a period of time
     * @param runnable
     * @param initDelay
     * @param period
     * @return
     */
    @Override
    public ScheduledTask runRepeatingTask(Runnable runnable, long initDelay, long period) {
        return new BukkitScheduledTask(toBukkitRunnable(runnable).runTaskTimer(plugin, initDelay, period).getTaskId());
    }

    @Override
    public ScheduledTask runAsyncTask(Runnable runnable, long delay) {
        if(delay > 0){
            return new BukkitScheduledTask(toBukkitRunnable(runnable).runTaskLaterAsynchronously(plugin, delay).getTaskId());
        }
        else return new BukkitScheduledTask(toBukkitRunnable(runnable).runTaskAsynchronously(plugin).getTaskId());
    }

    @Override
    public ScheduledTask runAsyncRepeatingTask(Runnable runnable, long initDelay, long period) {
        return new BukkitScheduledTask(toBukkitRunnable(runnable).runTaskTimerAsynchronously(plugin, initDelay, period).getTaskId());
    }

    /**
     * This method is only present to allow {@link BukkitSchedulerHook} and {@link RegionisedSchedulerHook} to share
     * the same interface ({@link SchedulerHook}).<br/>
     * Other than that, it's no different from {@link BukkitSchedulerHook#runTask(Runnable, long)}
     * @param runnable
     * @param retired
     * @param entity
     * @param delay
     * @return
     */
    @Override
    public ScheduledTask runEntityTask(Runnable runnable, Runnable retired, Entity entity, long delay) {
        return runTask(runnable, delay);
    }

    @Override
    public ScheduledTask runEntityRepeatingTask(Runnable runnable, Runnable retired, Entity entity, long initDelay, long period) {
        return runRepeatingTask(runnable, initDelay, period);
    }

    @Override
    public ScheduledTask runEntityTaskAsap(Runnable runnable, Runnable retired, Entity entity) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
            return new ScheduledTask() {
                @Override
                public void cancel() {}

                @Override
                public boolean isCancelled() {
                    return false;
                }
            };
        }
        return runTask(runnable, 0);
    }

    @Override
    public ScheduledTask runLocationTask(Runnable runnable, Location location, long delay) {
        return runTask(runnable, delay);
    }

    @Override
    public ScheduledTask runLocationTaskAsap(Runnable runnable, Location location) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
            return new ScheduledTask() {
                @Override
                public void cancel() {}

                @Override
                public boolean isCancelled() {
                    return false;
                }
            };
        }
        return runTask(runnable, 0);
    }

    private static class BukkitScheduledTask implements ScheduledTask {
        private final int taskId;

        private BukkitScheduledTask(int taskId) {
            this.taskId = taskId;
        }

        @Override
        public void cancel() {
            Bukkit.getScheduler().cancelTask(taskId);
        }

        @Override
        public boolean isCancelled() {
            return !Bukkit.getScheduler().isQueued(taskId) && !Bukkit.getScheduler().isCurrentlyRunning(taskId);
        }
    }

}

