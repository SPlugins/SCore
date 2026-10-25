package com.ssomar.score.commands.runnable;

import com.ssomar.score.SsomarDev;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter @Setter
public class SCommandToExec {

    private SCommand sCommand;

    private Map<CommandSetting, Object> settingsValues;

    private List<String> otherArgs;

    private ActionInfo actionInfo;

    public SCommandToExec(SCommand sCommand) {
        super();
        this.sCommand = sCommand;
    }

    public SCommandToExec() {
        this.settingsValues = new HashMap<>();
        this.otherArgs = new ArrayList<>();
    }

    public void extractSettings(String entry) {

        entry = sCommand.stripName(entry);
        entry = entry.trim();
        SsomarDev.testMsg("entry: "+entry, true);
        if (entry.equals("")) {
            settingsValues = new HashMap<>();
            otherArgs = new ArrayList<>();

            for (CommandSetting setting : sCommand.getSettings()) {
               settingsValues.put(setting, setting.getValue(null));
            }
        }
        else{
            List<String> arguments = new ArrayList<>(Arrays.asList(entry.split(" ")));
            Map<CommandSetting, Object> settingObjectMap = new HashMap<>();
            // fully new system
            for(CommandSetting setting : sCommand.getSettings()){
                if(!arguments.isEmpty()){
                    for(String name : setting.getNames()) {
                        Optional<String> value = arguments.stream().filter(arg -> arg.startsWith(name + ":")).findFirst();
                        if (value.isPresent()) {
                            settingObjectMap.put(setting, setting.getValue(value.get().replace(name + ":", "")));
                            arguments.remove(value.get());
                            break;
                        }
                    }
                }
            }
            // Maintain old system
            List<Integer> indexToRemove = new ArrayList<>();
            for (CommandSetting setting : sCommand.getSettings()) {

                if(settingObjectMap.containsKey(setting)) continue;

                if(setting.getOldSystemIndex() == -1) settingObjectMap.put(setting, setting.getValue(null));
                else if(arguments.size() > setting.getOldSystemIndex()) {
                    String value = arguments.get(setting.getOldSystemIndex());
                    if(setting.isOldSystemOptional()){
                        if(setting.getType() == Boolean.class && !(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))) {
                            settingObjectMap.put(setting, setting.getValue(null));
                        }
                        else if(setting.getType() == Double.class || setting.getType() == Integer.class) {
                            try {
                                Double.parseDouble(value);
                                settingObjectMap.put(setting, setting.getValue(value));
                                indexToRemove.add(setting.getOldSystemIndex());
                            } catch (NumberFormatException e) {
                                settingObjectMap.put(setting, setting.getValue(null));
                            }
                        }
                        else {
                            settingObjectMap.put(setting, setting.getValue(value));
                            indexToRemove.add(setting.getOldSystemIndex());
                        }
                    }else {
                        settingObjectMap.put(setting, setting.getValue(arguments.get(setting.getOldSystemIndex())));
                        indexToRemove.add(setting.getOldSystemIndex());
                    }
                }
                else settingObjectMap.put(setting, setting.getValue(null));
            }
            //for(CommandSetting i : settingObjectMap.keySet()) {
            //    SsomarDev.testMsg("settingObjectMap "+i+" >> "+settingObjectMap.get(i), true);
            //}
            // Sort the index to remove
            Collections.sort(indexToRemove, Collections.reverseOrder());
            for(int index : indexToRemove){
                //String value = arguments.get(index);
                //SsomarDev.testMsg("REMOVE value: "+value, true);
                arguments.remove(index);
            }
            this.settingsValues = settingObjectMap;
            this.otherArgs = arguments;
        }
    }

    public Object getSettingValue(String setting) {
        for(CommandSetting s : sCommand.getSettings()) {
            for(String name : s.getNames()) {
                if (name.equals(setting)) return settingsValues.get(s);
            }
        }
        return null;
    }
}
