package com.ssomar.score.utils.numbers;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class NTools implements Serializable {

    public static DecimalFormat numberFormat_1 = new DecimalFormat("#.0", DecimalFormatSymbols.getInstance(Locale.US));
    public static DecimalFormat numberFormat_2 = new DecimalFormat("#.00", DecimalFormatSymbols.getInstance(Locale.US));
    public static DecimalFormat numberFormat_3 = new DecimalFormat("#.000", DecimalFormatSymbols.getInstance(Locale.US));

    public static Optional<Integer> getInteger(String s) {
        Optional<Integer> result = Optional.empty();
        try {
            result = Optional.of(Integer.valueOf(s));
        } catch (NumberFormatException e) {
            return result;
        }
        return result;
    }

    public static Optional<Double> getDouble(String s) {
        Optional<Double> result = Optional.empty();
        try {
            result = Optional.of(Double.valueOf(s));
        } catch (NumberFormatException e) {
            return result;
        }
        return result;
    }

    public static Optional<Long> getLong(String s) {
        Optional<Long> result = Optional.empty();
        try {
            result = Optional.of(Long.valueOf(s));
        } catch (NumberFormatException e) {
            return result;
        }
        return result;
    }

    public static Optional<Float> getFloat(String s) {
        Optional<Float> result = Optional.empty();
        try {
            result = Optional.of(Float.valueOf(s));
        } catch (NumberFormatException e) {
            return result;
        }
        return result;
    }

    public static boolean isNumber(String s) {
        try {
            Double.valueOf(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Parses {@code s} as a number, or as a plain arithmetic expression ({@code + - * /}, parentheses,
     * unary minus, decimals) when it is not one: {@code "64.0-0.2"} gives 63.8. Empty when neither.
     * Used by the placeholder conditions, where {@code part2: '%player_y%-0.2'} used to fail silently.
     */
    public static Optional<Double> toNumber(String s) {
        if (s == null) return Optional.empty();
        String trimmed = s.trim();
        if (trimmed.isEmpty()) return Optional.empty();
        try {
            return Optional.of(Double.valueOf(trimmed));
        } catch (NumberFormatException ignored) {
        }
        return evaluateArithmetic(trimmed);
    }

    /** Evaluates a plain arithmetic expression; empty when it is not one (letters, unbalanced parentheses…). */
    public static Optional<Double> evaluateArithmetic(String expression) {
        if (expression == null) return Optional.empty();
        String s = expression.replace(" ", "");
        if (s.isEmpty()) return Optional.empty();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!(Character.isDigit(c) || c == '.' || c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == ')')) return Optional.empty();
        }
        try {
            ArithmeticParser parser = new ArithmeticParser(s);
            double value = parser.parseExpression();
            if (parser.pos != s.length()) return Optional.empty();
            if (Double.isNaN(value) || Double.isInfinite(value)) return Optional.empty();
            return Optional.of(value);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /** Recursive-descent parser: expression = term (('+'|'-') term)* ; term = factor (('*'|'/') factor)* ; factor = '-' factor | '(' expression ')' | number */
    private static final class ArithmeticParser {
        private final String s;
        private int pos = 0;

        ArithmeticParser(String s) {
            this.s = s;
        }

        double parseExpression() {
            double value = parseTerm();
            while (pos < s.length()) {
                char c = s.charAt(pos);
                if (c == '+') { pos++; value += parseTerm(); }
                else if (c == '-') { pos++; value -= parseTerm(); }
                else break;
            }
            return value;
        }

        private double parseTerm() {
            double value = parseFactor();
            while (pos < s.length()) {
                char c = s.charAt(pos);
                if (c == '*') { pos++; value *= parseFactor(); }
                else if (c == '/') { pos++; value /= parseFactor(); }
                else break;
            }
            return value;
        }

        private double parseFactor() {
            if (pos >= s.length()) throw new IllegalArgumentException("unexpected end");
            char c = s.charAt(pos);
            if (c == '-') { pos++; return -parseFactor(); }
            if (c == '+') { pos++; return parseFactor(); }
            if (c == '(') {
                pos++;
                double value = parseExpression();
                if (pos >= s.length() || s.charAt(pos) != ')') throw new IllegalArgumentException("missing )");
                pos++;
                return value;
            }
            int start = pos;
            while (pos < s.length() && (Character.isDigit(s.charAt(pos)) || s.charAt(pos) == '.')) pos++;
            if (start == pos) throw new IllegalArgumentException("number expected at " + pos);
            return Double.parseDouble(s.substring(start, pos));
        }
    }

    public static double reduceDouble(@NotNull double number, int numbersAfterComma) {
        // Use direct multiplication and division instead of string formatting
        if (numbersAfterComma >= 0) {
            double factor = Math.pow(10, numbersAfterComma);
            return Math.round(number * factor) / factor;
        }

        // Handle negative numbersAfterComma or other special cases by falling back to formatter
        // Create a reusable thread-local formatter map for rare cases
        else {
            return formatWithDecimalFormat(number, numbersAfterComma);
        }
    }

    // Extract formatter logic to a separate method for the rare cases
    private static double formatWithDecimalFormat(double number, int numbersAfterComma) {
        // Use a ConcurrentHashMap to cache formatters by precision
        DecimalFormat formatter = FORMATTER_CACHE.computeIfAbsent(numbersAfterComma, precision -> {
            StringBuilder pattern = new StringBuilder("#.");
            for (int i = 0; i < precision; i++) {
                pattern.append("0");
            }
            DecimalFormat df = new DecimalFormat(pattern.toString(), DecimalFormatSymbols.getInstance(Locale.US));
            df.setRoundingMode(RoundingMode.HALF_UP);
            return df;
        });

        return Double.parseDouble(formatter.format(number));
    }

    // Static cache of formatters
    private static final ConcurrentHashMap<Integer, DecimalFormat> FORMATTER_CACHE = new ConcurrentHashMap<>();

    // Initialize the three most common formatters
    static {
        for (int i = 1; i <= 3; i++) {
            FORMATTER_CACHE.computeIfAbsent(i, precision -> {
                StringBuilder pattern = new StringBuilder("#.");
                for (int j = 0; j < precision; j++) {
                    pattern.append("0");
                }
                DecimalFormat df = new DecimalFormat(pattern.toString(), DecimalFormatSymbols.getInstance(Locale.US));
                df.setRoundingMode(RoundingMode.HALF_UP);
                return df;
            });
        }
    }

    public static void main(String[] args) {
       System.out.println(reduceDouble(1.87656789, 2));

    }
}
