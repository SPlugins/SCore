package com.ssomar.score.utils.strings;

import com.ssomar.score.SsomarDev;
import com.ssomar.score.utils.Couple;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringSetting {

    // format xxxxx{Var:{var1:value1,var2:value2,...},Usage:1, Test:[hello,hhhh]}

    public static Map<String, Object> extractSettingsAndRebuildCorrectly(List<String> args, int startIndex, List<String> startToIgnore) {

        List<String> rebuildArgs = new ArrayList<>();
        StringBuilder potentialInput = new StringBuilder();
        int index = 0;
        for (String arg : args){
            if(index >= startIndex) potentialInput.append(arg).append(" ");
            else rebuildArgs.add(arg);
            index++;
        }
        if(potentialInput.length() > 0) potentialInput.deleteCharAt(potentialInput.length() - 1);
        Couple<Map<String, Object>,String> couple = extractSettings(potentialInput.toString(), startToIgnore);
        rebuildArgs.addAll(Arrays.asList(couple.getElem2().split(" ")));
        args.clear();
        args.addAll(rebuildArgs);

        return couple.getElem1();
    }

    // Map of the settings
    // String is the input string without the settings
    public static Couple<Map<String, Object>,String> extractSettings(String input, List<String> startToIgnore){
        input = input.trim();

        String start = "";
        for(String sToIgnore : startToIgnore){
            if(input.startsWith(sToIgnore)){
                input = input.substring(sToIgnore.length());
                start = sToIgnore;
                break;
            }
        }

        int breakAtNextClosure = 0;
        StringBuilder sb = new StringBuilder(start);
        StringBuilder sbSettings = new StringBuilder();
        boolean isOutSideSettings = true;
        boolean finished = false;

        for (char c : input.toCharArray()) {
            if(isOutSideSettings) sb.append(c);
            else sbSettings.append(c);

            if(!finished){
                if (isOutSideSettings){
                    if(c  == ' ') finished = true;
                    else if (c == '{') {
                        isOutSideSettings = false;
                        sb.deleteCharAt(sb.length()-1);
                    }
                }
                else {
                    if (c == '{') breakAtNextClosure++;
                    else if (c == '}') {
                        if (breakAtNextClosure == 0) {
                            finished = true;
                            isOutSideSettings = true;
                        } else breakAtNextClosure--;
                    }
                }

            }

        }
        if(sbSettings.length() >= 1) sbSettings.deleteCharAt(sbSettings.length()-1); // remove the last character (}
        SsomarDev.testMsg("sb: "+sb, true);
        SsomarDev.testMsg("sbSettings: "+sbSettings, true);
        Couple<Map<String, Object>,String> result = new Couple<>(getSettings(sbSettings.toString()), sb.toString());
        return result;
    }


    /**
     * Use this to extract nested values from a string that's formatted similarly to a JSON string.
     * The return object of this method is a Map object that may use recursion to handle deeper values, so
     * think of the return object like: <code>Map&lt;String, Map&lt;String, String>></code>
     * @param input
     * @return <code>Map&lt;String, Object></code>
     */
    public static Map<String, Object> getSettings(String input) {
        Map<String, Object> result = new HashMap<>();

        input = input.trim();
        // format Var:{var1:value1,var2:value2,...},Usage:1, Test:[hello,hhhh]
        String savedKey = "";
        StringBuilder sb = new StringBuilder();

        boolean inList = false;
        boolean inMap = false;
        boolean inKey = true;
        boolean inText = false;  // -> default
        boolean inLongText = false; // -> ""
        int breakAtNextClosure = 0;
        for (char c : input.toCharArray()) {

            sb.append(c);
            if (c == '[') {
                if (inList) breakAtNextClosure++;
                inList = true;
            } else if (c == ']') {
                if (inList) {
                    if (breakAtNextClosure == 0) {
                        inList = false;
                        result.put(savedKey, loadList(sb.deleteCharAt(0).deleteCharAt(sb.length()-1).toString()));
                        savedKey = "";
                        sb.setLength(0);
                    } else breakAtNextClosure--;
                }
            } else if (c == '"') {
                if (inLongText) {
                    inLongText = false;
                    result.put(savedKey, sb.deleteCharAt(0).deleteCharAt(sb.length()-1).toString());
                    savedKey = "";
                    sb.setLength(0);
                } else if(!inMap && !inList) inLongText = true;
            } else if (c == '{') {
                if (inMap) breakAtNextClosure++;
                inMap = true;
            } else if (c == '}') {
                if (inMap) {
                    if (breakAtNextClosure == 0) {
                        inMap = false;
                        result.put(savedKey, getSettings(sb.deleteCharAt(0).deleteCharAt(sb.length()-1).toString()));
                        savedKey = "";
                        sb.setLength(0);
                    } else breakAtNextClosure--;
                }
            } else if (c == ':') {
                if (inKey) {
                    savedKey = sb.deleteCharAt(sb.length()-1).toString();
                    sb.setLength(0);
                    inKey = false;
                }
            } else if (c == ',') {

                if (inText) {
                    result.put(savedKey, sb.deleteCharAt(sb.length()-1).toString());
                    savedKey = "";
                    sb.setLength(0);
                    inText = false;
                }

                if (!inList && !inMap && !inText && !inLongText && !inKey) {
                    sb.setLength(0);
                    inKey = true;
                }
            } else if (!inList && !inMap && !inText && !inLongText && !inKey) {
                inText = true;
            }
        }
        if(inText){
            result.put(savedKey, sb.toString());
        }

        for(Map.Entry<String, Object> entry : result.entrySet()) {
           SsomarDev.testMsg("Variableeeeeeeeee > "+entry.getKey() + " -> " + entry.getValue(), true);
        }
        return result;
    }

    private static List<String> loadList(String s) {
        return Arrays.asList(s.split(","));
    }

    public static void main(String[] args) {
        StringSetting test = new StringSetting();
        String input = "Var:{var1:value1,var2:\"value2,  et\"},Usage:1, Test:[hello,hhhh]";
        Map<String, Object> settings = test.getSettings(input);
        for(Map.Entry<String, Object> entry : settings.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }

        String input2 = "xxxxfrikd:{Var:{var1:value1,var2:\"value2,  et\"},Usage:1, Test:[hello,hhhh]} other_arg 145";
        Couple<Map<String, Object>,String> couple = test.extractSettings(input2, new ArrayList<>());
        System.out.println("String without settings: "+couple.getElem2());
    }
}
