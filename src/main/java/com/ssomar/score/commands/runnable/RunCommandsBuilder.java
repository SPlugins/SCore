package com.ssomar.score.commands.runnable;

import com.ssomar.score.SsomarDev;
import com.ssomar.score.commands.runnable.util.commands.*;
import com.ssomar.score.utils.messages.SendMessage;
import com.ssomar.score.utils.strings.StringConverter;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class RunCommandsBuilder {

    public static SendMessage sm = new SendMessage();
    /* Commands to run */
    private List<String> commands;
    private ActionInfo actionInfo;
    /* delay in tick - commands */
    private TreeMap<Integer, List<RunCommand>> finalCommands = new TreeMap<>();

    public RunCommandsBuilder(List<String> commands, ActionInfo actionInfo) {
        this.actionInfo = actionInfo;
        //SsomarDev.testMsg("CURRENT STEP: "+actionInfo.getStep(), true);
        this.commands = commands;
        this.init();
        //SsomarDev.testMsg("CURRENT STEP after init: "+actionInfo.getStep(), true);
    }

    public static SendMessage getSm() {
        return sm;
    }

    public static void setSm(SendMessage sm) {
        RunCommandsBuilder.sm = sm;
    }

    public void init() {
        /* SsomarDev.testMsg("=================== init  commands ==========================", true);
        for (String s : this.commands) {
            SsomarDev.testMsg(s, true);
        } */
        this.commands = this.replaceFor(this.commands);
        /*System.out.println("=================== after for  commands ==========================");
        for (String s : this.commands) {
            System.out.println(s);
        }*/
        this.commands = this.replaceLoop(commands);
        this.initFinalCommands();
        /* SsomarDev.testMsg("=================== finals   commands ==========================", true);
        for (String s : this.commands) {
            SsomarDev.testMsg(s, true);
        } */
    }

    public List<String> selectRandomCommands(List<String> commands, Integer amount, @Nullable NothingObject nothingObject) {
        List<String> commandsList = new ArrayList<>(commands);

        List<String> result = new ArrayList<>();

        int nothingTotal = 0;
        if (nothingObject != null) {
            nothingTotal = nothingObject.getNothingCount();
        }

        for (int i = 0; i < amount; i++) {
            int commandsSize = commandsList.size();
            if (commandsSize == 0) return result;
            int rdn = (int) (Math.random() * (commandsSize+nothingTotal));
            if(rdn >= commandsSize) {
                nothingTotal--;
                if(nothingObject.hasNothingString()) result.add(nothingObject.getNothingString());
            } else {
                result.add(commandsList.get(rdn));
                commandsList.remove(rdn);
            }
        }
        return result;
    }

    public List<String> replaceLoop(List<String> commands) {

        List<String> result = new ArrayList<>();
        boolean isInLoop = false;
        int loopAmount = 0;
        List<String> commandsInLoop = new ArrayList<>();

        for (String s : commands) {

            String command = s;

            if (!command.contains("+++")) {
                if (LoopStart.checkContains(command)) {
                    try {
                        String secondPart = LoopStart.extractIterations(command);
                        if(secondPart == null) continue;
                        if (secondPart.contains("%")) {
                            secondPart = actionInfo.getSp().replacePlaceholder(secondPart, true);
                        }
                        loopAmount = Double.valueOf(secondPart).intValue();
                        isInLoop = true;
                        continue;
                    } catch (Exception e) {
                        loopAmount = 0;
                        isInLoop = false;
                        continue;
                    }
                } else if (LoopEnd.checkContains(command)) {
                    for (int k = 0; k < loopAmount; k++) {
                        result.addAll(commandsInLoop);
                    }
                    loopAmount = 0;
                    isInLoop = false;
                    commandsInLoop.clear();
                    continue;
                }
            } else command = StringConverter.coloredString(s);

            if (isInLoop) commandsInLoop.add(command);
            else result.add(command);
        }
        return result;
    }

    public List<String> replaceFor(List<String> commands) {

        while (containsFor(commands)) {


            boolean isInFor = false;
            int forStart = -1;
            int forEnd = -1;
            List<String> commandsInFor = new ArrayList<>();
            List<Integer> indexToRemove = new ArrayList<>();

            int index = 0;
            String forId = "";
            for (String s : commands) {
                String command = s;
                /* Because the placeholders are not parsed before so we force it for FOR */
                if(command.contains("FOR")) command = actionInfo.getSp().replacePlaceholder(command, true);
                if (!command.contains("+++")) {
                    //SsomarDev.testMsg("command: "+command, true);
                    if (command.contains("FOR [") && command.contains("]") && !isInFor) {
                        if(command.contains(">")){
                            forId = command.split(">")[1];
                            forId = forId.replaceAll(" ", "");
                            //SsomarDev.testMsg("forId: "+forId, true);
                        }
                        commandsInFor.clear();
                        forStart = index;
                        isInFor = true;
                        commandsInFor.add(command);
                        indexToRemove.add(index);
                        // ENDFOR is the old syntax
                    } else if (command.contains("END_FOR") || command.contains("ENDFOR")) {
                        if(!forId.equals("") && (command.split(" ").length < 2 || !command.split(" ")[1].equals(forId))){
                            commandsInFor.add(command);
                            indexToRemove.add(index);
                        }
                        else {
                            forEnd = index;
                            commandsInFor.add(command);
                            indexToRemove.add(index);
                            break;
                        }
                    } else if (isInFor){
                        commandsInFor.add(command);
                        indexToRemove.add(index);
                    }

                }
                index++;
            }

            if(forStart == -1 || forEnd == -1) break;

            /* System.out.println(" ===================== FOR =====================");
            for (String s : commandsInFor) {
                System.out.println(s);
            }*/

            List<String> commandsToReplaceWith = transformFor(commandsInFor);

            // Sort the list in reverse order
            Collections.sort(indexToRemove, Collections.reverseOrder());

            // Remove the elements from indexes
            for (int i : indexToRemove) {
                commands.remove(i);
            }

            commands.addAll(forStart, commandsToReplaceWith);

        }

        return commands;
    }

    public boolean containsFor(List<String> commands) {
       // System.out.println("containsFor ===============================");
        for (String s : commands) {
           // System.out.println("containsFor: "+s);
            /* Because the placeholders are not parsed before so we force it for FOR */
            if(s.contains("FOR")) s = actionInfo.getSp().replacePlaceholder(s, true);
            if (s.contains("FOR [") && s.contains("]") && !s.contains("+++")) return true;
        }
        return false;
    }

    public List<String> transformFor(List<String> commands) {
        if (commands != null) {
            if (commands.size() > 2) {
                List<String> results = new ArrayList<>();
                String forCommand = commands.get(0);

                // delete for command
                commands.remove(0);
                // delete endfor command
                commands.remove(commands.size() - 1);

                if (forCommand.contains("FOR [") && forCommand.contains("]")) {
                    String[] split = forCommand.split("FOR \\[");
                    String[] split2 = split[1].split("]");
                    String values = split2[0];
                    Optional<String> optionalID = Optional.empty();
                    try{
                        optionalID = Optional.of("%"+split2[1].split("\\>")[1].trim()+"%");
                        //SsomarDev.testMsg(optionalID.get(), true);
                    } catch (Exception ignored) {}
                    String[] split3 = values.split(",");
                    for(String s : split3) {
                        s = s.trim();
                        for(String command : commands) {
                            results.add(command.replaceAll(optionalID.orElse("%for%"), s));
                        }
                    }
                }

                return results;
            }
        }
        return new ArrayList<>();
    }

    public abstract RunCommand buildRunCommand(Integer delay, String command, ActionInfo aInfo);

    public void insertFinalCommands(Integer delay, String command) {
        RunCommand runCommand = this.buildRunCommand(delay, command, actionInfo);
        if (finalCommands.containsKey(delay)) {
            finalCommands.get(delay).add(runCommand);
        } else {
            List<RunCommand> result = new ArrayList<>(Collections.singletonList(runCommand));
            finalCommands.put(delay, result);
        }
    }

    public static Map<String, String> nothingMap = new HashMap<String, String>(){{
        put("nothing*", "nothing\\*");
        put("NOTHING*", "NOTHING\\*");
    }};

    public Optional<NothingObject> replaceNothing(String command) {

        for (Map.Entry<String, String> entry : nothingMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (command.contains(key)) {
                command = actionInfo.getSp().replacePlaceholder(command, true);
                try {
                    int m;
                    String nothingString = "";
                    if (command.contains("//") && !command.contains("https://")){
                        m = Double.valueOf(command.split(value)[1].split("//")[0].trim()).intValue();
                        nothingString = "SEND_MESSAGE " + command.split("//")[1];
                    }
                    else m = Double.valueOf(command.split(value)[1]).intValue();

                   NothingObject nothingObject = new NothingObject(m, nothingString);
                   return Optional.of(nothingObject);
                } catch (Exception err) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    /*
     * Method to select the random commands of RANDOM RUN / RANDOM END
     * */
    public List<String> replaceRandomCommands(List<String> commands) {

        List<String> result = new ArrayList<>();
        List<String> commandsRandom = new ArrayList<>();
        NothingObject nothingObject = null;
        boolean inRandom = false;
        int nbRandom = 0;

        for (String command : commands) {

            if (RandomRun.checkContains(command) && !AllCommandsManager.getInstance().startsWithCommandThatRunCommands(command)) {
                String secondPart = RandomRun.extractSelectionCount(command);
                if (secondPart == null) continue;
                if (secondPart.contains("%")) {
                    SsomarDev.testMsg("secondPart: "+secondPart, true);
                    secondPart = actionInfo.getSp().replacePlaceholder(secondPart, true);
                }
                nbRandom = Integer.parseInt(secondPart);
                inRandom = true;
                continue;
            } else if (RandomEnd.checkContains(command) && !AllCommandsManager.getInstance().startsWithCommandThatRunCommands(command)) {
                result.addAll(this.selectRandomCommands(commandsRandom, nbRandom, nothingObject));
                inRandom = false;
                commandsRandom.clear();
                nbRandom = 0;
                continue;
            } else if (inRandom) {
                Optional<NothingObject> nothingObjectOpt = this.replaceNothing(command);
                if (nothingObjectOpt.isPresent()) {
                    nothingObject = nothingObjectOpt.get();
                }
                else commandsRandom.add(command);
                continue;
            } else result.add(command);
        }

        if (!commandsRandom.isEmpty()) {
            result.addAll(this.selectRandomCommands(commandsRandom, nbRandom, nothingObject));
        }

        return result;
    }

    public List<String> decompMultipleCommandsAndMsg(List<String> commands) {
        List<String> result = new ArrayList<>();

        for (String command : commands) {
            String[] tab;
            if (command.contains("+++")) tab = command.split("\\+\\+\\+");
            else {
                tab = new String[1];
                tab[0] = command;
            }
            for (String s : tab) {
                while (s.startsWith(" ")) {
                    s = s.substring(1);
                }
                while (s.endsWith(" ")) {
                    s = s.substring(0, s.length() - 1);
                }
                if (s.startsWith("/")) s = s.substring(1);
                result.add(s);
            }

            String s = command;
            if (!result.isEmpty()) {
                s = result.get(result.size() - 1);
                result.remove(result.size() - 1);
            }

            if (s.contains("//") && !command.contains("https://")) {
                String[] spliter = s.split("//");

                String commandF = spliter[0];
                result.add(commandF);
                String message = "";
                if (spliter.length >= 2) {
                    if (spliter[1].charAt(0) != ' ') {
                        message = "SEND_MESSAGE " + spliter[1];
                    } else message = "SEND_MESSAGE" + spliter[1];
                    result.add(message);
                }

            } else result.add(s);
        }
        return result;
    }

    public boolean initFinalCommands() {

        int delay = 0;

        commands = this.replaceRandomCommands(commands);

        commands = this.decompMultipleCommandsAndMsg(commands);

        commands = this.replaceFor(commands);

        commands = this.replaceLoop(commands);

        for (String command : commands) {

            if (command.trim().length() == 0) continue;

            /* No edits for the commands executed by score run-... the edits are made after */
            if (command.startsWith("score run-")) {
                this.insertFinalCommands(delay, command);
                continue;
            }

            /* The delay for AROUND and MOB_AROUND is catch after */
            /* A delay is a directive only when it STARTS the command or one of its +++ fragments.
             * A DELAYTICK/DELAY inside another command's payload (cooldowncommand "... ,, DELAYTICK 3 ,, ...")
             * is part of that command and must be dispatched with it. */
            String delayTickStr = DelayTick.delayDirective(command, DelayTick.DELAY_TICK_NAMES);
            String delaySecStr = delayTickStr == null ? DelayTick.delayDirective(command, DelayTick.DELAY_SECONDS_NAMES) : null;
            boolean runsCommands = command.startsWith("IF") || AllCommandsManager.getInstance().startsWithCommandThatRunCommands(command);
            if (delayTickStr != null && !runsCommands) {
                String secondPart = DelayTick.replaceCommand(delayTickStr).replaceAll(" ", "");
                if (secondPart.contains("%")) {
                    secondPart = actionInfo.getSp().replacePlaceholder(secondPart, true);
                }
                delay = delay + (Integer.parseInt(secondPart));
            } else if (delaySecStr != null && !runsCommands) {
                String secondPart = delaySecStr.replaceAll("DELAY ", "").replaceAll(" ", "");
                if (secondPart.contains("%")) {
                    secondPart = actionInfo.getSp().replacePlaceholder(secondPart, true);
                }
                delay = delay + (Integer.parseInt(secondPart) * 20);
            } else {
                //SsomarDev.testMsg(" insert command step3 : "+command, true);
                this.insertFinalCommands(delay, command);
            }
        }

        return true;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public ActionInfo getActionInfo() {
        return actionInfo;
    }

    public void setActionInfo(ActionInfo actionInfo) {
        this.actionInfo = actionInfo;
    }

    public TreeMap<Integer, List<RunCommand>> getFinalCommands() {
        return finalCommands;
    }

    public void setFinalCommands(TreeMap<Integer, List<RunCommand>> finalCommands) {
        this.finalCommands = finalCommands;
    }
}
