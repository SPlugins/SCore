package com.ssomar.score.commands.runnable;


import com.ssomar.score.config.GeneralConfig;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;

import java.util.ArrayList;
import java.util.List;

public class LogFilter implements Filter, LifeCycle {

    private final boolean debug = false;

    private List<String> messageToHide = new ArrayList<>();

    private List<String> globalMessageToHide = new ArrayList<>();

    public LogFilter() {
        reload();
    }

    public void reload(){
        messageToHide = new ArrayList<>();
        messageToHide.add("Applied effect");
        messageToHide.add("Playing effect");
        messageToHide.add("Removed effect");
        messageToHide.add("Title command successfully executed");
        messageToHide.add("Gave");
        messageToHide.add("Given");
        messageToHide.add("Giving");
        messageToHide.add("Displaying particle");
        messageToHide.add("Summoned new");
        messageToHide.add("Played sound");
        messageToHide.add("Teleported");
        messageToHide.add("Could not set the block");
        messageToHide.add("Changed the block at");
        messageToHide.add("Unable to apply this effect");
        messageToHide.add("That position is not loaded");
        messageToHide.add("The particle was not visible for anybody");
        messageToHide.add("Disabling SCore");
        messageToHide.add("Successfully filled");
        messageToHide.add("Replaced a slot on");
        messageToHide.add("The number you have entered");
        messageToHide.add("Killed");
        messageToHide.add("No entity was found");
        messageToHide.add("Unknown command");
        messageToHide.add("Modified entity data of");
        messageToHide.add("Target has no effects to remove");
        messageToHide.add("Target doesn't have the requested effect");
        messageToHide.add("Showing new title");
        messageToHide.add("Showing new subtitle");
        messageToHide.add("Showing new actionbar");
        messageToHide.add("[SCore]");
        messageToHide.add("[ExecutableBlocks]");
        messageToHide.add("[ExecutableItems]");
        messageToHide.add("Added tag");
        messageToHide.add("Removed tag");
        messageToHide.add("Set the weather");
        messageToHide.add(" from any team");
        messageToHide.add("Set");
        messageToHide.add("Removed");
        messageToHide.add("Added");
        messageToHide.add("Changed title display times for");
        messageToHide.add("Custom bossbar");
        messageToHide.add("Set the time");
        messageToHide.add("Stopped sound");
        messageToHide.add("started riding");
        messageToHide.add("has no modifier"); // related to attrbute command
        messageToHide.add("Replaced a slot at");
        messageToHide.add("Changed the block at");
        messageToHide.add("Base value for attribute");
        /* recent versions write "The base value for attribute ... set to / reset to" (lowercase b), the line above no longer matched (Discord 1557465154952765582) */
        messageToHide.add("base value for attribute");
        messageToHide.add("base value of attribute");
        messageToHide.add("Nothing changed.");
        messageToHide.add("is not holding any item");
        messageToHide.add("cannot support that enchantment");
        messageToHide.add("The sound is too far away to be heard");
        messageToHide.add("No player was found");
        messageToHide.addAll(GeneralConfig.getInstance().getSilenceOutputs());

        globalMessageToHide = new ArrayList<>();
        globalMessageToHide.add("Loaded class");
        globalMessageToHide.addAll(GeneralConfig.getInstance().getGlobalSilenceOutputs());
    }


    public Filter.Result checkMessage(String message) {
        boolean hide = false;

        if (message == null) {
            return Result.NEUTRAL;
        }
        //SsomarDev.testMsg("checkMessage "+message+ " silence: &e"+FilterManager.getInstance().isSilenceOuput(), true);
        if (FilterManager.getInstance().isSilenceOuput()) {
            for (String s : this.messageToHide) {
                if (message.contains(s)) {
                    hide = true;
                    break;
                }
            }
        }
        for (String s : this.globalMessageToHide) {
            if (message.contains(s)) {
                hide = true;
                break;
            }
        }


        return hide ? Result.DENY : Result.NEUTRAL;
    }

    public Filter.Result checkError(String message) {
        boolean hide = false;
        // Remove Location for deserialization message for EB
        if(message.contains("org.bukkit.Location for deserialization")) {
            hide = true;
        }
        // Remove dependency message
        if(message.contains("Loaded class")) {
            hide = true;
        }

        return hide ? Result.DENY : Result.NEUTRAL;
    }

    public Result filter(LogEvent event) {
        if(event.getLevel() == Level.ERROR) return this.checkError(event.getMessage().getFormattedMessage());
        return this.checkMessage(event.getMessage().getFormattedMessage());
    }


    public Result getOnMatch() {
        return Result.NEUTRAL;
    }

    public Result getOnMismatch() {
        return Result.NEUTRAL;
    }

    public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, String msg, Object... params) {
        if(logger.getLevel() == Level.ERROR) return this.checkError(msg);
        return this.checkMessage(msg);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0) {
        if(logger.getLevel() == Level.ERROR) return this.checkError(message);
        return this.checkMessage(message);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1) {
        if(logger.getLevel() == Level.ERROR) return this.checkError(message);
        return this.checkMessage(message);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2) {
        if(logger.getLevel() == Level.ERROR) return this.checkError(message);
        return this.checkMessage(message);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        if(logger.getLevel() == Level.ERROR) return this.checkError(message);
        return this.checkMessage(message);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        if(logger.getLevel() == Level.ERROR) return this.checkError(message);
        return this.checkMessage(message);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        if(logger.getLevel() == Level.ERROR) return this.checkError(message);
        return this.checkMessage(message);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        if(logger.getLevel() == Level.ERROR) return this.checkError(message);
        return this.checkMessage(message);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        if(logger.getLevel() == Level.ERROR) return this.checkError(message);
        return this.checkMessage(message);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        if(logger.getLevel() == Level.ERROR) return this.checkError(message);
        return this.checkMessage(message);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        if(logger.getLevel() == Level.ERROR) return this.checkError(message);
        return this.checkMessage(message);
    }

    public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        if(logger.getLevel() == Level.ERROR) return this.checkError(msg.toString());
        return this.checkMessage(msg.toString());
    }

    public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        if(logger.getLevel() == Level.ERROR) return this.checkError(msg.toString());
        return this.checkMessage(msg.getFormattedMessage());
    }

    @Override
    public State getState() {
        return State.STARTED;
    }

    @Override
    public void initialize() {

    }

    public void start() {
    }

    public void stop() {
    }

    public boolean isStarted() {
        return false;
    }

    @Override
    public boolean isStopped() {
        return false;
    }

}
