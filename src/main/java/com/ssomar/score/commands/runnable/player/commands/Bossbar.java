package com.ssomar.score.commands.runnable.player.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.SsomarDev;
import com.ssomar.score.commands.runnable.CommandSetting;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.player.PlayerCommand;
import com.ssomar.score.utils.scheduler.ScheduledTask;
import com.ssomar.score.utils.strings.StringConverter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class Bossbar extends PlayerCommand {

    private static Bossbar instance;
    Map<Player, Map<BossBar, ScheduledTask>> cache;

    public Bossbar() {
        instance = this;
        cache = new HashMap<>();
        CommandSetting time = new CommandSetting("time", 0, Integer.class, 200);
        CommandSetting color = new CommandSetting("color", 1, BarColor.class, BarColor.BLUE);
        CommandSetting text = new CommandSetting("text", 2, String.class, "Hello_world");
        CommandSetting count = new CommandSetting("count", -1, Integer.class, 0);
        CommandSetting countTicks = new CommandSetting("countTicks", -1, Boolean.class, false);
        CommandSetting countOrder = new CommandSetting("countOrder", -1, String.class, "descending");
        CommandSetting hideCount = new CommandSetting("hideCount", -1, Boolean.class, false);
        // NO_OVERRIDE , OVERRIDE_ALL, OVERRIDE_SAME_TEXT
        CommandSetting overrideMode = new CommandSetting("overrideMode", -1, String.class, "NO_OVERRIDE");
        CommandSetting barProgress = new CommandSetting("barProgress", 0, Float.class, 1.0f);

        text.setAcceptUnderScoreForLongText(true);
        List<CommandSetting> settings = getSettings();
        settings.add(time);
        settings.add(color);
        settings.add(text);
        settings.add(count);
        settings.add(countTicks);
        settings.add(countOrder);
        settings.add(hideCount);
        settings.add(overrideMode);
        settings.add(barProgress);
        setNewSettingsMode(true);
    }

    @Override
    public void run(Player p, Player receiver, SCommandToExec sCommandToExec) {
        BarColor color = (BarColor) sCommandToExec.getSettingValue("color");
        String text = (String) sCommandToExec.getSettingValue("text");
        Integer time = (Integer) sCommandToExec.getSettingValue("time");
        Integer count = (Integer) sCommandToExec.getSettingValue("count");
        Boolean countTicks = (Boolean) sCommandToExec.getSettingValue("countTicks");
        String countOrder = ((String) sCommandToExec.getSettingValue("countOrder")).toLowerCase();
        boolean isAscending = countOrder.equalsIgnoreCase("ascending");
        boolean hideCount = (Boolean) sCommandToExec.getSettingValue("hideCount");
        String overrideMode = (String) sCommandToExec.getSettingValue("overrideMode");
        Float barProgress = (Float) sCommandToExec.getSettingValue("barProgress");

        List<String> args = sCommandToExec.getOtherArgs();
        StringBuilder message = new StringBuilder(text);
        message.append(" ");
        for (String s : args) {
            message.append(s).append(" ");
        }
        message = new StringBuilder(message.substring(0, message.length() - 1));
        String coloredMessage = StringConverter.coloredString(message.toString());

        if (overrideMode.equalsIgnoreCase("OVERRIDE_ALL")) {
            Map<org.bukkit.boss.BossBar, ScheduledTask> tasks = this.cache.get(receiver);
            if (tasks != null) {
                for (Map.Entry<org.bukkit.boss.BossBar, ScheduledTask> entry : tasks.entrySet()) {
                    removeBossBar(entry.getKey(), receiver);
                }
            }
        } else if (overrideMode.equalsIgnoreCase("OVERRIDE_SAME_TEXT")) {
            SsomarDev.testMsg("Override mode: " + overrideMode, true);
            Map<org.bukkit.boss.BossBar, ScheduledTask> tasks = this.cache.get(receiver);
            if (tasks != null) {
                for (Map.Entry<org.bukkit.boss.BossBar, ScheduledTask> entry : tasks.entrySet()) {
                    org.bukkit.boss.BossBar bossBar = entry.getKey();
                    SsomarDev.testMsg("Checking boss bar with same text: " + bossBar.getTitle(), true);
                    SsomarDev.testMsg("Boss bar with same text: " + bossBar.getTitle() + " vs " + coloredMessage, true);
                    if (StringConverter.decoloredString(bossBar.getTitle()).contains(StringConverter.decoloredString(coloredMessage))) {
                        SsomarDev.testMsg("Removing boss bar with same text: " + bossBar.getTitle(), true);
                        removeBossBar(entry.getKey(), receiver);
                    }
                }
            }
        }


        if(!coloredMessage.isEmpty()) {
            final String finalMessage = message.toString();
            // On Folia, Bukkit.createBossBar registers into the server's CustomBossEvents
            // HashMap, which is iterated on player connect from a different region thread —
            // causing ConcurrentModificationException (SCore #173). Dispatch creation (and
            // later server.removeBossBar) exclusively through GlobalRegionScheduler so they
            // never race with onPlayerConnect. On Bukkit the runnable runs inline (delay=0
            // maps to synchronous in BukkitSchedulerHook when on primary thread).
            Runnable setupBossBar = () -> {
                BossBar bossBar = Bukkit.createBossBar(NamespacedKey.randomKey(), coloredMessage, color, BarStyle.SOLID);
                bossBar.addPlayer(receiver);

                if(barProgress < 1.0) {
                    bossBar.setProgress((double) barProgress);
                }

                // Register bossbar without task
                if(cache.containsKey(receiver)) {
                    cache.get(receiver).put(bossBar, null);
                } else {
                    Map<org.bukkit.boss.BossBar, ScheduledTask> map = new HashMap<>();
                    map.put(bossBar, null);
                    cache.put(receiver, map);
                }

                if (count > 0) {
                    AtomicReference<ScheduledTask> task = new AtomicReference<>();
                    Runnable runnable = new Runnable() {
                        int counter = isAscending ? 0 : Math.round(count * barProgress);

                        @Override
                        public void run() {

                            if (isAscending) {
                                if (counter >= count) {
                                    removeBossBar(bossBar, receiver);
                                    return;
                                }
                                counter++;
                            } else {
                                if (counter <= 0) {
                                    removeBossBar(bossBar, receiver);
                                    return;
                                }
                                counter--;
                            }

                            String countText = String.valueOf(counter);
                            if (!countTicks) {
                                countText += "s";
                            }
                            bossBar.setProgress((double) counter / count);
                            if (hideCount) {
                                bossBar.setTitle(StringConverter.coloredString(finalMessage));
                            } else bossBar.setTitle(StringConverter.coloredString(finalMessage + " " + countText));
                        }
                    };
                    task.set(SCore.schedulerHook.runEntityRepeatingTask(runnable, () -> removeBossBar(bossBar, receiver), receiver, 0, countTicks ? 1 : 20));

                    // Register bossbar with task
                    if(cache.containsKey(receiver)) {
                        cache.get(receiver).put(bossBar, task.get());
                    } else {
                        Map<org.bukkit.boss.BossBar, ScheduledTask> map = new HashMap<>();
                        map.put(bossBar, task.get());
                        cache.put(receiver, map);
                    }
                } else {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            removeBossBar(bossBar, receiver);
                        }
                    };
                    SCore.schedulerHook.runEntityTask(runnable, () -> removeBossBar(bossBar, receiver), receiver, time);
                }
            };

            // Route through global region scheduler on Folia to serialise registry writes
            // with onPlayerConnect; on Bukkit run directly (BukkitSchedulerHook.runTask with
            // delay=0 on primary thread calls runnable.run() synchronously).
            SCore.schedulerHook.runTask(setupBossBar, 0);
        }
    }

    public void removeBossBar(org.bukkit.boss.BossBar bossBar, Player p) {
        bossBar.removeAll();
        if(bossBar instanceof KeyedBossBar){
            NamespacedKey key = ((KeyedBossBar) bossBar).getKey();
            // Must run on GlobalRegionScheduler to avoid racing with onPlayerConnect (#173)
            SCore.schedulerHook.runTask(() -> Bukkit.getServer().removeBossBar(key), 0);
        }
        Map<org.bukkit.boss.BossBar, ScheduledTask> tasks = this.cache.get(p);
        if (tasks == null) return;
        ScheduledTask task = tasks.get(bossBar);
        if (task == null) return;
        task.cancel();
        tasks.remove(bossBar);
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("BOSSBAR");
        return names;
    }

    @Override
    public String getTemplate() {
        return "BOSSBAR time:200 color:BLUE text:Hello_world count:0 countTicks:false countOrder:descending";
    }

    @Override
    public ChatColor getColor() {
        return null;
    }

    @Override
    public ChatColor getExtraColor() {
        return null;
    }

    public static Bossbar getInstance() {
        if (instance == null) {
            instance = new Bossbar();
        }
        return instance;
    }

    public void clearTasks(Player p) {
        Map<org.bukkit.boss.BossBar, ScheduledTask> tasks = this.cache.get(p);
        if (tasks != null) {
            for (Map.Entry<org.bukkit.boss.BossBar, ScheduledTask> entry : tasks.entrySet()) {
                removeBossBar(entry.getKey(), p);
            }
            tasks.clear();
        }
    }

    public void clearTasks() {
        for (Player p : cache.keySet()) {
            clearTasks(p);
        }
    }
}
