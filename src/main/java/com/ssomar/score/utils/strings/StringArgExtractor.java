package com.ssomar.score.utils.strings;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringArgExtractor {

    /**
     * It will extract the value of a prefix in a placeholder string (Without the percent signs at the front and back char)
     * @param input The string placeholder
     * @param keyArg the prefix you'd specify to extract its value
     * @return
     */
    public static String[] extractArgValue(String input, String keyArg) {
        if (!input.contains(keyArg+":")) return new String[]{};

        // Find the first detected pattern that identifies the target prefix
        int prefixIdx = input.indexOf(keyArg+":");
        //System.out.println("START => "+prefixIdx+" | CHAR => "+input.charAt(prefixIdx));

        // Remove the string before the prefix pattern
        String firstChop = input.substring(prefixIdx);
        //System.out.println("FIRSTCHOP => "+firstChop);

        // Find the first colon and the second potential colon in the string. -1 means unknown / no trailing colons found.
        int firstColonIdx = firstChop.indexOf(':');
        //System.out.println("FIRST IDX => "+firstColonIdx);
        int secondColonIdx = firstChop.indexOf(':', firstColonIdx + 1);
        //System.out.println("SECOND IDX => "+secondColonIdx);

        // No second colon means that there's no trailing prefixes to watch out for
        if (secondColonIdx == -1) {
            //System.out.println("EVALUATING FIRSTCHOP => "+firstChop);
            return firstChop.split(":")[1].split(",");
        }
        // A second colon was detected, meaning there are trailing prefixes to watch out for
        else {
            //System.out.println("EVALUATING FIRSTCHOP => "+firstChop);

            // Remove trailing string starting from the index of the second colon
            String secondChop = firstChop.substring(0, secondColonIdx);
            //System.out.println("SECONDCHOP => "+secondChop);

            // Underscores are used to separate arguments. Because a trailing prefix was detected, the rightmost underscore detected is definitely
            // used to separate the value assigned at the first prefix and the characters identifying the second prefix.
            String thirdChop = secondChop.substring(0, secondChop.lastIndexOf("_"));
            //System.out.println("THIRDCHOP => "+thirdChop);

            // With the unwanted characters trimmed out, we're left with the prefix and the value. Split the string using the separator and get the 2nd arg
            String finalChop = thirdChop.split(":")[1];
            //System.out.println("FINALCHOP => "+finalChop);

            return finalChop.split(",");
        }

    }

    /**
     * The "slots:" argument is required to be present in the string argument
     * to sum up the values of multiple EI/EB ItemStack variable values.
     * @param string
     * @param keyArg
     * @return
     */
    static int extractArgValueTotal(String string, String keyArg) {
        return 0;
    }

}
