package com.ssomar.score.utils.placeholders;

import com.ssomar.score.utils.numbers.RomanNumber;

import java.util.regex.Matcher;

public abstract class PlaceholdersInterface {

    public static boolean isNumeric(String strNum) {
        if (strNum == null)
            return false;
        try {
            @SuppressWarnings("unused")
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static String replaceCalculPlaceholder(String s, String placeholder, String value, boolean isInteger) {
        return replaceCalculPlaceholder(s, placeholder, value, "", "", isInteger, false);
    }

    public static String replaceCalculPlaceholder(String s, String placeholder, String value, String optionalTagSurroundValue, String optionalTagEndBeforeSurround , boolean isInteger) {
        return replaceCalculPlaceholder(s, placeholder, value, optionalTagSurroundValue, optionalTagEndBeforeSurround, isInteger, false);
    }

    public static String replaceCalculPlaceholder(String s, String placeholder, String value, String optionalTagSurroundValue, String optionalTagEndBeforeSurround, boolean isInteger, boolean convertToRoman) {

        String result = s;
        if(optionalTagSurroundValue == null) optionalTagSurroundValue = "";
        if(optionalTagEndBeforeSurround == null) optionalTagEndBeforeSurround = "";

        if(!isNumeric(value)){
            // it may not be a number, example: https://discord.com/channels/701066025516531753/1311795346157994146/1437107585554583664
            //Utils.sendConsoleMsg(SCore.NAME_COLOR+" &cInvalid value &6"+value+" &c used in placeholder calculation. String : &6"+s);

            // Matcher.quoteReplacement: prevents '$' / '\' in the value from being treated
            // as regex back-references in the replacement string (EI #50).
            String safeReplacement = Matcher.quoteReplacement(optionalTagSurroundValue + value + optionalTagEndBeforeSurround + optionalTagSurroundValue);
            while (result.contains(placeholder)) {
                result = result.replaceAll(placeholder, safeReplacement);
            }
            return result;
        }


        if(result.contains(placeholder)) {
            while (result.contains(placeholder + "+")) {
                String suit = result.split(placeholder + "\\+")[1];
                StringBuilder sb = new StringBuilder();
                for (char c : suit.toCharArray()) {
                    /* . is accepted because we accept double , not , because its used as separator for list so it can cause issue */
                    if ((c < '0' || c > '9') && !(c == '.')) break;
                    sb.append(c);
                }
                if (isNumeric(sb.toString())) {
                    double d = Double.parseDouble(sb.toString()) + Double.parseDouble(value);
                    String finalValue = String.valueOf(d);
                    if (isInteger) finalValue = String.valueOf((int) d);
                    if (convertToRoman) finalValue = RomanNumber.toRoman((int) d);
                    result = result.replaceFirst(placeholder + "\\+" + sb, optionalTagSurroundValue + finalValue +optionalTagEndBeforeSurround + optionalTagSurroundValue);
                } else {
                    result = result.replaceFirst(placeholder + "\\+" + sb, optionalTagSurroundValue+value+ optionalTagEndBeforeSurround+optionalTagSurroundValue);
                }
            }

            while (result.contains(placeholder + "-")) {
                String suit = result.split(placeholder + "-")[1];
                StringBuilder sb = new StringBuilder();
                for (char c : suit.toCharArray()) {
                    /* . is accepted because we accept double , not , because its used as separator for list so it can cause issue */
                    if ((c < '0' || c > '9') && !(c == '.')) break;
                    sb.append(c);
                }
                if (isNumeric(sb.toString())) {
                    double d = Double.parseDouble(value) - Double.parseDouble(sb.toString());
                    String finalValue = String.valueOf(d);
                    if (isInteger) finalValue = String.valueOf((int) d);
                    if (convertToRoman) finalValue = RomanNumber.toRoman((int) d);
                    result = result.replaceFirst(placeholder + "-" + sb, optionalTagSurroundValue + finalValue+optionalTagEndBeforeSurround+optionalTagSurroundValue);
                } else {
                    result = result.replaceFirst(placeholder + "-" + sb, optionalTagSurroundValue+value+optionalTagEndBeforeSurround+optionalTagSurroundValue);
                }
            }
            while (result.contains(placeholder)) {
                if (convertToRoman) {
                    try {
                        int i = Integer.valueOf(value);
                        result = result.replaceAll(placeholder, optionalTagSurroundValue+RomanNumber.toRoman(i)+optionalTagEndBeforeSurround+optionalTagSurroundValue);
                    } catch (Exception ignored) {
                    }
                } else result = result.replaceAll(placeholder, optionalTagSurroundValue+value+optionalTagEndBeforeSurround+optionalTagSurroundValue);
            }
        }
        return result;
    }
}
