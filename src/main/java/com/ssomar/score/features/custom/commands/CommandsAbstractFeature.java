package com.ssomar.score.features.custom.commands;

import com.ssomar.score.commands.runnable.util.commands.DelayTick;
import com.ssomar.score.features.*;

import java.util.ArrayList;
import java.util.List;

public abstract class CommandsAbstractFeature<T, Y extends FeatureInterface<T, Y>> extends FeatureAbstract<T, Y> implements FeatureRequireSubTextEditorInEditor {

    public CommandsAbstractFeature(FeatureParentInterface parent, FeatureSettingsInterface featureSettings) {
        super(parent, featureSettings);
    }

    public List<String> prepareActionbarArgs(List<String> commands, String objectName) {
        List<String> result = new ArrayList<>();
        int cpt = 0;
        int placeActionbar = -1;
        int delay = 0;
        boolean isInActionbar = false;

        for (String s : commands) {
            if (s.equalsIgnoreCase("ACTIONBAR ON")) {
                isInActionbar = true;
                placeActionbar = cpt;

            } else if (s.equalsIgnoreCase("ACTIONBAR OFF") && isInActionbar) {
                if (placeActionbar != -1) {
                    delay = delay / 20;
                    String actionbarWithArgs = "ACTIONBAR " + objectName + " " + delay;
                    result.set(placeActionbar, actionbarWithArgs);
                    placeActionbar = -1;
                    delay = 0;
                }
                continue;
            } else {
                /* Try because for some case users will use placeholders, but for the actionbar, I dont accept it*/
                try {
                    if (DelayTick.checkStartsWith(s) && !s.contains("AROUND") && !s.contains("+++")) {
                        delay = delay + (Integer.valueOf(DelayTick.replaceCommand(s).trim()));
                    } else if (s.trim().startsWith("DELAY ") && !s.contains("AROUND") && !s.contains("+++")) {
                        delay = delay + (Integer.valueOf(s.replaceAll("DELAY ", "")) * 20);
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
            result.add(s);
            cpt++;
        }
        if (placeActionbar != -1) {
            delay = delay / 20;
            String actionbarWithArgs = "ACTIONBAR " + objectName + " " + delay;
            result.set(placeActionbar, actionbarWithArgs);
            placeActionbar = -1;
            delay = 0;
        }
        return result;
    }
}
