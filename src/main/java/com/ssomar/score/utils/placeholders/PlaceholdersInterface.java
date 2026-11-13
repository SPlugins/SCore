package com.ssomar.score.utils.placeholders;

import com.ssomar.score.utils.numbers.RomanNumber;


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

        /* Most calls are for a placeholder the string doesn't contain: return before isNumeric, which parses the value (and throws for a word) */
        if (!s.contains(placeholder)) return s;

        if (optionalTagSurroundValue == null) optionalTagSurroundValue = "";
        if (optionalTagEndBeforeSurround == null) optionalTagEndBeforeSurround = "";
        String before = optionalTagSurroundValue;
        String after = optionalTagEndBeforeSurround + optionalTagSurroundValue;

        /* Literal replacements only: the placeholder name and the value may contain regex characters ('(', '$'...),
         * with replaceAll a `while (contains)` loop could never end (server freeze) */
        if (!isNumeric(value)) {
            // it may not be a number, example: https://discord.com/channels/701066025516531753/1311795346157994146/1437107585554583664
            return s.replace(placeholder, before + value + after);
        }

        /* One pass over the string. %placeholder%+N / %placeholder%-N are calculated; a sign not followed by a number
         * stays in the text (%x%-%y% used to lose its '-', and a trailing '-' threw) */
        double numValue = Double.parseDouble(value);
        StringBuilder out = new StringBuilder();
        int from = 0;
        int idx;
        while ((idx = s.indexOf(placeholder, from)) >= 0) {
            out.append(s, from, idx);
            int end = idx + placeholder.length();
            String replacement = convertToRoman ? toRomanOrRaw(value) : value;
            if (end < s.length() && (s.charAt(end) == '+' || s.charAt(end) == '-')) {
                int j = end + 1;
                /* . is accepted because we accept double , not , because its used as separator for list so it can cause issue */
                while (j < s.length() && ((s.charAt(j) >= '0' && s.charAt(j) <= '9') || s.charAt(j) == '.')) j++;
                String number = s.substring(end + 1, j);
                if (isNumeric(number)) {
                    double d = s.charAt(end) == '+' ? numValue + Double.parseDouble(number) : numValue - Double.parseDouble(number);
                    replacement = String.valueOf(d);
                    if (isInteger) replacement = String.valueOf((int) d);
                    if (convertToRoman) replacement = toRomanOrRaw(String.valueOf((int) d));
                    end = j;
                }
            }
            out.append(before).append(replacement).append(after);
            from = end;
        }
        out.append(s.substring(from));
        return out.toString();
    }

    /* Roman number of an integer value, the value itself when it has none (negative, decimal) */
    private static String toRomanOrRaw(String value) {
        try {
            int i = Integer.parseInt(value);
            if (i >= 0) return RomanNumber.toRoman(i);
        } catch (NumberFormatException ignored) {
        }
        return value;
    }
}
