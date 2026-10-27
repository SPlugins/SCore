package com.ssomar.score.utils.strings;

import com.ssomar.score.SsomarDev;
import com.ssomar.score.utils.numbers.NTools;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.MalformedParametersException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringCalculation {

    private static final boolean DEBUG = false;

    public static boolean calculation(String s, double value) {
        String calculStr = s;

        calculStr = calculStr.replaceAll(" ", "");

        SsomarDev.testMsg("calculStr: " + calculStr, DEBUG);

        if (calculStr.contains("CONDITION")) {
            if (calculStr.startsWith("CONDITION"))
                return calculationWithConditionFirst(calculStr.replaceAll("CONDITION", ""), value);
            else if (calculStr.endsWith("CONDITION"))
                return calculationWithConditionAtTheEnd(calculStr.replaceAll("CONDITION", ""), value);
            else {
                String conditionEnd = calculStr.split("CONDITION")[0];
                String conditionstart = calculStr.split("CONDITION")[1];

                return calculationWithConditionAtTheEnd(conditionEnd, value) && calculationWithConditionFirst(conditionstart, value);
            }
        } else {
            return calculationWithConditionFirst(calculStr, value);
        }
    }

    public static boolean calculationWithConditionFirst(String calculStr, double value) {
        String symbol = null;

        if (calculStr.startsWith("<=") || calculStr.startsWith("=<")) {
            symbol = "<=";
        } else if (calculStr.startsWith("<")) {
            symbol = "<";
        } else if (calculStr.startsWith(">=") || calculStr.startsWith("=>")) {
            symbol = ">=";
        } else if (calculStr.startsWith(">")) {
            symbol = ">";
        } else if (calculStr.startsWith("==") || calculStr.startsWith("=")) {
            symbol = "==";
        }

        if (symbol == null) {
            throw new NullPointerException();
        }

        int symbolLength = symbol.length();
        String conditionNumberStr = calculStr.substring(symbolLength);

        if (NTools.isNumber(conditionNumberStr)) {
            return calculation(symbol, value, NTools.getDouble(conditionNumberStr).get(), true);
        } else {
            throw new MalformedParametersException("Reason: " + conditionNumberStr);
        }
    }

    public static boolean calculationWithConditionAtTheEnd(String calculStr, double value) {
        String symbol = null;

        if (calculStr.endsWith("<=") || calculStr.endsWith("=<")) {
            symbol = "<=";
        } else if (calculStr.endsWith("<")) {
            symbol = "<";
        } else if (calculStr.endsWith(">=") || calculStr.endsWith("=>")) {
            symbol = ">=";
        } else if (calculStr.endsWith(">")) {
            symbol = ">";
        } else if (calculStr.endsWith("==") || calculStr.endsWith("=")) {
            symbol = "==";
        }

        if (symbol == null) {
            throw new NullPointerException();
        }

        int symbolLength = symbol.length();
        String conditionNumberStr = calculStr.substring(0, calculStr.length() - symbolLength);

        if (NTools.isNumber(conditionNumberStr)) {
            return calculation(symbol, value, NTools.getDouble(conditionNumberStr).get(), false);
        } else {
            throw new MalformedParametersException("Reason: " + conditionNumberStr);
        }
    }


    public static boolean calculation(String symbol, double value, double condition, boolean isValueFirst) {
        /* Invert symbol if isValueNotFirst */
        if (!isValueFirst) {
            if (symbol.equals("<=") || symbol.equals("=<")) symbol = ">";
            else if (symbol.equals("<")) symbol = ">=";
            else if (symbol.equals(">")) symbol = "<=";
            else if (symbol.equals(">=") || symbol.equals("=>")) symbol = "<";
        }

        if (symbol.equals("<=") || symbol.equals("=<")) {
            return value <= condition;
        } else if (symbol.equals("<")) {
            return value < condition;
        } else if (symbol.equals(">=") || symbol.equals("=>")) {
            return value >= condition;
        } else if (symbol.equals(">")) {
            return value > condition;
        } else if (symbol.equals("==") || symbol.equals("=")) {
            return value == condition;
        }

        return false;
    }

    public static boolean isStringCalculation(String s) {
        try {
            //ignore if there are more % than 2, ex : %math_0_(%target_max_health%)*0.2%
            if (s.chars().filter(ch -> ch == '%').count() > 2) return true;
            if (s.contains("%")) {
                boolean pass = false;
                boolean passSecond = false;
                StringBuilder sb = new StringBuilder();
                for (char c : s.toCharArray()) {
                    if (c == '%') {
                        if (pass) {
                            passSecond = true;
                        } else {
                            pass = true;
                        }
                    } else if (pass) sb.append(c);

                    if (passSecond) {
                        sb.append(0);
                        passSecond = false;
                        pass = false;
                    }
                }
                s = sb.toString();
            }
            calculation(s, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) {
        String test = "<=9";
        System.out.println(StringCalculation.isStringCalculation(test));
        System.out.println(StringCalculation.calculation(test, 6));
    }
}
