package com.ssomar.score.utils.scheduler;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

public class RegionisedSchedulerHook implements SchedulerHook {
    private final Plugin plugin;

    public RegionisedSchedulerHook(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public ScheduledTask runTask(Runnable runnable, long delay) {
        if(plugin == null || !plugin.isEnabled()) return null;
        if(delay > 0)
            return new RegionisedScheduledTask(((GlobalRegionScheduler)getReflectedObjectOfBukkit("getGlobalRegionScheduler")).runDelayed(plugin, task -> runnable.run(), delay));
        else
            return new RegionisedScheduledTask(((GlobalRegionScheduler)getReflectedObjectOfBukkit("getGlobalRegionScheduler")).run(plugin, task -> runnable.run()));
    }


    @Override
    public ScheduledTask runRepeatingTask(Runnable runnable, long initDelay, long period) {
        if(plugin == null || !plugin.isEnabled()) return null;
        if(initDelay <= 0) initDelay = 1;
        return new RegionisedScheduledTask(((GlobalRegionScheduler)getReflectedObjectOfBukkit("getGlobalRegionScheduler")).runAtFixedRate(plugin, task -> runnable.run(), initDelay, period));
    }

    @Override
    public ScheduledTask runAsyncTask(Runnable runnable, long delay) {
        if(plugin == null || !plugin.isEnabled()) return null;
        // convert tick to ms
        delay *= 50;
        if(delay > 0)
            return new RegionisedScheduledTask(((AsyncScheduler)getReflectedObjectOfBukkit("getAsyncScheduler")).runDelayed(plugin, task -> runnable.run(), delay, TimeUnit.MILLISECONDS));
        else
            return new RegionisedScheduledTask(((AsyncScheduler)getReflectedObjectOfBukkit("getAsyncScheduler")).runNow(plugin, task -> runnable.run()));
    }

    @Override
    public ScheduledTask runAsyncRepeatingTask(Runnable runnable, long initDelay, long period) {
        if(plugin == null || !plugin.isEnabled()) return null;
        // convert tick to ms
        initDelay *= 50;
        period *= 50;
        if(initDelay <= 0) initDelay = 1;
        return new RegionisedScheduledTask(((AsyncScheduler)getReflectedObjectOfBukkit("getAsyncScheduler")).runAtFixedRate(plugin, task -> runnable.run(), initDelay, period, TimeUnit.MILLISECONDS));
    }

    @Override
    public ScheduledTask runEntityTask(Runnable runnable, Runnable retired, Entity entity, long delay) {
        if(plugin == null || !plugin.isEnabled()) return null;
        io.papermc.paper.threadedregions.scheduler.ScheduledTask scheduledTask = null;
        EntityScheduler scheduler = null;
        try {
            scheduler = (EntityScheduler) Entity.class.getMethod("getScheduler").invoke(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        if(delay > 0)
            scheduledTask = scheduler.runDelayed(plugin, task -> runnable.run(), retired, delay);
        else scheduledTask = scheduler.run(plugin, task -> runnable.run(), retired);
        return scheduledTask == null ? null : new RegionisedScheduledTask(scheduledTask);
    }

    @Override
    public ScheduledTask runEntityRepeatingTask(Runnable runnable, Runnable retired, Entity entity, long initDelay, long period) {
        if(plugin == null || !plugin.isEnabled()) return null;
        if(initDelay <= 0) initDelay = 1;
        EntityScheduler scheduler;
        try {
            scheduler = (EntityScheduler) Entity.class.getMethod("getScheduler").invoke(entity);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        io.papermc.paper.threadedregions.scheduler.ScheduledTask scheduledTask =
                scheduler.runAtFixedRate(plugin, task -> runnable.run(), retired, initDelay, period);
        return scheduledTask == null ? null : new RegionisedScheduledTask(scheduledTask);
    }

    @Override
    public ScheduledTask runEntityTaskAsap(Runnable runnable, Runnable retired, Entity entity) {
        if(plugin == null || !plugin.isEnabled()) return null;

        boolean isOwnedByCurrentRegion = false;
        try {
            isOwnedByCurrentRegion = (boolean) Bukkit.class.getMethod("isOwnedByCurrentRegion", Entity.class).invoke(Bukkit.class, entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        if (isOwnedByCurrentRegion) {
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
        return runEntityTask(runnable, retired, entity, 0);
    }

    @Override
    public ScheduledTask runLocationTask(Runnable runnable,Location location, long delay) {
        if(plugin == null || !plugin.isEnabled()) return null;
        io.papermc.paper.threadedregions.scheduler.ScheduledTask scheduledTask = null;
        if(delay > 0)
            scheduledTask = ((RegionScheduler)getReflectedObjectOfBukkit("getRegionScheduler")).runDelayed(plugin, location, task -> runnable.run(), delay);
        else scheduledTask = ((RegionScheduler)getReflectedObjectOfBukkit("getRegionScheduler")).run(plugin, location, task -> runnable.run());
        return scheduledTask == null ? null : new RegionisedScheduledTask(scheduledTask);
    }

    @Override
    public ScheduledTask runLocationTaskAsap(Runnable runnable, Location location) {
        if(plugin == null || !plugin.isEnabled()) return null;

        boolean isOwnedByCurrentRegion = false;
        try {
            isOwnedByCurrentRegion = (boolean) Bukkit.class.getMethod("isOwnedByCurrentRegion", Location.class).invoke(Bukkit.class, location);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        if (isOwnedByCurrentRegion) {
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
        return runLocationTask(runnable, location, 0);
    }

    public Object getReflectedObjectOfBukkit(String methodName) {
        try {
            return Bukkit.class.getMethod(methodName).invoke(Bukkit.class);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isCompatible() {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    private static class RegionisedScheduledTask implements ScheduledTask {
        private final io.papermc.paper.threadedregions.scheduler.ScheduledTask scheduledTask;

        private RegionisedScheduledTask(io.papermc.paper.threadedregions.scheduler.ScheduledTask scheduledTask) {
            this.scheduledTask = scheduledTask;
        }

        @Override
        public void cancel() {
            scheduledTask.cancel();
        }

        @Override
        public boolean isCancelled() {
            return scheduledTask.isCancelled();
        }
    }

}
