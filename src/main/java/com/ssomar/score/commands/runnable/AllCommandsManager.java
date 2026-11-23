package com.ssomar.score.commands.runnable;

import com.ssomar.score.commands.runnable.block.BlockCommandManager;
import com.ssomar.score.commands.runnable.entity.EntityCommandManager;
import com.ssomar.score.commands.runnable.mixed_player_entity.MixedCommandsManager;
import com.ssomar.score.commands.runnable.player.PlayerCommandManager;

import java.util.ArrayList;
import java.util.List;

public class AllCommandsManager extends CommandManager<SCommand> {

    private static AllCommandsManager instance;

    private List<String> commandsThatRunCommandsNames;

    public AllCommandsManager() {
        List<SCommand> commands = new ArrayList<>();
        commands.addAll(MixedCommandsManager.getInstance().getCommands());
        commands.addAll(PlayerCommandManager.getInstance().getCommands());
        commands.addAll(EntityCommandManager.getInstance().getCommands());
        commands.addAll(BlockCommandManager.getInstance().getCommands());

        /* Sort by priority */
        commands.sort((c1, c2) -> {
            if (c1.getPriority() < c2.getPriority()) return 1;
            else if (c1.getPriority() > c2.getPriority()) return -1;
            else return 0;
        });

        setCommands(commands);

        commandsThatRunCommandsNames = new ArrayList<>();
        for(SCommand command : commands) {
        	if(command.isCanExecuteCommands()) commandsThatRunCommandsNames.addAll(command.getNames());
        }
    }

    public List<String> getCommandsThatRunCommandsNames() {
        return commandsThatRunCommandsNames;
    }

    public boolean startsWithCommandThatRunCommands(String command) {
    	for(String cmd : commandsThatRunCommandsNames) {
    		if(command.startsWith(cmd)) return true;
    	}
    	return false;
    }

    public static AllCommandsManager getInstance() {
        if (instance == null) instance = new AllCommandsManager();
        return instance;
    }
}
