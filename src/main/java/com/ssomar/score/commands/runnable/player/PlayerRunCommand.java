package com.ssomar.score.commands.runnable.player;

import com.ssomar.score.SCore;
import com.ssomar.score.SsomarDev;
import com.ssomar.score.commands.runnable.ActionInfo;
import com.ssomar.score.commands.runnable.CommandsHandler;
import com.ssomar.score.commands.runnable.RunCommand;
import com.ssomar.score.commands.runnable.SCommandToExec;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerRunCommand extends RunCommand {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Getter @Setter
    private UUID launcherUUID;
    @Getter @Setter
    private UUID receiverUUID;
    @Getter @Setter
    private boolean silenceOutput;

    @Getter
    private boolean runOffline;

    @Getter
    private boolean clearIfDisconnect;

    public PlayerRunCommand(String brutCommand, int delay, ActionInfo aInfo) {
        super(brutCommand, delay, aInfo);
        this.initRunOffline(brutCommand);
        this.initClearIfDisconnect(brutCommand);
    }

    public PlayerRunCommand(String brutCommand, long runTime, ActionInfo aInfo) {
        super(brutCommand, runTime, aInfo);
        this.initRunOffline(brutCommand);
        this.initClearIfDisconnect(brutCommand);
    }

    public void initRunOffline(String brutCommand) {
        if (brutCommand.contains("[<OFFLINE>]")) {
            runOffline = true;
            this.setBrutCommand(brutCommand.replaceAll("\\[<OFFLINE>]", "").trim());
        } else runOffline = false;
    }

    public void initClearIfDisconnect(String brutCommand) {
        if (brutCommand.contains("[<CLEAR_IF_DISCONNECT>]")) {
            clearIfDisconnect = true;
            this.setBrutCommand(brutCommand.replaceAll("\\[<CLEAR_IF_DISCONNECT>]", "").trim());
        } else clearIfDisconnect = false;
    }

    /* The tags were removed from the command by initRunOffline / initClearIfDisconnect: saved again with it, the command loaded after a restart keeps them */
    @Override
    public String getBrutCommandToSave() {
        String command = super.getBrutCommandToSave();
        if (clearIfDisconnect) command = "[<CLEAR_IF_DISCONNECT>] " + command;
        if (runOffline) command = "[<OFFLINE>] " + command;
        return command;
    }

    @Override
    public void pickupInfo() {
        ActionInfo aInfo = this.getaInfo();

        launcherUUID = aInfo.getLauncherUUID();

        receiverUUID = aInfo.getReceiverUUID();

        silenceOutput = aInfo.isSilenceOutput();
    }

    @Override
    public void runGetManager() {
        this.runCommand(PlayerCommandManager.getInstance());
    }

    @Override
    public void runCommand(SCommandToExec sCommandToExec) {
        PlayerSCommand pCommand = (PlayerSCommand) sCommandToExec.getSCommand();

        //SsomarDev.testMsg("PRE RUN COMMAND BEFORE CHECK LAUNCHER", true);
        @Nullable Player launcher = null;
        if(launcherUUID != null) launcher = Bukkit.getPlayer(launcherUUID);

        Player receiver = Bukkit.getPlayer(receiverUUID);

        //SsomarDev.testMsg("PRE RUN COMMAND STEP: "+this.getaInfo().getStep(), true);
        sCommandToExec.setActionInfo(getaInfo());
        pCommand.run(launcher, receiver, sCommandToExec);
    }


    @Override
    public void insideDelayedCommand() {
        Player receiver = Bukkit.getPlayer(receiverUUID);

        if ((receiver != null && receiver.isOnline()) || runOffline) {
            runCommand(PlayerCommandManager.getInstance());
        }
        //else {
        //ADD THE COMMAND IN THE DB
        /* No need >> onPlayerQuitEvent its auto delete and save in the DB */
        //}
        CommandsHandler.getInstance().removeDelayedCommand(getUuid(), receiverUUID);
    }

    @Override
    public void executeRunnable(Runnable runnable) {
        /* Exceptional case for server loop */
        /* if(receiverUUID == null) {
            SCore.schedulerHook.runTask(runnable, 0);
            return;
        } */
        SsomarDev.testMsg("[#s_1] [-1] PlayerRunCommand.java > executeRunnable() : "+receiverUUID, false, SsomarDev.DebugMsgGroups._1);
        Player receiver = Bukkit.getPlayer(receiverUUID);
        // Explanation:
        // - Ssomar wants to use runEntityTask

        if (receiver == null) {
            SCore.schedulerHook.runTask(runnable, 0);
        } else {
            SCore.schedulerHook.runEntityTaskAsap(runnable, null, receiver);
        }
    }
}
